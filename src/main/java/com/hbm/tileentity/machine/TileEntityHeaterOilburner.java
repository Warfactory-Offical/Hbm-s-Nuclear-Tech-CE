package com.hbm.tileentity.machine;

import api.hbm.fluid.IFluidStandardTransceiver;
import api.hbm.tile.IHeatSource;
import com.hbm.capability.NTMFluidHandlerWrapper;
import com.hbm.forgefluid.ModForgeFluids;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.interfaces.IFFtoNTMF;
import com.hbm.inventory.container.ContainerOilburner;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.inventory.fluid.trait.FT_Flammable;
import com.hbm.inventory.gui.GUIOilburner;
import com.hbm.lib.DirPos;
import com.hbm.lib.Library;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityMachineBase;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

public class TileEntityHeaterOilburner extends TileEntityMachineBase implements ITickable, IGUIProvider, IHeatSource, IControlReceiver, IFluidStandardTransceiver, IFFtoNTMF {
    public boolean isOn = false;

    public FluidTankNTM tankNew;
    public FluidTank tank;
    public Fluid fluidType;

    private int cacheHeat = 0;

    public int setting = 1;

    public int heatEnergy = 0;

    public static final int maxHeatEnergy = 1_000_000;
    public FluidTankNTM smoke;
    public FluidTankNTM smoke_leaded;
    public FluidTankNTM smoke_poison;
    public int buffer;
    private static boolean converted = false;

    public TileEntityHeaterOilburner() {
        super(3);

        tankNew = new FluidTankNTM(Fluids.HEATINGOIL, 16000);
        tank = new FluidTank(16000);
        fluidType = ModForgeFluids.gas;
        smoke = new FluidTankNTM(Fluids.SMOKE, buffer);
        smoke_leaded = new FluidTankNTM(Fluids.SMOKE_LEADED, buffer);
        smoke_poison = new FluidTankNTM(Fluids.SMOKE_POISON, buffer);

        converted = true;
    }

    public DirPos[] getConPos() {
        return new DirPos[] {
                new DirPos(pos.getX() + 2, pos.getY(), pos.getZ(), Library.POS_X),
                new DirPos(pos.getX() - 2, pos.getY(), pos.getZ(), Library.NEG_X),
                new DirPos(pos.getX(), pos.getY(), pos.getZ() + 2, Library.POS_Z),
                new DirPos(pos.getX(), pos.getY(), pos.getZ() - 2, Library.NEG_Z)
        };
    }

    @Override
    public void update() {

        if(!world.isRemote) {
            if(!converted){
                convertAndSetFluid(fluidType, tank, tankNew);
                converted = true;
            }
            tankNew.loadTank(0, 1, inventory);
            tankNew.setType(2, inventory);

            for(DirPos pos : this.getConPos()) {
                this.trySubscribe(tankNew.getTankType(), world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());
            }

            boolean shouldCool = true;

            if(this.isOn && this.heatEnergy < maxHeatEnergy) {

                if(tankNew.getTankType().hasTrait(FT_Flammable.class)) {
                    FT_Flammable type = tankNew.getTankType().getTrait(FT_Flammable.class);

                    int burnRate = setting;
                    int toBurn = Math.min(burnRate, tankNew.getFill());

                    tankNew.setFill(tankNew.getFill() - toBurn);

                    int heat = (int)(type.getHeatEnergy() / 1000);

                    this.heatEnergy += heat * toBurn;

                    shouldCool = false;
                }
            }

            if(this.heatEnergy >= maxHeatEnergy)
                shouldCool = false;

            if(shouldCool)
                this.heatEnergy = Math.max(this.heatEnergy - Math.max(this.heatEnergy / 1000, 1), 0);

            this.networkPackNT(25);
        }
    }

    @Override
    public void serialize(ByteBuf buf) {
        super.serialize(buf);
        tankNew.serialize(buf);

        buf.writeBoolean(isOn);
        buf.writeInt(heatEnergy);
        buf.writeByte((byte) this.setting);
    }

    @Override
    public void deserialize(ByteBuf buf) {
        super.deserialize(buf);
        tankNew.deserialize(buf);

        isOn = buf.readBoolean();
        heatEnergy = buf.readInt();
        setting = buf.readByte();
    }

    @Override
    public String getName() {
        return "container.heaterOilburner";
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        if(!converted){
            tank.readFromNBT(nbt);
            if(nbt.hasKey("fluidType")) fluidType = FluidRegistry.getFluid(nbt.getString("fluidType"));
        } else{
            tankNew.readFromNBT(nbt, "tank");
            if(nbt.hasKey("fluidType")) nbt.removeTag("fluidType");
        }
        isOn = nbt.getBoolean("isOn");
        heatEnergy = nbt.getInteger("heatEnergy");
        setting = nbt.getByte("setting");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        if(!converted){
            tank.writeToNBT(nbt);
            if(fluidType != null) {
                nbt.setString("fluidType", fluidType.getName());
            }
        } else tankNew.writeToNBT(nbt, "tank");
        nbt.setBoolean("isOn", isOn);
        nbt.setInteger("heatEnergy", heatEnergy);
        nbt.setByte("setting", (byte) this.setting);
        return super.writeToNBT(nbt);
    }

    public void toggleSettingUp() {
        setting++;

        if(setting > 100) {
            setting = 1;
        }
    }

    public void toggleSettingDown() {
        setting--;

        if(setting < 1) {
            setting = 100;
        }
    }

    @Override
    public FluidTankNTM[] getReceivingTanks()  {
        return new FluidTankNTM[] {tankNew};
    }

    @Override
    public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new ContainerOilburner(player.inventory, this);
    }

    @SideOnly(Side.CLIENT)
    private ResourceLocation texture;

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
        if(texture == null) {
            texture = new ResourceLocation(RefStrings.MODID + ":textures/gui/machine/gui_oilburner.png");
        }

        return new GUIOilburner(player.inventory, this, texture);
    }

    @Override
    public int getHeatStored() {
        return heatEnergy;
    }

    @Override
    public void useUpHeat(int heat) {
        this.heatEnergy = Math.max(0, this.heatEnergy - heat);
    }

    @Override
    public boolean hasPermission(EntityPlayer player) {
        return player.getDistanceSq(pos) <= 256;
    }

    @Override
    public void receiveControl(NBTTagCompound data) {
        if(data.hasKey("toggle")) {
            this.isOn = !this.isOn;
        }

        this.markDirty();
    }

    AxisAlignedBB bb = null;

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        if(bb == null) {
            bb = new AxisAlignedBB(pos.getX() - 1, pos.getY(), pos.getZ() - 1, pos.getX() + 2, pos.getY() + 2, pos.getZ() + 2);
        }

        return bb;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public double getMaxRenderDistanceSquared() {
        return 65536.0D;
    }

    @Override
    public FluidTankNTM[] getAllTanks() {
        return new FluidTankNTM[] {tankNew};
    }

    @Override
    public FluidTankNTM[] getSendingTanks() {
        return new FluidTankNTM[] {smoke, smoke_leaded, smoke_poison};
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
