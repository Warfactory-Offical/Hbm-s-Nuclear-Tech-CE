package com.hbm.tileentity.machine;

import api.hbm.energymk2.IEnergyReceiverMK2;
import api.hbm.fluid.IFluidStandardReceiver;
import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.machine.MachineITER;
import com.hbm.capability.NTMFluidHandlerWrapper;
import com.hbm.forgefluid.ModForgeFluids;
import com.hbm.interfaces.IFFtoNTMF;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.Library;
import com.hbm.tileentity.TileEntityMachineBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class TileEntityMachinePlasmaHeater extends TileEntityMachineBase implements ITickable, IFluidStandardReceiver, IEnergyReceiverMK2, IFFtoNTMF {

	public long power;
	public static final long maxPower = 10000000000L;

	public FluidTankNTM[] tanksNew;
	public FluidTankNTM plasmaNew;

	public FluidTank[] tanks;
	public Fluid[] types = new Fluid[]{ModForgeFluids.deuterium, ModForgeFluids.tritium};
	public FluidTank plasma;
	public Fluid plasmaType = ModForgeFluids.plasma_dt;

	private static boolean converted = false;
	
	public TileEntityMachinePlasmaHeater() {
		super(1);
		tanksNew = new FluidTankNTM[2];
		tanksNew[0] = new FluidTankNTM(Fluids.DEUTERIUM, 16000, 0);
		tanksNew[1] = new FluidTankNTM(Fluids.TRITIUM, 16000, 1);
		plasmaNew = new FluidTankNTM(Fluids.PLASMA_DT, 64000, 2);

		tanks = new FluidTank[2];
		tanks[0] = new FluidTank(16000);
		tanks[1] = new FluidTank(16000);
		plasma = new FluidTank(64000);

		converted = true;
	}

	@Override
	public String getName() {
		return "container.plasmaHeater";
	}

	@Override
	public void update() {
		if(!world.isRemote) {
			if(!converted){
				convertAndSetFluids(types, tanks, tanksNew);
				convertAndSetFluid(plasmaType, plasma, plasmaNew);
				converted = true;
			}
			if(this.world.getTotalWorldTime() % 20 == 0)
				this.updateConnections();

			/// START Managing all the internal stuff ///
			power = Library.chargeTEFromItems(inventory, 0, power, maxPower);
			tanksNew[0].setType(1, 2, inventory);
			tanksNew[1].setType(3, 4, inventory);
			updateType();

			int maxConv = 50;
			int powerReq = 100000;

			int convert = Math.min(tanksNew[0].getFill(), tanksNew[1].getFill());
			convert = Math.min(convert, (plasmaNew.getMaxFill() - plasmaNew.getFill()) * 2);
			convert = Math.min(convert, maxConv);
			convert = (int) Math.min(convert, power / powerReq);
			convert = Math.max(0, convert);

			if(convert > 0 && plasmaNew.getTankType() != Fluids.NONE) {

				tanksNew[0].setFill(tanksNew[0].getFill() - convert);
				tanksNew[1].setFill(tanksNew[1].getFill() - convert);

				plasmaNew.setFill(plasmaNew.getFill() + convert * 2);
				power -= convert * powerReq;

				this.markDirty();
			}
			/// END Managing all the internal stuff ///

			/// START Loading plasma into the ITER ///

			ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset).getOpposite();
			int dist = 11;

			if(world.getBlockState(new BlockPos(pos.getX() + dir.offsetX * dist, pos.getY() + 2, pos.getZ() + dir.offsetZ * dist)).getBlock() == ModBlocks.iter) {
				int[] pos1 = ((MachineITER)ModBlocks.iter).findCore(world, pos.getX() + dir.offsetX * dist, pos.getY() + 2, pos.getZ() + dir.offsetZ * dist);
				
				if(pos1 != null) {
					TileEntity te = world.getTileEntity(new BlockPos(pos1[0], pos1[1], pos1[2]));

					if(te instanceof TileEntityITER) {
						TileEntityITER iter = (TileEntityITER)te;

						if(iter.plasmaNew.getFill() == 0 && this.plasmaNew.getTankType() != Fluids.NONE) {
							iter.plasmaNew.setTankType(this.plasmaNew.getTankType());
						}

						if(iter.isOn) {

							if(iter.plasmaNew.getTankType() == this.plasmaNew.getTankType()) {

								int toLoad = Math.min(iter.plasmaNew.getMaxFill() - iter.plasmaNew.getFill(), this.plasmaNew.getFill());
								toLoad = Math.min(toLoad, 40);
								this.plasmaNew.setFill(this.plasmaNew.getFill() - toLoad);
								iter.plasmaNew.setFill(iter.plasmaNew.getFill() + toLoad);
								this.markDirty();
								iter.markDirty();
							}
						}
					}
				}
			}

			/// END Loading plasma into the ITER ///

			/// START Notif packets ///
			for(int i = 0; i < tanksNew.length; i++)
				tanksNew[i].updateTank(pos.getX(), pos.getY(), pos.getZ(), world.provider.getDimension());
			plasmaNew.updateTank(pos.getX(), pos.getY(), pos.getZ(), world.provider.getDimension());
			NBTTagCompound data = new NBTTagCompound();
			data.setLong("power", power);
			this.networkPack(data, 50);
			/// END Notif packets ///
		}
	}
	
	@Override
	public void networkUnpack(NBTTagCompound nbt) {
		this.power = nbt.getLong("power");
	}

	private void updateConnections()  {

		ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset);
		ForgeDirection side = dir.getRotation(ForgeDirection.UP);

		for(int i = 1; i < 4; i++) {
			for(int j = -1; j < 2; j++) {
				this.trySubscribe(world, pos.getX() + side.offsetX * j + dir.offsetX * 2, pos.getY() + i, pos.getZ() + side.offsetZ * j + dir.offsetZ * 2, j < 0 ? ForgeDirection.DOWN : ForgeDirection.UP);
				this.trySubscribe(tanksNew[0].getTankType(), world, pos.getX() + side.offsetX * j + dir.offsetX * 2, pos.getY() + i, pos.getZ() + side.offsetZ * j + dir.offsetZ * 2, j < 0 ? ForgeDirection.DOWN : ForgeDirection.UP);
				this.trySubscribe(tanksNew[1].getTankType(), world, pos.getX() + side.offsetX * j + dir.offsetX * 2, pos.getY() + i, pos.getZ() + side.offsetZ * j + dir.offsetZ * 2, j < 0 ? ForgeDirection.DOWN : ForgeDirection.UP);
			}
		}
	}

	private void updateType() {

		List<FluidType> types = new ArrayList() {{ add(tanksNew[0].getTankType()); add(tanksNew[1].getTankType()); }};

		if(types.contains(Fluids.DEUTERIUM) && types.contains(Fluids.TRITIUM)) {
			plasmaNew.setTankType(Fluids.PLASMA_DT);
			return;
		}
		if(types.contains(Fluids.DEUTERIUM) && types.contains(Fluids.HELIUM3)) {
			plasmaNew.setTankType(Fluids.PLASMA_DH3);
			return;
		}
		if(types.contains(Fluids.DEUTERIUM) && types.contains(Fluids.HYDROGEN)) {
			plasmaNew.setTankType(Fluids.PLASMA_HD);
			return;
		}
		if(types.contains(Fluids.HYDROGEN) && types.contains(Fluids.TRITIUM)) {
			plasmaNew.setTankType(Fluids.PLASMA_HT);
			return;
		}
		if(types.contains(Fluids.HELIUM4) && types.contains(Fluids.OXYGEN)) {
			plasmaNew.setTankType(Fluids.PLASMA_XM);
			return;
		}
		if(types.contains(Fluids.BALEFIRE) && types.contains(Fluids.AMAT)) {
			plasmaNew.setTankType(Fluids.PLASMA_BF);
			return;
		}

		plasmaNew.setTankType(Fluids.NONE);
	}
	
	public long getPowerScaled(int i) {
		return (power * i) / maxPower;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);

		this.power = nbt.getLong("power");
		if(!converted){
			tanks[0].readFromNBT(nbt.getCompoundTag("fuel_1"));
			tanks[1].readFromNBT(nbt.getCompoundTag("fuel_2"));
			plasma.readFromNBT(nbt.getCompoundTag("plasma"));
			plasmaType = FluidRegistry.getFluid(nbt.getString("plasma_type"));
		} else {
			tanksNew[0].readFromNBT(nbt, "fuel_1n");
			tanksNew[1].readFromNBT(nbt, "fuel_2n");
			plasmaNew.readFromNBT(nbt, "plasman");
			if(nbt.hasKey("fuel_1")){
				nbt.removeTag("fuel_1");
				nbt.removeTag("fuel_2");
				nbt.removeTag("plasma");
				nbt.removeTag("plasma_type");
			}
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);

		nbt.setLong("power", power);
		if(!converted){
			nbt.setTag("fuel_1", tanks[0].writeToNBT(new NBTTagCompound()));
			nbt.setTag("fuel_2", tanks[1].writeToNBT(new NBTTagCompound()));
			nbt.setTag("plasma", plasma.writeToNBT(new NBTTagCompound()));
			if(plasmaType != null)
				nbt.setString("plasma_type", plasmaType.getName());
		} else {
			tanksNew[0].writeToNBT(nbt, "fuel_1n");
			tanksNew[1].writeToNBT(nbt, "fuel_2n");
			plasmaNew.writeToNBT(nbt, "plasman");
		}
		return nbt;
	}
	
	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return TileEntity.INFINITE_EXTENT_AABB;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared()
	{
		return 65536.0D;
	}
	
	@Override
	public void setPower(long i) {
		this.power = i;
	}

	@Override
	public long getPower() {
		return power;
	}

	@Override
	public long getMaxPower() {
		return maxPower;
	}

	@Override
	public FluidTankNTM[] getAllTanks() {
		return new FluidTankNTM[] {tanksNew[0], tanksNew[1], plasmaNew};
	}

	@Override
	public FluidTankNTM[] getReceivingTanks() {
		return tanksNew;
	}

	@Override
	public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
		if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
			return true;
		}
		return super.hasCapability(capability, facing);
	}

	@Override
	public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
		if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
			return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(
					new NTMFluidHandlerWrapper(this.getReceivingTanks(), null)
			);
		}
		return super.getCapability(capability, facing);
	}
}