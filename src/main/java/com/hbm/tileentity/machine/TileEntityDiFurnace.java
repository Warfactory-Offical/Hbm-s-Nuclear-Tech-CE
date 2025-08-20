package com.hbm.tileentity.machine;

import com.hbm.api.fluid.IFluidStandardSender;
import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.machine.MachineDiFurnace;
import com.hbm.handler.pollution.PollutionHandler;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.container.ContainerDiFurnace;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.inventory.gui.GUIDiFurnace;
import com.hbm.inventory.recipes.BlastFurnaceRecipes;
import com.hbm.lib.ForgeDirection;
import com.hbm.tileentity.IGUIProvider;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

@AutoRegister
public class TileEntityDiFurnace extends TileEntityMachinePolluting implements ITickable, IFluidStandardSender, IGUIProvider {


    private static final int maxFuel = 12800;
    private static final int processingSpeed = 400;
    private static final int[] slots_io = new int[]{0, 1, 2, 3};
    public int progress;


    //	private static final int[] slots_top = new int[] {0, 1};
//	private static final int[] slots_bottom = new int[] {3};
//	private static final int[] slots_side = new int[] {2};
    public int fuel;
    // mlbv: yeah 1.7 have them all set to 1 as well
    public byte sideFuel = 1;
    public byte sideUpper = 1;
    public byte sideLower = 1;

    public TileEntityDiFurnace() {
        super(4, 50, true, false);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        this.fuel = compound.getInteger("powerTime");
        this.progress = compound.getShort("cookTime");

        byte[] modes = compound.getByteArray("modes");
        if (modes.length != 3) return;
        this.sideFuel = modes[0];
        this.sideUpper = modes[1];
        this.sideLower = modes[2];
    }

    @NotNull
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setInteger("powerTime", fuel);
        compound.setShort("cookTime", (short) progress);
        compound.setByteArray("modes", new byte[]{sideFuel, sideUpper, sideLower});
        return compound;
    }

    @Override
    public void update() {


        if (!world.isRemote) {

            boolean extension = world.getBlockState(pos.offset(EnumFacing.UP)).getBlock() == ModBlocks.machine_difurnace_ext;

            for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
                this.sendSmoke(pos.getX() + dir.offsetX, pos.getY() + dir.offsetY, pos.getZ() + dir.offsetZ, dir);
            }

            if (extension) this.sendSmoke(pos.getX(), pos.getY() + 2, pos.getZ(), ForgeDirection.UP);

            boolean markDirty = false;

            if (this.hasItemPower(this.getFuelStack()) && this.fuel <= (TileEntityDiFurnace.maxFuel - BlastFurnaceRecipes.getItemPower(this.getFuelStack()))) {
                this.fuel += BlastFurnaceRecipes.getItemPower(this.getFuelStack());
                if (!this.getFuelStack().isEmpty()) {
                    markDirty = true;
                    this.getFuelStack().shrink(1);
                    if (this.getFuelStack().isEmpty()) {
                        inventory.setStackInSlot(2, this.getFuelStack().getItem().getContainerItem(this.getFuelStack()));
                    }
                }
            }

            if (canProcess()) {

                //fuel -= extension ? 2 : 1;
                fuel -= 1; //switch it up on me, fuel efficiency, on fumes i'm running - running - running - running
                progress += extension ? 3 : 1;

                if (this.progress >= TileEntityDiFurnace.processingSpeed) {
                    this.progress = 0;
                    this.processItem();
                    markDirty = true;
                }

                if (fuel < 0) {
                    fuel = 0;
                }

                if (world.getTotalWorldTime() % 20 == 0)
                    this.pollute(PollutionHandler.PollutionType.SOOT, PollutionHandler.SOOT_PER_SECOND * (extension ? 3 : 1));

            } else {
                progress = 0;
            }

            boolean trigger = !canProcess() || this.progress != 0;

            if (trigger) {
                markDirty = true;
                MachineDiFurnace.updateBlockState(this.progress > 0, extension, world, pos);
            }

            networkPackNT(15);

            if (markDirty) {
                this.markDirty();
            }
        }
    }

    private boolean hasItemPower(ItemStack itemStack) {
        return BlastFurnaceRecipes.getItemPower(itemStack) > 0;
    }

    @Override
    public String getName() {
        return "container.diFurnace";
    }

    @Override
    public int[] getAccessibleSlotsFromSide(EnumFacing e) {
        int i = e.ordinal();
        return i == 0 ? new int[]{3} : (i == 1 ? new int[]{0, 1} : new int[]{2});
    }

    @Override
    public boolean isItemValidForSlot(int i, ItemStack stack) {
        if (i == 3) return false;
        if (i == 2) return hasItemPower(stack);
        return true;
    }

    @Override
    public boolean canInsertItem(int slot, ItemStack itemStack) {
        if (slot == 0 && isItemValidForSlot(slot, itemStack)) return inventory.getStackInSlot(1).getItem() != itemStack.getItem();
        if (slot == 1 && isItemValidForSlot(slot, itemStack)) return inventory.getStackInSlot(0).getItem() != itemStack.getItem();
        return isItemValidForSlot(slot, itemStack);
    }

    @Override
    public boolean canExtractItem(int slot, ItemStack itemStack, int amount) {
        return slot == 3;
    }

    public boolean isUsableByPlayer(EntityPlayer player) {
        if (world.getTileEntity(pos) != this) {
            return false;
        } else {
            return player.getDistanceSq(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 64;
        }
    }

    public int getDiFurnaceProgressScaled(int i) {
        return (progress * i) / processingSpeed;
    }

    public int getPowerRemainingScaled(int i) {
        return (fuel * i) / maxFuel;
    }

    public boolean canProcess() {
        ItemStack input1 = inventory.getStackInSlot(0);
        ItemStack input2 = inventory.getStackInSlot(1);
        ItemStack outputSlot = inventory.getStackInSlot(3);

        if (input1.isEmpty() || input2.isEmpty()) return false;
        if (!this.hasPower()) return false;

        ItemStack result = BlastFurnaceRecipes.getOutput(input1, input2);
        if (result.isEmpty()) return false;

        if (outputSlot.isEmpty()) return true;
        if (!ItemStack.areItemsEqual(outputSlot, result)) return false;

        int combined = outputSlot.getCount() + result.getCount();
        return combined <= outputSlot.getMaxStackSize();
    }

    private void processItem() {
        if (canProcess()) {
            ItemStack itemStack = BlastFurnaceRecipes.getOutput(inventory.getStackInSlot(0), inventory.getStackInSlot(1));

            if (inventory.getStackInSlot(3).isEmpty()) {
                inventory.setStackInSlot(3, itemStack.copy());
            } else if (inventory.getStackInSlot(3).isItemEqual(itemStack)) {
                inventory.getStackInSlot(3).grow(itemStack.getCount());
            }

            for (int i = 0; i < 2; i++) {
                if (inventory.getStackInSlot(i).getCount() <= 0) {
                    inventory.setStackInSlot(i, new ItemStack(inventory.getStackInSlot(i).getItem().setFull3D()));
                } else {
                    inventory.getStackInSlot(i).shrink(1);
                }
                if (inventory.getStackInSlot(i).getCount() <= 0) {
                    inventory.setStackInSlot(i, ItemStack.EMPTY);
                }
            }
        }
    }

    public boolean hasPower() {
        return fuel > 0;
    }

    public boolean isProcessing() {
        return this.progress > 0;
    }

    @Override
    public void serialize(ByteBuf buf) {
        buf.writeShort(this.progress);
        buf.writeShort(this.fuel);
        buf.writeBytes(new byte[]{this.sideFuel, this.sideUpper, this.sideLower});
    }

    @Override
    public void deserialize(ByteBuf buf) {
        this.progress = buf.readShort();
        this.fuel = buf.readShort();
        byte[] modes = new byte[3];
        buf.readBytes(modes);
        this.sideFuel = modes[0];
        this.sideUpper = modes[1];
        this.sideLower = modes[2];
    }

    @Override
    public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new ContainerDiFurnace(player.inventory, this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new GUIDiFurnace(player.inventory, this);
    }

    @Override
    public FluidTankNTM[] getAllTanks() {
        return new FluidTankNTM[0];
    }

    @Override
    public FluidTankNTM[] getSendingTanks() {
        return this.getSmokeTanks();
    }

    private ItemStack getFuelStack() {
        return inventory.getStackInSlot(2);
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
        boolean isSwapBetweenVariants = (oldState.getBlock() == ModBlocks.machine_difurnace_off && newState.getBlock() == ModBlocks.machine_difurnace_on) ||
                        (oldState.getBlock() == ModBlocks.machine_difurnace_on  && newState.getBlock() == ModBlocks.machine_difurnace_off);
        if (isSwapBetweenVariants) return false;
        return super.shouldRefresh(world, pos, oldState, newState);
    }
}
