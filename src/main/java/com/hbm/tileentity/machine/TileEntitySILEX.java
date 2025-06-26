package com.hbm.tileentity.machine;

import api.hbm.fluid.IFluidStandardReceiver;
import com.hbm.capability.NTMFluidHandlerWrapper;
import com.hbm.interfaces.IFFtoNTMF;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.SILEXRecipes;
import com.hbm.inventory.SILEXRecipes.SILEXRecipe;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemFELCrystal.EnumWavelengths;
import com.hbm.lib.ForgeDirection;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.BufferUtil;
import com.hbm.util.InventoryUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.HashMap;

public class TileEntitySILEX extends TileEntityMachineBase implements ITickable, IFluidStandardReceiver, IFFtoNTMF{

	public EnumWavelengths mode = EnumWavelengths.NULL;
	public FluidTank tank;
	public FluidTankNTM tankNew;
	public ComparableStack current;
	public int currentFill;
	public static final int maxFill = 16000;
	public int progress;
	public final int processTime = 80;

	private static boolean converted = false;
	
	//0: Input
	//2-3: Fluid Containers
	//4: Output
	//5-10: Queue

	public TileEntitySILEX() {
		super(11);
		//acid
		tank = new FluidTank(16000);
		tankNew = new FluidTankNTM(Fluids.PEROXIDE, 16000);
		converted = true;
	}

	public Fluid getTankType(){
		return tank.getFluid() == null ? null : tank.getFluid().getFluid();
	}
	
	@Override
	public String getName() {
		return "container.machineSILEX";
	}

	@Override
	public void update() {
		if(!converted){
			resizeInventory(11);
			convertAndSetFluid(tank.getFluid().getFluid(), tank, tankNew);
			converted = true;
		}
		if(!world.isRemote) {

			tankNew.setType(1, 1, inventory);
			tankNew.loadTank(2, 3, inventory);

			ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - 10).getRotation(ForgeDirection.UP);
			this.trySubscribe(tankNew.getTankType(), world, pos.getX() + dir.offsetX * 2, pos.getY() + 1, pos.getZ() + dir.offsetZ * 2, dir);
			this.trySubscribe(tankNew.getTankType(), world, pos.getX() - dir.offsetX * 2, pos.getY() + 1, pos.getZ() - dir.offsetZ * 2, dir.getOpposite());

			loadFluid();

			if(!process()) {
				this.progress = 0;
			}

			dequeue();

			if(currentFill <= 0) {
				current = null;
			}

			this.networkPackNT(50);

			this.mode = EnumWavelengths.NULL;
		}
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeInt(currentFill);
		buf.writeInt(progress);
		BufferUtil.writeString(buf, mode.toString());

		tankNew.serialize(buf);

		if(this.current != null) {
			buf.writeInt(Item.getIdFromItem(this.current.item));
			buf.writeInt(this.current.meta);
		}
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		currentFill = buf.readInt();
		progress = buf.readInt();
		mode = EnumWavelengths.valueOf(BufferUtil.readString(buf));

		tankNew.deserialize(buf);

		if(currentFill > 0) {
			current = new ComparableStack(Item.getItemById(buf.readInt()), 1, buf.readInt());
		} else
			current = null;
	}
	
	public void handleButtonPacket(int value, int meta) {
		
		this.currentFill = 0;
		this.current = null;
	}
	
	public int getProgressScaled(int i) {
		return (progress * i) / processTime;
	}
	
	public int getFluidScaled(int i) {
		return (tankNew.getFill() * i) / tankNew.getMaxFill();
	}
	
	public int getFillScaled(int i) {
		return (currentFill * i) / maxFill;
	}
	
	public static final HashMap<FluidType, ComparableStack> fluidConversion = new HashMap<>();

	static {
		putFluid(Fluids.UF6);
		putFluid(Fluids.PUF6);
		putFluid(Fluids.DEATH);
	}

	private static void putFluid(FluidType fluid) {
		fluidConversion.put(fluid, new ComparableStack(ModItems.fluid_icon, 1, fluid.getID()));
	}
	
	int loadDelay;
	
	public void loadFluid() {
		
		ComparableStack conv = fluidConversion.get(tankNew.getTankType());

		if(conv != null) {

			if(currentFill == 0) {
				current = (ComparableStack) conv.copy();
			}

			if(current != null && current.equals(conv)) {

				int toFill = Math.min(50, Math.min(maxFill - currentFill, tankNew.getFill()));
				currentFill += toFill;
				tankNew.setFill(tankNew.getFill() - toFill);
			}
		} else {
			ComparableStack direct = new ComparableStack(ModItems.fluid_icon, 1, tankNew.getTankType().getID());

			if(SILEXRecipes.getOutput(direct.toStack()) != null) {

				if(currentFill == 0) {
					current = (ComparableStack) direct.copy();
				}

				if(current != null && current.equals(direct)) {

					int toFill = Math.min(50, Math.min(maxFill - currentFill, tankNew.getFill()));
					currentFill += toFill;
					tankNew.setFill(tankNew.getFill() - toFill);
				}
			}
		}

		loadDelay++;

		if(loadDelay > 20)
			loadDelay = 0;

		if(loadDelay == 0 && !inventory.getStackInSlot(0).isEmpty() && tankNew.getTankType() == Fluids.PEROXIDE && (this.current == null || this.current.equals(new ComparableStack(inventory.getStackInSlot(0)).makeSingular()))) {
			SILEXRecipe recipe = SILEXRecipes.getOutput(inventory.getStackInSlot(0));

			if(recipe == null)
				return;

			int load = recipe.fluidProduced;

			if(load <= this.maxFill - this.currentFill && load <= tankNew.getFill()) {
				this.currentFill += load;
				this.current = new ComparableStack(inventory.getStackInSlot(0)).makeSingular();
				tankNew.setFill(tankNew.getFill() - load);
				this.inventory.getStackInSlot(0).shrink(1);
			}
		}
	}
	
	private boolean process() {
		
		if(current == null || currentFill <= 0)
			return false;
		
		SILEXRecipe recipe = SILEXRecipes.getOutput(current.toStack());
		
		if(recipe == null)
			return false;

		if(!(recipe.laserStrength.ordinal() <= this.mode.ordinal()))
			return false;
		
		if(currentFill < recipe.fluidConsumed)
			return false;
		
		if(!inventory.getStackInSlot(4).isEmpty())
			return false;

		progress += Math.pow(2, this.mode.ordinal() - recipe.laserStrength.ordinal() + 1) / 2;
		
		if(progress >= processTime) {
			
			currentFill -= recipe.fluidConsumed;
			
			ItemStack out = (WeightedRandom.getRandomItem(world.rand, recipe.outputs)).asStack();
			inventory.setStackInSlot(4, out.copy());
			progress = 0;
			this.markDirty();
		}
		
		return true;
	}
	
	private void dequeue() {
		
		if(!inventory.getStackInSlot(4).isEmpty()) {
			
			for(int i = 5; i < 11; i++) {
				
				if(!inventory.getStackInSlot(i).isEmpty() && inventory.getStackInSlot(i).getCount() < inventory.getStackInSlot(i).getMaxStackSize() && InventoryUtil.doesStackDataMatch(inventory.getStackInSlot(3), inventory.getStackInSlot(i))) {
					inventory.getStackInSlot(i).grow(1);
					inventory.getStackInSlot(3).shrink(1);
					return;
				}
			}
			
			for(int i = 5; i < 11; i++) {
				
				if(inventory.getStackInSlot(i).isEmpty()) {
					inventory.setStackInSlot(i, inventory.getStackInSlot(3).copy());
					inventory.setStackInSlot(3, ItemStack.EMPTY);
					return;
				}
			}
		}
	}

	@Override
	public int[] getAccessibleSlotsFromSide(EnumFacing p_94128_1_) {
		return new int[] { 0, 5, 6, 7, 8, 9, 10 };
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemStack) {
		
		if(i == 0) return SILEXRecipes.getOutput(itemStack) != null;
		
		return false;
	}

	@Override
	public boolean canExtractItem(int slot, ItemStack itemStack, int side) {
		return slot >= 5;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		if(!converted){
			this.tank.readFromNBT(nbt.getCompoundTag("tank"));
		} else{
			this.tankNew.readFromNBT(nbt, "tankNew");
		}
		this.currentFill = nbt.getInteger("fill");
		this.mode = EnumWavelengths.valueOf(nbt.getString("mode"));
		
		if(this.currentFill > 0) {
			this.current = new ComparableStack(Item.getItemById(nbt.getInteger("item")), 1, nbt.getInteger("meta"));
		}
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		if(!converted){
			nbt.setTag("tank", this.tank.writeToNBT(new NBTTagCompound()));
		} else{
			this.tankNew.writeToNBT(nbt, "tankNew");
		}
		nbt.setInteger("fill", this.currentFill);
		nbt.setString("mode", mode.toString());
		
		if(this.current != null) {
			nbt.setInteger("item", Item.getIdFromItem(this.current.item));
			nbt.setInteger("meta", this.current.meta);
		}
		return nbt;
	}
	
	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return new AxisAlignedBB(
				pos.getX() - 1,
				pos.getY(),
				pos.getZ() - 1,
				pos.getX() + 2,
				pos.getY() + 3,
				pos.getZ() + 2
			);
	}

	@Override
	public FluidTankNTM[] getAllTanks() {
		return new FluidTankNTM[] {tankNew};
	}

	@Override
	public FluidTankNTM[] getReceivingTanks() {
		return new FluidTankNTM[] {tankNew};
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared()
	{
		return 65536.0D;
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