package com.hbm.items.armor;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.hbm.handler.ArmorModHandler;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.items.ModItems.ArmorSets;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.render.model.ModelArmorDiesel;

import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class ArmorDiesel extends ArmorFSBFueled {

	public ArmorDiesel(ArmorMaterial material, int layer, EntityEquipmentSlot slot, String texture, FluidType fuelType, int maxFuel, int fillRate, int consumption, int drain, String s) {
		super(material, layer, slot, texture, fuelType, maxFuel, fillRate, consumption, drain, s);
	}

	@Override
	public Multimap<String, AttributeModifier> getItemAttributeModifiers(EntityEquipmentSlot slot) {
		Multimap<String, AttributeModifier> multimap = HashMultimap.create();

		if (slot == this.armorType) {
			multimap.put(
					SharedMonsterAttributes.KNOCKBACK_RESISTANCE.getName(),
					new AttributeModifier(
							ArmorModHandler.fixedUUIDs[this.armorType.getIndex()],
							"Armor modifier",
							-0.025D,
							1
					)
			);
		}

		return multimap;
	}

	@SideOnly(Side.CLIENT)
	ModelArmorDiesel[] models;

	@Override
	@SideOnly(Side.CLIENT)
	public ModelBiped getArmorModel(EntityLivingBase entityLiving, ItemStack itemStack, EntityEquipmentSlot armorSlot, ModelBiped _default) {

		if(models == null) {
			models = new ModelArmorDiesel[4];

			for(int i = 0; i < 4; i++)
				models[i] = new ModelArmorDiesel(i);
		}

		return models[3 - armorSlot.getIndex()];
	}

	@Override
	public void onArmorTick(World world, EntityPlayer player, ItemStack stack) {
		super.onArmorTick(world, player, stack);

		if(!world.isRemote && this == ArmorSets.dieselsuit_legs && this.hasFSBArmor(player) && world.getTotalWorldTime() % 3 == 0) {
			NBTTagCompound data = new NBTTagCompound();
			data.setString("type", "bnuuy");
			data.setInteger("player", player.getEntityId());
			PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(data, player.posX, player.posY, player.posZ), new TargetPoint(world.provider.getDimension(), player.posX, player.posY, player.posZ, 100));
		}
	}

	@Override
	public boolean acceptsFluid(FluidType type, ItemStack stack) {
		return type == Fluids.DIESEL || type == Fluids.DIESEL_CRACK;
	}
}
