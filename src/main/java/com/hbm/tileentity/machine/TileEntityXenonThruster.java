package com.hbm.tileentity.machine;

import com.hbm.api.energymk2.IEnergyReceiverMK2;
import com.hbm.api.fluid.IFluidStandardReceiver;
import com.hbm.api.tile.IPropulsion;
import com.hbm.blocks.BlockDummyable;
import com.hbm.dim.CelestialBody;
import com.hbm.dim.SolarSystem;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.inventory.fluid.trait.FT_Rocket;
import com.hbm.lib.DirPos;
import com.hbm.lib.ForgeDirection;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.BobMathUtil;
import com.hbm.util.I18nUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@AutoRegister
public class TileEntityXenonThruster extends TileEntityMachineBase implements ITickable, IPropulsion, IFluidStandardReceiver, IEnergyReceiverMK2 {

	public FluidTankNTM[] tanks;

	public long power;
	public static long maxPower = 20_000_000;

	private static final int POWER_COST_MULTIPLIER = 5_000;

	private boolean isOn;
	public float thrustAmount;
	
	private boolean hasRegistered;

	private int fuelCost;

	public TileEntityXenonThruster() {
		super(0, true, true);
		tanks = new FluidTankNTM[1];
		tanks[0] = new FluidTankNTM(Fluids.XENON, 4_000);
	}

	@Override
	public String getDefaultName() {
		return "container.xenonThruster";
	}

	@Override
	public void update() {
		if(!world.isRemote && CelestialBody.inOrbit(world)) {
			if(!hasRegistered) {
				if(isFacingPrograde()) registerPropulsion();
				hasRegistered = true;
			}

			for(DirPos pos : getConPos()) {
				for(FluidTankNTM tank : tanks) {
					trySubscribe(world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());
					trySubscribe(tank.getTankType(), world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());
				}
			}

			networkPackNT(250);
		} else {
			if(isOn) {
				thrustAmount += 0.01D;
				if(thrustAmount > 1) thrustAmount = 1;
			} else {
				thrustAmount -= 0.01D;
				if(thrustAmount < 0) thrustAmount = 0;
			}
		}
	}

	private DirPos[] getConPos() {
		ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset);
		ForgeDirection rot = dir.getRotation(ForgeDirection.UP);
		
		return new DirPos[] {
			new DirPos(pos.getX() - dir.offsetX - rot.offsetX, pos.getY(), pos.getZ() - dir.offsetZ - rot.offsetZ, dir),
			new DirPos(pos.getX() - dir.offsetX, pos.getY(), pos.getZ() - dir.offsetZ, dir),
			new DirPos(pos.getX() - dir.offsetX + rot.offsetX, pos.getY(), pos.getZ() - dir.offsetZ + rot.offsetZ, dir),
		};
	}

	@Override
	public void invalidate() {
		super.invalidate();

		if(hasRegistered) {
			unregisterPropulsion();
			hasRegistered = false;
		}
	}

	@Override
	public void onChunkUnload() {
		super.onChunkUnload();

		if(hasRegistered) {
			unregisterPropulsion();
			hasRegistered = false;
		}
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeBoolean(isOn);
		buf.writeLong(power);
		buf.writeInt(fuelCost);
		for (FluidTankNTM tank : tanks) tank.serialize(buf);
	}
	
	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		isOn = buf.readBoolean();
		power = buf.readLong();
		fuelCost = buf.readInt();
		for (FluidTankNTM tank : tanks) tank.deserialize(buf);
	}

	@Override
	public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		nbt.setBoolean("on", isOn);
		nbt.setLong("power", power);
		for(int i = 0; i < tanks.length; i++) tanks[i].writeToNBT(nbt, "t" + i);
		return super.writeToNBT(nbt);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		isOn = nbt.getBoolean("on");
		power = nbt.getLong("power");
		for(int i = 0; i < tanks.length; i++) tanks[i].readFromNBT(nbt, "t" + i);
	}

	public boolean isFacingPrograde() {
		return ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset) == ForgeDirection.WEST;
	}
	
	AxisAlignedBB bb = null;
	
	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		if(bb == null) bb = new AxisAlignedBB(pos.getX() - 2, pos.getY() - 1, pos.getZ() - 2, pos.getX() + 3, pos.getY() + 2, pos.getZ() + 3);
		return bb;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return 65536.0D;
	}

	@Override
	public TileEntity getTileEntity() {
		return this;
	}

	@Override
	public boolean canPerformBurn(int shipMass, double deltaV) {
		FT_Rocket trait = tanks[0].getTankType().getTrait(FT_Rocket.class);
		int isp = trait != null ? trait.getISP() : 300;

		fuelCost = SolarSystem.getFuelCost(deltaV, shipMass, isp);

		if(power < (long) fuelCost * POWER_COST_MULTIPLIER) return false;

		for(FluidTankNTM tank : tanks) {
			if(tank.getFill() < fuelCost) return false;
		}

		return true;
	}

	@Override
	public void addErrors(List<String> errors) {
		if(power < (long) fuelCost * POWER_COST_MULTIPLIER) {
			errors.add(TextFormatting.RED + I18nUtil.resolveKey(getBlockType().getTranslationKey() + ".name") + " - Insufficient power: needs " + BobMathUtil.getShortNumber((long) fuelCost * POWER_COST_MULTIPLIER) + "HE");
		}

		for(FluidTankNTM tank : tanks) {
			if(tank.getFill() < fuelCost) {
				errors.add(TextFormatting.RED + I18nUtil.resolveKey(getBlockType().getTranslationKey() + ".name") + " - Insufficient fuel: needs " + fuelCost + "mB");
			}
		}
	}

	@Override
	public float getThrust() {
		// but le realisme :((((
		// do not speak to me of realisme, mr I can carry 2,880,000kg of dirt in my fucking pocket
		return 1_400_000;
	}

	@Override
	public int startBurn() {
		isOn = true;
		power -= (long) fuelCost * POWER_COST_MULTIPLIER;
		for(FluidTankNTM tank : tanks) {
			tank.setFill(tank.getFill() - fuelCost);
		}
		return 200; // ten second prewarm
	}

	@Override
	public int endBurn() {
		isOn = false;
		return 200; // ten second recharge time
	}

	@Override
	public FluidTankNTM[] getAllTanks() {
		return tanks;
	}

	@Override
	public FluidTankNTM[] getReceivingTanks() {
		return tanks;
	}

	@Override
	public long getPower() {
		return power;
	}

	@Override
	public void setPower(long power) {
		this.power = power;
	}

	@Override
	public long getMaxPower() {
		return maxPower;
	}
}
