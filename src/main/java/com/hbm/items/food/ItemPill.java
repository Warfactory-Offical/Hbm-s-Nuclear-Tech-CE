package com.hbm.items.food;

import com.hbm.capability.HbmLivingCapability.EntityHbmProps;
import com.hbm.capability.HbmLivingProps;
import com.hbm.config.VersatileConfig;
import com.hbm.items.ModItems;
import com.hbm.lib.ModDamageSource;
import com.hbm.potion.HbmPotion;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ItemPill extends ItemFood {

	Random rand = new Random();
	
	public ItemPill(int hunger, String s) {
		super(hunger, false);
		this.setTranslationKey(s);
		this.setRegistryName(s);
		this.setAlwaysEdible();
		
		ModItems.ALL_ITEMS.add(this);
	}
	
	@Override
	protected void onFoodEaten(ItemStack stack, World worldIn, EntityPlayer player) {
		if (!worldIn.isRemote)
        {
			VersatileConfig.applyPotionSickness(player, 5);
        	if(this == ModItems.pill_iodine) {
        		player.removePotionEffect(MobEffects.BLINDNESS);
        		player.removePotionEffect(MobEffects.NAUSEA);
        		player.removePotionEffect(MobEffects.MINING_FATIGUE);
        		player.removePotionEffect(MobEffects.HUNGER);
        		player.removePotionEffect(MobEffects.SLOWNESS);
        		player.removePotionEffect(MobEffects.POISON);
        		player.removePotionEffect(MobEffects.WEAKNESS);
        		player.removePotionEffect(MobEffects.WITHER);
        		player.removePotionEffect(HbmPotion.radiation);
        	}

        	if(this == ModItems.plan_c) {
        		for(int i = 0; i < 10; i++)
        			player.attackEntityFrom(rand.nextBoolean() ? ModDamageSource.euthanizedSelf : ModDamageSource.euthanizedSelf2, 1000);
        	}

        	if(this == ModItems.radx) {
        		player.addPotionEffect(new PotionEffect(HbmPotion.radx, 3 * 60 * 20, 3));
        	}
        	if(this == ModItems.siox) {
				HbmLivingProps.setAsbestos(player, 0);
				HbmLivingProps.setBlackLung(player, Math.min(HbmLivingProps.getBlackLung(player), EntityHbmProps.maxBlacklung / 5));
			}

			if(this == ModItems.pill_herbal) {
				HbmLivingProps.setAsbestos(player, 0);
				HbmLivingProps.setBlackLung(player, Math.min(HbmLivingProps.getBlackLung(player), EntityHbmProps.maxBlacklung / 5));
				HbmLivingProps.incrementRadiation(player, -100F);
				
				player.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, 10 * 20, 0));
				player.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 10 * 60 * 20, 2));
				player.addPotionEffect(new PotionEffect(MobEffects.MINING_FATIGUE, 10 * 60 * 20, 2));
				player.addPotionEffect(new PotionEffect(MobEffects.POISON, 5 * 20, 2));
				
				PotionEffect eff = new PotionEffect(HbmPotion.potionsickness, 10 * 60 * 20);
				eff.setCurativeItems(new ArrayList());
				player.addPotionEffect(eff);
			}

            if (this == ModItems.xanax) {
                double digamma = HbmLivingProps.getDigamma(player);
                HbmLivingProps.setDigamma(player, Math.max(digamma - 0.5D, 0D));
            }

            if(this == ModItems.chocolate) {
                if(rand.nextInt(25) == 0) {
                    player.attackEntityFrom(ModDamageSource.overdose, 1000); //mlbv: chocolate overdose? seriously?
                }
                player.addPotionEffect(new PotionEffect(MobEffects.HASTE, 60 * 20, 3));
                player.addPotionEffect(new PotionEffect(MobEffects.SPEED, 60 * 20, 3));
                player.addPotionEffect(new PotionEffect(MobEffects.JUMP_BOOST, 60 * 20, 3));
            }

            if (this == ModItems.fmn) {
                double digamma = HbmLivingProps.getDigamma(player);
                HbmLivingProps.setDigamma(player, Math.min(digamma, 2D));
                player.addPotionEffect(new PotionEffect(MobEffects.BLINDNESS, 60, 0));
            }

            if (this == ModItems.five_htp) {
                HbmLivingProps.setDigamma(player, 0D);
                player.addPotionEffect(new PotionEffect(HbmPotion.stability, 10 * 60 * 20, 0));
            }
        }
	}
	
	@Override
	public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		if(this == ModItems.pill_iodine) {
			tooltip.add("Removes negative effects");
		}
		if(this == ModItems.plan_c) {
			tooltip.add("Deadly");
		}
		if(this == ModItems.radx) {
			tooltip.add("Increases radiation resistance by 0.4 for 3 minutes");
		}
		if(this == ModItems.siox) {
			tooltip.add("Reverses mesothelioma with the power of Asbestos!");
		}
		if(this == ModItems.pill_herbal) {
			tooltip.add("Effective treatment against lung disease and mild radiation poisoning");
			tooltip.add("Comes with side effects");
		}
		if(this == ModItems.xanax) {
			tooltip.add("Removes 500mDRX");
		}
		if(this == ModItems.fmn) {
			tooltip.add("Removes all DRX above 2,000mDRX");
		}
		if(this == ModItems.five_htp) {
			tooltip.add("Removes all DRX, Stability for 10 minutes");
		}
	}
	
	@Override
	public int getMaxItemUseDuration(ItemStack stack) {
		return 10;
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
		if(!VersatileConfig.hasPotionSickness(playerIn))
			playerIn.setActiveHand(handIn);
		return super.onItemRightClick(worldIn, playerIn, handIn);
	}
}
