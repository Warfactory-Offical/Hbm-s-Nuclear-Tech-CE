package com.hbm.items.weapon;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;

import java.util.ArrayList;
import java.util.List;

public interface IMetaItemTesr {
    List<IMetaItemTesr> INSTANCES = new ArrayList<>();

    static void redirectModels() {
        INSTANCES.forEach(IMetaItemTesr::redirectModel);
    }

    int getSubitemCount();

    // mlbv: was getName(); renamed to prevent clash with IWorldNameable#getName
    String getResourceLocationAsString();

    default void redirectModel() {
        for (int i = 1; i < getSubitemCount(); i++) {
            Item itemInsance = null;
            if(this instanceof Block block)
                itemInsance = Item.getItemFromBlock(block);
            if(this instanceof Item item)
                itemInsance = item;

            if(itemInsance == null)
                return;
            ModelLoader.setCustomModelResourceLocation(itemInsance, i, new ModelResourceLocation(getResourceLocationAsString(), "inventory"));
        }
    }


}
