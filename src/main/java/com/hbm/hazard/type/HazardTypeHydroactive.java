package com.hbm.hazard.type;

import com.hbm.config.RadiationConfig;
import com.hbm.hazard.modifier.HazardModifier;
import com.hbm.util.I18nUtil;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class HazardTypeHydroactive extends HazardTypeBase {

	@Override
    public void onUpdate(final EntityLivingBase target, final double level, final ItemStack stack) {

		if(RadiationConfig.disableHydro) return;

		final boolean playerIsWet = target.isWet() || (target.world.isRaining() && target.world.canSeeSky(target.getPosition()));

        if (playerIsWet && stack.getCount() > 0) {
            stack.setCount(0);
            target.world.newExplosion(null, target.posX, target.posY + target.getEyeHeight() - target.getYOffset(), target.posZ, (float) level, false, true);
        }
    }

    @Override
    public void updateEntity(final EntityItem item, final double level) {
		
		if(RadiationConfig.disableHydro)
			return;

        if (item.isWet()) {
            item.setDead();
            item.world.newExplosion(null, item.posX, item.posY + item.height * 0.5, item.posZ, (float) level, false, true);
        }
    }

	@Override
	@SideOnly(Side.CLIENT)
    public void addHazardInformation(final EntityPlayer player, final List<String> list, final double level, final ItemStack stack, final List<HazardModifier> modifiers) {
        list.add(TextFormatting.RED + "[" + I18nUtil.resolveKey("trait.hydro") + "]");
    }
}
