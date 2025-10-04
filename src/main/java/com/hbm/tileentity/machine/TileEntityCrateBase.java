package com.hbm.tileentity.machine;

import com.hbm.api.tile.IWorldRenameable;
import com.hbm.config.MachineConfig;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.hazard.HazardSystem;
import com.hbm.items.ModItems;
import com.hbm.items.tool.ItemKeyPin;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.lib.InventoryHelper;
import com.hbm.lib.Library;
import com.hbm.main.MainRegistry;
import com.hbm.packet.toclient.BufPacket;
import com.hbm.tileentity.IBufPacketReceiver;
import com.hbm.tileentity.IGUIProvider;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.IWorldNameable;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

// mlbv: I tried overriding markDirty to calculate the changes but somehow it always delays by one operation.
// also, implementing ITickable is a bad idea, remove it if you can find a better way.
public abstract class TileEntityCrateBase extends TileEntityLockableBase implements IGUIProvider, IBufPacketReceiver, ITickable, IWorldRenameable {

    private final AtomicBoolean isCheckScheduled = new AtomicBoolean(false);
    public ItemStackHandler inventory;
    public float fillPercentage = 0.0F;
    protected String customName;
    protected String name;
    boolean needsUpdate = false;
    private boolean needsSync = false;

    public TileEntityCrateBase(int scount, String name) {
        inventory = new ItemStackHandler(scount) {
            @Override
            protected void onContentsChanged(int slot) {
                markDirty();
                needsUpdate = true;
            }
        };
        this.name = name;
    }

    public void openInventory(EntityPlayer player) {
        if (!world.isRemote) {
            // mlbv: i know how terrible this looks, change it if you can find a more elegant solution
            PacketThreading.createSendToThreadedPacket(new BufPacket(pos.getX(), pos.getY(), pos.getZ(), this),
                    (EntityPlayerMP) ((WorldServer) world).getEntityFromUuid(player.getPersistentID()));
        }
        player.world.playSound(player.posX, player.posY, player.posZ, HBMSoundHandler.crateOpen, SoundCategory.BLOCKS, 1.0F, 1.0F, false);
    }

    public void closeInventory(EntityPlayer player) {
        player.world.playSound(player.posX, player.posY, player.posZ, HBMSoundHandler.crateClose, SoundCategory.BLOCKS, 1.0F, 1.0F, false);
    }

    @Override
    public void update() {
        if (world.isRemote) return;
        if (needsUpdate && world.getTotalWorldTime() % 5 == 4) {
            scheduleCheck();
            needsUpdate = false;
        }
        if (needsSync) {
            networkPackNT(10);
            needsSync = false;
        }
    }

    void scheduleCheck() {
        if (this.isCheckScheduled.compareAndSet(false, true)) {
            CompletableFuture.supplyAsync(this::getSize).whenComplete((currentSize, error) -> {
                try {
                    if (error != null) {
                        MainRegistry.logger.error("Error checking crate size at {}", pos, error);
                        return;
                    }
                    if (currentSize > MachineConfig.crateByteSize * 2L) {
                        ((WorldServer) world).addScheduledTask(this::ejectAndClearInventory);
                    } else {
                        this.fillPercentage = (float) currentSize / MachineConfig.crateByteSize * 100F;
                    }
                } finally {
                    this.isCheckScheduled.set(false);
                    needsSync = true;
                }
            });
        }
    }

    private void ejectAndClearInventory() {
        InventoryHelper.dropInventoryItems(world, pos, this);
        for (int i = 0; i < inventory.getSlots(); i++) {
            inventory.setStackInSlot(i, ItemStack.EMPTY);
        }
        this.fillPercentage = 0.0F;
        super.markDirty();
        MainRegistry.logger.debug("Crate at {} was oversized and has been emptied to prevent data corruption.", pos);
    }

    public long getSize() {
        NBTTagCompound nbt = new NBTTagCompound();
        float rads = 0;
        for (int i = 0; i < inventory.getSlots(); i++) {

            ItemStack stack = inventory.getStackInSlot(i);
            if (stack.isEmpty()) continue;

            rads += HazardSystem.getTotalRadsFromStack(stack) * stack.getCount();
            NBTTagCompound slot = new NBTTagCompound();
            stack.writeToNBT(slot);
            nbt.setTag("slot" + i, slot);
        }
        if (rads > 0) {
            nbt.setFloat("cRads", rads);
        }
        if (this.isLocked()) {
            nbt.setInteger("lock", this.getPins());
            nbt.setDouble("lockMod", this.getMod());
        }
        return Library.getCompressedNbtSize(nbt);
    }

    @Override
    public boolean canAccess(EntityPlayer player) {

        if (!this.isLocked() || player == null) {
            return true;
        } else {
            ItemStack stack = player.getHeldItemMainhand();

            if (stack.getItem() instanceof ItemKeyPin && ItemKeyPin.getPins(stack) == this.lock) {
                world.playSound(null, player.posX, player.posY, player.posZ, HBMSoundHandler.lockOpen, SoundCategory.BLOCKS, 1.0F, 1.0F);
                return true;
            }

            if (stack.getItem() == ModItems.key_red) {
                world.playSound(null, player.posX, player.posY, player.posZ, HBMSoundHandler.lockOpen, SoundCategory.BLOCKS, 1.0F, 1.0F);
                return true;
            }

            return this.tryPick(player);
        }
    }

    @Override
    public String getName() {
        return this.hasCustomName() ? this.customName : name;
    }

    @Override
    public boolean hasCustomName() {
        return this.customName != null && !this.customName.isEmpty();
    }

    @Override
    public void setCustomName(String name) {
        this.customName = name;
    }

    public boolean isUseableByPlayer(EntityPlayer player) {
        if (world.getTileEntity(pos) != this) {
            return false;
        } else {
            return player.getDistanceSq(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 64;
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        if (compound.hasKey("inventory")) {
            inventory.deserializeNBT(compound.getCompoundTag("inventory"));
        }
        fillPercentage = compound.getFloat("fill");
    }

    @Override
    public @NotNull NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setTag("inventory", inventory.serializeNBT());
        compound.setFloat("fill", fillPercentage);
        return compound;
    }

    public void networkPackNT(int range) {
        if (!world.isRemote)
            PacketThreading.createAllAroundThreadedPacket(new BufPacket(pos.getX(), pos.getY(), pos.getZ(), this),
                    new NetworkRegistry.TargetPoint(this.world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), range));
    }

    public void serialize(ByteBuf buf) {
        buf.writeFloat(this.fillPercentage);
    }

    public void deserialize(ByteBuf buf) {
        this.fillPercentage = buf.readFloat();
    }

    @Override
    public boolean hasCapability(@NotNull Capability<?> capability, EnumFacing facing) {
        return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && !isLocked() || super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(@NotNull Capability<T> capability, EnumFacing facing) {
        return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && !isLocked() ?
                CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(inventory) : super.getCapability(capability, facing);
    }
}