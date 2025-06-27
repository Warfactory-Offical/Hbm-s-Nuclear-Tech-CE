package com.hbm.tileentity.bomb;

import com.hbm.inventory.fluid.Fluids;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemBreedingRod;
import com.hbm.items.special.ItemCell;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

public class TileEntityNukePrototype extends TileEntity {

	public ItemStackHandler inventory;
	private String customName;
	
	public TileEntityNukePrototype() {
		inventory = new ItemStackHandler(14){
			@Override
			protected void onContentsChanged(int slot) {
				markDirty();
				super.onContentsChanged(slot);
			}
		};
	}
	
	public String getInventoryName() {
		return this.hasCustomInventoryName() ? this.customName : "container.nukePrototype";
	}

	public boolean hasCustomInventoryName() {
		return this.customName != null && this.customName.length() > 0;
	}
	
	public void setCustomName(String name) {
		this.customName = name;
	}
	
	public boolean isUseableByPlayer(EntityPlayer player) {
		if(world.getTileEntity(pos) != this)
		{
			return false;
		}else{
			return player.getDistanceSq(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <=64;
		}
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		if(compound.hasKey("inventory"))
			inventory.deserializeNBT(compound.getCompoundTag("inventory"));
		super.readFromNBT(compound);
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setTag("inventory", inventory.serializeNBT());
		return super.writeToNBT(compound);
	}
	
	public boolean isReady() {
		
			if(ItemCell.isFullCell(inventory.getStackInSlot(0), Fluids.SAS3) &&
			ItemCell.isFullCell(inventory.getStackInSlot(1), Fluids.SAS3) &&
			inventory.getStackInSlot(2).isItemEqual(new ItemStack(ModItems.rod_quad, 1, ItemBreedingRod.BreedingRodType.URANIUM.ordinal())) &&
			inventory.getStackInSlot(3).isItemEqual(new ItemStack(ModItems.rod_quad, 1, ItemBreedingRod.BreedingRodType.URANIUM.ordinal())) &&
			inventory.getStackInSlot(4).isItemEqual(new ItemStack(ModItems.rod_quad, 1, ItemBreedingRod.BreedingRodType.LEAD.ordinal())) &&
			inventory.getStackInSlot(5).isItemEqual(new ItemStack(ModItems.rod_quad, 1, ItemBreedingRod.BreedingRodType.LEAD.ordinal())) &&
			inventory.getStackInSlot(6).isItemEqual(new ItemStack(ModItems.rod_quad, 1, ItemBreedingRod.BreedingRodType.NP237.ordinal())) &&
			inventory.getStackInSlot(7).isItemEqual(new ItemStack(ModItems.rod_quad, 1, ItemBreedingRod.BreedingRodType.NP237.ordinal())) &&
			inventory.getStackInSlot(8).isItemEqual(new ItemStack(ModItems.rod_quad, 1, ItemBreedingRod.BreedingRodType.LEAD.ordinal())) &&
			inventory.getStackInSlot(9).isItemEqual(new ItemStack(ModItems.rod_quad, 1, ItemBreedingRod.BreedingRodType.LEAD.ordinal())) &&
			inventory.getStackInSlot(10).isItemEqual(new ItemStack(ModItems.rod_quad, 1, ItemBreedingRod.BreedingRodType.URANIUM.ordinal())) &&
			inventory.getStackInSlot(11).isItemEqual(new ItemStack(ModItems.rod_quad, 1, ItemBreedingRod.BreedingRodType.URANIUM.ordinal())) &&
			ItemCell.isFullCell(inventory.getStackInSlot(12), Fluids.SAS3) &&
			ItemCell.isFullCell(inventory.getStackInSlot(13), Fluids.SAS3))
			{
				return true;
			}
		
		return false;
	}
	
	public void clearSlots() {
		for(int i = 0; i < inventory.getSlots(); i++)
		{
			inventory.setStackInSlot(i, ItemStack.EMPTY);
		}
	}
	
	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return TileEntity.INFINITE_EXTENT_AABB;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared()
	{
		return 65536.0D;
	}
	
	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || super.hasCapability(capability, facing);
	}
	
	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY ? CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(inventory) :super.getCapability(capability, facing);
	}
}
