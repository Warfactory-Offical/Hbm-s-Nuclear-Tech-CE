package com.hbm.tileentity.turret;

import api.hbm.fluid.IFluidStandardReceiver;
import com.hbm.blocks.BlockDummyable;
import com.hbm.capability.NTMFluidHandlerWrapper;
import com.hbm.forgefluid.ModForgeFluids;
import com.hbm.handler.BulletConfigSyncingUtil;
import com.hbm.handler.BulletConfiguration;
import com.hbm.interfaces.IFFtoNTMF;
import com.hbm.inventory.FluidCombustionRecipes;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.inventory.fluid.trait.FT_Flammable;
import com.hbm.inventory.fluid.trait.FluidTraitSimple;
import com.hbm.items.ModItems;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.packet.AuxParticlePacketNT;
import com.hbm.packet.PacketDispatcher;
import com.hbm.render.amlfrom1710.Vec3;
import com.hbm.tileentity.IFluidCopiable;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;

import javax.annotation.Nullable;
import java.util.List;

public class TileEntityTurretFritz extends TileEntityTurretBaseNT implements IFluidStandardReceiver, IFluidCopiable, IFFtoNTMF {

	public FluidTank tankOld;
	public FluidTankNTM tank;

	public static int drain = 2;

	private Fluid oldFluid = ModForgeFluids.none;
	private static boolean converted = false;
	
	public TileEntityTurretFritz() {
		super();
		this.tankOld = new FluidTank(16000);
		this.tank = new FluidTankNTM(Fluids.DIESEL, 16000);
	}
	
	@Override
	public String getName() {
		return "container.turretFritz";
	}

	@Override
	protected List<Integer> getAmmoList() {
		return null;
	}
	
	@Override
	public double getDecetorRange() {
		return 32D;
	}
	
	@Override
	public double getDecetorGrace() {
		return 2D;
	}

	@Override
	public double getTurretElevation() {
		return 45D;
	}

	@Override
	public long getMaxPower() {
		return 10000;
	}

	@Override
	public double getBarrelLength() {
		return 2.25D;
	}

	@Override
	public double getAcceptableInaccuracy() {
		return 15;
	}
	
	@Override
	public void updateFiringTick() {

		if(this.tank.getTankType().hasTrait(FT_Flammable.class) && this.tank.getTankType().hasTrait(FluidTraitSimple.FT_Liquid.class) && this.tank.getFill() >= 2) {

			BulletConfiguration conf = BulletConfigSyncingUtil.pullConfig(BulletConfigSyncingUtil.FLA_NORMAL);
			this.spawnBullet(conf, FluidCombustionRecipes.getFlameEnergy(tank.getTankType()) * 0.002F);
			this.tank.setFill(this.tank.getFill() - 2);

			Vec3d pos = this.getTurretPos();
			Vec3 vec = Vec3.createVectorHelper(this.getBarrelLength(), 0, 0);
			vec.rotateAroundZ((float) -this.rotationPitch);
			vec.rotateAroundY((float) -(this.rotationYaw + Math.PI * 0.5));
			
			world.playSound(null, this.pos.getX(), this.pos.getY(), this.pos.getZ(), HBMSoundHandler.flamethrowerShoot, SoundCategory.BLOCKS, 2F, 1F + world.rand.nextFloat() * 0.5F);
			
			NBTTagCompound data = new NBTTagCompound();
			data.setString("type", "vanillaburst");
			data.setString("mode", "flame");
			data.setInteger("count", 2);
			data.setDouble("motion", 0.025D);
			PacketDispatcher.wrapper.sendToAllAround(new AuxParticlePacketNT(data, pos.x + vec.xCoord, pos.y + vec.yCoord, pos.z + vec.zCoord), new TargetPoint(world.provider.getDimension(), this.pos.getX(), this.pos.getY(), this.pos.getZ(), 50));
		}
	}
	
	public int getDelay() {
		return 2;
	}
	
	@Override
	public void update(){
		super.update();
		if(!converted && tank.getTankType() == Fluids.NONE) {
			convertAndSetFluid(oldFluid, tankOld, tank);
			converted = true;
		}
		if(!world.isRemote) {
			tank.setType(9, 9, inventory);
			tank.loadTank(0, 1, inventory);

			for(int i = 1; i < 10; i++) {

				if(!inventory.getStackInSlot(i).isEmpty() && inventory.getStackInSlot(i).getItem() == ModItems.ammo_fuel) {
					if(this.tank.getTankType() == Fluids.DIESEL && this.tank.getFill() + 1000 <= this.tank.getMaxFill()) {
						this.tank.setFill(this.tank.getFill() + 1000);
						this.inventory.getStackInSlot(i).shrink(1);
					}
				}
			}
		}
	}

	@Override
	protected NBTTagCompound writePacket() {
		NBTTagCompound data = super.writePacket();
		tank.writeToNBT(data, "t");
		return data;
	}

	@Override
	public void networkUnpack(NBTTagCompound nbt) {
		super.networkUnpack(nbt);
		tank.readFromNBT(nbt, "t");
	}

	@Override //TODO: clean this shit up
	protected void updateConnections() {
		ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset).getOpposite();
		ForgeDirection rot = dir.getRotation(ForgeDirection.UP);

		this.trySubscribe(world, pos.getX() + dir.offsetX * -1 + rot.offsetX * 0, pos.getY(), pos.getZ() + dir.offsetZ * -1 + rot.offsetZ * 0, dir.getOpposite());
		this.trySubscribe(world, pos.getX() + dir.offsetX * -1 + rot.offsetX * -1, pos.getY(), pos.getZ() + dir.offsetZ * -1 + rot.offsetZ * -1, dir.getOpposite());
		this.trySubscribe(world, pos.getX() + dir.offsetX * 0 + rot.offsetX * -2, pos.getY(), pos.getZ() + dir.offsetZ * 0 + rot.offsetZ * -2, rot.getOpposite());
		this.trySubscribe(world, pos.getX() + dir.offsetX * 1 + rot.offsetX * -2, pos.getY(), pos.getZ() + dir.offsetZ * 1 + rot.offsetZ * -2, rot.getOpposite());
		this.trySubscribe(world, pos.getX() + dir.offsetX * 0 + rot.offsetX * 1, pos.getY(), pos.getZ() + dir.offsetZ * 0 + rot.offsetZ * 1, rot);
		this.trySubscribe(world, pos.getX() + dir.offsetX * 1 + rot.offsetX * 1, pos.getY(), pos.getZ() + dir.offsetZ * 1 + rot.offsetZ * 1, rot);
		this.trySubscribe(world, pos.getX() + dir.offsetX * 2 + rot.offsetX * 0, pos.getY(), pos.getZ() + dir.offsetZ * 2 + rot.offsetZ * 0, dir);
		this.trySubscribe(world, pos.getX() + dir.offsetX * 2 + rot.offsetX * -1, pos.getY(), pos.getZ() + dir.offsetZ * 2 + rot.offsetZ * -1, dir);

		this.trySubscribe(tank.getTankType(), world, pos.getX() + dir.offsetX * -1 + rot.offsetX * 0, pos.getY(), pos.getZ() + dir.offsetZ * -1 + rot.offsetZ * 0, dir.getOpposite());
		this.trySubscribe(tank.getTankType(), world, pos.getX() + dir.offsetX * -1 + rot.offsetX * -1, pos.getY(), pos.getZ() + dir.offsetZ * -1 + rot.offsetZ * -1, dir.getOpposite());
		this.trySubscribe(tank.getTankType(), world, pos.getX() + dir.offsetX * 0 + rot.offsetX * -2, pos.getY(), pos.getZ() + dir.offsetZ * 0 + rot.offsetZ * -2, rot.getOpposite());
		this.trySubscribe(tank.getTankType(), world, pos.getX() + dir.offsetX * 1 + rot.offsetX * -2, pos.getY(), pos.getZ() + dir.offsetZ * 1 + rot.offsetZ * -2, rot.getOpposite());
		this.trySubscribe(tank.getTankType(), world, pos.getX() + dir.offsetX * 0 + rot.offsetX * 1, pos.getY(), pos.getZ() + dir.offsetZ * 0 + rot.offsetZ * 1, rot);
		this.trySubscribe(tank.getTankType(), world, pos.getX() + dir.offsetX * 1 + rot.offsetX * 1, pos.getY(), pos.getZ() + dir.offsetZ * 1 + rot.offsetZ * 1, rot);
		this.trySubscribe(tank.getTankType(), world, pos.getX() + dir.offsetX * 2 + rot.offsetX * 0, pos.getY(), pos.getZ() + dir.offsetZ * 2 + rot.offsetZ * 0, dir);
		this.trySubscribe(tank.getTankType(), world, pos.getX() + dir.offsetX * 2 + rot.offsetX * -1, pos.getY(), pos.getZ() + dir.offsetZ * 2 + rot.offsetZ * -1, dir);
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt){
		if(!converted && tank.getTankType() == Fluids.NONE){
			nbt.setInteger("cap", tankOld.getCapacity());
			tankOld.writeToNBT(nbt);
			nbt.setBoolean("converted", true);
		} else tank.writeToNBT(nbt, "tank");
		return super.writeToNBT(nbt);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbt){
		tank.readFromNBT(nbt,"tank");
		if (!converted && tank.getTankType() == Fluids.NONE){
			if(tankOld == null || tankOld.getCapacity() <= 0)
				tankOld = new FluidTank(nbt.getInteger("cap"));
			tankOld.readFromNBT(nbt);
			if(tankOld.getFluid() != null) {
				oldFluid = tankOld.getFluid().getFluid();
			}
		} else {
			if(nbt.hasKey("cap")) nbt.removeTag("cap");
		}
		super.readFromNBT(nbt);
	}
	
	@Override
	public int[] getAccessibleSlotsFromSide(EnumFacing e){
		return new int[] { 1, 2, 3, 4, 5, 6, 7, 8 };
	}

	@Override
	public FluidTankNTM[] getReceivingTanks() {
		return new FluidTankNTM[] { tank };
	}

	@Override
	public FluidTankNTM[] getAllTanks() {
		return new FluidTankNTM[] { tank };
	}

	@Override
	public FluidTankNTM getTankToPaste() {
		return tank;
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
