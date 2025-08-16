package com.hbm.tileentity.machine;

import com.hbm.api.energymk2.IEnergyReceiverMK2;
import com.hbm.api.fluidmk2.IFluidStandardTransceiverMK2;
import com.hbm.capability.NTMEnergyCapabilityWrapper;
import com.hbm.capability.NTMFluidHandlerWrapper;
import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.inventory.recipes.ChemplantRecipes;
import com.hbm.items.ModItems;
import com.hbm.lib.DirPos;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.Library;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.InventoryUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public abstract class TileEntityMachineChemplantBase extends TileEntityMachineBase implements IEnergyReceiverMK2, ITickable, IFluidStandardTransceiverMK2, IGUIProvider {
	public long power;
	public int[] progress;
	public int[] maxProgress;
	public boolean isProgressing;

	public FluidTankNTM[] tanksNew;

	int consumption = 100;
	int speed = 100;

	public TileEntityMachineChemplantBase(int scount) {
		super(scount);

		int count = this.getRecipeCount();

		progress = new int[count];
		maxProgress = new int[count];

		tanksNew = new FluidTankNTM[4 * count];
		for(int i = 0; i < 4 * count; i++) {
			tanksNew[i] = new FluidTankNTM(Fluids.NONE, getTankCapacity(), i);
		}
	}

	@Override
	public void update() {
		if(!world.isRemote) {
			int count = this.getRecipeCount();

			this.isProgressing = false;
			this.power = Library.chargeTEFromItems(inventory, 0, this.power, this.getMaxPower());

			for(int idx = 0; idx < count; ++idx) {
				loadItems(idx);
				unloadItems(idx);
			}

			for(int i = 0; i < count; i++) {
				if(!canProcess(i)) {
					this.progress[i] = 0;
				} else {
					isProgressing = true;
					process(i);
				}
			}
		}
	}

	protected boolean canProcess(int index) {

		int template = getTemplateIndex(index);

		if(inventory.getStackInSlot(template).isEmpty() || inventory.getStackInSlot(template).getItem() != ModItems.chemistry_template)
			return false;

		ChemplantRecipes.ChemRecipe recipe = ChemplantRecipes.indexMapping.get(inventory.getStackInSlot(template).getItemDamage());

		if(recipe == null)
			return false;

		setupTanks(recipe, index);

		if(this.power < this.consumption) return false;
		if(!hasRequiredFluids(recipe, index)) return false;
		if(!hasSpaceForFluids(recipe, index)) return false;
		if(!hasRequiredItems(recipe, index)) return false;
		if(!hasSpaceForItems(recipe, index)) return false;

		return true;
	}

	private void setupTanks(ChemplantRecipes.ChemRecipe recipe, int index) {
		if(recipe.inputFluids[0] != null) tanksNew[index * 4].withPressure(recipe.inputFluids[0].pressure).setTankType(recipe.inputFluids[0].type);		else tanksNew[index * 4].setTankType(Fluids.NONE);
		if(recipe.inputFluids[1] != null) tanksNew[index * 4 + 1].withPressure(recipe.inputFluids[1].pressure).setTankType(recipe.inputFluids[1].type);	else tanksNew[index * 4 + 1].setTankType(Fluids.NONE);
		if(recipe.outputFluids[0] != null) tanksNew[index * 4 + 2].withPressure(recipe.outputFluids[0].pressure).setTankType(recipe.outputFluids[0].type);	else tanksNew[index * 4 + 2].setTankType(Fluids.NONE);
		if(recipe.outputFluids[1] != null) tanksNew[index * 4 + 3].withPressure(recipe.outputFluids[1].pressure).setTankType(recipe.outputFluids[1].type);	else tanksNew[index * 4 + 3].setTankType(Fluids.NONE);
	}

	private boolean hasRequiredFluids(ChemplantRecipes.ChemRecipe recipe, int index) {
		if(recipe.inputFluids[0] != null && tanksNew[index * 4].getFill() < recipe.inputFluids[0].fill) return false;
		if(recipe.inputFluids[1] != null && tanksNew[index * 4 + 1].getFill() < recipe.inputFluids[1].fill) return false;
		return true;
	}

	private boolean hasSpaceForFluids(ChemplantRecipes.ChemRecipe recipe, int index) {
		if(recipe.outputFluids[0] != null && tanksNew[index * 4 + 2].getFill() + recipe.outputFluids[0].fill > tanksNew[index * 4 + 2].getMaxFill()) return false;
		if(recipe.outputFluids[1] != null && tanksNew[index * 4 + 3].getFill() + recipe.outputFluids[1].fill > tanksNew[index * 4 + 3].getMaxFill()) return false;
		return true;
	}

	private boolean hasRequiredItems(ChemplantRecipes.ChemRecipe recipe, int index) {
		int[] indices = getSlotIndicesFromIndex(index);
		return InventoryUtil.doesArrayHaveIngredients(inventory, indices[0], indices[1], recipe.inputs);
	}

	private boolean hasSpaceForItems(ChemplantRecipes.ChemRecipe recipe, int index) {
		int[] indices = getSlotIndicesFromIndex(index);

		return InventoryUtil.doesArrayHaveSpace(inventory, indices[2], indices[3], recipe.outputs);
	}

	protected void process(int index) {

		this.power -= this.consumption;
		this.progress[index]++;

		if(!inventory.getStackInSlot(0).isEmpty() && inventory.getStackInSlot(0).getItem() == ModItems.meteorite_sword_machined)
			inventory.setStackInSlot(0, new ItemStack(ModItems.meteorite_sword_treated)); //fisfndmoivndlmgindgifgjfdnblfm

		int template = getTemplateIndex(index);
		ChemplantRecipes.ChemRecipe recipe = ChemplantRecipes.indexMapping.get(inventory.getStackInSlot(template).getItemDamage());

		this.maxProgress[index] = recipe.getDuration() * this.speed / 100;

		if(maxProgress[index] <= 0) maxProgress[index] = 1;

		if(this.progress[index] >= this.maxProgress[index]) {
			consumeFluids(recipe, index);
			produceFluids(recipe, index);
			consumeItems(recipe, index);
			produceItems(recipe, index);
			this.progress[index] = 0;
			this.markDirty();
		}
	}

	private void consumeFluids(ChemplantRecipes.ChemRecipe recipe, int index) {
		if(recipe.inputFluids[0] != null) tanksNew[index * 4].setFill(tanksNew[index * 4].getFill() - recipe.inputFluids[0].fill);
		if(recipe.inputFluids[1] != null) tanksNew[index * 4 + 1].setFill(tanksNew[index * 4 + 1].getFill() - recipe.inputFluids[1].fill);
	}

	private void produceFluids(ChemplantRecipes.ChemRecipe recipe, int index) {
		if(recipe.outputFluids[0] != null) tanksNew[index * 4 + 2].setFill(tanksNew[index * 4 + 2].getFill() + recipe.outputFluids[0].fill);
		if(recipe.outputFluids[1] != null) tanksNew[index * 4 + 3].setFill(tanksNew[index * 4 + 3].getFill() + recipe.outputFluids[1].fill);
	}

	private void consumeItems(ChemplantRecipes.ChemRecipe recipe, int index) {

		int[] indices = getSlotIndicesFromIndex(index);

		for(AStack in : recipe.inputs) {
			if(in != null)
				InventoryUtil.tryConsumeAStack(inventory, indices[0], indices[1], in);
		}
	}

	private void produceItems(ChemplantRecipes.ChemRecipe recipe, int index) {

		int[] indices = getSlotIndicesFromIndex(index);

		for(ItemStack out : recipe.outputs) {
			if(out != null)
				InventoryUtil.tryAddItemToInventory(inventory, indices[2], indices[3], out.copy());
		}
	}

	private void loadItems(int index) {
		int template = getTemplateIndex(index);
		if(inventory.getStackInSlot(template).isEmpty() || inventory.getStackInSlot(template).getItem() != ModItems.chemistry_template)
			return;

		ChemplantRecipes.ChemRecipe recipe = ChemplantRecipes.indexMapping.get(inventory.getStackInSlot(template).getItemDamage());

		if(recipe == null || recipe.inputs == null)
			return;

		List<AStack> ingredients = new ArrayList<>();
		for (AStack a : recipe.inputs) {
			if (a != null) ingredients.add(a);
		}

		if (ingredients.isEmpty()) {
			return;
		}

		DirPos[] positions = getInputPositions();
		int[] indices = getSlotIndicesFromIndex(index);

		for(DirPos pos1 : positions) {
			BlockPos pos = pos1.getPos();
			TileEntity te = world.getTileEntity(pos);
			EnumFacing facing = pos1.getDir().getOpposite().toEnumFacing();

			if(te != null && te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing)) {
				IItemHandler cap = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing);
				int[] slots;
				TileEntityMachineBase sourceTE = (te instanceof TileEntityMachineBase) ? (TileEntityMachineBase) te : null;
				if(sourceTE != null) {
					slots = sourceTE.getAccessibleSlotsFromSide(facing);
				} else {
					slots = new int[cap.getSlots()];
					for(int i = 0; i < slots.length; i++)
						slots[i] = i;
				}
				Library.pullItemsForRecipe(cap, slots, this.inventory, ingredients, sourceTE, indices[0], indices[1], this::markDirty);
			}
		}
	}

	private void unloadItems(int index) {
		DirPos[] positions = getOutputPositions();
		int[] indices = getSlotIndicesFromIndex(index);

		List<ItemStack> itemsToEject = new ArrayList<>();
		for (int i = indices[2]; i <= indices[3]; i++) {
			ItemStack stack = inventory.getStackInSlot(i);
			if (!stack.isEmpty()) {
				itemsToEject.add(stack);
				inventory.setStackInSlot(i, ItemStack.EMPTY);
			}
		}
		if (itemsToEject.isEmpty()) return;
		List<ItemStack> leftovers = itemsToEject;
		for (DirPos pos1 : positions) {
			if (leftovers.isEmpty()) break;
			BlockPos pos = pos1.getPos();
			ForgeDirection accessSide = pos1.getDir().getOpposite();
			leftovers = Library.popProducts(world, pos, accessSide, leftovers);
		}
		if (!leftovers.isEmpty()) {
			for (ItemStack leftover : leftovers) {
				InventoryUtil.tryAddItemToInventory(inventory, indices[2], indices[3], leftover);
			}
		}
		if (itemsToEject.size() != leftovers.size()) {
			this.markDirty();
		}
	}

	@Override
	public long getPower() {
		return this.power;
	}

	@Override
	public void setPower(long power) {
		this.power = power;
	}

	public int getMaxFluidFill(FluidType type) {

		int maxFill = 0;

		for(FluidTankNTM tank : inTanks()) {
			if(tank.getTankType() == type) {
				maxFill += tank.getMaxFill();
			}
		}

		return maxFill;
	}

	public List<FluidTankNTM> inTanks() {

		List<FluidTankNTM> inTanks = new ArrayList<>();

		for(int i = 0; i < tanksNew.length; i++) {
			FluidTankNTM tank = tanksNew[i];
			if(tank.index % 4 < 2) {
				inTanks.add(tank);
			}
		}

		return inTanks;
	}

	public int getFluidFillForTransfer(FluidType type, int pressure) {

		int fill = 0;

		for(FluidTankNTM tank : outTanks()) {
			if(tank.getTankType() == type && tank.getPressure() == pressure) {
				fill += tank.getFill();
			}
		}

		return fill;
	}

	public List<FluidTankNTM> outTanks() {

		List<FluidTankNTM> outTanks = new ArrayList();

		for(int i = 0; i < tanksNew.length; i++) {
			FluidTankNTM tank = tanksNew[i];
			if(tank.index % 4 > 1) {
				outTanks.add(tank);
			}
		}

		return outTanks;
	}

	@Override
	public FluidTankNTM[] getAllTanks() {
		return tanksNew;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);

		this.power = nbt.getLong("power");
		this.progress = nbt.getIntArray("progress");

		if(progress.length == 0)
			progress = new int[this.getRecipeCount()];

		for (int i = 0; i < tanksNew.length; i++) {
			tanksNew[i].readFromNBT(nbt, "t" + i);
		}
		if(nbt.hasKey("tanks")) nbt.removeTag("tanks");
	}

	@Override
	public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);

		nbt.setLong("power", power);
		nbt.setIntArray("progress", progress);
		for (int i = 0; i < tanksNew.length; i++) {
			tanksNew[i].writeToNBT(nbt, "t" + i);
		}
		return nbt;
	}

	@Override
	public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
		if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY || capability == CapabilityEnergy.ENERGY) {
			return true;
		}
		return super.hasCapability(capability, facing);
	}

	@Override
	public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
		if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
			return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(
					new NTMFluidHandlerWrapper(this.inTanks(), this.outTanks())
			);
		}
		if (capability == CapabilityEnergy.ENERGY) {
			return CapabilityEnergy.ENERGY.cast(
					new NTMEnergyCapabilityWrapper(this)
			);
		}
		return super.getCapability(capability, facing);
	}

	public abstract int getRecipeCount();

	public abstract int getTankCapacity();

	public abstract int getTemplateIndex(int index);

	/**
	 * @param index
	 * @return A size 4 int array containing min input, max input, min output and max output indices in that order.
	 */
	public abstract int[] getSlotIndicesFromIndex(int index);

	public abstract DirPos[] getInputPositions();

	public abstract DirPos[] getOutputPositions();
}
