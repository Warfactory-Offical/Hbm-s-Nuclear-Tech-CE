package com.hbm.tileentity.machine.rbmk;

import api.hbm.fluid.IFluidStandardSender;
import com.hbm.blocks.ModBlocks;
import com.hbm.capability.NTMFluidHandlerWrapper;
import com.hbm.entity.projectile.EntityRBMKDebris.DebrisType;
import com.hbm.inventory.RBMKOutgasserRecipes;
import com.hbm.inventory.control_panel.DataValue;
import com.hbm.inventory.control_panel.DataValueFloat;
import com.hbm.inventory.fluid.FluidStack;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.lib.DirPos;
import com.hbm.lib.Library;
import com.hbm.tileentity.machine.rbmk.TileEntityRBMKConsole.ColumnType;
import com.hbm.util.ContaminationUtil;
import com.hbm.util.Tuple;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

import javax.annotation.Nullable;
import java.util.Map;

public class TileEntityRBMKOutgasser extends TileEntityRBMKSlottedBase implements IRBMKFluxReceiver, IFluidStandardSender, IRBMKLoadable {

	public FluidTankNTM gas;
	public double progress = 0;
	public double usedFlux = 0;
	public int duration = 10000;

	public TileEntityRBMKOutgasser() {
		super(2);
		gas = new FluidTankNTM(Fluids.TRITIUM, 64000);
	}

	@Override
	public String getName() {
		return "container.rbmkOutgasser";
	}
	
	@Override
	public void update() {

		if(!world.isRemote) {

			if(!canProcess()) {
				this.progress = 0;
			}

			for(DirPos pos : getOutputPos()) {
				if(this.gas.getFill() > 0) this.sendFluid(gas, world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());
			}
		}
		
		super.update();
	}

	protected DirPos[] getOutputPos() {

		if(world.getBlockState(pos.add(0, -1, 0)).getBlock() == ModBlocks.rbmk_loader) {
			return new DirPos[] {
					new DirPos(this.pos.getX(), this.pos.getY() + RBMKDials.getColumnHeight(world) + 1, this.pos.getZ(), Library.POS_Y),
					new DirPos(this.pos.getX() + 1, this.pos.getY() - 1, this.pos.getZ(), Library.POS_X),
					new DirPos(this.pos.getX() - 1, this.pos.getY() - 1, this.pos.getZ(), Library.NEG_X),
					new DirPos(this.pos.getX(), this.pos.getY() - 1, this.pos.getZ() + 1, Library.POS_Z),
					new DirPos(this.pos.getX(), this.pos.getY() - 1, this.pos.getZ() - 1, Library.NEG_Z),
					new DirPos(this.pos.getX(), this.pos.getY() - 2, this.pos.getZ(), Library.NEG_Y)
			};
		} else if(world.getBlockState(pos.add(0, -2, 0)).getBlock() == ModBlocks.rbmk_loader) {
			return new DirPos[] {
					new DirPos(this.pos.getX(), this.pos.getY() + RBMKDials.getColumnHeight(world) + 1, this.pos.getZ(), Library.POS_Y),
					new DirPos(this.pos.getX() + 1, this.pos.getY() - 2, this.pos.getZ(), Library.POS_X),
					new DirPos(this.pos.getX() - 1, this.pos.getY() - 2, this.pos.getZ(), Library.NEG_X),
					new DirPos(this.pos.getX(), this.pos.getY() - 2, this.pos.getZ() + 1, Library.POS_Z),
					new DirPos(this.pos.getX(), this.pos.getY() - 2, this.pos.getZ() - 1, Library.NEG_Z),
					new DirPos(this.pos.getX(), this.pos.getY() - 3, this.pos.getZ(), Library.NEG_Y)
			};
		} else {
			return new DirPos[] {
					new DirPos(this.pos.getX(), this.pos.getY() + RBMKDials.getColumnHeight(world) + 1, this.pos.getZ(), Library.POS_Y)
			};
		}
	}

	@Override
	public void receiveFlux(NType type, double flux) {
		
		if(canProcess()) {
			
			if(type == NType.FAST)
				flux *= 0.2D;
			
			progress += flux * RBMKDials.getOutgasserMod(world);
			
			if(progress > duration) {
				process();
				this.markDirty();
			}
		} else if(!inventory.getStackInSlot(0).isEmpty()){
			if(type == NType.FAST)
				flux *= 0.2D;
			ContaminationUtil.neutronActivateItem(inventory.getStackInSlot(0), (float)(flux * 0.001), 1F);
			this.markDirty();
		}
		this.usedFlux = flux;
	}



	public boolean canProcess() {

		if(inventory.getStackInSlot(0).isEmpty())
			return false;

		Tuple.Pair<ItemStack, FluidStack> output = RBMKOutgasserRecipes.getOutput(inventory.getStackInSlot(0));

		if(output == null)
			return false;

		FluidStack fluid = output.getValue();

		if(fluid != null) {
			if(gas.getTankType() != fluid.type && gas.getFill() > 0) return false;
			gas.setTankType(fluid.type);
			if(gas.getFill() + fluid.fill > gas.getMaxFill()) return false;
		}

		ItemStack out = output.getKey();

		if(inventory.getStackInSlot(1).isEmpty() || out == null)
			return true;

		return inventory.getStackInSlot(1).getItem() == out.getItem() && inventory.getStackInSlot(1).getItemDamage() == out.getItemDamage() && inventory.getStackInSlot(1).getCount() + out.getCount() <= inventory.getStackInSlot(1).getMaxStackSize();
	}


	private void process() {

		Tuple.Pair<ItemStack, FluidStack> output = RBMKOutgasserRecipes.getOutput(inventory.getStackInSlot(0));
		this.inventory.getStackInSlot(0).shrink(1);
		this.progress = 0;

		if(output.getValue() != null) {
			gas.setFill(gas.getFill() + output.getValue().fill);
		}

		ItemStack out = output.getKey();

		if(out != null) {
			if(inventory.getStackInSlot(1).isEmpty()) {
				inventory.setStackInSlot(1, out.copy());
			} else {
				inventory.getStackInSlot(1).setCount(inventory.getStackInSlot(1).getCount() + out.getCount());
			}
		}
	}
	
	
	@Override
	public void onMelt(int reduce) {
		
		int count = 4 + world.rand.nextInt(2);
		
		for(int i = 0; i < count; i++) {
			spawnDebris(DebrisType.BLANK);
		}
		
		super.onMelt(reduce);
	}

	@Override
	public ColumnType getConsoleType() {
		return ColumnType.OUTGASSER;
	}

	@Override
	public NBTTagCompound getNBTForConsole() {
		NBTTagCompound data = new NBTTagCompound();
		data.setInteger("gas", this.gas.getFill());
		data.setInteger("maxGas", this.gas.getMaxFill());
		data.setShort("type", (short)this.gas.getTankType().getID());
		data.setDouble("usedFlux", this.usedFlux);
		data.setDouble("progress", this.progress);
		data.setDouble("maxProgress", this.duration);
		return data;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		
		this.progress = nbt.getDouble("progress");
		this.duration = nbt.getInteger("duration");
		this.gas.readFromNBT(nbt, "gas");
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		
		nbt.setDouble("progress", this.progress);
		nbt.setInteger("duration", this.duration);
		this.gas.writeToNBT(nbt, "gas");
		
		return nbt;
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemStack) {
		return RBMKOutgasserRecipes.getOutput(itemStack) != null && i == 0;
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemStack, int j) {
		return i == 1;
	}

	@Override
	public boolean canLoad(ItemStack toLoad) {
		return toLoad != null && inventory.getStackInSlot(0).isEmpty();
	}

	@Override
	public void load(ItemStack toLoad) {
		inventory.setStackInSlot(0, toLoad.copy());
		this.markDirty();
	}

	@Override
	public boolean canUnload() {
		return !inventory.getStackInSlot(1).isEmpty();
	}

	@Override
	public ItemStack provideNext() {
		return inventory.getStackInSlot(1);
	}

	@Override
	public void unload() {
		inventory.setStackInSlot(1, ItemStack.EMPTY);
		this.markDirty();
	}

	@Override
	public FluidTankNTM[] getAllTanks() {
		return new FluidTankNTM[] {gas};
	}

	@Override
	public FluidTankNTM[] getSendingTanks() {
		return new FluidTankNTM[] {gas};
	}

	// control panel
	@Override
	public Map<String, DataValue> getQueryData() {
		Map<String, DataValue> data = super.getQueryData();

		data.put("gas", new DataValueFloat(this.gas.getFill()));
		data.put("progress", new DataValueFloat((float) this.progress));
		data.put("maxProgress", new DataValueFloat((float) this.duration));

		return data;
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
					new NTMFluidHandlerWrapper(this.getSendingTanks(), null)
			);
		}
		return super.getCapability(capability, facing);
	}
}