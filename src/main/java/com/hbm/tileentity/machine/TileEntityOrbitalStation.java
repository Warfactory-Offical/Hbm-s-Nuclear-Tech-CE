package com.hbm.tileentity.machine;

import api.hbm.fluid.IFluidStandardReceiver;
import com.hbm.blocks.ModBlocks;
import com.hbm.capability.NTMFluidHandlerWrapper;
import com.hbm.dim.CelestialBody;
import com.hbm.dim.SolarSystem;
import com.hbm.dim.orbit.OrbitalStation;
import com.hbm.entity.missile.EntityRideableRocket;
import com.hbm.entity.missile.EntityRideableRocket.RocketState;
import com.hbm.handler.RocketStruct;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.items.weapon.ItemCustomRocket;
import com.hbm.lib.DirPos;
import com.hbm.lib.ForgeDirection;
import com.hbm.tileentity.TileEntityMachineBase;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.Stack;
import java.util.stream.IntStream;

public class TileEntityOrbitalStation extends TileEntityMachineBase implements IFluidStandardReceiver, ITickable {

	private OrbitalStation station;
	private EntityRideableRocket docked;

	private FluidTankNTM[] tanks;

	public boolean isReserved = false;

	// Client synced state information
	public boolean hasDocked = false;
	public boolean hasRider = false;
	public boolean needsFuel = false;
	public boolean hasFuel = false;

	public float rot;
	public float prevRot;

	public TileEntityOrbitalStation() {
		super(16);
		tanks = new FluidTankNTM[2];
		tanks[0] = new FluidTankNTM(Fluids.HYDROGEN, 16_000);
		tanks[1] = new FluidTankNTM(Fluids.OXYGEN, 16_000);
	}

	@Override
	public String getName() {
		return "container.orbitalStation";
	}

	@Override
	public void update() {
		if(!CelestialBody.inOrbit(world)) return;

		if(!world.isRemote) {
			// Station TEs handle syncing information about the current orbital parameters to players on the station
			station = OrbitalStation.getStationFromPosition(pos.getX(), pos.getZ());

			if(isCore()) station.update(world);
			station.addPort(this);

			if(docked != null && docked.isReusable()) {
				int fillRequirement = getFillRequirement(false); // Use higher fill requirement for tank sizing

				// Update tank sizes based on fuel requirement, preserving existing fills
				for(FluidTankNTM tank : tanks) {
					tank.changeTankSize(Math.max(fillRequirement, tank.getFill()));
				}

				// Connections
				for(DirPos pos : getConPos()) {
					for(FluidTankNTM tank : tanks) {
						if(tank.getTankType() == Fluids.NONE) continue;
						trySubscribe(tank.getTankType(), world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());
					}
				}
			}

			if(docked != null && (docked.isDead || docked.getState() == RocketState.UNDOCKING)) {
				// Drain tanks only upon successful undocking, just in case the pod gets stashed before the player can launch (in multi-player stations)
				if(!docked.isDead && docked.isReusable()) {
					boolean toOrbit = docked.getTarget().inOrbit;
					for(FluidTankNTM tank : tanks) tank.changeTankSize(Math.max(0, tank.getFill() - getFillRequirement(toOrbit)));
				}
				
				undockRocket();
			}

			// if we have enough fuel, transition to ready to launch
			if(docked != null && docked.isReusable()) {
				boolean hasFuel = hasSufficientFuel(docked.getTarget().inOrbit);
				
				if(hasFuel && docked.getState() == RocketState.NEEDSFUEL) {
					docked.setState(docked.navDrive != null ? RocketState.AWAITING : RocketState.LANDED);
				} else if (!hasFuel && docked.getState() != RocketState.NEEDSFUEL) {
					docked.setState(RocketState.NEEDSFUEL);
				}
			}

			hasDocked = docked != null;
			hasRider = hasDocked && docked.getRidingEntity() != null;
			needsFuel = hasDocked && docked.isReusable();
			hasFuel = needsFuel && hasSufficientFuel(docked.getTarget().inOrbit);

			if(hasDocked) isReserved = false;

			this.networkPackNT(OrbitalStation.STATION_SIZE / 2);
		} else {
			if(isCore() && station != null) station.update(world);

			prevRot = rot;
			if(hasDocked) {
				rot += 2.25F;
				if(rot > 90) rot = 90;
			} else {
				rot -= 2.25F;
				if(rot < 0) rot = 0;
			}
		}
	}

	@Override
	public void invalidate() {
		super.invalidate();
		
		if(!world.isRemote && station != null) {
			if(!isCore()) station.removePort(this);
			if(docked != null) docked.dropNDie(null);
		}
	}

	public boolean isCore() {
		return getBlockType() == ModBlocks.orbital_station;
	}

	public DirPos[] getConPos() {
		return new DirPos[] {
			new DirPos(pos.getX() - 1, pos.getY() + 1, pos.getZ() + 3, ForgeDirection.NORTH),
			new DirPos(pos.getX() + 0, pos.getY() + 1, pos.getZ() + 3, ForgeDirection.NORTH),
			new DirPos(pos.getX() + 1, pos.getY() + 1, pos.getZ() + 3, ForgeDirection.NORTH),

			new DirPos(pos.getX() - 1, pos.getY() + 1, pos.getZ() - 3, ForgeDirection.SOUTH),
			new DirPos(pos.getX() + 0, pos.getY() + 1, pos.getZ() - 3, ForgeDirection.SOUTH),
			new DirPos(pos.getX() + 1, pos.getY() + 1, pos.getZ() - 3, ForgeDirection.SOUTH),

			new DirPos(pos.getX() + 3, pos.getY() + 1, pos.getZ() - 1, ForgeDirection.EAST),
			new DirPos(pos.getX() + 3, pos.getY() + 1, pos.getZ() + 0, ForgeDirection.EAST),
			new DirPos(pos.getX() + 3, pos.getY() + 1, pos.getZ() + 1, ForgeDirection.EAST),

			new DirPos(pos.getX() - 3, pos.getY() + 1, pos.getZ() - 1, ForgeDirection.WEST),
			new DirPos(pos.getX() - 3, pos.getY() + 1, pos.getZ() + 0, ForgeDirection.WEST),
			new DirPos(pos.getX() - 3, pos.getY() + 1, pos.getZ() + 1, ForgeDirection.WEST),
		};
	}

	public void enterCapsule(EntityPlayer player) {
		if(docked == null || docked.getRidingEntity() != null) return;
		docked.processInitialInteract(player, player.getActiveHand());
	}

	public void dockRocket(EntityRideableRocket rocket) {
		despawnRocket();

		docked = rocket;
	}

	public void undockRocket() {
		docked = null;
	}

	public void despawnRocket() {
		if(docked != null) {
			Stack<ItemStack> itemsToStuff = new Stack<ItemStack>();

			RocketStruct rocket = docked.getRocket();
			if(rocket.stages.size() > 0) {
				itemsToStuff.push(ItemCustomRocket.build(docked.getRocket(), true));
			} else {
				itemsToStuff.push(new ItemStack(rocket.capsule.part));
			}

			if(docked.navDrive != null) itemsToStuff.push(docked.navDrive.copy());

			for(int i = 0; i < inventory.getSlots(); i++) {
				if(inventory.getStackInSlot(i).isEmpty()) {
					inventory.setStackInSlot(i, itemsToStuff.pop());
					if(itemsToStuff.empty()) break;
				}
			}

			docked.setDead();
			docked = null;
		}
	}

	public void reservePort() {
		isReserved = true;
	}

	public void spawnRocket(ItemStack stack) {
		EntityRideableRocket rocket = new EntityRideableRocket(world, pos.getX() + 0.5F, pos.getY() + 1.5F, pos.getZ() + 0.5F, stack);
		rocket.posY -= rocket.height;
		rocket.setState(rocket.isReusable() ? RocketState.NEEDSFUEL : RocketState.LANDED);
		world.spawnEntity(rocket);

		dockRocket(rocket);
	}

	public boolean hasStoredItems() {
		for(int i = 0; i < inventory.getSlots(); i++){
			if(!inventory.getStackInSlot(i).isEmpty()) return true;
		}

		return false;
	}

	public void giveStoredItems(EntityPlayer player) {
		for(int i = 0; i < inventory.getSlots(); i++) {
			if(!inventory.getStackInSlot(i).isEmpty()) {
				if(!player.inventory.addItemStackToInventory(inventory.getStackInSlot(i).copy())) {
					player.dropItem(inventory.getStackInSlot(i).copy(), false);
				}
				inventory.setStackInSlot(i, ItemStack.EMPTY);
			}
		}
		player.inventoryContainer.detectAndSendChanges();
		markDirty();
	}

	public boolean hasSufficientFuel(boolean toOrbit) {
		int fillRequirement = getFillRequirement(toOrbit);
		for(FluidTankNTM tank : tanks) {
			if(tank.getFill() < fillRequirement) return false;
		}

		return true;
	}

	public int getFillRequirement(boolean toOrbit) {
		if(toOrbit) return 500; // Transferring between stations is much cheaper
		int mass = docked != null ? docked.getRocket().getLaunchMass() : 4_000;
		return SolarSystem.getCostBetween(station.orbiting, station.orbiting, mass, 600_000, 350, false, true);
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemStack, int j) {
		return true;
	}
	
	@Override
	public int[] getAccessibleSlotsFromSide(EnumFacing side) {
		return IntStream.range(0, inventory.getSlots()).toArray();
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);

		if(isCore()) station.serialize(buf);

		buf.writeBoolean(hasDocked);
		buf.writeBoolean(hasRider);
		buf.writeBoolean(needsFuel);
		buf.writeBoolean(hasFuel);

		for(int i = 0; i < inventory.getSlots(); i++) {
			if(!inventory.getStackInSlot(i).isEmpty()) {
				buf.writeShort(Item.getIdFromItem(inventory.getStackInSlot(i).getItem()));
			} else {
				buf.writeShort(-1);
			}
		}

		for(int i = 0; i < tanks.length; i++) tanks[i].serialize(buf);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);

		if(isCore()) OrbitalStation.clientStation = station = OrbitalStation.deserialize(buf);

		hasDocked = buf.readBoolean();
		hasRider = buf.readBoolean();
		needsFuel = buf.readBoolean();
		hasFuel = buf.readBoolean();

		for(int i = 0; i < inventory.getSlots(); i++) {
			short id = buf.readShort();
			if(id > 0) {
				inventory.setStackInSlot(i, new ItemStack(Item.getItemById(id)));
			} else {
				inventory.setStackInSlot(i, ItemStack.EMPTY);
			}
		}

		for(int i = 0; i < tanks.length; i++) tanks[i].deserialize(buf);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		for(int i = 0; i < tanks.length; i++) tanks[i].writeToNBT(nbt, "t" + i);
		return super.writeToNBT(nbt);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		for(int i = 0; i < tanks.length; i++) tanks[i].readFromNBT(nbt, "t" + i);
	}

	AxisAlignedBB bb = null;
	
	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		if(bb == null) {
			bb = new AxisAlignedBB(
				pos.getX() - 2,
				pos.getY() - 2,
				pos.getZ() - 2,
				pos.getX() + 3,
				pos.getY() + 2,
				pos.getZ() + 3
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
