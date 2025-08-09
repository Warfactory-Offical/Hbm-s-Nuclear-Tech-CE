package com.hbm.tileentity.network.energy;

import com.hbm.api.energymk2.IEnergyConductorMK2;
import com.hbm.api.energymk2.Nodespace;
import com.hbm.api.energymk2.Nodespace.PowerNode;
import com.hbm.api.energymk2.PowerNetMK2;
import com.hbm.capability.NTMCableEnergyCapabilityWrapper;
import com.hbm.config.GeneralConfig;
import com.hbm.interfaces.AutoRegister;
import com.hbm.lib.DirPos;
import com.hbm.lib.ForgeDirection;
import com.hbm.tileentity.TileEntityLoadedBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.Nullable;

@AutoRegister
public class TileEntityCableBaseNT extends TileEntityLoadedBase implements IEnergyConductorMK2, ITickable {
	protected PowerNode node;

	@Override
	public void update() {
		if (!world.isRemote) {

			if (this.node == null || this.node.expired) {

				if (this.shouldCreateNode()) {
					this.node = Nodespace.getNode(world, pos);

					if (this.node == null || this.node.expired) {
						this.node = this.createNode();
						Nodespace.createNode(world, this.node);
					}
				}
			}

			if (this.node != null && this.node.hasValidNet()) {
				PowerNetMK2 net = this.node.net;
				this.handleFETransfers(net);
			}
		}
	}

	private void handleFETransfers(PowerNetMK2 net) {
		for (DirPos con : this.node.connections) {
			BlockPos neighborPos = con.getPos();
			ForgeDirection dir = con.getDir();

			if (dir == ForgeDirection.UNKNOWN) {
				continue;
			}
			EnumFacing facing = dir.toEnumFacing();

			TileEntity te = world.getTileEntity(neighborPos);
			if (te == null || te.isInvalid()) continue;

			EnumFacing opposite = facing.getOpposite();
			if (te.hasCapability(CapabilityEnergy.ENERGY, opposite)) {
				IEnergyStorage storage = te.getCapability(CapabilityEnergy.ENERGY, opposite);
				if (storage == null) continue;

				if (storage.canExtract()) {
					int maxExtractFE = storage.extractEnergy(Integer.MAX_VALUE, true);
					if (maxExtractFE > 0 && GeneralConfig.conversionRateHeToRF > 0) {
						long heToReceive = (long) Math.floor(maxExtractFE / GeneralConfig.conversionRateHeToRF);
						long leftoverHE = net.sendPowerDiode(heToReceive, true);
						long acceptedHE = heToReceive - leftoverHE;
						if (acceptedHE > 0) {
							int feToExtract = (int) Math.min(Math.round(acceptedHE * GeneralConfig.conversionRateHeToRF), maxExtractFE);
							int feExtracted = storage.extractEnergy(feToExtract, false);
							long heInjected = (long) Math.floor(feExtracted / GeneralConfig.conversionRateHeToRF);
							net.sendPowerDiode(heInjected, false);
						}
					}
				}

				if (storage.canReceive()) {
					int freeSpaceFE = storage.receiveEnergy(Integer.MAX_VALUE, true);
					if (freeSpaceFE > 0 && GeneralConfig.conversionRateHeToRF > 0) {
						long heToExtract = (long) Math.floor(freeSpaceFE / GeneralConfig.conversionRateHeToRF);
						long extractedHE = net.extractPowerDiode(heToExtract, true);
						if (extractedHE > 0) {
							int feToSend = (int) Math.min(Math.round(extractedHE * GeneralConfig.conversionRateHeToRF), freeSpaceFE);
							int feReceived = storage.receiveEnergy(feToSend, false);
							long heUsed = (long) Math.floor(feReceived / GeneralConfig.conversionRateHeToRF);
							net.extractPowerDiode(heUsed, false);
						}
					}
				}
			}
		}
	}

	public boolean shouldCreateNode() {
		return true;
	}

	@Override
	public void invalidate() {
		super.invalidate();

		if(!world.isRemote) {
			if(this.node != null) {
				Nodespace.destroyNode(world, pos);
			}
		}
	}

	@Override
	public boolean canConnect(ForgeDirection dir) {
		return dir != ForgeDirection.UNKNOWN;
	}

	@Override
	public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
		if (capability == CapabilityEnergy.ENERGY && facing != null) {
			ForgeDirection dir = ForgeDirection.getOrientation(facing.getIndex());
			if (canConnect(dir) && this.node != null && this.node.hasValidNet()) {
				return true;
			}
		}
		return super.hasCapability(capability, facing);
	}

	@Override
	public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
		if (capability == CapabilityEnergy.ENERGY && facing != null) {
			ForgeDirection dir = ForgeDirection.getOrientation(facing.getIndex());
			if (canConnect(dir) && this.node != null && this.node.hasValidNet()) {
				return CapabilityEnergy.ENERGY.cast(new NTMCableEnergyCapabilityWrapper(this.node.net));
			}
		}
		return super.getCapability(capability, facing);
	}
}
