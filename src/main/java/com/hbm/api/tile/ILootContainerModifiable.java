package com.hbm.api.tile;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.ILootContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ILootContainerModifiable extends ILootContainer {
    void setLootTable(@NotNull ResourceLocation table, long seed);
    void fillWithLoot(@Nullable EntityPlayer player);
}
