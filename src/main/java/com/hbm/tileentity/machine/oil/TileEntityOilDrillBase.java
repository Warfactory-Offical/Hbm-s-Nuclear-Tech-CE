package com.hbm.tileentity.machine.oil;

import api.hbm.energymk2.IEnergyReceiverMK2;
import api.hbm.fluid.IFluidStandardTransceiver;
import com.hbm.blocks.ModBlocks;
import com.hbm.capability.NTMFluidHandlerWrapper;
import com.hbm.forgefluid.FFUtils;
import com.hbm.forgefluid.ModForgeFluids;
import com.hbm.interfaces.IFFtoNTMF;
import com.hbm.inventory.UpgradeManager;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.items.machine.ItemMachineUpgrade;
import com.hbm.lib.DirPos;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.Library;
import com.hbm.tileentity.*;
import com.hbm.util.BobMathUtil;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public abstract class TileEntityOilDrillBase extends TileEntityMachineBase implements ITickable, IEnergyReceiverMK2, IFluidStandardTransceiver, IConfigurableMachine, IPersistentNBT, IGUIProvider, IFluidCopiable, IFFtoNTMF
{
    private final UpgradeManager upgradeManager;
    public long power;
    public int indicator = 0;
    public FluidTank[] tanksOld;
    public Fluid[] tankTypes;
    public FluidTankNTM[] tanks;
    private static boolean converted = false;

    public TileEntityOilDrillBase() {
        super(8);
        tanksOld = new FluidTank[3];
        tankTypes = new Fluid[3];

        tanksOld[0] = new FluidTank(128000);
        tankTypes[0] = ModForgeFluids.oil;
        tanksOld[1] = new FluidTank(128000);
        tankTypes[1] = ModForgeFluids.gas;

        tanks = new FluidTankNTM[2];
        tanks[0] = new FluidTankNTM(Fluids.OIL, 64_000);
        tanks[1] = new FluidTankNTM(Fluids.GAS, 64_000);

        upgradeManager = new UpgradeManager();

        converted = true;
    }

    private String customName;


    public boolean hasCustomInventoryName() {
        return this.customName != null && this.customName.length() > 0;
    }

    public void setCustomName(String name) {
        this.customName = name;
    }

    protected String getCustomName() {
        return customName;
    }

    public boolean isUseableByPlayer(EntityPlayer player) {
        if(world.getTileEntity(pos) != this)
        {
            return false;
        }else{
            return player.getDistanceSq(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <=128;
        }
    }


    @Override
    public void readFromNBT(NBTTagCompound compound) {
        this.power = compound.getLong("powerTime");
        if(!converted) {
            tankTypes[0] = ModForgeFluids.oil;
            tankTypes[1] = ModForgeFluids.gas;
            if (compound.hasKey("tanks"))
                FFUtils.deserializeTankArray(compound.getTagList("tanks", 10), tanksOld);
        } else{
            for(int i = 0; i < this.tanks.length; i++)
                this.tanks[i].readFromNBT(compound, "t" + i);
            if (compound.hasKey("tanks"))
                compound.removeTag("tanks");
            if (compound.hasKey("age"))
                compound.removeTag("age");
        }
        super.readFromNBT(compound);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setLong("powerTime", power);
        if(!converted){
            compound.setTag("tanks", FFUtils.serializeTankArray(tanksOld));
        } else{
            for(int i = 0; i < this.tanks.length; i++)
                this.tanks[i].writeToNBT(compound, "t" + i);
        }
        return super.writeToNBT(compound);
    }

    @Override
    public void writeNBT(NBTTagCompound nbt) {

        boolean empty = power == 0;
        for(FluidTankNTM tank : tanks) if(tank.getFill() > 0) empty = false;

        if(!empty) {
            nbt.setLong("power", power);
            for(int i = 0; i < this.tanks.length; i++) {
                this.tanks[i].writeToNBT(nbt, "t" + i);
            }
        }
    }

    @Override
    public void readNBT(NBTTagCompound nbt) {
        this.power = nbt.getLong("power");
        for(int i = 0; i < this.tanks.length; i++)
            this.tanks[i].readFromNBT(nbt, "t" + i);
    }

    public int speedLevel;
    public int energyLevel;
    public int overLevel;

    @Override
    public void update() {
        if (!converted){
            convertAndSetFluids(tankTypes, tanksOld, tanks);
            converted = true;
        }
        if(!world.isRemote) {

            this.updateConnections();

            this.tanks[0].unloadTank(1, 2, inventory);
            this.tanks[1].unloadTank(3, 4, inventory);

            upgradeManager.eval(inventory, 5, 7);
            this.speedLevel = Math.min(upgradeManager.getLevel(ItemMachineUpgrade.UpgradeType.SPEED), 3);
            this.energyLevel = Math.min(upgradeManager.getLevel(ItemMachineUpgrade.UpgradeType.POWER), 3);
            this.overLevel = Math.min(upgradeManager.getLevel(ItemMachineUpgrade.UpgradeType.OVERDRIVE), 3) + 1;
            int abLevel = Math.min(upgradeManager.getLevel(ItemMachineUpgrade.UpgradeType.AFTERBURN), 3);

            int toBurn = Math.min(tanks[1].getFill(), abLevel * 10);

            if(toBurn > 0) {
                tanks[1].setFill(tanks[1].getFill() - toBurn);
                this.power += toBurn * 5;

                if(this.power > this.getMaxPower())
                    this.power = this.getMaxPower();
            }

            power = Library.chargeTEFromItems(inventory, 0, power, this.getMaxPower());

            for(DirPos pos : getConPos()) {
                if(tanks[0].getFill() > 0) this.sendFluid(tanks[0], world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());
                if(tanks[1].getFill() > 0) this.sendFluid(tanks[1], world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());
            }

            if(this.power >= this.getPowerReqEff() && this.tanks[0].getFill() < this.tanks[0].getMaxFill() && this.tanks[1].getFill() < this.tanks[1].getMaxFill()) {

                this.power -= this.getPowerReqEff();

                if(world.getTotalWorldTime() % getDelayEff() == 0) {
                    this.indicator = 0;

                    for(int y = pos.getY() - 1; y >= getDrillDepth(); y--) {

                        if(world.getBlockState(new BlockPos(pos.getX(), y, pos.getZ())).getBlock() != ModBlocks.oil_pipe) {

                            if(trySuck(y)) {
                                break;
                            } else {
                                tryDrill(y);
                                break;
                            }
                        }

                        if(y == getDrillDepth())
                            this.indicator = 1;
                    }
                }

            } else {
                this.indicator = 2;
            }

            this.sendUpdate();
        }
    }

    public void sendUpdate() {
        NBTTagCompound data = new NBTTagCompound();
        data.setLong("power", power);
        data.setInteger("indicator", this.indicator);
        for(int i = 0; i < tanks.length; i++) tanks[i].writeToNBT(data, "t" + i);
        this.networkPack(data, 25);
    }

    public void networkUnpack(NBTTagCompound nbt) {
        super.networkUnpack(nbt);

        this.power = nbt.getLong("power");
        this.indicator = nbt.getInteger("indicator");
        for(int i = 0; i < tanks.length; i++) tanks[i].readFromNBT(nbt, "t" + i);
    }

    public boolean canPump() {
        return true;
    }

    public int getPowerReqEff() {
        int req = this.getPowerReq();
        return (req + (req / 4 * this.speedLevel) - (req / 4 * this.energyLevel)) * this.overLevel;
    }

    public int getDelayEff() {
        int delay = getDelay();
        return Math.max((delay - (delay / 4 * this.speedLevel) + (delay / 10 * this.energyLevel)) / this.overLevel, 1);
    }

    public abstract int getPowerReq();
    public abstract int getDelay();

    List<int[]> list = new ArrayList<int[]>();
    HashSet<BlockPos> processed = new HashSet<BlockPos>();

    public void tryDrill(int y) {
        BlockPos posD = new BlockPos(pos.getX(), y, pos.getZ());
        Block b = world.getBlockState(posD).getBlock();

        if(b.getExplosionResistance(null) < 1000) {
            onDrill(y);
            world.setBlockState(posD, ModBlocks.oil_pipe.getDefaultState());
        } else {
            this.indicator = 2;
        }
    }

    public void onDrill(int y) { }

    public int getDrillDepth() {
        return 5;
    }

    public boolean trySuck(int y) {
        BlockPos sPos = new BlockPos(pos.getX(), y, pos.getZ());
        Block b = world.getBlockState(sPos).getBlock();

        if(!canSuckBlock(b))
            return false;

        if(!this.canPump())
            return true;

        processed.clear();

        return suckRec(new BlockPos(pos.getX(), y, pos.getZ()), 0);
    }

    public boolean canSuckBlock(Block b) {
        return b == ModBlocks.ore_oil || b == ModBlocks.ore_oil_empty || b == ModBlocks.ore_gas || b == ModBlocks.ore_gas_empty;
    }

    public boolean suckRec(BlockPos pos, int layer) {

        if(processed.contains(pos))
            return false;

        processed.add(pos);

        if(layer > 64)
            return false;

        Block b = world.getBlockState(pos).getBlock();

        if(b == ModBlocks.ore_oil || b == ModBlocks.ore_bedrock_oil || b == ModBlocks.ore_gas) {
            doSuck(pos);
            return true;
        }

        if(b == ModBlocks.ore_oil_empty || b == ModBlocks.ore_gas_empty) {
            ForgeDirection[] dirs = BobMathUtil.getShuffledDirs();

            for(ForgeDirection dir : dirs) {
                if(suckRec(pos.add(dir.offsetX, dir.offsetY, dir.offsetZ), layer + 1))
                    return true;
            }
        }

        return false;
    }

    public void doSuck(BlockPos pos) {
        Block b = world.getBlockState(pos).getBlock();

        if(b == ModBlocks.ore_oil || b == ModBlocks.ore_gas) {
            onSuck(pos);
        }
    }
    public abstract void onSuck(BlockPos pos);


    @Override
    public void setPower(long i) {
        power = i;

    }

    @Override
    public long getPower() {
        return power;
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
    public FluidTankNTM[] getSendingTanks() {
        return tanks;
    }

    @Override
    public FluidTankNTM[] getReceivingTanks() {
        return new FluidTankNTM[0];
    }

    @Override
    public FluidTankNTM[] getAllTanks() {
        return tanks;
    }

    public abstract DirPos[] getConPos();

    protected void updateConnections() {
        for(DirPos pos : getConPos()) {
            this.trySubscribe(world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());
        }
    }

    @Override
    public FluidTankNTM getTankToPaste() {
        return null;
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