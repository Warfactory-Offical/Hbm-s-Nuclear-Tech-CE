package com.hbm.items.food;

import com.google.common.collect.ImmutableMap;
import com.hbm.config.BombConfig;
import com.hbm.entity.effect.EntityNukeTorex;
import com.hbm.entity.logic.EntityNukeExplosionMK5;
import com.hbm.items.IDynamicModels;
import com.hbm.items.ModItems;
import com.hbm.lib.ModDamageSource;
import com.hbm.lib.RefStrings;
import com.hbm.potion.HbmPotion;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.model.ModelRotation;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

import static com.hbm.items.ItemEnumMulti.ROOT_PATH;

public class ItemFoodBase extends ItemFood implements IDynamicModels {
	String texturePath;

	public ItemFoodBase(int amount, float saturation, boolean isWolfFood, String s){
		super(amount, saturation, isWolfFood);
		this.setTranslationKey(s);
		this.setRegistryName(s);
		this.texturePath = s;
		INSTANCES.add(this);

		ModItems.ALL_ITEMS.add(this);
	}
	public ItemFoodBase(int amount, float saturation, boolean isWolfFood, String s, String texturePath){
		super(amount, saturation, isWolfFood);
		this.setTranslationKey(s);
		this.setRegistryName(s);
		this.texturePath = texturePath;
		INSTANCES.add(this);

		ModItems.ALL_ITEMS.add(this);
	}

	@Override
	public void bakeModel(ModelBakeEvent event) {
		try {
			IModel baseModel = ModelLoaderRegistry.getModel(new ResourceLocation("minecraft", "item/generated"));
			ResourceLocation spriteLoc = new ResourceLocation(RefStrings.MODID, ROOT_PATH + texturePath);
			IModel retexturedModel = baseModel.retexture(
					ImmutableMap.of(
							"layer0", spriteLoc.toString()
					)

			);
			IBakedModel bakedModel = retexturedModel.bake(ModelRotation.X0_Y0, DefaultVertexFormats.ITEM, ModelLoader.defaultTextureGetter());
			ModelResourceLocation bakedModelLocation = new ModelResourceLocation(spriteLoc, "inventory");
			event.getModelRegistry().putObject(bakedModelLocation, bakedModel);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	@Override
	public void registerModel() {
		ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(new ResourceLocation(RefStrings.MODID, ROOT_PATH + texturePath), "inventory"));
	}

	@Override
	public void registerSprite(TextureMap map) {
		map.registerSprite(new ResourceLocation(RefStrings.MODID, ROOT_PATH + texturePath));
	}

	@Override
	public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flagIn){
		if(this == ModItems.bomb_waffle) {
			list.add("60s of Insanity");
			list.add("§4[DEMON CORE]§r");
		}
		if(this == ModItems.cotton_candy) {
			list.add("Gives you a radioactive sugarshock");
			list.add("§b[SPEED V]§r");
		}
		if(this == ModItems.schnitzel_vegan) {
			list.add("Wasteschnitzel is all i need.");
			list.add("§c[STRENGTH X]§r");
		}
		if(this == ModItems.apple_lead) {
			list.add("Lead shields radiation right? So lets eat some of it!");
			list.add("Might have some minor side effects");
			list.add("§a[RAD-X (0.5) for 8min]§r");
		}
		if(this == ModItems.apple_lead1) {
			list.add("Lead shields radiation right? So lets eat a lot it!");
			list.add("Are you sure about that?");
			list.add("§a[RAD-X (1) for 4min]§r");
		}
		if(this == ModItems.apple_lead2) {
			list.add("Lead shields radiation right? So lets eat tons of it!");
			list.add("I will survive it right?");
			list.add("...");
			list.add("right?");
			list.add("§a[RAD-X (4) for 1min]§r");
		}

		super.addInformation(stack, world, list, flagIn);
	}

	@Override
	protected void onFoodEaten(ItemStack stack, World worldIn, EntityPlayer player) {
		if(stack.getItem() == ModItems.bomb_waffle){
			player.setFire(60 * 20);
			player.motionY = -2;
			player.addPotionEffect(new PotionEffect(MobEffects.STRENGTH, 60 * 20, 20));
			player.addPotionEffect(new PotionEffect(MobEffects.RESISTANCE, 60 * 20, 10));
			player.addPotionEffect(new PotionEffect(MobEffects.REGENERATION, 60 * 20, 20));
			player.addPotionEffect(new PotionEffect(MobEffects.FIRE_RESISTANCE, 60 * 20, 0));
			player.addPotionEffect(new PotionEffect(MobEffects.HASTE, 60 * 20, 10));
			player.addPotionEffect(new PotionEffect(MobEffects.SPEED, 60 * 20, 10));
			worldIn.spawnEntity(EntityNukeExplosionMK5.statFac(worldIn, (int)(BombConfig.fatmanRadius * 1.5), player.posX, player.posY, player.posZ).setDetonator(player));
			EntityNukeTorex.statFac(worldIn, player.posX, player.posY, player.posZ, (int)(BombConfig.fatmanRadius * 1.5));
		}
		if(stack.getItem() == ModItems.cotton_candy){
			player.addPotionEffect(new PotionEffect(MobEffects.WITHER, 5 * 20, 0));
			player.addPotionEffect(new PotionEffect(MobEffects.POISON, 15 * 20, 0));
			player.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 25 * 20, 2));
			player.addPotionEffect(new PotionEffect(MobEffects.SPEED, 25 * 20, 5));
			player.addPotionEffect(new PotionEffect(MobEffects.HASTE, 25 * 20, 5));
		}
		if(stack.getItem() == ModItems.schnitzel_vegan){
			player.addPotionEffect(new PotionEffect(MobEffects.BLINDNESS, 10 * 20, 0));
			player.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, 3 * 20, 0));
			player.addPotionEffect(new PotionEffect(MobEffects.HUNGER, 3 * 60 * 20, 4));
			player.addPotionEffect(new PotionEffect(MobEffects.WITHER, 3 * 20, 0));
			player.addPotionEffect(new PotionEffect(MobEffects.STRENGTH, 30 * 20, 10));

			player.setFire(5 * 20);
			player.motionY = 2;
		}
		if(stack.getItem() == ModItems.apple_schrabidium){
			player.addPotionEffect(new PotionEffect(MobEffects.BLINDNESS, 10 * 20, 0));
			player.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, 3 * 20, 0));
			player.addPotionEffect(new PotionEffect(MobEffects.REGENERATION, 600, 4));
			player.addPotionEffect(new PotionEffect(MobEffects.RESISTANCE, 600, 0));
			player.addPotionEffect(new PotionEffect(MobEffects.FIRE_RESISTANCE, 600, 0));
		}
		if(stack.getItem() == ModItems.apple_schrabidium1){
			player.addPotionEffect(new PotionEffect(MobEffects.BLINDNESS, 10 * 20, 0));
			player.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, 3 * 20, 0));
			player.addPotionEffect(new PotionEffect(MobEffects.REGENERATION, 1200, 4));
			player.addPotionEffect(new PotionEffect(MobEffects.RESISTANCE, 1200, 4));
			player.addPotionEffect(new PotionEffect(MobEffects.FIRE_RESISTANCE, 1200, 0));
			player.addPotionEffect(new PotionEffect(MobEffects.STRENGTH, 1200, 4));
			player.addPotionEffect(new PotionEffect(MobEffects.HASTE, 1200, 2));
			player.addPotionEffect(new PotionEffect(MobEffects.SPEED, 1200, 2));
			player.addPotionEffect(new PotionEffect(MobEffects.JUMP_BOOST, 1200, 4));
			player.addPotionEffect(new PotionEffect(MobEffects.HEALTH_BOOST, 1200, 9));
			player.addPotionEffect(new PotionEffect(MobEffects.ABSORPTION, 1200, 4));
			player.addPotionEffect(new PotionEffect(MobEffects.SATURATION, 1200, 9));
		}
		if(stack.getItem() == ModItems.apple_schrabidium2){
			player.addPotionEffect(new PotionEffect(MobEffects.BLINDNESS, 10 * 20, 0));
			player.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, 3 * 20, 0));
			player.addPotionEffect(new PotionEffect(MobEffects.REGENERATION, 2147483647, 4));
			player.addPotionEffect(new PotionEffect(MobEffects.RESISTANCE, 2147483647, 1));
			player.addPotionEffect(new PotionEffect(MobEffects.FIRE_RESISTANCE, 2147483647, 0));
			player.addPotionEffect(new PotionEffect(MobEffects.STRENGTH, 2147483647, 9));
			player.addPotionEffect(new PotionEffect(MobEffects.HASTE, 2147483647, 4));
			player.addPotionEffect(new PotionEffect(MobEffects.SPEED, 2147483647, 3));
			player.addPotionEffect(new PotionEffect(MobEffects.JUMP_BOOST, 2147483647, 4));
			player.addPotionEffect(new PotionEffect(MobEffects.HEALTH_BOOST, 2147483647, 24));
			player.addPotionEffect(new PotionEffect(MobEffects.ABSORPTION, 2147483647, 14));
			player.addPotionEffect(new PotionEffect(MobEffects.SATURATION, 2147483647, 99));
		}
		if(stack.getItem() == ModItems.apple_lead){
			player.addPotionEffect(new PotionEffect(HbmPotion.radx, 8 * 60 * 20, 5));
			player.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 5 * 60 * 20, 0));
			player.addPotionEffect(new PotionEffect(MobEffects.FIRE_RESISTANCE, 30 * 20, 0));
			player.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, 5 * 20, 0));
			player.addPotionEffect(new PotionEffect(HbmPotion.lead, 2 * 20, 0));
			player.attackEntityFrom(ModDamageSource.lead, 1F);
		}
		if(stack.getItem() == ModItems.apple_lead1){
			player.addPotionEffect(new PotionEffect(HbmPotion.radx, 4 * 60 * 20, 10));
			player.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 10 * 60 * 20, 1));
			player.addPotionEffect(new PotionEffect(MobEffects.FIRE_RESISTANCE, 60 * 20, 0));
			player.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, 15 * 20, 0));
			player.addPotionEffect(new PotionEffect(HbmPotion.lead, 4 * 20, 1));
			player.attackEntityFrom(ModDamageSource.lead, 10F);
		}
		if(stack.getItem() == ModItems.apple_lead2){
			player.addPotionEffect(new PotionEffect(HbmPotion.radx, 1 * 60 * 20, 40));
			player.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 15 * 60 * 20, 2));
			player.addPotionEffect(new PotionEffect(MobEffects.FIRE_RESISTANCE, 2 * 60 * 20, 0));
			player.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, 30 * 20, 0));
			player.addPotionEffect(new PotionEffect(HbmPotion.lead, 8 * 20, 2));
			player.attackEntityFrom(ModDamageSource.lead, 24F);
		}
	}

	@Override
	public EnumRarity getRarity(ItemStack stack) {
		if(stack.getItem() == ModItems.apple_schrabidium || stack.getItem() == ModItems.apple_lead){
			return EnumRarity.UNCOMMON;
		}

		if(stack.getItem() == ModItems.apple_schrabidium1 || stack.getItem() == ModItems.apple_lead1){
			return EnumRarity.RARE;
		}

		if(stack.getItem() == ModItems.apple_schrabidium2 || stack.getItem() == ModItems.apple_lead2){
			return EnumRarity.EPIC;
		}

		return EnumRarity.COMMON;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean hasEffect(ItemStack stack) {
		return stack.getItem() == ModItems.apple_schrabidium2 || stack.getItem() == ModItems.apple_lead2;
	}
}
