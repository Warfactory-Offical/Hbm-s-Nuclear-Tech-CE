package com.hbm.tileentity.machine;

import api.hbm.fluid.*;
import com.hbm.blocks.ModBlocks;
import com.hbm.forgefluid.ModForgeFluids;
import com.hbm.handler.CompatHandler;
import com.hbm.interfaces.IFFtoNTMF;
import com.hbm.interfaces.IFluidAcceptor;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.inventory.fluid.trait.FT_Corrosive;
import com.hbm.lib.DirPos;
import com.hbm.lib.Library;
import com.hbm.tileentity.IFluidCopiable;
import com.hbm.tileentity.IPersistentNBT;
import com.hbm.tileentity.TileEntityMachineBase;
import io.netty.buffer.ByteBuf;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.SimpleComponent;
import net.minecraft.block.Block;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Optional.InterfaceList({@Optional.Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "opencomputers")})
public class TileEntityBarrel extends TileEntityMachineBase implements
        ITickable, IPersistentNBT, IFluidCopiable,
        IFluidStandardTransceiver, SimpleComponent,
        CompatHandler.OCComponent, IFFtoNTMF, IFluidHandler {

    public static final short modes = 4;
    private static final int[] slots_top = new int[]{2};
    private static final int[] slots_bottom = new int[]{3, 5};
    private static final int[] slots_side = new int[]{4};
    private static boolean converted = false;
    public FluidTank tank;
    public FluidTankNTM tankNew;
    //Drillgon200: I think this would be much easier to read as an enum.
    public short mode = 0;
    public List<IFluidAcceptor> list = new ArrayList();
    protected boolean sendingBrake = false;
    private int age = 0;
    // Th3_Sl1ze: Ugh. Maybe there's a smarter way to convert fluids from forge tank to NTM tank but I don't know any other client-seamless methods.
    private Fluid oldFluid = ModForgeFluids.none;

    public TileEntityBarrel() {
        super(6);
        tank = new FluidTank(-1);
        tankNew = new FluidTankNTM(Fluids.NONE, 0, 0);
        converted = true;
    }

    public TileEntityBarrel(int cap) {
        super(6);
        tank = new FluidTank(cap);
        tankNew = new FluidTankNTM(Fluids.NONE, cap, 0);
    }

    protected static int transmitFluidFairly(World world, FluidTankNTM tank, IFluidConnector that, int fill, boolean connect, boolean send, DirPos[] connections) {

        Set<IPipeNet> nets = new HashSet<>();
        Set<IFluidConnector> consumers = new HashSet<>();
        FluidType type = tank.getTankType();
        int pressure = tank.getPressure();

        for (DirPos pos : connections) {

            TileEntity te = world.getTileEntity(pos.getPos());

            if (te instanceof IFluidConductor) {
                IFluidConductor con = (IFluidConductor) te;
                if (con.getPipeNet(type) != null) {
                    nets.add(con.getPipeNet(type));
                    con.getPipeNet(type).unsubscribe(that);
                    consumers.addAll(con.getPipeNet(type).getSubscribers());
                }

                //if it's just a consumer, buffer it as a subscriber
            } else if (te instanceof IFluidConnector) {
                consumers.add((IFluidConnector) te);
            }
        }

        consumers.remove(that);

        if (fill > 0 && send) {
            List<IFluidConnector> con = new ArrayList<>();
            con.addAll(consumers);

            con.removeIf(x -> x == null || !(x instanceof TileEntity) || ((TileEntity) x).isInvalid());

            if (PipeNet.trackingInstances == null) {
                PipeNet.trackingInstances = new ArrayList<>();
            }

            PipeNet.trackingInstances.clear();
            nets.forEach(x -> {
                if (x instanceof PipeNet) PipeNet.trackingInstances.add((PipeNet) x);
            });

            fill = (int) PipeNet.fairTransfer(con, type, pressure, fill);
        }

        //resubscribe to buffered nets, if necessary
        if (connect) {
            nets.forEach(x -> x.subscribe(that));
        }

        return fill;
    }

    @Override
    public long getDemand(FluidType type, int pressure) {

        if (this.mode == 2 || this.mode == 3 || this.sendingBrake)
            return 0;

        if (tankNew.getPressure() != pressure) return 0;

        return type == tankNew.getTankType() ? tankNew.getMaxFill() - tankNew.getFill() : 0;
    }

    @Override
    public long transferFluid(FluidType type, int pressure, long fluid) {
        long toTransfer = Math.min(getDemand(type, pressure), fluid);
        tankNew.setFill(tankNew.getFill() + (int) toTransfer);
        this.markDirty();
        return fluid - toTransfer;
    }

    @Override
    public void update() {
        //Fix up old tanks
        if (!converted && tankNew.getTankType() == Fluids.NONE) {
            this.resizeInventory(6);
            convertAndSetFluid(oldFluid, tank, tankNew);
            converted = true;
        }
        //

        if (!world.isRemote) {
            tankNew.setType(0, 1, inventory);
            tankNew.loadTank(2, 3, inventory);
            tankNew.unloadTank(4, 5, inventory);

            this.sendingBrake = true;
            tankNew.setFill(transmitFluidFairly(world, tankNew, this, tankNew.getFill(), this.mode == 0 || this.mode == 1, this.mode == 1 || this.mode == 2, getConPos()));
            this.sendingBrake = false;

            if (tankNew.getFill() > 0) {
                checkFluidInteraction();
            }

            this.networkPackNT(50);
        }
    }

    @Override
    public void serialize(ByteBuf buf) {
        super.serialize(buf);
        buf.writeShort(mode);
        tankNew.serialize(buf);
    }

    @Override
    public void deserialize(ByteBuf buf) {
        super.deserialize(buf);
        mode = buf.readShort();
        tankNew.deserialize(buf);
    }

    protected DirPos[] getConPos() {
        return new DirPos[]{
                new DirPos(pos.getX() + 1, pos.getY(), pos.getZ(), Library.POS_X),
                new DirPos(pos.getX() - 1, pos.getY(), pos.getZ(), Library.NEG_X),
                new DirPos(pos.getX(), pos.getY() + 1, pos.getZ(), Library.POS_Y),
                new DirPos(pos.getX(), pos.getY() - 1, pos.getZ(), Library.NEG_Y),
                new DirPos(pos.getX(), pos.getY(), pos.getZ() + 1, Library.POS_Z),
                new DirPos(pos.getX(), pos.getY(), pos.getZ() - 1, Library.NEG_Z)
        };
    }

    public void checkFluidInteraction() {
        Block b = this.getBlockType();

        //for when you fill antimatter into a matter tank
        if (b != ModBlocks.barrel_antimatter && tankNew.getTankType().isAntimatter()) {
            world.destroyBlock(pos, false);
            world.newExplosion(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 5, true, true);
        }

        //for when you fill hot or corrosive liquids into a plastic tank
        if (b == ModBlocks.barrel_plastic && (tankNew.getTankType().isCorrosive() || tankNew.getTankType().isHot())) {
            world.destroyBlock(pos, false);
            world.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.BLOCK_LAVA_EXTINGUISH, SoundCategory.BLOCKS, 1.0F, 1.0F);
        }

        //for when you fill corrosive liquid into an iron tank
        if ((b == ModBlocks.barrel_iron && tankNew.getTankType().isCorrosive()) ||
                (b == ModBlocks.barrel_steel && tankNew.getTankType().hasTrait(FT_Corrosive.class) && tankNew.getTankType().getTrait(FT_Corrosive.class).getRating() > 50)) {

            world.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.BLOCK_LAVA_EXTINGUISH, SoundCategory.BLOCKS, 1.0F, 1.0F);
            ItemStackHandler copy = new ItemStackHandler(this.inventory.getSlots());
            for (int i = 0; i < this.inventory.getSlots(); i++) {
                copy.setStackInSlot(i, this.inventory.getStackInSlot(i).copy());
            }

            this.inventory = new ItemStackHandler(6);
            world.setBlockState(pos, ModBlocks.barrel_corroded.getDefaultState());

            TileEntityBarrel barrel = (TileEntityBarrel) world.getTileEntity(pos);

            if (barrel != null) {
                barrel.tankNew.setTankType(tankNew.getTankType());
                barrel.tankNew.setFill(Math.min(barrel.tankNew.getMaxFill(), tankNew.getFill()));
                barrel.inventory = copy;
            }
        }

        if (b == ModBlocks.barrel_corroded) {
            if (world.rand.nextInt(3) == 0) {
                tankNew.setFill(tankNew.getFill() - 1);
            }
            if (world.rand.nextInt(3 * 60 * 20) == 0) world.destroyBlock(pos, false);
        }
    }

    @Override
    public String getName() {
        return "container.barrel";
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setShort("mode", mode);
        if (!converted && tankNew.getTankType() == Fluids.NONE) {
            compound.setInteger("cap", tank.getCapacity());
            tank.writeToNBT(compound);
            compound.setBoolean("converted", true);
        } else tankNew.writeToNBT(compound, "tank");
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        mode = compound.getShort("mode");
        converted = compound.getBoolean("converted");
        tankNew.readFromNBT(compound, "tank");
        if (!converted && tankNew.getTankType() == Fluids.NONE) {
            if (tank == null || tank.getCapacity() <= 0)
                tank = new FluidTank(compound.getInteger("cap"));
            tank.readFromNBT(compound);
            if (tank.getFluid() != null) {
                oldFluid = tank.getFluid().getFluid();
            }
        } else {
            if (compound.hasKey("cap")) compound.removeTag("cap");
        }
    }

    @Override
    public FluidTankNTM[] getSendingTanks() {
        return (mode == 1 || mode == 2) ? new FluidTankNTM[]{tankNew} : new FluidTankNTM[0];
    }

    @Override
    public FluidTankNTM[] getReceivingTanks() {
        return (mode == 0 || mode == 1) && !sendingBrake ? new FluidTankNTM[]{tankNew} : new FluidTankNTM[0];
    }

    @Override
    public FluidTankNTM[] getAllTanks() {
        return new FluidTankNTM[]{tankNew};
    }

    @Override
    public int[] getFluidIDToCopy() {
        return new int[]{tankNew.getTankType().getID()};
    }

    @Override
    public FluidTankNTM getTankToPaste() {
        return tankNew;
    }

    @Override
    public void writeNBT(NBTTagCompound nbt) {
        if (tankNew.getFill() == 0) return;
        NBTTagCompound data = new NBTTagCompound();
        this.tankNew.writeToNBT(data, "tank");
        data.setShort("mode", mode);
        nbt.setTag(NBT_PERSISTENT_KEY, data);
    }

    @Override
    public void readNBT(NBTTagCompound nbt) {
        NBTTagCompound data = nbt.getCompoundTag(NBT_PERSISTENT_KEY);
        this.tankNew.readFromNBT(data, "tank");
        this.mode = data.getShort("nbt");
    }

    @Override
    public int[] getAccessibleSlotsFromSide(EnumFacing e) {
        int i = e.ordinal();
        return i == 0 ? slots_bottom : (i == 1 ? slots_top : slots_side);
    }

    @Override
    public boolean isItemValidForSlot(int i, ItemStack stack) {
        if (i == 0) {
            return true;
        }

        if (i == 2) {
            return true;
        }

        return false;
    }

    @Override
    public boolean canInsertItem(int slot, ItemStack itemStack, int amount) {
        return isItemValidForSlot(slot, itemStack);
    }

    @Override
    public boolean canExtractItem(int slot, ItemStack itemStack, int amount) {
        if (slot == 1) {
            return true;
        }

        if (slot == 3) {
            return true;
        }

        return false;
    }

    @Override
    @Optional.Method(modid = "OpenComputers")
    public String getComponentName() {
        return "ntm_fluid_tank";
    }

    @Callback(direct = true)
    @Optional.Method(modid = "OpenComputers")
    public Object[] getFluidStored(Context context, Arguments args) {
        return new Object[]{tankNew.getFill()};
    }

    @Callback(direct = true)
    @Optional.Method(modid = "OpenComputers")
    public Object[] getMaxStored(Context context, Arguments args) {
        return new Object[]{tankNew.getMaxFill()};
    }

    @Callback(direct = true)
    @Optional.Method(modid = "OpenComputers")
    public Object[] getTypeStored(Context context, Arguments args) {
        return new Object[]{tankNew.getTankType().getName()};
    }

    @Callback(direct = true)
    @Optional.Method(modid = "OpenComputers")
    public Object[] getInfo(Context context, Arguments args) {
        return new Object[]{tankNew.getFill(), tankNew.getMaxFill(), tankNew.getTankType().getName()};
    }

    @Override
    @Optional.Method(modid = "OpenComputers")
    public String[] methods() {
        return new String[]{
                "getFluidStored",
                "getMaxStored",
                "getTypeStored",
                "getInfo"
        };
    }

    @Override
    @Optional.Method(modid = "OpenComputers")
    public Object[] invoke(String method, Context context, Arguments args) throws Exception {
        switch (method) {
            case "getFluidStored":
                return getFluidStored(context, args);
            case "getMaxStored":
                return getMaxStored(context, args);
            case "getTypeStored":
                return getTypeStored(context, args);
            case "getInfo":
                return getInfo(context, args);
        }
        throw new NoSuchMethodException();
    }

    @Override
    public IFluidTankProperties[] getTankProperties() {
        return new IFluidTankProperties[]{
                new FluidTankProperties(new FluidStack(tankNew.getTankTypeFF(), tankNew.getFill()), tankNew.getMaxFill())
        };
    }

    @Override
    public int fill(FluidStack resource, boolean doFill) {
        if (tankNew.getTankTypeFF() == resource.getFluid())
            return 0;
        int amount = resource.amount;
        return (int) transferFluid(tankNew.getTankType(), 0, amount); //I FUCKING LOVE CASTING
    }

    @Override
    public @Nullable FluidStack drain(FluidStack resource, boolean doDrain) {
        Fluid currentType = tankNew.getTankTypeFF();
        if (resource == null || !resource.getFluid().equals(currentType)) return null;
        sendingBrake = true; //Poor man's mutex o algo
        int toDrain = Math.min(resource.amount, tankNew.getFill());
        FluidStack drained = new FluidStack(currentType, toDrain);
        if (doDrain) {
            tankNew.setFill(tankNew.getFill() - toDrain);
            markDirty();
        }
        sendingBrake = false;
        return drained;
    }

    @Override
    public @Nullable FluidStack drain(int maxDrain, boolean doDrain) {
        if (tankNew.getTankType() == Fluids.NONE || tankNew.getTankTypeFF() == null || tankNew.getFill() == 0)
            return null;
        sendingBrake = true;
        int toDrain = Math.min(maxDrain, tankNew.getFill());
        FluidStack drained = new FluidStack(tankNew.getTankTypeFF(), toDrain);


        if (doDrain) {
            tankNew.setFill(tankNew.getFill() - toDrain);
            markDirty();
        }

        sendingBrake = false;
        return drained;
    }
}
