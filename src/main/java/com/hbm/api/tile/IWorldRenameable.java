package com.hbm.api.tile;

import net.minecraft.world.IWorldNameable;

// Why mojang why didn't you make this interface?
public interface IWorldRenameable extends IWorldNameable {
    void setCustomName(String name);
}
