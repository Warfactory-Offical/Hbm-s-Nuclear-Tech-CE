package com.hbm.items.armor;

import com.hbm.items.ModItems.ArmorSets;
import com.hbm.items.gear.ArmorFSB;
import com.hbm.lib.ModDamageSource;
import com.hbm.render.model.ModelArmorBJ;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ArmorBJ extends ArmorFSBPowered {

	public ArmorBJ(ArmorMaterial material, int layer, EntityEquipmentSlot slot, String texture, long maxPower, long chargeRate, long consumption, long drain, String s) {
		super(material, layer, slot, texture, maxPower, chargeRate, consumption, drain, s);
	}

	@SideOnly(Side.CLIENT)
	ModelArmorBJ[] models;
	
	@Override
	@SideOnly(Side.CLIENT)
	public ModelBiped getArmorModel(EntityLivingBase entityLiving, ItemStack itemStack, EntityEquipmentSlot armorSlot, ModelBiped _default) {
		if(models == null) {
			models = new ModelArmorBJ[4];

			for(int i = 0; i < 4; i++)
				models[i] = new ModelArmorBJ(i);
		}
		return models[3-armorSlot.getIndex()];
	}
	
	@Override
	public void onArmorTick(World world, EntityPlayer player, ItemStack itemStack) {
		super.onArmorTick(world, player, itemStack);

    	if(this == ArmorSets.bj_helmet && ArmorFSB.hasFSBArmorIgnoreCharge(player) && !ArmorFSB.hasFSBArmor(player)) {

    		ItemStack helmet = player.inventory.armorInventory.get(3);

    		if(!player.inventory.addItemStackToInventory(helmet))
    			player.dropItem(helmet, false);

    		player.inventory.armorInventory.set(3, ItemStack.EMPTY);

    		player.attackEntityFrom(ModDamageSource.lunar, 1000);
    	}
	}
}