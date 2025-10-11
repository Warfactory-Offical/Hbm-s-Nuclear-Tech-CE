package com.hbm.hazard.transformer;

import com.hbm.hazard.HazardEntry;
import com.hbm.hazard.HazardRegistry;
import com.hbm.hazard.HazardSystem;
import com.hbm.util.Compat;
import net.minecraft.item.ItemStack;

import java.util.List;

public class HazardTransformerRadiationME extends HazardTransformerBase {

	@Override
	public void transformPre(final ItemStack stack, final List<HazardEntry> entries) { }

	@Override
	public void transformPost(final ItemStack stack, final List<HazardEntry> entries) {
		
		final String name = stack.getItem().getClass().getName();
		if(name.equals("appeng.items.storage.ItemBasicStorageCell") || name.equals("appeng.items.tools.powered.ToolPortableCell")) {
			final List<ItemStack> stacks = Compat.scrapeItemFromME(stack);
            double radiation = 0D;

            for (final ItemStack held : stacks) {
                radiation += HazardSystem.getHazardLevelFromStack(held, HazardRegistry.RADIATION) * held.getCount();
            }

            if (radiation > 0) {
                entries.add(new HazardEntry(HazardRegistry.RADIATION, radiation));
            }
		}
	}
}
