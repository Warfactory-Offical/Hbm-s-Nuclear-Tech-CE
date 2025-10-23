package com.hbm.items.gear;

import com.google.common.collect.Multimap;
import com.hbm.config.BombConfig;
import com.hbm.entity.effect.EntityNukeTorex;
import com.hbm.entity.logic.EntityNukeExplosionMK5;
import com.hbm.entity.projectile.EntityRubble;
import com.hbm.handler.ArmorUtil;
import com.hbm.items.ModItems;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.main.AdvancementManager;
import com.hbm.main.MainRegistry;
import com.hbm.potion.HbmPotion;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import java.util.List;
import java.util.Random;
import java.util.UUID;

public class WeaponSpecial extends ItemSword {

	Random rand = new Random();
	
	public WeaponSpecial(ToolMaterial material, String s) {
		super(material);
		this.setTranslationKey(s);
		this.setRegistryName(s);
		this.setCreativeTab(MainRegistry.controlTab);
		ModItems.ALL_ITEMS.add(this);
	}
	
	@Override
	public EnumRarity getRarity(ItemStack stack) {
		if(this == ModItems.schrabidium_hammer) {
			return EnumRarity.RARE;
		}
		if(this == ModItems.ullapool_caber) {
			return EnumRarity.UNCOMMON;
		}
		if(this == ModItems.shimmer_sledge || this == ModItems.shimmer_axe) {
			return EnumRarity.EPIC;
		}
		return EnumRarity.COMMON;
	}
	
	@Override
	public boolean hitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker) {
		World world = target.world;
		if(this == ModItems.schrabidium_hammer) {
			if (!world.isRemote)
        	{
				target.setHealth(0.0F);
        	}
        	world.playSound(null, target.posX, target.posY, target.posZ, HBMSoundHandler.bonk, SoundCategory.PLAYERS, 3.0F, 0.1F);
		}
		if(this == ModItems.bottle_opener) {
			if (!target.world.isRemote)
        	{
				int i = rand.nextInt(7);
				if(i == 0)
					target.addPotionEffect(new PotionEffect(MobEffects.BLINDNESS, 5 * 60 * 20, 0));
				if(i == 1)
					target.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 5 * 60 * 20, 2));
				if(i == 2)
					target.addPotionEffect(new PotionEffect(MobEffects.MINING_FATIGUE, 5 * 60 * 20, 2));
				if(i == 3)
					target.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, 1 * 60 * 20, 0));
        	}
        	target.playSound(SoundEvents.BLOCK_ANVIL_LAND, 3.0F, 1.0F);
		}
		if(this == ModItems.shimmer_sledge) {
			Vec3d vec = attacker.getLookVec();
			double dX = vec.x * 5;
			double dY = vec.y * 5;
			double dZ = vec.z * 5;

			target.motionX += dX;
			target.motionY += dY;
			target.motionZ += dZ;
        	world.playSound(null, target.posX, target.posY, target.posZ, HBMSoundHandler.bang, SoundCategory.PLAYERS, 3.0F, 1.F);
		}
    	
		if(this == ModItems.shimmer_axe) {
			
			target.setHealth(target.getHealth() / 2);
			
			world.playSound(null, target.posX, target.posY, target.posZ, HBMSoundHandler.slice, SoundCategory.PLAYERS, 3.0F, 1.F);
		}
		if(this == ModItems.wrench) {

			Vec3d vec = attacker.getLookVec();
			
			double dX = vec.x * 0.5;
			double dY = vec.y * 0.5;
			double dZ = vec.z * 0.5;

			target.motionX += dX;
			target.motionY += dY;
			target.motionZ += dZ;
        	world.playSound(null, target.posX, target.posY, target.posZ, SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.PLAYERS, 3.0F, 0.75F);
		}
		if(this == ModItems.ullapool_caber) {
			if (!world.isRemote)
        	{
				world.createExplosion(null, target.posX, target.posY, target.posZ, 7.5F, true);
        	}
			
			stack.damageItem(505, attacker);
		}
		if(this == ModItems.memespoon) {

			if(attacker.fallDistance >= 2) {
				world.playSound(null, target.posX, target.posY, target.posZ, HBMSoundHandler.bang, SoundCategory.PLAYERS, 3.0F, 0.75F);
				target.setHealth(0);
			}
			
			if(!(attacker instanceof EntityPlayer))
				return false;
			
			if(attacker.fallDistance >= 20 && !((EntityPlayer)attacker).capabilities.isCreativeMode) {
				if(!world.isRemote) {
					world.spawnEntity(EntityNukeExplosionMK5.statFac(world, 100, target.posX, target.posY, target.posZ).setDetonator(attacker));
					if(BombConfig.enableNukeClouds) {
						EntityNukeTorex.statFac(world, target.posX, target.posY, target.posZ, 100);
					}
				}
			}
		}
		if(this == ModItems.stopsign || this == ModItems.sopsign)
        	world.playSound(null, target.posX, target.posY, target.posZ, HBMSoundHandler.stop, SoundCategory.PLAYERS, 1.0F, 1.0F);
		if(this == ModItems.wood_gavel) {
        	world.playSound(null, target.posX, target.posY, target.posZ, HBMSoundHandler.whack, SoundCategory.PLAYERS, 3.0F, 1.F);
		}

		if(this == ModItems.lead_gavel) {
			world.playSound(null, target.posX, target.posY, target.posZ, HBMSoundHandler.whack, SoundCategory.PLAYERS, 3.0F, 1.F);

			target.addPotionEffect(new PotionEffect(HbmPotion.lead, 15 * 20, 4));
		}

		if(this == ModItems.diamond_gavel) {

			float ded = target.getMaxHealth() / 3;
			target.setHealth(target.getHealth() - ded);

			world.playSound(null, target.posX, target.posY, target.posZ, HBMSoundHandler.whack, SoundCategory.PLAYERS, 3.0F, 1.F);
		}

		return false;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if(this == ModItems.shimmer_sledge) {
			if(world.getBlockState(pos).getBlock() != Blocks.AIR && world.getBlockState(pos).getBlock().getExplosionResistance(null) < 6000) {
				
				EntityRubble rubble = new EntityRubble(world);
				rubble.posX = pos.getX() + 0.5F;
				rubble.posY = pos.getY();
				rubble.posZ = pos.getZ() + 0.5F;
				
				rubble.setMetaBasedOnBlock(world.getBlockState(pos).getBlock(), (world.getBlockState(pos).getBlock().getMetaFromState(world.getBlockState(pos))));
				
				Vec3d vec = player.getLookVec();
				double dX = vec.x * 5;
				double dY = vec.y * 5;
				double dZ = vec.z * 5;

				rubble.motionX += dX;
				rubble.motionY += dY;
				rubble.motionZ += dZ;
	        	world.playSound(null, rubble.posX, rubble.posY, rubble.posZ, HBMSoundHandler.bang, SoundCategory.PLAYERS, 3.0F, 1.0F);
				
	        	if(!world.isRemote) {
	        		
	        		world.spawnEntity(rubble);
					world.destroyBlock(pos, false);
	        	}
			}
			return EnumActionResult.SUCCESS;
		}
		
		if(this == ModItems.shimmer_axe) {

        	world.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, HBMSoundHandler.kaping, SoundCategory.PLAYERS, 3.0F, 1.0F);

        	if(!world.isRemote) {
				if(world.getBlockState(pos).getBlock() != Blocks.AIR && world.getBlockState(pos).getBlock().getExplosionResistance(null) < 6000) {
					world.destroyBlock(pos, false);
				}
				if(world.getBlockState(pos.up()).getBlock() != Blocks.AIR && world.getBlockState(pos.up()).getBlock().getExplosionResistance(null) < 6000) {
					world.destroyBlock(pos.up(), false);
				}
				if(world.getBlockState(pos.down()).getBlock() != Blocks.AIR && world.getBlockState(pos.down()).getBlock().getExplosionResistance(null) < 6000) {
					world.destroyBlock(pos.down(), false);
				}
        	}
			return EnumActionResult.SUCCESS;
		}
		return super.onItemUse(player, world, pos, hand, facing, hitX, hitY, hitZ);
	}
	
	@Override
	public Multimap<String, AttributeModifier> getAttributeModifiers(EntityEquipmentSlot slot, ItemStack stack) {
		Multimap<String, AttributeModifier> map = super.getAttributeModifiers(slot, stack);
		if(slot == EntityEquipmentSlot.MAINHAND || slot == EntityEquipmentSlot.OFFHAND){
			if(this == ModItems.schrabidium_hammer) {
				map.put(SharedMonsterAttributes.MOVEMENT_SPEED.getName(), new AttributeModifier(UUID.fromString("91AEAA56-376B-4498-935B-2F7F68070635"), "Weapon modifier", -0.5, 1));
			}
			if(this == ModItems.wrench || this == ModItems.wrench_flipped) {
				map.put(SharedMonsterAttributes.MOVEMENT_SPEED.getName(), new AttributeModifier(UUID.fromString("91AEAA56-376B-4498-935B-2F7F68070635"), "Weapon modifier", -0.1, 1));
			}
		}
		return map;
	}
	
	@Override
	public void onUpdate(ItemStack stack, World worldIn, Entity entity, int itemSlot, boolean isSelected) {
		if(entity instanceof EntityPlayer) {
    		if(ArmorUtil.checkForFiend((EntityPlayer) entity)) {
    			AdvancementManager.grantAchievement(((EntityPlayer) entity), AdvancementManager.achFiend);
    		} else if(ArmorUtil.checkForFiend2((EntityPlayer) entity)) {
        		AdvancementManager.grantAchievement(((EntityPlayer) entity), AdvancementManager.achFiend2);
        	}
    	}
	}
	
	@Override
	public void addInformation(ItemStack stack, World worldIn, List<String> list, ITooltipFlag flagIn) {
		if(this == ModItems.schrabidium_hammer) {
			list.add("Even though it says \"+1000000000");
			list.add("damage\", it's actually \"onehit anything\"");
		}
		if(this == ModItems.bottle_opener) {
			list.add("My very own bottle opener.");
			list.add("Use with caution!");
		}
		if(this == ModItems.ullapool_caber) {
			list.add("High-yield Scottish face removal.");
			list.add("A sober person would throw it...");
		}
		if(this == ModItems.wrench) {
			list.add("Mechanic Richard");
		}
		if(this == ModItems.wrench_flipped) {
			list.add("Wrench 2: The Wrenchening");
		}
		if(this == ModItems.memespoon) {
			list.add(TextFormatting.DARK_GRAY + "Level 10 Shovel");
			list.add(TextFormatting.AQUA + "Deals crits while the wielder is rocket jumping");
			list.add(TextFormatting.RED + "20% slower firing speed");
			list.add(TextFormatting.RED + "No random critical hits");
		}
		if(this == ModItems.shimmer_sledge) {
			if(MainRegistry.polaroidID == 11) {
				list.add("shimmer no");
				list.add("drop that hammer");
				list.add("you're going to hurt somebody");
				list.add("shimmer no");
				list.add("shimmer pls");
			} else {
				list.add("Breaks everything, even portals.");
			}
		}
		if(this == ModItems.shimmer_axe) {
			if(MainRegistry.polaroidID == 11) {
				list.add("shim's toolbox does an e-x-p-a-n-d");
			} else {
				list.add("Timber!");
			}
		}
		if(this == ModItems.wood_gavel) {
			list.add("Thunk!");
		}
		if(this == ModItems.lead_gavel) {
			list.add("You are hereby sentenced to lead poisoning.");
		}
		if(this == ModItems.diamond_gavel) {
			list.add("The joke! It makes sense now!!");
			list.add("");
			list.add(TextFormatting.BLUE + "Deals as much damage as it needs to.");
		}
	}

}
