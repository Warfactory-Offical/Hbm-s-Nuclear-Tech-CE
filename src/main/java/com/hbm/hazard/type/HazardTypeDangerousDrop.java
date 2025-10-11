package com.hbm.hazard.type;

import com.hbm.hazard.modifier.HazardModifier;
import com.hbm.util.I18nUtil;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;

import java.util.List;

public class HazardTypeDangerousDrop extends HazardTypeBase {
    IDropPayload payload;

    public HazardTypeDangerousDrop(IDropPayload payload) {
        this.payload = payload;
    }


    @Override
    public void onUpdate(EntityLivingBase target, double level, ItemStack stack) {
        //Nothing
    }

    @Override
    public void updateEntity(EntityItem item, double level) {
        payload.updateEntity(item, level);
    }

    @Override
    public void addHazardInformation(EntityPlayer player, List list, double level, ItemStack stack, List<HazardModifier> modifiers) {
        list.add(TextFormatting.RED + "[" + I18nUtil.resolveKey("trait.drop") + "]");
    }

    @FunctionalInterface
    public interface IDropPayload {
        void updateEntity(EntityItem item, double level);
    }
}
