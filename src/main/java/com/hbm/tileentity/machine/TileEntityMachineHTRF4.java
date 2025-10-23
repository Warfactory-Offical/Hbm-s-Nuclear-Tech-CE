package com.hbm.tileentity.machine;

import com.hbm.api.energymk2.IEnergyReceiverMK2;
import com.hbm.api.fluid.IFluidStandardReceiver;
import com.hbm.api.tile.IPropulsion;
import com.hbm.blocks.BlockDummyable;
import com.hbm.dim.CelestialBody;
import com.hbm.dim.SolarSystem;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.inventory.fluid.trait.FT_Rocket;
import com.hbm.lib.DirPos;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.HBMSoundEvents;
import com.hbm.main.MainRegistry;
import com.hbm.sound.AudioWrapper;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.BobMathUtil;
import com.hbm.util.I18nUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@AutoRegister
public class TileEntityMachineHTRF4 extends TileEntityMachineBase implements ITickable, IPropulsion, IFluidStandardReceiver, IEnergyReceiverMK2 {

	public FluidTankNTM[] tanks;

	public long power;
	public static long maxPower = 1_000_000_000;

	private static final int POWER_COST_MULTIPLIER = 1_000_000;

	private boolean isOn;
	private float speed;
	public double lastTime;
	public double time;
	private float soundtime;
	private AudioWrapper audio;

	private boolean hasRegistered;

	private int fuelCost;

	public TileEntityMachineHTRF4() {
		super(0, true, true);
		tanks = new FluidTankNTM[1];
		tanks[0] = new FluidTankNTM(Fluids.PLASMA_DT, 64000);
		tanks[0] = new FluidTankNTM(Fluids.PLASMA_HD, 64000);
		tanks[0] = new FluidTankNTM(Fluids.PLASMA_HT, 64000);
		tanks[0] = new FluidTankNTM(Fluids.PLASMA_DH3, 64000);
		tanks[0] = new FluidTankNTM(Fluids.PLASMA_XM, 64000);
		tanks[0] = new FluidTankNTM(Fluids.PLASMA_BF, 64000);
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

			if(isOn) {
				soundtime++;

				if(soundtime == 1) {
					this.world.playSound(null, this.pos.getX(), this.pos.getY(), this.pos.getZ(), HBMSoundEvents.lpwstart, SoundCategory.BLOCKS, 1.5F, 1F);
				} else if(soundtime > 20) {
					soundtime = 20;
				}
			}else {
				soundtime--;

				if(soundtime == 19) {
					this.world.playSound(null, this.pos.getX(), this.pos.getY(), this.pos.getZ(), HBMSoundEvents.lpwstop, SoundCategory.BLOCKS, 1.5F, 1F);
				} else if(soundtime <= 0) {
					soundtime = 0;
				}
			}

			networkPackNT(250);
		} else {
			if(isOn) {
				speed += 0.05D;
				if(speed > 1) speed = 1;

				if(soundtime > 18) {
					if(audio == null) {
						audio = createAudioLoop();
						audio.startSound();
					} else if(!audio.isPlaying()) {
						audio = rebootAudio(audio);
					}

					audio.updateVolume(getVolume(1F));
					audio.keepAlive();

					{
						List<FluidType> types = new ArrayList() {{ add(tanks[0].getTankType()); }};
				
						if(types.contains(Fluids.PLASMA_BF)) {

							ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset).getRotation(ForgeDirection.UP);

							NBTTagCompound data = new NBTTagCompound();
							data.setDouble("posX", pos.getX() + dir.offsetX * 12);
							data.setDouble("posY", pos.getY() + 4);
							data.setDouble("posZ", pos.getZ() + dir.offsetZ * 12);
							data.setString("type", "missileContrailbf");
							data.setFloat("scale", 3);
							data.setDouble("moX", dir.offsetX * 10);
							data.setDouble("moY", 0);
							data.setDouble("moZ", dir.offsetZ * 10);
							data.setInteger("maxAge", 40 + world.rand.nextInt(40));
							MainRegistry.proxy.effectNT(data);
							return;
							}
					}

					ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset).getRotation(ForgeDirection.UP);

					NBTTagCompound data = new NBTTagCompound();
					data.setDouble("posX", pos.getX() + dir.offsetX * 12);
					data.setDouble("posY", pos.getY() + 4);
					data.setDouble("posZ", pos.getZ() + dir.offsetZ * 12);
					data.setString("type", "missileContrailf");
					data.setFloat("scale", 3);
					data.setDouble("moX", dir.offsetX * 10);
					data.setDouble("moY", 0);
					data.setDouble("moZ", dir.offsetZ * 10);
					data.setInteger("maxAge", 40 + world.rand.nextInt(40));
					MainRegistry.proxy.effectNT(data);
				}
			} else {
				speed -= 0.05D;
				if(speed < 0) speed = 0;
				
				if(audio != null) {
					audio.stopSound();
					audio = null;
				}
			}

		}

		lastTime = time;
		time += speed;
	}

	private void updateType() {
		
		List<FluidType> types = new ArrayList() {{ add(tanks[0].getTankType()); }};

		if(types.contains(Fluids.PLASMA_BF)) {
			ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset).getRotation(ForgeDirection.UP);
			NBTTagCompound data = new NBTTagCompound();
			data.setDouble("posX", pos.getX() + dir.offsetX * 12);
			data.setDouble("posY", pos.getY() + 4);
			data.setDouble("posZ", pos.getZ() + dir.offsetZ * 12);
			data.setString("type", "missileContrailbf");
			data.setFloat("scale", 3);
			data.setDouble("moX", dir.offsetX * 10);
			data.setDouble("moY", 0);
			data.setDouble("moZ", dir.offsetZ * 10);
			data.setInteger("maxAge", 20 + world.rand.nextInt(20));
			MainRegistry.proxy.effectNT(data);
			return;
		    }
	}

	private DirPos[] getConPos() {
		ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset);
		ForgeDirection rot = dir.getRotation(ForgeDirection.UP);
		
		return new DirPos[] {
			new DirPos(pos.getX() + 10, pos.getY() + 2, pos.getZ() - 1, rot.getOpposite()),
			new DirPos(pos.getX() + 10, pos.getY() + 2, pos.getZ() + 1, rot.getOpposite())
		};
	}
	
	@Override
	public AudioWrapper createAudioLoop() {
		return MainRegistry.proxy.getLoopedSound(HBMSoundEvents.lpwloop, SoundCategory.BLOCKS, pos.getX(), pos.getY(), pos.getZ(), 0.25F, 27.5F, 1.0F, 20);
	}

	@Override
	public void invalidate() {
		super.invalidate();

		if(hasRegistered) {
			unregisterPropulsion();
			hasRegistered = false;
		}

		if(audio != null) {
			audio.stopSound();
			audio = null;
		}
	}

	@Override
	public void onChunkUnload() {
		super.onChunkUnload();

		if(hasRegistered) {
			unregisterPropulsion();
			hasRegistered = false;
		}

		if(audio != null) {
			audio.stopSound();
			audio = null;
		}
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeBoolean(isOn);
		buf.writeFloat(soundtime);
		buf.writeInt(fuelCost);
		buf.writeLong(power);
        for (FluidTankNTM tank : tanks) tank.serialize(buf);
	}
	
	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		isOn = buf.readBoolean();
		soundtime = buf.readFloat();
		fuelCost = buf.readInt();
		power = buf.readLong();
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
		return ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset) == ForgeDirection.SOUTH;
	}
	
	AxisAlignedBB bb = null;
	
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
	public TileEntity getTileEntity() {
		return this;
	}

	@Override
	public boolean canPerformBurn(int shipMass, double deltaV) {
		FT_Rocket trait = tanks[0].getTankType().getTrait(FT_Rocket.class);
		int isp = trait != null ? trait.getISP() : 300;

		fuelCost = SolarSystem.getFuelCost(deltaV, shipMass, isp);

		if(power < fuelCost * POWER_COST_MULTIPLIER) return false;

		for(FluidTankNTM tank : tanks) {
			if(tank.getFill() < fuelCost) return false;
		}

		return true;
	}

	@Override
	public void addErrors(List<String> errors) {
		if(power < fuelCost * POWER_COST_MULTIPLIER) {
			errors.add(TextFormatting.RED + I18nUtil.resolveKey(getBlockType().getTranslationKey() + ".name") + " - Insufficient power: needs " + BobMathUtil.getShortNumber(fuelCost * POWER_COST_MULTIPLIER) + "HE");
		}
		
		for(FluidTankNTM tank : tanks) {
			if(tank.getFill() < fuelCost) {
				errors.add(TextFormatting.RED + I18nUtil.resolveKey(getBlockType().getTranslationKey() + ".name") + " - Insufficient fuel: needs " + fuelCost + "mB");
			}
		}
	}

	@Override
	public float getThrust() {
		return 1_600_000_000.0F; // F1 thrust
	}

	@Override
	public int startBurn() {
		isOn = true;
		power -= fuelCost * POWER_COST_MULTIPLIER;
		for(FluidTankNTM tank : tanks) {
			tank.setFill(tank.getFill() - fuelCost);
		}
		return 20;
	}

	@Override
	public int endBurn() {
		isOn = false;
		return 20; // Cooldown
	}

	@Override
	public String getDefaultName() {
		return "container.htrf4";
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
