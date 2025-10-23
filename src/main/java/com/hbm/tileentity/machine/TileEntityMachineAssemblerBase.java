package com.hbm.tileentity.machine;

import com.hbm.api.energymk2.IEnergyReceiverMK2;
import com.hbm.inventory.RecipesCommon;
import com.hbm.inventory.recipes.AssemblerRecipes;
import com.hbm.items.ModItems;
import com.hbm.items.ModItems.ToolSets;
import com.hbm.items.machine.ItemAssemblyTemplate;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.Library;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.InventoryUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public abstract class TileEntityMachineAssemblerBase extends TileEntityMachineBase implements IEnergyReceiverMK2, ITickable {

    public long power;
    public int[] progress;
    public int[] maxProgress;
    public boolean isProgressing;
    public boolean[] needsTemplateSwitch;

    int consumption = 100;
    int speed = 100;

    public TileEntityMachineAssemblerBase(int scount, boolean enableFluidWrapper, boolean enableEnergyWrapper) {
        super(scount, enableFluidWrapper, enableEnergyWrapper);

        int count = this.getRecipeCount();

        progress = new int[count];
        maxProgress = new int[count];
        needsTemplateSwitch = new boolean[count];
    }

    @Override
    public void update() {

        if(!world.isRemote) {

            int count = this.getRecipeCount();

            this.isProgressing = false;
            this.power = Library.chargeTEFromItems(this.inventory, getPowerSlot(), power, this.getMaxPower());

            for(int i = 0; i < count; i++) {
                unloadItems(i);
                loadItems(i);
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

        if (template < 0 || template >= this.inventory.getSlots()) {
            return false;
        }

        ItemStack templateStack = this.inventory.getStackInSlot(template);
        if(templateStack.isEmpty() || templateStack.getItem() != ModItems.assembly_template)
            return false;

        List<RecipesCommon.AStack> recipe = AssemblerRecipes.getRecipeFromTempate(this.inventory.getStackInSlot(template));
        ItemStack output = AssemblerRecipes.getOutputFromTempate(this.inventory.getStackInSlot(template));

        if(recipe == null)
            return false;

        if(this.power < this.consumption) return false;
        if(!hasRequiredItems(recipe, index)) return false;
        if(!hasSpaceForItems(output, index)) return false;

        return true;
    }

    private boolean hasRequiredItems(List<RecipesCommon.AStack> recipe, int index) {
        int[] indices = getSlotIndicesFromIndex(index);
        return InventoryUtil.doesArrayHaveIngredients(this.inventory, indices[0], indices[1], recipe.toArray(new RecipesCommon.AStack[0]));
    }

    private boolean hasSpaceForItems(ItemStack recipe, int index) {
        int[] indices = getSlotIndicesFromIndex(index);
        return InventoryUtil.doesArrayHaveSpace(this.inventory, indices[2], indices[2], new ItemStack[] { recipe });
    }

    protected void process(int index) {

        this.power -= this.consumption;
        this.progress[index]++;

        if(!this.inventory.getStackInSlot(0).isEmpty() && this.inventory.getStackInSlot(0).getItem() == ToolSets.meteorite_sword_alloyed)
            this.inventory.setStackInSlot(0, new ItemStack(ToolSets.meteorite_sword_machined)); //fisfndmoivndlmgindgifgjfdnblfm

        int template = getTemplateIndex(index);

        List<RecipesCommon.AStack> recipe = AssemblerRecipes.getRecipeFromTempate(this.inventory.getStackInSlot(template));
        ItemStack output = AssemblerRecipes.getOutputFromTempate(this.inventory.getStackInSlot(template));
        int time = ItemAssemblyTemplate.getProcessTime(this.inventory.getStackInSlot(template));

        this.maxProgress[index] = time * this.speed / 100;

        if(this.progress[index] >= this.maxProgress[index]) {
            consumeItems(recipe, index);
            produceItems(output, index);
            this.progress[index] = 0;
            this.needsTemplateSwitch[index] = true;
            this.markDirty();
        }
    }

    private void consumeItems(List<RecipesCommon.AStack> recipe, int index) {

        int[] indices = getSlotIndicesFromIndex(index);

        for(RecipesCommon.AStack in : recipe) {
            if(in != null)
                InventoryUtil.tryConsumeAStack(this.inventory, indices[0], indices[1], in);
        }
    }

    private void produceItems(ItemStack out, int index) {

        int[] indices = getSlotIndicesFromIndex(index);

        if(out != null) {
            InventoryUtil.tryAddItemToInventory(this.inventory, indices[2], indices[2], out.copy());
        }
    }

    private void loadItems(int index) {
        int templateSlot = getTemplateIndex(index);
        if(templateSlot < 0 || templateSlot >= inventory.getSlots()) return;
        ItemStack templateStack = inventory.getStackInSlot(templateSlot);
        if(templateStack.isEmpty() || templateStack.getItem() != ModItems.assembly_template)
            return;
        List<RecipesCommon.AStack> recipe = AssemblerRecipes.getRecipeFromTempate(templateStack);
        if(recipe == null || recipe.isEmpty()) return;
        int[] indices = getSlotIndicesFromIndex(index);
        ImmutablePair<BlockPos, ForgeDirection>[] positions = getInputPositions();

        for(ImmutablePair<BlockPos, ForgeDirection> posPair : positions) {
            BlockPos sourcePos = posPair.left;
            EnumFacing accessFacing = posPair.right.getOpposite().toEnumFacing();

            TileEntity te = world.getTileEntity(sourcePos);
            if(te != null && te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, accessFacing)) {
                IItemHandler cap = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, accessFacing);
                int[] slots;
                TileEntityMachineBase sourceTE = (te instanceof TileEntityMachineBase) ? (TileEntityMachineBase) te : null;
                if(sourceTE != null) {
                    slots = sourceTE.getAccessibleSlotsFromSide(accessFacing);
                } else {
                    slots = new int[cap.getSlots()];
                    for(int i = 0; i < slots.length; i++)
                        slots[i] = i;
                }
                Library.pullItemsForRecipe(cap, slots, this.inventory, recipe, sourceTE, indices[0], indices[1], this::markDirty);
            }
        }
    }

    private void unloadItems(int index) {
        int[] indices = getSlotIndicesFromIndex(index);
        int outputSlot = indices[2];

        ItemStack stackToEject = inventory.getStackInSlot(outputSlot);
        if (stackToEject.isEmpty()) {
            return;
        }

        List<ItemStack> itemsToEject = new ArrayList<>();
        itemsToEject.add(stackToEject.copy());
        inventory.setStackInSlot(outputSlot, ItemStack.EMPTY);

        List<ItemStack> leftovers = itemsToEject;
        ImmutablePair<BlockPos, ForgeDirection>[] positions = getOutputPositions();

        for (ImmutablePair<BlockPos, ForgeDirection> posPair : positions) {
            if (leftovers.isEmpty()) break;

            BlockPos exportToPos = posPair.left;
            ForgeDirection accessSide = posPair.right.getOpposite();

            leftovers = Library.popProducts(world, exportToPos, accessSide, leftovers);
        }

        if (!leftovers.isEmpty()) {
            inventory.setStackInSlot(outputSlot, leftovers.get(0));
        }

        if (itemsToEject.size() != leftovers.size()) {
            this.markDirty();
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);

        this.power = nbt.getLong("power");
        if(nbt.hasKey("progress")) this.progress = nbt.getIntArray("progress");
        if(nbt.hasKey("maxProgress")) this.maxProgress = nbt.getIntArray("maxProgress");
    }

    @Override
    public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);

        nbt.setLong("power", power);
        nbt.setIntArray("progress", progress);
        nbt.setIntArray("maxProgress", maxProgress);
        return nbt;
    }

    @Override
    public long getPower() {
        return this.power;
    }

    @Override
    public void setPower(long power) {
        this.power = power;
    }

    public abstract int getRecipeCount();
    public abstract int getTemplateIndex(int index);

    /**
     * @param index
     * @return A size 4 int array containing min input, max input, min output and max output indices in that order.
     */
    public abstract int[] getSlotIndicesFromIndex(int index);

    public abstract ImmutablePair<BlockPos, ForgeDirection>[] getInputPositions();

    public abstract ImmutablePair<BlockPos, ForgeDirection>[] getOutputPositions();
    public abstract int getPowerSlot();
}
