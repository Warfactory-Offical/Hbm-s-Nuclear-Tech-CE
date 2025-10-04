package com.hbm.tileentity.machine;

import com.hbm.entity.missile.EntityMinerRocket;
import com.hbm.handler.WeightedRandomChestContentFrom1710;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.container.ContainerMachineSatDock;
import com.hbm.inventory.gui.GUIMachineSatDock;
import com.hbm.itempool.ItemPool;
import com.hbm.items.ISatChip;
import com.hbm.saveddata.satellites.Satellite;
import com.hbm.saveddata.satellites.SatelliteHorizons;
import com.hbm.saveddata.satellites.SatelliteMiner;
import com.hbm.saveddata.satellites.SatelliteSavedData;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityMachineBase;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import java.util.List;
import java.util.Random;

@AutoRegister
public class TileEntityMachineSatDock extends TileEntityMachineBase implements ITickable, IGUIProvider {

	private static final int[] access = new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14 };

	public TileEntityMachineSatDock(){
		super(16);
	}

	@Override
	public String getDefaultName() {
		return "container.satDock";
	}

	public boolean isUseableByPlayer(EntityPlayer player){
		if(world.getTileEntity(pos) != this) {
			return false;
		} else {
			return player.getDistanceSq(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 64;
		}
	}

	SatelliteSavedData data = null;

	@Override
	public void update(){
		if(!world.isRemote) {

			if(data == null)
				data = (SatelliteSavedData)world.getPerWorldStorage().getOrLoadData(SatelliteSavedData.class, "satellites");

			if(data == null) {
				world.getPerWorldStorage().setData("satellites", new SatelliteSavedData());
				data = (SatelliteSavedData)world.getPerWorldStorage().getOrLoadData(SatelliteSavedData.class, "satellites");
			}
			data.markDirty();

			if(data != null && !inventory.getStackInSlot(15).isEmpty()) {
				int freq = ISatChip.getFreqS(inventory.getStackInSlot(15));

				Satellite sat = data.getSatFromFreq(freq);

				int delay = 10 * 60 * 1000; //10min

				if(sat != null && sat instanceof SatelliteMiner) {

					SatelliteMiner miner = (SatelliteMiner)sat;

					if(miner.lastOp + delay < System.currentTimeMillis()) {

						EntityMinerRocket rocket = new EntityMinerRocket(world);
						rocket.posX = pos.getX() + 0.5;
						rocket.posY = 300;
						rocket.posZ = pos.getZ() + 0.5;
						world.spawnEntity(rocket);
						miner.lastOp = System.currentTimeMillis();
						data.markDirty();
					}
				}
				if(sat != null && sat instanceof SatelliteHorizons) {
					
					SatelliteHorizons gerald = (SatelliteHorizons)sat;

					if(gerald.lastOp + delay < System.currentTimeMillis()) {

						EntityMinerRocket rocket = new EntityMinerRocket(world, (byte)1);
						rocket.posX = pos.getX() + 0.5;
						rocket.posY = 300;
						rocket.posZ = pos.getZ() + 0.5;
						rocket.setRocketType((byte)1);
						world.spawnEntity(rocket);
						gerald.lastOp = System.currentTimeMillis();
						data.markDirty();
					}
				}
			}

			List<Entity> list = world.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(pos.getX() - 0.25 + 0.5, pos.getY() + 0.75, pos.getZ() - 0.25 + 0.5, pos.getX() + 0.25 + 0.5, pos.getY() + 2, pos.getZ() + 0.25 + 0.5));

			for(Entity e : list) {

				if(e instanceof EntityMinerRocket) {

					EntityMinerRocket rocket = (EntityMinerRocket)e;

					if(rocket.getDataManager().get(EntityMinerRocket.TIMER) == 1 && rocket.timer == 50) {
						Satellite sat = data.getSatFromFreq(ISatChip.getFreqS(inventory.getStackInSlot(15)));
						unloadCargo((SatelliteMiner) sat);
					}
				}
			}

			ejectInto(pos.getX() + 2, pos.getY(), pos.getZ());
			ejectInto(pos.getX() - 2, pos.getY(), pos.getZ());
			ejectInto(pos.getX(), pos.getY(), pos.getZ() + 2);
			ejectInto(pos.getX(), pos.getY(), pos.getZ() - 2);
		}
	}

	private static Random rand = new Random();

	private void unloadCargo(SatelliteMiner satellite) {
		int itemAmount = world.rand.nextInt(6) + 10;

		WeightedRandomChestContentFrom1710[] cargo = ItemPool.getPool(satellite.getCargo());

		for(int i = 0; i < itemAmount; i++) {
			addToInv(ItemPool.getStack(cargo, world.rand));
		}
	}

	private void addToInv(ItemStack stack){

		for(int i = 0; i < 15; i++) {

			if(!inventory.getStackInSlot(i).isEmpty() && inventory.getStackInSlot(i).getItem() == stack.getItem() && inventory.getStackInSlot(i).getItemDamage() == stack.getItemDamage() && inventory.getStackInSlot(i).getCount() < inventory.getStackInSlot(i).getMaxStackSize()) {

				inventory.getStackInSlot(i).grow(1);

				return;
			}
		}

		for(int i = 0; i < 15; i++) {

			if(inventory.getStackInSlot(i).isEmpty()) {
				inventory.setStackInSlot(i, new ItemStack(stack.getItem(), 1, stack.getItemDamage()));
				return;
			}
		}
	}

	private void ejectInto(int x, int y, int z){
		BlockPos eject = new BlockPos(x, y, z);
		TileEntity te = world.getTileEntity(eject);

		if(te != null && te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null)) {
			IItemHandler chest = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
			for(int i = 0; i < 15; i++) {
				for(int j = 0; j < chest.getSlots(); j++) {
					ItemStack stack = inventory.getStackInSlot(i);
					if(stack.isEmpty()) break;
					inventory.setStackInSlot(i, chest.insertItem(j, stack, false));
				}
			}
		}
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox(){
		return TileEntity.INFINITE_EXTENT_AABB;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared(){
		return 65536.0D;
	}
	
	@Override
	public int[] getAccessibleSlotsFromSide(EnumFacing e){
		return access;
	}
	
	@Override
	public boolean canExtractItem(int slot, ItemStack itemStack, int amount){
		return slot != 15;
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerMachineSatDock(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUIMachineSatDock(player.inventory, this);
	}
}
