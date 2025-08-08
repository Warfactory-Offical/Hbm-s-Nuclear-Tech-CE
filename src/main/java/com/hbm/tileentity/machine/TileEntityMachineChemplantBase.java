package com.hbm.tileentity.machine;

import com.hbm.api.energymk2.IEnergyReceiverMK2;
import com.hbm.api.fluidmk2.IFluidStandardTransceiverMK2;
import com.hbm.blocks.BlockDummyable;
import com.hbm.capability.NTMEnergyCapabilityWrapper;
import com.hbm.capability.NTMFluidHandlerWrapper;
import com.hbm.interfaces.IFFtoNTMF;
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
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.oredict.OreDictionary;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class TileEntityMachineChemplantBase extends TileEntityMachineBase implements IEnergyReceiverMK2, ITickable, IFluidStandardTransceiverMK2, IGUIProvider, IFFtoNTMF {
	public long power;
	public int[] progress;
	public int[] maxProgress;
	public boolean isProgressing;

	public FluidTankNTM[] tanksNew;
	public TypedFluidTank[] tanks;

	public static class TypedFluidTank {
		protected Fluid type;
		protected final FluidTank tank;

		protected TypedFluidTank(Fluid type, FluidTank tank) {
			this.type = type;
			this.tank = tank;
		}

		public void setType(@Nullable Fluid type) {
			if(type == null) {
				this.tank.setFluid(null);
			}

			if(this.type == type) {
				return;
			}

			this.type = type;
			this.tank.setFluid(null);
		}

		public void writeToNBT(NBTTagCompound nbt) {
			if(this.type != null) {
				nbt.setString("type", this.type.getName());
			}

			this.tank.writeToNBT(nbt);
		}

		public void readFromNBT(NBTTagCompound nbt) {
			if(nbt.hasKey("type")) {
				this.type = FluidRegistry.getFluid(nbt.getString("type"));
			}
			this.tank.readFromNBT(nbt);
		}

		public FluidTank getTank() {
			return tank;
		}

		public Fluid getType() {
			return type;
		}
	}

	int consumption = 100;
	int speed = 100;
	private static boolean converted = false;

	public TileEntityMachineChemplantBase(int scount) {
		super(scount);

		int count = this.getRecipeCount();

		progress = new int[count];
		maxProgress = new int[count];

		tanksNew = new FluidTankNTM[4 * count];
		for(int i = 0; i < 4 * count; i++) {
			tanksNew[i] = new FluidTankNTM(Fluids.NONE, getTankCapacity(), i);
		}
		tanks = new TypedFluidTank[4 * count];
		for(int idx = 0; idx < tanks.length; ++idx) {
			tanks[idx] = new TypedFluidTank(null, new FluidTank(this.getTankCapacity()));
		}

		converted = true;
	}

	//Norwood: Why the fuck was the fluid array downsized by one? that causes out of bounds you know?
	public Fluid[] getFluidsTyped(TypedFluidTank[] tanks){
		Fluid[] fluid = new Fluid[tanks.length];
		for(int i = 0; i < tanks.length; i++){
			if (tanks[i].getType() != null) fluid[i] = tanks[i].getType();
			else fluid[i] =Fluids.NONE.getFF();;
		}
		return fluid;
	}

	public FluidTank[] getTanks(TypedFluidTank[] tanksTyped){
		FluidTank[] tanks = new FluidTank[tanksTyped.length];
		for(int i = 0; i < tanksTyped.length; i++){
			tanks[i] = tanksTyped[i].getTank();
		}
		return tanks;
	}

	@Override
	public void update() {
		if(!world.isRemote) {
			if(!converted){
				convertAndSetFluids(getFluidsTyped(tanks), getTanks(tanks), tanksNew);
				converted = true;
			}
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

		if(recipe != null) {

			DirPos[] positions = getInputPositions();
			int[] indices = getSlotIndicesFromIndex(index);

			for(DirPos pos1 : positions) {
				BlockPos pos = pos1.getPos();
				TileEntity te = world.getTileEntity(pos);

				if(te != null && te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.NORTH)) {
					IItemHandler cap = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.NORTH);
					int[] slots;
					if(te instanceof TileEntityMachineBase) {
						ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset).getOpposite();
						slots = ((TileEntityMachineBase) te).getAccessibleSlotsFromSide(dir.toEnumFacing());
						tryFillAssemblerCap(cap, slots, (TileEntityMachineBase) te, indices[0], indices[1], recipe.inputs);
					} else {
						slots = new int[cap.getSlots()];
						for(int i = 0; i < slots.length; i++)
							slots[i] = i;
						tryFillAssemblerCap(cap, slots, null, indices[0], indices[1], recipe.inputs);
					}
				}
			}
		}
	}

	private void unloadItems(int index) {
		DirPos[] positions = getOutputPositions();
		int[] indices = getSlotIndicesFromIndex(index);

		for(DirPos pos1 : positions) {
			BlockPos pos = pos1.getPos();
			TileEntity te = world.getTileEntity(pos);
			if(te != null && te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.NORTH)) {
				IItemHandler cap = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.NORTH);
				for(int i = indices[2]; i <= indices[3]; i++) {
					tryFillContainerCap(cap, i);
				}
			}
		}
	}

	//Unloads output into chests. Capability version.
	public boolean tryFillContainerCap(IItemHandler chest, int slot) {
		//Check if we have something to output
		if(inventory.getStackInSlot(slot).isEmpty())
			return false;

		for(int i = 0; i < chest.getSlots(); i++) {

			ItemStack outputStack = inventory.getStackInSlot(slot).copy();
			if(outputStack.isEmpty())
				return false;

			ItemStack chestItem = chest.getStackInSlot(i).copy();
			if(chestItem.isEmpty() || (Library.areItemStacksCompatible(outputStack, chestItem, false) && chestItem.getCount() < chestItem.getMaxStackSize())) {
				inventory.getStackInSlot(slot).shrink(1);

				outputStack.setCount(1);
				chest.insertItem(i, outputStack, false);

				return true;
			}
		}

		return false;
	}

	public boolean tryFillAssemblerCap(IItemHandler container, int[] allowedSlots, TileEntityMachineBase te, int minSlot, int maxSlot, AStack[] recipeIngredients) {
		if(allowedSlots.length < 1)
			return false;

		if(recipeIngredients == null) //No recipe template found
			return false;
		else {
			Map<Integer, ItemStack> itemStackMap = new HashMap<Integer, ItemStack>();

			for(int slot : allowedSlots) {
				container.getStackInSlot(slot);
				if(container.getStackInSlot(slot).isEmpty()) { // check next slot in chest if it is empty
					continue;
				} else { // found an item in chest
					itemStackMap.put(slot, container.getStackInSlot(slot).copy());
				}
			}
			if(itemStackMap.size() == 0) {
				return false;
			}

			for(int ig = 0; ig < recipeIngredients.length; ig++) {

				AStack nextIngredient = recipeIngredients[ig].copy(); // getting new ingredient

				int ingredientSlot = getValidSlot(nextIngredient, minSlot, maxSlot);


				if(ingredientSlot < minSlot)
					continue; // Ingredient filled or Assembler is full

				int possibleAmount = inventory.getStackInSlot(ingredientSlot).getMaxStackSize() - inventory.getStackInSlot(ingredientSlot).getCount(); // how many items do we need to fill the stack?

				if(possibleAmount == 0) { // full
					System.out.println("This should never happen method getValidSlot broke");
					continue;
				}
				// Ok now we know what we are looking for(nexIngredient) and where to put it (ingredientSlot) - So lets see if we find some of it in containers
				for(Map.Entry<Integer, ItemStack> set :
						itemStackMap.entrySet()) {
					ItemStack stack = set.getValue();
					int slot = set.getKey();
					ItemStack compareStack = stack.copy();
					compareStack.setCount(1);

					if(isItemAcceptable(nextIngredient.getStack(), compareStack)) { // bingo found something

						int foundCount = Math.min(stack.getCount(), possibleAmount);
						if(te != null && !te.canExtractItem(slot, stack, foundCount))
							continue;
						if(foundCount > 0) {
							possibleAmount -= foundCount;
							container.extractItem(slot, foundCount, false);
							inventory.getStackInSlot(ingredientSlot);
							if(inventory.getStackInSlot(ingredientSlot).isEmpty()) {

								stack.setCount(foundCount);
								inventory.setStackInSlot(ingredientSlot, stack);

							} else {
								inventory.getStackInSlot(ingredientSlot).grow(foundCount); // transfer complete
							}
						} else {
							break; // ingredientSlot filled
						}
					}
				}

			}
			return true;
		}
	}

	private int getValidSlot(AStack nextIngredient, int minSlot, int maxSlot) {
		int firstFreeSlot = -1;
		int stackCount = (int) Math.ceil(nextIngredient.count() / 64F);
		int stacksFound = 0;

		nextIngredient = nextIngredient.singulize();

		for(int k = minSlot; k <= maxSlot; k++) { //scaning inventory if some of the ingredients allready exist
			if(stacksFound < stackCount) {
				ItemStack assStack = inventory.getStackInSlot(k).copy();
				if(assStack.isEmpty()) {
					if(firstFreeSlot < minSlot)
						firstFreeSlot = k;
					continue;
				} else { // check if there are already enough filled stacks is full

					assStack.setCount(1);
					if(nextIngredient.isApplicable(assStack)) { // check if it is the right item

						if(inventory.getStackInSlot(k).getCount() < assStack.getMaxStackSize()) // is that stack full?
							return k; // found a not full slot where we already have that ingredient
						else
							stacksFound++;
					}
				}
			} else {
				return -1; // All required stacks are full
			}
		}
		if(firstFreeSlot < minSlot) // nothing free in assembler inventory anymore
			return -2;
		return firstFreeSlot;
	}

	public boolean isItemAcceptable(ItemStack stack1, ItemStack stack2) {

		if(stack1 != null && stack2 != null && stack1.getItem() != Items.AIR && stack1.getItem() != Items.AIR) {
			if(Library.areItemStacksCompatible(stack1, stack2))
				return true;

			int[] ids1 = OreDictionary.getOreIDs(stack1);
			int[] ids2 = OreDictionary.getOreIDs(stack2);

			if(ids1.length > 0 && ids2.length > 0) {
				for(int i = 0; i < ids1.length; i++)
					for(int j = 0; j < ids2.length; j++)
						if(ids1[i] == ids2[j])
							return true;
			}
		}

		return false;
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

		List<FluidTankNTM> inTanks = new ArrayList();

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

	public void transferFluid(int amount, FluidType type, int pressure) {

		/*
		 * this whole new fluid mumbo jumbo extra abstraction layer might just be a bandaid
		 * on the gushing wound that is the current fluid systemm but i'll be damned if it
		 * didn't at least do what it's supposed to. half a decade and we finally have multi
		 * tank support for tanks with matching fluid types!!
		 */
		if(amount <= 0)
			return;

		List<FluidTankNTM> send = new ArrayList();

		for(FluidTankNTM tank : outTanks()) {
			if(tank.getTankType() == type && tank.getPressure() == pressure) {
				send.add(tank);
			}
		}

		if(send.size() == 0)
			return;

		int offer = 0;
		List<Integer> weight = new ArrayList();

		for(FluidTankNTM tank : send) {
			int fillWeight = tank.getFill();
			offer += fillWeight;
			weight.add(fillWeight);
		}

		int tracker = amount;

		for(int i = 0; i < send.size(); i++) {

			FluidTankNTM tank = send.get(i);
			int fillWeight = weight.get(i);
			int part = amount * fillWeight / offer;

			tank.setFill(tank.getFill() - part);
			tracker -= part;
		}

		//making sure to properly deduct even the last mB lost by rounding errors
		for(int i = 0; i < 100 && tracker > 0; i++) {

			FluidTankNTM tank = send.get(i % send.size());

			if(tank.getFill() > 0) {
				int total = Math.min(tank.getFill(), tracker);
				tracker -= total;
				tank.setFill(tank.getFill() - total);
			}
		}
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
	public long transferFluid(FluidType type, int pressure, long fluid) {
		int amount = (int) fluid;

		if(amount <= 0)
			return 0;

		List<FluidTankNTM> rec = new ArrayList();

		for(FluidTankNTM tank : inTanks()) {
			if(tank.getTankType() == type && tank.getPressure() == pressure) {
				rec.add(tank);
			}
		}

		if(rec.size() == 0)
			return fluid;

		int demand = 0;
		List<Integer> weight = new ArrayList();

		for(FluidTankNTM tank : rec) {
			int fillWeight = tank.getMaxFill() - tank.getFill();
			demand += fillWeight;
			weight.add(fillWeight);
		}

		for(int i = 0; i < rec.size(); i++) {

			if(demand <= 0)
				break;

			FluidTankNTM tank = rec.get(i);
			int fillWeight = weight.get(i);
			int part = (int) (Math.min((long)amount, (long)demand) * (long)fillWeight / (long)demand);

			tank.setFill(tank.getFill() + part);
			fluid -= part;
		}

		return fluid;
	}

	@Override
	public long getDemand(FluidType type, int pressure) {
		return getMaxFluidFill(type) - getFluidFillForTransfer(type, pressure);
	}

	protected NBTTagList serializeTanks() {
		NBTTagList tankList = new NBTTagList();
		for(int i = 0; i < tanks.length; ++i) {
			NBTTagCompound tank = new NBTTagCompound();
			tanks[i].writeToNBT(tank);
			tank.setByte("index", (byte) i);

			tankList.appendTag(tank);
		}

		return tankList;
	}

	protected void deserializeTanks(NBTTagList tankList) {
		for(int i = 0; i < tankList.tagCount(); ++i) {
			NBTTagCompound tank = tankList.getCompoundTagAt(i);
			int index = tank.getByte("index");

			tanks[index].readFromNBT(tank);
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);

		this.power = nbt.getLong("power");
		this.progress = nbt.getIntArray("progress");

		if(progress.length == 0)
			progress = new int[this.getRecipeCount()];

		if(!converted){
			NBTTagList tankList = nbt.getTagList("tanks", 10);
			deserializeTanks(tankList);
		} else {
			for (int i = 0; i < tanksNew.length; i++) {
				tanksNew[i].readFromNBT(nbt, "t" + i);
			}
			if(nbt.hasKey("tanks")) nbt.removeTag("tanks");
		}
	}

	@Override
	public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);

		nbt.setLong("power", power);
		nbt.setIntArray("progress", progress);
		if(!converted){
			nbt.setTag("tanks", serializeTanks());
		} else {
			for (int i = 0; i < tanksNew.length; i++) {
				tanksNew[i].writeToNBT(nbt, "t" + i);
			}
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