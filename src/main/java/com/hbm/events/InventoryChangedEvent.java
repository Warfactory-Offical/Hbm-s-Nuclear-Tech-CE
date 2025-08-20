package com.hbm.events;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Event;
import org.jetbrains.annotations.NotNull;

/**
 * An event fired whenever a player's inventory changes.
 * <p>
 * This event is triggered through the {@link com.hbm.core.InventoryPlayerTransformer InventoryPlayerTransformer}, which is invoked by the {@link com.hbm.core.InventoryHook InventoryHook}.
 * </p>
 * <p>
 * The {@link #getType()} indicates the nature of the inventory change.
 * <br>
 * The {@link #getPlayer()} references the player whose inventory was modified.
 * <br>
 * For {@link EventType#DELTA DELTA} events:
 * <ul>
 *   <li>{@link #getOldStack()} holds the {@link ItemStack} present in the slot prior to the change.</li>
 *   <li>{@link #getNewStack()} holds the {@link ItemStack} present in the slot after the change.</li>
 *   <li>{@link #getSlotIndex()} specifies the index of the modified slot.</li>
 * </ul>
 * For {@link EventType#COMPLEX COMPLEX} events:
 * <ul>
 *   <li>{@link #getOldStack()} and {@link #getNewStack()} return {@link ItemStack#EMPTY}</li>
 *   <li>{@link #getSlotIndex()} returns -1</li>
 * </ul>
 * </p>
 * <p>
 * This event is posted on the {@link MinecraftForge#EVENT_BUS}.
 * </p>
 */
public class InventoryChangedEvent extends Event {

    private final EventType type;
    private final EntityPlayer player;
    private final ItemStack oldStack;
    private final ItemStack newStack;
    private final int slotIndex;
    private final boolean serverSide;

    public InventoryChangedEvent(EntityPlayer player, boolean serverSide) {
        this.type = EventType.COMPLEX;
        this.slotIndex = -1;
        this.player = player;
        this.oldStack = ItemStack.EMPTY;
        this.newStack = ItemStack.EMPTY;
        this.serverSide = serverSide;
    }

    public InventoryChangedEvent(EntityPlayer player, int slotIndex, @NotNull ItemStack oldStack, @NotNull ItemStack newStack, boolean serverSide) {
        this.type = EventType.DELTA;
        this.slotIndex = slotIndex;
        this.player = player;
        this.oldStack = oldStack;
        this.newStack = newStack;
        this.serverSide = serverSide;
    }

    public EventType getType() {
        return type;
    }

    public EntityPlayer getPlayer() {
        return player;
    }

    /**
     * @return The ItemStack that was in the slot *before* the change. Never null.<br>
     * Use {@link ItemStack#copy()} if you don't want to modify the returned stack.<br>
     * Returns {@link ItemStack#EMPTY} for {@link EventType#COMPLEX} events.
     */
    @NotNull
    public ItemStack getOldStack() {
        return oldStack;
    }

    /**
     * @return The ItemStack that is in the slot *after* the change. Never null.<br>
     * Use {@link ItemStack#copy()} if you don't want to modify the returned stack.<br>
     * Returns {@link ItemStack#EMPTY} for {@link EventType#COMPLEX} events.
     */
    @NotNull
    public ItemStack getNewStack() {
        return newStack;
    }

    /**
     * @return The raw index of the slot that was changed. Only applicable to {@link EventType#DELTA} events.
     */
    public int getRawSlotIndex() {
        return slotIndex;
    }

    /**
     * @return The index of the slot that was changed, normalized to server index. Only applicable to {@link EventType#DELTA} events.<br>
     * On server:
     * <ul>
     *   <li>0-4 is reserved for survival mode inventory crafting.</li>
     *   <li>armor = 5-8</li>
     *   <li>inventory = 9-35</li>
     *   <li>hotbar = 36-44</li>
     *   <li>offhand = 45</li>
     * </ul>
     */
    public int getSlotIndex() {
        if (serverSide) return slotIndex;
        if (slotIndex >= 0 && slotIndex <= 8) return slotIndex + 36; // hotbar
        if (slotIndex <= 35) return slotIndex; // inventory
        if (slotIndex <= 39) return 44 - slotIndex; // armor
        if (slotIndex == 40) return 45; // offhand
        return slotIndex;
    }

    public enum EventType {
        /**
         * A single slot was changed. Use {@link #getOldStack()} and {@link #getNewStack()}.
         */
        DELTA,
        /**
         * A complex or unknown change occurred. A full inventory rescan is recommended.
         */
        COMPLEX
    }
}