package com.hbm.tileentity.machine.oil;

import api.hbm.energymk2.IEnergyReceiverMK2;
import api.hbm.fluid.IFluidStandardSender;
import com.hbm.capability.NTMFluidHandlerWrapper;
import com.hbm.interfaces.IFluidAcceptor;
import com.hbm.interfaces.IFluidSource;
import com.hbm.inventory.LiquefactionRecipes;
import com.hbm.inventory.UpgradeManager;
import com.hbm.inventory.container.ContainerLiquefactor;
import com.hbm.inventory.fluid.FluidStack;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.inventory.gui.GUILiquefactor;
import com.hbm.items.machine.ItemMachineUpgrade;
import com.hbm.lib.DirPos;
import com.hbm.lib.Library;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityMachineBase;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class TileEntityMachineLiquefactor extends TileEntityMachineBase implements IEnergyReceiverMK2, IFluidSource, IFluidStandardSender, IGUIProvider, ITickable {

    public long power;
    public static final long maxPower = 100000;
    public static final int usageBase = 500;
    public int usage;
    public int progress;
    public static final int processTimeBase = 200;
    public boolean needsUpdate = false;
    public int processTime;

    public FluidTankNTM tank;

    private final UpgradeManager upgradeManager = new UpgradeManager();

    public TileEntityMachineLiquefactor() {
        super(4);
        tank = new FluidTankNTM(Fluids.NONE, 24000, 0);
    }

    @Override
    public String getName() {
        return "container.machineLiquefactor";
    }

    @Override
    public void update() {

        if(!world.isRemote) {
            this.power = Library.chargeTEFromItems(inventory, 1, power, maxPower);
            tank.updateTank(this);

            this.updateConnections();

            upgradeManager.eval(inventory, 2, 3);
            int speed = Math.min(upgradeManager.getLevel(ItemMachineUpgrade.UpgradeType.SPEED), 3);
            int power = Math.min(upgradeManager.getLevel(ItemMachineUpgrade.UpgradeType.POWER), 3);

            this.processTime = processTimeBase - (processTimeBase / 4) * speed;
            this.usage = (usageBase + (usageBase * speed)) / (power + 1);

            if(this.canProcess())
                this.process();
            else
                this.progress = 0;

            if(world.getTotalWorldTime() % 10 == 0) {
                this.fillFluidInit(tank.getTankType());
            }

            this.sendFluid();

            NBTTagCompound data = new NBTTagCompound();
            data.setLong("power", this.power);
            data.setInteger("progress", this.progress);
            data.setInteger("usage", this.usage);
            data.setInteger("processTime", this.processTime);
            this.networkPack(data, 50);
        }
    }

    private void updateConnections() {
        for(DirPos pos : getConPos()) {
            this.trySubscribe(world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());
        }
    }

    private void sendFluid() {
        for(DirPos pos : getConPos()) {
            this.sendFluid(tank, world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());
        }
    }

    private DirPos[] getConPos() {
        return new DirPos[] {
                new DirPos(pos.getX(), pos.getY() + 4, pos.getZ(), Library.POS_Y),
                new DirPos(pos.getX(), pos.getY() - 1, pos.getZ(), Library.NEG_Y),
                new DirPos(pos.getX() + 2, pos.getY() + 1, pos.getZ(), Library.POS_X),
                new DirPos(pos.getX() - 2, pos.getY() + 1, pos.getZ(), Library.NEG_X),
                new DirPos(pos.getX(), pos.getY() + 1, pos.getZ() + 2, Library.POS_Z),
                new DirPos(pos.getX(), pos.getY() + 1, pos.getZ() - 2, Library.NEG_Z)
        };
    }

    @Override
    public boolean isItemValidForSlot(int i, ItemStack itemStack) {
        return i == 0 && LiquefactionRecipes.getOutput(itemStack) != null;
    }

    public boolean canProcess() {

        if(this.power < usage)
            return false;

        if(inventory.getStackInSlot(0).isEmpty())
            return false;

        FluidStack out = LiquefactionRecipes.getOutput(inventory.getStackInSlot(0));

        if(out == null)
            return false;

        if(out.type != tank.getTankType() && tank.getFill() > 0)
            return false;

        if(out.fill + tank.getFill() > tank.getMaxFill())
            return false;

        return true;
    }

    public void process() {

        this.power -= usage;

        progress++;

        if(progress >= processTime) {

            FluidStack out = LiquefactionRecipes.getOutput(inventory.getStackInSlot(0));
            tank.setTankType(out.type);
            tank.setFill(tank.getFill() + out.fill);
            this.inventory.getStackInSlot(0).shrink(1);

            progress = 0;

            this.markDirty();
        }
    }

    @Override
    public void networkUnpack(NBTTagCompound nbt) {
        super.networkUnpack(nbt);

        this.power = nbt.getLong("power");
        this.progress = nbt.getInteger("progress");
        this.usage = nbt.getInteger("usage");
        this.processTime = nbt.getInteger("processTime");
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        tank.readFromNBT(nbt, "tank");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        tank.writeToNBT(nbt, "tank");
        return nbt;
    }

    @Override
    public void setPower(long power) {
        this.power = power;
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
    public void setFillForSync(int fill, int index) {
        tank.setFill(fill);
    }

    @Override
    public void setFluidFill(int fill, FluidType type) {
        if(type == tank.getTankType())
            tank.setFill(fill);
    }

    @Override
    public void setTypeForSync(FluidType type, int index) {
        tank.setTankType(type);
    }

    @Override
    public int getFluidFill(FluidType type) {
        return type == tank.getTankType() ? tank.getFill() : 0;
    }

    @Override
    public void fillFluidInit(FluidType type) {
        fillFluid(pos.getX(), pos.getY() - 1, pos.getZ(), getTact(), type);
        fillFluid(pos.getX(), pos.getY() + 4, pos.getZ(), getTact(), type);
        fillFluid(pos.getX() + 2, pos.getY() + 1, pos.getZ(), getTact(), type);
        fillFluid(pos.getX() - 2, pos.getY() + 1, pos.getZ(), getTact(), type);
        fillFluid(pos.getX(), pos.getY() + 1, pos.getZ() + 2, getTact(), type);
        fillFluid(pos.getX(), pos.getY() + 1, pos.getZ() - 2, getTact(), type);
    }

    @Override
    public void fillFluid(int x, int y, int z, boolean newTact, FluidType type) {
        Library.transmitFluid(x, y, z, newTact, this, world, type);
    }

    @Override
    public boolean getTact() {
        return world.getTotalWorldTime() % 20 < 10;
    }

    private List<IFluidAcceptor> consumers = new ArrayList();

    @Override
    public List<IFluidAcceptor> getFluidList(FluidType type) {
        return consumers;
    }

    @Override
    public void clearFluidList(FluidType type) {
        consumers.clear();
    }

    AxisAlignedBB bb = null;

    @Override
    public AxisAlignedBB getRenderBoundingBox() {

        if(bb == null) {
            bb = new AxisAlignedBB(
                    pos.getX() - 1,
                    pos.getY(),
                    pos.getZ() - 1,
                    pos.getX() + 2,
                    pos.getY() + 4,
                    pos.getZ() + 2
            );
        }

        return bb;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public double getMaxRenderDistanceSquared() {
        return 65536.0D;
    }

    @Override
    public FluidTankNTM[] getSendingTanks() {
        return new FluidTankNTM[] { tank };
    }

    @Override
    public FluidTankNTM[] getAllTanks() {
        return new FluidTankNTM[] { tank };
    }


    @Override
    public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new ContainerLiquefactor(player.inventory, this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new GUILiquefactor(player.inventory, this);
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
                    new NTMFluidHandlerWrapper(null, this.getSendingTanks())
            );
        }
        return super.getCapability(capability, facing);
    }
}
