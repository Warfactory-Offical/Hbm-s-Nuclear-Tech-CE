package com.hbm.tileentity.machine;

import com.hbm.api.block.IToolable.ToolType;
import com.hbm.handler.ArmorUtil;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.items.ModItems;
import com.hbm.items.tool.ItemKeyPin;
import com.hbm.items.tool.ItemTooling;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.main.MainRegistry;
import com.hbm.packet.toclient.BufPacket;
import com.hbm.tileentity.IBufPacketReceiver;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.fml.common.network.NetworkRegistry;

public class TileEntityLockableBase extends TileEntity implements IBufPacketReceiver {
	protected int lock;
	private boolean isLocked = false;
	protected double lockMod = 0.1D;
	private ByteBuf lastPackedBuf;

	public boolean isLocked() {
		return isLocked;
	}

	public boolean canLock(EntityPlayer player, EnumHand hand, EnumFacing facing) {
		return true;
	}

	public void lock() {
		
		if(lock == 0) {
			MainRegistry.logger.error("A block has been set to locked state before setting pins, this should not happen and may cause errors! " + this.toString());
		}
		if(isLocked == false)
			markDirty();
		isLocked = true;
	}
	
	public void setPins(int pins) {
		if(lock != pins)
			markDirty();
		lock = pins;
	}
	
	public int getPins() {
		return lock;
	}
	
	public void setMod(double mod) {
		if(lockMod != mod)
			markDirty();
		lockMod = mod;
	}
	
	public double getMod() {
		return lockMod;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		lock = compound.getInteger("lock");
		isLocked = compound.getBoolean("isLocked");
		lockMod = compound.getDouble("lockMod");
		super.readFromNBT(compound);
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setInteger("lock", lock);
		compound.setBoolean("isLocked", isLocked);
		compound.setDouble("lockMod", lockMod);
		return super.writeToNBT(compound);
	}
	
	public boolean canAccess(EntityPlayer player) {
		if(!isLocked) {
			return true;
		}
		if(player == null) { //!isLocked ||
			return false;
		}

		ItemStack stack = player.getHeldItemMainhand();

		if(stack.getItem() instanceof ItemKeyPin && ItemKeyPin.getPins(stack) == this.lock) {
			world.playSound(null, player.posX, player.posY, player.posZ, HBMSoundHandler.lockOpen, SoundCategory.BLOCKS, 1.0F, 1.0F);
			return true;
		}

		if(stack.getItem() == ModItems.key_red) {
			world.playSound(null, player.posX, player.posY, player.posZ, HBMSoundHandler.lockOpen, SoundCategory.BLOCKS, 1.0F, 1.0F);
			return true;
		}
		return tryPick(player);
	}

	public static int hasLockPickTools(EntityPlayer player){
		ItemStack stackR = player.getHeldItemMainhand();
		ItemStack stackL = player.getHeldItemOffhand();
		if(stackR == null || stackL == null) return -1;
		if(stackR.getItem() == ModItems.pin){
			if(stackL.getItem() instanceof ItemTooling && ((ItemTooling)stackL.getItem()).getType() == ToolType.SCREWDRIVER){
				return 1;
			}
		} else if(stackL.getItem() == ModItems.pin){
			if(stackR.getItem() instanceof ItemTooling && ((ItemTooling)stackR.getItem()).getType() == ToolType.SCREWDRIVER){
				return 2;
			}
		}
		return -1;
	}	
	
	public boolean tryPick(EntityPlayer player) {

		boolean canPick = false;
		int hand = hasLockPickTools(player);
		double chanceOfSuccess = this.lockMod * 100;
		
		if(hand == 1) {
			player.getHeldItemMainhand().shrink(1);
			canPick = true;
		} else if(hand == 2){
			player.getHeldItemOffhand().shrink(1);
			canPick = true;
		}
		
		if(canPick) {
			
			if(ArmorUtil.checkArmorPiece(player, ModItems.jackt, 2) || ArmorUtil.checkArmorPiece(player, ModItems.jackt2, 2))
				chanceOfSuccess *= 100D;
			
			double rand = player.world.rand.nextDouble() * 100;
			
			if(chanceOfSuccess > rand) {
        		world.playSound(null, player.posX, player.posY, player.posZ, HBMSoundHandler.pinUnlock, SoundCategory.BLOCKS, 1.0F, 1.0F);
				return true;
			}

    		world.playSound(null, player.posX, player.posY, player.posZ, HBMSoundHandler.pinBreak, SoundCategory.BLOCKS, 1.0F, 0.8F + player.world.rand.nextFloat() * 0.2F);
		}
		
		return false;
	}

	@Override
	public void serialize(ByteBuf buf) {
	}

	@Override
	public void deserialize(ByteBuf buf) {
	}

	public void networkPackNT(int range) {
		if (world.isRemote) return;
		BufPacket packet = new BufPacket(pos.getX(), pos.getY(), pos.getZ(), this);
		ByteBuf preBuf = packet.getCompiledBuffer();
		if(preBuf.equals(lastPackedBuf) && this.world.getTotalWorldTime() % 20 != 0) return;
		this.lastPackedBuf = preBuf.copy();
		PacketThreading.createAllAroundThreadedPacket(new BufPacket(pos.getX(), pos.getY(), pos.getZ(), this), new NetworkRegistry.TargetPoint(this.world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), range));
	}
}
