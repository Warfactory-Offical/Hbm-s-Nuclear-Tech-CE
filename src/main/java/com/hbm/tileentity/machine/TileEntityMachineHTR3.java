package com.hbm.tileentity.machine;

import api.hbm.fluid.IFluidStandardReceiver;
import api.hbm.tile.IPropulsion;
import com.hbm.blocks.BlockDummyable;
import com.hbm.capability.NTMFluidHandlerWrapper;
import com.hbm.dim.CelestialBody;
import com.hbm.dim.SolarSystem;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.inventory.fluid.trait.FT_Rocket;
import com.hbm.lib.DirPos;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.main.MainRegistry;
import com.hbm.sound.AudioWrapper;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.I18nUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class TileEntityMachineHTR3 extends TileEntityMachineBase implements ITickable, IPropulsion, IFluidStandardReceiver {

	public FluidTankNTM[] tanks;

	private boolean isOn;
	private float speed;
	public double lastTime;
	public double time;
	private float soundtime;
	private AudioWrapper audio;

	private boolean hasRegistered;

	private int fuelCost;

	public TileEntityMachineHTR3() {
		super(7);
		tanks = new FluidTankNTM[1];
        tanks[0] = new FluidTankNTM(Fluids.WASTEGAS, 1_280_000);
        tanks[0] = new FluidTankNTM(Fluids.GAS_WATZ, 1_280_000);
        tanks[0] = new FluidTankNTM(Fluids.GASEOUS_URANIUM_BROMIDE, 1_280_000);
        tanks[0] = new FluidTankNTM(Fluids.GASEOUS_PLUTONIUM_BROMIDE, 1_280_000);
        tanks[0] = new FluidTankNTM(Fluids.GASEOUS_THORIUM_BROMIDE, 1_280_000);
        tanks[0] = new FluidTankNTM(Fluids.GASEOUS_SCHRABIDIUM_BROMIDE, 1_280_000);
	tanks[0] = new FluidTankNTM(Fluids.SUPERHEATED_HYDROGEN, 1_280_000);
        tanks[0] = new FluidTankNTM(Fluids.NONE, 1_280_000);
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
					trySubscribe(tank.getTankType(), world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());
				}
			}

			if(isOn) {
				soundtime++;

				if(soundtime == 1) {
					this.world.playSound(null, this.pos.getX(), this.pos.getY(), this.pos.getZ(), HBMSoundHandler.htrstart, SoundCategory.BLOCKS, 1.5F, 1F);
				} else if(soundtime > 20) {
					soundtime = 20;
				}
			}else {
				soundtime--;

				if(soundtime == 19) {
					this.world.playSound(null, this.pos.getX(), this.pos.getY(), this.pos.getZ(), HBMSoundHandler.htrstop, SoundCategory.BLOCKS, 2.0F, 1F);
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
				
						if(types.contains(Fluids.SUPERHEATED_HYDROGEN)) {

							ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset).getRotation(ForgeDirection.UP);

							NBTTagCompound data = new NBTTagCompound();
							data.setDouble("posX", pos.getX() + dir.offsetX * 12);
							data.setDouble("posY", pos.getY() + 4);
							data.setDouble("posZ", pos.getZ() + dir.offsetZ * 12);
							data.setString("type", "missileContrail");
							data.setFloat("scale", 3);
							data.setDouble("moX", dir.offsetX * 10);
							data.setDouble("moY", 0);
							data.setDouble("moZ", dir.offsetZ * 10);
							data.setInteger("maxAge", 40 + world.rand.nextInt(40));
							MainRegistry.proxy.effectNT(data);
							return;
							}
					}
					
					{
						List<FluidType> types = new ArrayList() {{ add(tanks[0].getTankType()); }};
				
						if(types.contains(Fluids.GAS_WATZ) || types.contains(Fluids.WASTEGAS) || types.contains(Fluids.GASEOUS_THORIUM_BROMIDE)) {

							ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset).getRotation(ForgeDirection.UP);

							NBTTagCompound data = new NBTTagCompound();
							data.setDouble("posX", pos.getX() + dir.offsetX * 12);
							data.setDouble("posY", pos.getY() + 4);
							data.setDouble("posZ", pos.getZ() + dir.offsetZ * 12);
							data.setString("type", "missileContrailMUD");
							data.setFloat("scale", 3);
							data.setDouble("moX", dir.offsetX * 10);
							data.setDouble("moY", 0);
							data.setDouble("moZ", dir.offsetZ * 10);
							data.setInteger("maxAge", 40 + world.rand.nextInt(40));
							MainRegistry.proxy.effectNT(data);
							return;
							}
					}

                    {
						List<FluidType> types = new ArrayList() {{ add(tanks[0].getTankType()); }};
				
						if(types.contains(Fluids.GASEOUS_SCHRABIDIUM_BROMIDE)) {

							ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset).getRotation(ForgeDirection.UP);

							NBTTagCompound data = new NBTTagCompound();
							data.setDouble("posX", pos.getX() + dir.offsetX * 12);
							data.setDouble("posY", pos.getY() + 4);
							data.setDouble("posZ", pos.getZ() + dir.offsetZ * 12);
							data.setString("type", "missileContrailSCH");
							data.setFloat("scale", 3);
							data.setDouble("moX", dir.offsetX * 10);
							data.setDouble("moY", 0);
							data.setDouble("moZ", dir.offsetZ * 10);
							data.setInteger("maxAge", 40 + world.rand.nextInt(40));
							MainRegistry.proxy.effectNT(data);
							return;
							}
					}

                    {
						List<FluidType> types = new ArrayList() {{ add(tanks[0].getTankType()); }};
				
						if(types.contains(Fluids.GASEOUS_URANIUM_BROMIDE) || types.contains(Fluids.GASEOUS_PLUTONIUM_BROMIDE)) {

							ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset).getRotation(ForgeDirection.UP);

							NBTTagCompound data = new NBTTagCompound();
							data.setDouble("posX", pos.getX() + dir.offsetX * 12);
							data.setDouble("posY", pos.getY() + 4);
							data.setDouble("posZ", pos.getZ() + dir.offsetZ * 12);
							data.setString("type", "missileContrailUP");
							data.setFloat("scale", 3);
							data.setDouble("moX", dir.offsetX * 10);
							data.setDouble("moY", 0);
							data.setDouble("moZ", dir.offsetZ * 10);
							data.setInteger("maxAge", 40 + world.rand.nextInt(40));
							MainRegistry.proxy.effectNT(data);
							return;
							}
					}
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

	private DirPos[] getConPos() {
		ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset);
		ForgeDirection rot = dir.getRotation(ForgeDirection.UP);
		
		return new DirPos[] {
			new DirPos(pos.getX() + 6, pos.getY() + 3, pos.getZ() + 0, rot),
			new DirPos(pos.getX() + 6, pos.getY() + 3, pos.getZ() + 0, rot.getOpposite())
		};
	}
	
	@Override
	public AudioWrapper createAudioLoop() {
		return MainRegistry.proxy.getLoopedSound(HBMSoundHandler.htrloop, SoundCategory.BLOCKS, pos.getX(), pos.getY(), pos.getZ(), 0.25F, 1.0F);
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
		for(int i = 0; i < tanks.length; i++) tanks[i].serialize(buf);
	}
	
	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		isOn = buf.readBoolean();
		soundtime = buf.readFloat();
		fuelCost = buf.readInt();
		for(int i = 0; i < tanks.length; i++) tanks[i].deserialize(buf);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		nbt.setBoolean("on", isOn);
		for(int i = 0; i < tanks.length; i++) tanks[i].writeToNBT(nbt, "t" + i);
		return super.writeToNBT(nbt);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		isOn = nbt.getBoolean("on");
		for(int i = 0; i < tanks.length; i++) tanks[i].readFromNBT(nbt, "t" + i);
	}

	public boolean isFacingPrograde() {
		return ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset) == ForgeDirection.SOUTH;
	}
	
	AxisAlignedBB bb = null;
	
	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		if(bb == null) bb = new AxisAlignedBB(pos.getX() - 10, pos.getY(), pos.getZ() - 10, pos.getX() + 11, pos.getY() + 7, pos.getZ() + 11);
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

		for(FluidTankNTM tank : tanks) {
			if(tank.getFill() < fuelCost) return false;
		}

		return true;
	}

	@Override
	public void addErrors(List<String> errors) {
		for(FluidTankNTM tank : tanks) {
			if(tank.getFill() < fuelCost) {
				errors.add(TextFormatting.RED + I18nUtil.resolveKey(getBlockType().getTranslationKey() + ".name") + " - Insufficient fuel: needs " + fuelCost + "mB");
			}
		}
	}

	@Override
	public float getThrust() {
		return 800_000_000.0F;
	}

	@Override
	public int startBurn() {
		isOn = true;
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
	public String getName() {
		return "container.htr";
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
					new NTMFluidHandlerWrapper(this.getReceivingTanks(), null)
			);
		}
		return super.getCapability(capability, facing);
	}
}
