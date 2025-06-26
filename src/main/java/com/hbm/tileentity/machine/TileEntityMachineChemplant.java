package com.hbm.tileentity.machine;

import api.hbm.energymk2.IEnergyReceiverMK2;
import api.hbm.fluid.IFluidStandardTransceiver;
import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.machine.MachineChemplant;
import com.hbm.capability.NTMFluidHandlerWrapper;
import com.hbm.handler.MultiblockHandler;
import com.hbm.interfaces.IFFtoNTMF;
import com.hbm.inventory.ChemplantRecipes;
import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.inventory.UpgradeManager;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemMachineUpgrade;
import com.hbm.lib.DirPos;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.lib.Library;
import com.hbm.main.MainRegistry;
import com.hbm.packet.AuxParticlePacket;
import com.hbm.packet.PacketDispatcher;
import com.hbm.sound.AudioWrapper;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.InventoryUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TileEntityMachineChemplant extends TileEntityMachineBase implements IEnergyReceiverMK2, IFluidStandardTransceiver, ITickable, IFFtoNTMF {

	public long power;
	public static final long maxPower = 100000;
	public int progress;
	public int maxProgress = 100;
	public boolean isProgressing;
	private AudioWrapper audio;
	public FluidTank[] tanks;
	public Fluid[] tankTypes;
	public FluidTankNTM[] tanksNew;
	public ItemStack previousTemplate = ItemStack.EMPTY;
	//Drillgon200: Yeah I don't even know what I was doing originally
	public ItemStack previousTemplate2 = ItemStack.EMPTY;
	//upgraded stats
	int consumption = 100;
	int speed = 100;
	public UpgradeManager manager = new UpgradeManager();

	private boolean converted;

	public TileEntityMachineChemplant() {
		super(21);
		/*
		 * 0 Battery
		 * 1-3 Upgrades
		 * 4 Schematic
		 * 5-8 Output
		 * 9-10 FOut In
		 * 11-12 FOut Out
		 * 13-16 Input
		 * 17-18 FIn In
		 * 19-20 FIn Out
		 */
		tanksNew = new FluidTankNTM[4];
		for(int i = 0; i < 4; i++) {
			tanksNew[i] = new FluidTankNTM(Fluids.NONE, 24_000);
		}

		tanks = new FluidTank[4];
		tanks[0] = new FluidTank(24000);
		tanks[1] = new FluidTank(24000);
		tanks[2] = new FluidTank(24000);
		tanks[3] = new FluidTank(24000);
		tankTypes = new Fluid[]{null, null, null, null};

		converted = true;
	}

	// last successful load
	int lsl0 = 0;
	int lsl1 = 0;
	int lsu0 = 0;
	int lsu1 = 0;

	public boolean isUseableByPlayer(EntityPlayer player) {
		if(world.getTileEntity(pos) != this) {
			return false;
		} else {
			return player.getDistanceSqToCenter(pos) <= 128;
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		String[] types;
		this.power = nbt.getLong("powerTime");
		isProgressing = nbt.getBoolean("progressing");
		if(!converted) {
			tanks[0].readFromNBT(nbt.getCompoundTag("input1"));
			tanks[1].readFromNBT(nbt.getCompoundTag("input2"));
			tanks[2].readFromNBT(nbt.getCompoundTag("output1"));
			tanks[3].readFromNBT(nbt.getCompoundTag("output2"));

			types = new String[]{nbt.getString("tankType0"), nbt.getString("tankType1"), nbt.getString("tankType2"), nbt.getString("tankType3")};
			if(!types[0].equals("empty")) {
				tankTypes[0] = FluidRegistry.getFluid(types[0]);
			} else {
				tankTypes[0] = null;
			}
			if(!types[1].equals("empty")) {
				tankTypes[1] = FluidRegistry.getFluid(types[1]);
			} else {
				tankTypes[1] = null;
			}
			if(!types[2].equals("empty")) {
				tankTypes[2] = FluidRegistry.getFluid(types[2]);
			} else {
				tankTypes[2] = null;
			}
			if(!types[3].equals("empty")) {
				tankTypes[3] = FluidRegistry.getFluid(types[3]);
			} else {
				tankTypes[3] = null;
			}
		} else {
			for (int i = 0; i < tanksNew.length; i++) {
				tanksNew[i].readFromNBT(nbt, "t" + i);
			}
			if(nbt.hasKey("input1")){
				nbt.removeTag("input1");
				nbt.removeTag("input2");
				nbt.removeTag("output1");
				nbt.removeTag("output2");
				nbt.removeTag("tankType0");
				nbt.removeTag("tankType1");
				nbt.removeTag("tankType2");
				nbt.removeTag("tankType3");
			}
		}
		if(nbt.hasKey("inventory"))
			inventory.deserializeNBT((NBTTagCompound) nbt.getTag("inventory"));
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setLong("powerTime", power);

		nbt.setBoolean("progressing", isProgressing);
		if(!converted){
			String[] types = new String[]{tankTypes[0] != null ? tankTypes[0].getName() : "empty", tankTypes[1] != null ? tankTypes[1].getName() : "empty", tankTypes[2] != null ? tankTypes[2].getName() : "empty", tankTypes[3] != null ? tankTypes[3].getName() : "empty"};

			NBTTagCompound input1 = new NBTTagCompound();
			NBTTagCompound input2 = new NBTTagCompound();
			NBTTagCompound output1 = new NBTTagCompound();
			NBTTagCompound output2 = new NBTTagCompound();

			tanks[0].writeToNBT(input1);
			tanks[1].writeToNBT(input2);
			tanks[2].writeToNBT(output1);
			tanks[3].writeToNBT(output2);

			nbt.setTag("input1", input1);
			nbt.setTag("input2", input2);
			nbt.setTag("output1", output1);
			nbt.setTag("output2", output2);

			nbt.setString("tankType0", types[0] != null ? types[0] : "empty");
			nbt.setString("tankType1", types[1] != null ? types[1] : "empty");
			nbt.setString("tankType2", types[2] != null ? types[2] : "empty");
			nbt.setString("tankType3", types[3] != null ? types[3] : "empty");
		} else {
			for (int i = 0; i < tanksNew.length; i++) {
				tanksNew[i].writeToNBT(nbt, "t" + i);
			}
		}

		NBTTagCompound inv = inventory.serializeNBT();
		nbt.setTag("inventory", inv);
		return nbt;
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeLong(power);
		buf.writeInt(progress);
		buf.writeInt(maxProgress);
		buf.writeBoolean(isProgressing);

		for(int i = 0; i < tanks.length; i++)
			tanksNew[i].serialize(buf);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		power = buf.readLong();
		progress = buf.readInt();
		maxProgress = buf.readInt();
		isProgressing = buf.readBoolean();

		for(int i = 0; i < tanks.length; i++)
			tanksNew[i].deserialize(buf);
	}

	public long getPowerScaled(long i) {
		return (power * i) / maxPower;
	}

	public int getProgressScaled(int i) {
		return (progress * i) / Math.max(10, maxProgress);
	}

	@Override
	public AudioWrapper createAudioLoop() {
		return MainRegistry.proxy.getLoopedSound(HBMSoundHandler.chemplantOperate, SoundCategory.BLOCKS, pos.getX(), pos.getY(), pos.getZ(), 1.0F, 10F);
	}

	@Override
	public void onChunkUnload() {

		if(audio != null) {
			audio.stopSound();
			audio = null;
		}
	}

	@Override
	public void invalidate() {

		super.invalidate();

		if(audio != null) {
			audio.stopSound();
			audio = null;
		}
	}

	@Override
	public void update() {
		if(!converted){
			convertAndSetFluids(tankTypes, tanks, tanksNew);
			converted = true;
		}

		if(!world.isRemote) {

			this.speed = 100;
			this.consumption = 100;

			this.isProgressing = false;
			this.power = Library.chargeTEFromItems(inventory, 0, power, maxPower);

			int fluidDelay = 40;

			if(lsu0 >= fluidDelay && tanksNew[0].loadTank(17, 19, inventory)) lsl0 = 0;
			if(lsu1 >= fluidDelay && tanksNew[1].loadTank(18, 20, inventory)) lsl1 = 0;

			if(lsl0 >= fluidDelay && !inventory.getStackInSlot(17).isEmpty() && !FluidTankNTM.noDualUnload.contains(inventory.getStackInSlot(17).getItem())) if(tanksNew[0].unloadTank(17, 19, inventory)) lsu0 = 0;
			if(lsl1 >= fluidDelay && !inventory.getStackInSlot(18).isEmpty() && !FluidTankNTM.noDualUnload.contains(inventory.getStackInSlot(18).getItem())) if(tanksNew[1].unloadTank(18, 20, inventory)) lsu1 = 0;

			tanksNew[2].unloadTank(9, 11, inventory);
			tanksNew[3].unloadTank(10, 12, inventory);

			if(lsl0 < fluidDelay) lsl0++;
			if(lsl1 < fluidDelay) lsl1++;
			if(lsu0 < fluidDelay) lsu0++;
			if(lsu1 < fluidDelay) lsu1++;

			ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset).getOpposite();
			ForgeDirection rot = dir.getRotation(ForgeDirection.DOWN);
			TileEntity te1 = world.getTileEntity(new BlockPos(pos.getX() - dir.offsetX * 2, pos.getY(), pos.getZ() - dir.offsetZ * 2));
			TileEntity te2 = world.getTileEntity(new BlockPos(pos.getX() + dir.offsetX * 3 + rot.offsetX, pos.getY(), pos.getZ() + dir.offsetZ * 3 + rot.offsetZ));

			if(te1 != null && te1.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, dir.toEnumFacing().rotateY())) {
				IItemHandler cap = te1.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, dir.toEnumFacing().rotateY());
				int[] outputSlots = new int[]{ 5, 6, 7, 8, 11, 12, 19, 20 };
				for(int i : outputSlots) {
					tryFillContainerCap(cap, i);
				}
			}

			if(te2 != null && te2.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, dir.toEnumFacing().rotateY())) {
				IItemHandler cap = te2.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, dir.toEnumFacing().rotateY());
				int[] slots;
				if(te2 instanceof TileEntityMachineBase) {
					slots = ((TileEntityMachineBase) te2).getAccessibleSlotsFromSide(dir.toEnumFacing().rotateY());
					tryFillAssemblerCap(cap, slots, (TileEntityMachineBase) te2);
				} else {
					slots = new int[cap.getSlots()];
					for(int i = 0; i < slots.length; i++)
						slots[i] = i;
					tryFillAssemblerCap(cap, slots, null);
				}
			}


			if(isProgressing && this.world.getTotalWorldTime() % 3 == 0) {
				ForgeDirection rotP = dir.getRotation(ForgeDirection.UP);
				double x = pos.getX() + 0.5 + dir.offsetX * 1.125 + rotP.offsetX * 0.125;
				double y = pos.getY() + 3;
				double z = pos.getZ() + 0.5 + dir.offsetZ * 1.125 + rotP.offsetZ * 0.125;
				world.spawnParticle(EnumParticleTypes.CLOUD, x, y, z, 0.0, 0.1, 0.0);
			}

			if(world.getTotalWorldTime() % 20 == 0) {
				this.updateConnections();
			}

			for(DirPos pos : getConPos()) {
				if(tanksNew[2].getFill() > 0) this.sendFluid(tanksNew[2], world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());
				if(tanksNew[3].getFill() > 0) this.sendFluid(tanksNew[3], world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());
			}

			manager.eval(inventory, 1, 3);

			int speedLevel = Math.min(manager.getLevel(ItemMachineUpgrade.UpgradeType.SPEED), 3);
			int powerLevel = Math.min(manager.getLevel(ItemMachineUpgrade.UpgradeType.POWER), 3);
			int overLevel = manager.getLevel(ItemMachineUpgrade.UpgradeType.OVERDRIVE);

			this.speed -= speedLevel * 25;
			this.consumption += speedLevel * 300;
			this.speed += powerLevel * 5;
			this.consumption -= powerLevel * 20;
			this.speed /= (overLevel + 1);
			this.consumption *= (overLevel + 1);

			if(this.speed <= 0) {
				this.speed = 1;
			}

			if(!canProcess()) {
				this.progress = 0;
			} else {
				isProgressing = true;
				process();
			}

			this.networkPackNT(150);
		} else {

			float volume = this.getVolume(1F);

			if(isProgressing && volume > 0) {

				if(audio == null) {
					audio = this.createAudioLoop();
					audio.updateVolume(volume);
					audio.startSound();
				} else if(!audio.isPlaying()) {
					audio = rebootAudio(audio);
					audio.updateVolume(volume);
				}

			} else {

				if(audio != null) {
					audio.stopSound();
					audio = null;
				}
			}
		}
	}



	private void updateConnections() {

		for(DirPos pos : getConPos()) {
			this.trySubscribe(world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());
			this.trySubscribe(tanksNew[0].getTankType(), world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());
			this.trySubscribe(tanksNew[1].getTankType(), world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());
		}
	}

	public DirPos[] getConPos() {

		ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset).getOpposite();
		ForgeDirection rot = dir.getRotation(ForgeDirection.DOWN);

		return new DirPos[] {
				new DirPos(pos.getX() + rot.offsetX * 3,				pos.getY(),	pos.getZ() + rot.offsetZ * 3,				rot),
				new DirPos(pos.getX() - rot.offsetX * 2,				pos.getY(),	pos.getZ() - rot.offsetZ * 2,				rot.getOpposite()),
				new DirPos(pos.getX() + rot.offsetX * 3 + dir.offsetX,	pos.getY(),	pos.getZ() + rot.offsetZ * 3 + dir.offsetZ, rot),
				new DirPos(pos.getX() - rot.offsetX * 2 + dir.offsetX,	pos.getY(),	pos.getZ() - rot.offsetZ * 2 + dir.offsetZ, rot.getOpposite())
		};
	}

	private boolean canProcess() {

		if(inventory.getStackInSlot(4).isEmpty() || inventory.getStackInSlot(4).getItem() != ModItems.chemistry_template)
			return false;

		ChemplantRecipes.ChemRecipe recipe = ChemplantRecipes.indexMapping.get(inventory.getStackInSlot(4).getItemDamage());

		if(recipe == null)
			return false;

		setupTanks(recipe);

		if(this.power < this.consumption) return false;
		if(!hasRequiredFluids(recipe)) return false;
		if(!hasSpaceForFluids(recipe)) return false;
		if(!hasRequiredItems(recipe)) return false;
		if(!hasSpaceForItems(recipe)) return false;

		return true;
	}

	private void setupTanks(ChemplantRecipes.ChemRecipe recipe) {
		if(recipe.inputFluids[0] != null) tanksNew[0].withPressure(recipe.inputFluids[0].pressure).setTankType(recipe.inputFluids[0].type);	else tanksNew[0].setTankType(Fluids.NONE);
		if(recipe.inputFluids[1] != null) tanksNew[1].withPressure(recipe.inputFluids[1].pressure).setTankType(recipe.inputFluids[1].type);	else tanksNew[1].setTankType(Fluids.NONE);
		if(recipe.outputFluids[0] != null) tanksNew[2].withPressure(recipe.outputFluids[0].pressure).setTankType(recipe.outputFluids[0].type);	else tanksNew[2].setTankType(Fluids.NONE);
		if(recipe.outputFluids[1] != null) tanksNew[3].withPressure(recipe.outputFluids[1].pressure).setTankType(recipe.outputFluids[1].type);	else tanksNew[3].setTankType(Fluids.NONE);
	}

	private boolean hasRequiredFluids(ChemplantRecipes.ChemRecipe recipe) {
		if(recipe.inputFluids[0] != null && tanksNew[0].getFill() < recipe.inputFluids[0].fill) return false;
		if(recipe.inputFluids[1] != null && tanksNew[1].getFill() < recipe.inputFluids[1].fill) return false;
		return true;
	}

	private boolean hasSpaceForFluids(ChemplantRecipes.ChemRecipe recipe) {
		if(recipe.outputFluids[0] != null && tanksNew[2].getFill() + recipe.outputFluids[0].fill > tanksNew[2].getMaxFill()) return false;
		if(recipe.outputFluids[1] != null && tanksNew[3].getFill() + recipe.outputFluids[1].fill > tanksNew[3].getMaxFill()) return false;
		return true;
	}

	private boolean hasRequiredItems(ChemplantRecipes.ChemRecipe recipe) {
		return InventoryUtil.doesArrayHaveIngredients(inventory, 13, 16, recipe.inputs);
	}

	private boolean hasSpaceForItems(ChemplantRecipes.ChemRecipe recipe) {
		return InventoryUtil.doesArrayHaveSpace(inventory, 5, 8, recipe.outputs);
	}

	private void process() {

		this.power -= this.consumption;
		this.progress++;

		if(!inventory.getStackInSlot(0).isEmpty() && inventory.getStackInSlot(0).getItem() == ModItems.meteorite_sword_machined)
			inventory.setStackInSlot(0, ModItems.meteorite_sword_treated.getDefaultInstance()); //fisfndmoivndlmgindgifgjfdnblfm

		ChemplantRecipes.ChemRecipe recipe = ChemplantRecipes.indexMapping.get(inventory.getStackInSlot(4).getItemDamage());

		this.maxProgress = recipe.getDuration() * this.speed / 100;

		if(maxProgress <= 0) maxProgress = 1;

		if(this.progress >= this.maxProgress) {
			consumeFluids(recipe);
			produceFluids(recipe);
			consumeItems(recipe);
			produceItems(recipe);
			this.progress = 0;
			this.markDirty();
		}
	}

	private void consumeFluids(ChemplantRecipes.ChemRecipe recipe) {
		if(recipe.inputFluids[0] != null) tanksNew[0].setFill(tanksNew[0].getFill() - recipe.inputFluids[0].fill);
		if(recipe.inputFluids[1] != null) tanksNew[1].setFill(tanksNew[1].getFill() - recipe.inputFluids[1].fill);
	}

	private void produceFluids(ChemplantRecipes.ChemRecipe recipe) {
		if(recipe.outputFluids[0] != null) tanksNew[2].setFill(tanksNew[2].getFill() + recipe.outputFluids[0].fill);
		if(recipe.outputFluids[1] != null) tanksNew[3].setFill(tanksNew[3].getFill() + recipe.outputFluids[1].fill);
	}

	private void consumeItems(ChemplantRecipes.ChemRecipe recipe) {

		for(AStack in : recipe.inputs) {
			if(in != null)
				InventoryUtil.tryConsumeAStack(inventory, 13, 16, in);
		}
	}

	private void produceItems(ChemplantRecipes.ChemRecipe recipe) {

		for(ItemStack out : recipe.outputs) {
			if(out != null)
				InventoryUtil.tryAddItemToInventory(inventory, 5, 8, out.copy());
		}
	}

	//private int extractIngredient(IItemHandler container)

	//I can't believe that worked.
	public IItemHandlerModifiable cloneItemStackProper(IItemHandlerModifiable array) {
		IItemHandlerModifiable stack = new ItemStackHandler(array.getSlots());

		for(int i = 0; i < array.getSlots(); i++)
			if(array.getStackInSlot(i) != null)
				stack.setStackInSlot(i, array.getStackInSlot(i).copy());
			else
				stack.setStackInSlot(i, ItemStack.EMPTY);

		return stack;
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
				if(inventory.getStackInSlot(slot).isEmpty())
					inventory.setStackInSlot(slot, ItemStack.EMPTY);

				outputStack.setCount(1);
				chest.insertItem(i, outputStack, false);

				return true;
			}
		}
		//Chest is full
		return false;
	}

	private int getValidSlot(AStack nextIngredient) {
		int firstFreeSlot = -1;
		int stackCount = (int) Math.ceil(nextIngredient.count() / 64F);
		int stacksFound = 0;

		nextIngredient = nextIngredient.singulize();

		for(int k = 13; k < 17; k++) { //scaning inventory if some of the ingredients allready exist
			if(stacksFound < stackCount) {
				ItemStack assStack = inventory.getStackInSlot(k).copy();
				if(assStack.isEmpty()) {
					if(firstFreeSlot < 13)
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
		if(firstFreeSlot < 13) // nothing free in assembler inventory anymore
			return -2;
		return firstFreeSlot;
	}

	public boolean tryFillAssemblerCap(IItemHandler container, int[] allowedSlots, TileEntityMachineBase te) {
		if(allowedSlots.length < 1)
			return false;
		List<AStack> recipeIngredients = null;//Loading Ingredients
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

			for(int ig = 0; ig < recipeIngredients.size(); ig++) {

				AStack nextIngredient = recipeIngredients.get(ig).copy(); // getting new ingredient

				int ingredientSlot = getValidSlot(nextIngredient);


				if(ingredientSlot < 13)
					continue; // Ingredient filled or Assembler is full

				int possibleAmount = inventory.getStackInSlot(ingredientSlot).getMaxStackSize() - inventory.getStackInSlot(ingredientSlot).getCount(); // how many items do we need to fill the stack?

				if(possibleAmount == 0) { // full
					System.out.println("This should never happen method getValidSlot broke");
					continue;
				}
				// Ok now we know what we are looking for (nexIngredient) and where to put it (ingredientSlot) - So lets see if we find some of it in containers
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

	//boolean true: remove items, boolean false: simulation mode
	public boolean removeItems(List<AStack> stack, IItemHandlerModifiable array) {

		if(stack == null)
			return true;
		for(int i = 0; i < stack.size(); i++) {
			for(int j = 0; j < stack.get(i).count(); j++) {
				AStack sta = stack.get(i).copy();
				sta.setCount(1);
				if(!canRemoveItemFromArray(sta, array))
					return false;
			}
		}

		return true;

	}

	public boolean canRemoveItemFromArray(AStack stack, IItemHandlerModifiable array) {

		AStack st = stack.copy();

		for(int i = 6; i < 18; i++) {

			if(array.getStackInSlot(i).getItem() != Items.AIR) {
				ItemStack sta = array.getStackInSlot(i).copy();
				sta.setCount(1);

				if(st.isApplicable(sta) && array.getStackInSlot(i).getCount() > 0) {
					array.getStackInSlot(i).shrink(1);
					;

					if(array.getStackInSlot(i).isEmpty())
						array.setStackInSlot(i, ItemStack.EMPTY);
					;

					return true;
				}
			}
		}

		return false;
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
		return power;
	}

	@Override
	public void setPower(long i) {
		power = i;
	}

	@Override
	public long getMaxPower() {
		return maxPower;
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return TileEntity.INFINITE_EXTENT_AABB;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return 65536.0D;
	}

	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		NBTTagCompound tag = new NBTTagCompound();
		writeToNBT(tag);

		return new SPacketUpdateTileEntity(pos, 0, tag);
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {

		readFromNBT(pkt.getNbtCompound());
	}

	public ItemStack getStackInSlot(int i) {
		return inventory.getStackInSlot(i);
	}

	@Override
	public String getName() {
		return "container.chemplant";
	}

	@Override
	public FluidTankNTM[] getSendingTanks() {
		return new FluidTankNTM[] {tanksNew[2], tanksNew[3]};
	}

	@Override
	public FluidTankNTM[] getReceivingTanks() {
		return new FluidTankNTM[] {tanksNew[0], tanksNew[1]};
	}

	@Override
	public FluidTankNTM[] getAllTanks() {
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
					new NTMFluidHandlerWrapper(this.getReceivingTanks(), this.getSendingTanks())
			);
		}
		return super.getCapability(capability, facing);
	}
}
