package com.hbm.tileentity.machine;

import com.hbm.api.energymk2.IEnergyProviderMK2;
import com.hbm.blocks.ModBlocks;
import com.hbm.capability.NTMEnergyCapabilityWrapper;
import com.hbm.interfaces.AutoRegister;
import com.hbm.lib.ForgeDirection;
import com.hbm.tileentity.TileEntityLoadedBase;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import org.jetbrains.annotations.NotNull;

@AutoRegister
public class TileEntityMachineAmgen extends TileEntityLoadedBase implements ITickable, IEnergyProviderMK2 {

	public long power;
	public long maxPower = 500;
	protected long output = 0;
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		power = compound.getLong("power");
		super.readFromNBT(compound);
	}
	
	@Override
	public @NotNull NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setLong("power", power);
		return super.writeToNBT(compound);
	}
	
	@Override
	public void update() {
		if(!world.isRemote) {
			this.output = 0;

			Block block = world.getBlockState(pos).getBlock();

			if(block == ModBlocks.machine_geo) {
				this.checkGeoInteraction(pos.up());
				this.checkGeoInteraction(pos.down());
			}

			this.power += this.output;
			if(power > maxPower)
				power = maxPower;

			for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
				this.tryProvide(world, pos.getX() + dir.offsetX, pos.getY() + dir.offsetY, pos.getZ() + dir.offsetZ, dir);

		}
	}

	private void checkGeoInteraction(BlockPos pos) {

		Block b = world.getBlockState(pos).getBlock();

		if(b == ModBlocks.geysir_water) {
			this.output += 75;
		} else if(b == ModBlocks.geysir_chlorine) {
			this.output += 100;
		} else if(b == ModBlocks.geysir_vapor) {
			this.output += 50;
		} else if(b == ModBlocks.geysir_nether) {
			this.output += 500;
		} else if(b == Blocks.LAVA) {
			this.output += 100;

			if(world.rand.nextInt(6000) == 0) {
				world.setBlockState(pos.down(), Blocks.OBSIDIAN.getDefaultState());
			}
		} else if(b == Blocks.FLOWING_LAVA) {
			this.output += 25;

			if(world.rand.nextInt(3000) == 0) {
				world.setBlockState(pos.down(), Blocks.COBBLESTONE.getDefaultState());
			}
		}
	}

	@Override
	public long getPower() {
		return power;
	}

	@Override
	public void setPower(long i) {
		power = i;
	}

	@Override
	public long getMaxPower() {
		return this.maxPower;
	}

	@Override
	public boolean hasCapability(@NotNull Capability<?> capability, EnumFacing facing) {
		if (capability == CapabilityEnergy.ENERGY) {
			return true;
		}
		return super.hasCapability(capability, facing);
	}

	@Override
	public <T> T getCapability(@NotNull Capability<T> capability, EnumFacing facing) {
		if (capability == CapabilityEnergy.ENERGY) {
			return CapabilityEnergy.ENERGY.cast(
					new NTMEnergyCapabilityWrapper(this)
			);
		}
		return super.getCapability(capability, facing);
	}
}
