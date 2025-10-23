package com.hbm.items.food;

import com.hbm.capability.HbmLivingProps;
import com.hbm.config.VersatileConfig;
import com.hbm.explosion.ExplosionLarge;
import com.hbm.interfaces.Spaghetti;
import com.hbm.items.ModItems;
import com.hbm.items.ModItems.Foods;
import com.hbm.lib.Library;
import com.hbm.main.MainRegistry;
import com.hbm.util.ContaminationUtil;
import com.hbm.util.ContaminationUtil.ContaminationType;
import com.hbm.util.ContaminationUtil.HazardType;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.MobEffects;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;

import java.util.List;

public class ItemEnergy extends Item {

	private Item container = null;
	private Item cap = null;
	private boolean requiresOpener = false;

	public ItemEnergy(String s) {
		this.setTranslationKey(s);
		this.setRegistryName(s);
		this.setCreativeTab(MainRegistry.consumableTab);
		ModItems.ALL_ITEMS.add(this);
	}

	public ItemEnergy makeCan() {
		this.container = Foods.can_empty;
		this.cap = Foods.ring_pull;
		this.requiresOpener = false;
		this.setContainerItem(this.container);
		return this;
	}

	public ItemEnergy makeBottle(Item bottle, Item cap) {
		this.container = bottle;
		this.cap = cap;
		this.requiresOpener = true;
		this.setContainerItem(this.container);
		this.setCreativeTab(MainRegistry.consumableTab);
		return this;
	}

	@Spaghetti("clusterfuck")
	@Override
	public ItemStack onItemUseFinish(ItemStack stack, World worldIn, EntityLivingBase entity) {
		if(!worldIn.isRemote && entity instanceof EntityPlayer player) {
			if(player instanceof FakePlayer) {
        		worldIn.newExplosion(player, player.posX, player.posY, player.posZ, 5F, true, true);
        		return super.onItemUseFinish(stack, worldIn, entity);
        	}
			if (player instanceof EntityPlayerMP playerMP) {
				CriteriaTriggers.CONSUME_ITEM.trigger(playerMP, stack);
			}
			VersatileConfig.applyPotionSickness(player, 5);
			if(!player.capabilities.isCreativeMode) {
				stack.shrink(1);
			}
			if(this == Foods.can_smart) {
				player.addPotionEffect(new PotionEffect(MobEffects.SPEED, 30 * 20, 1));
				player.addPotionEffect(new PotionEffect(MobEffects.RESISTANCE, 30 * 20, 2));
				player.addPotionEffect(new PotionEffect(MobEffects.STRENGTH, 30 * 20, 0));
			}
			if(this == Foods.can_creature) {
				player.addPotionEffect(new PotionEffect(MobEffects.SPEED, 30 * 20, 0));
				player.addPotionEffect(new PotionEffect(MobEffects.RESISTANCE, 30 * 20, 2));
				player.addPotionEffect(new PotionEffect(MobEffects.REGENERATION, 30 * 20, 1));
			}
			if(this == Foods.can_redbomb) {
				player.addPotionEffect(new PotionEffect(MobEffects.SPEED, 30 * 20, 0));
				player.addPotionEffect(new PotionEffect(MobEffects.ABSORPTION, 30 * 20, 2));
				player.addPotionEffect(new PotionEffect(MobEffects.JUMP_BOOST, 30 * 20, 1));
			}
			if(this == Foods.can_mrsugar) {
				player.addPotionEffect(new PotionEffect(MobEffects.SPEED, 30 * 20, 0));
				player.addPotionEffect(new PotionEffect(MobEffects.HASTE, 30 * 20, 1));
				player.addPotionEffect(new PotionEffect(MobEffects.JUMP_BOOST, 30 * 20, 2));
			}
			if(this == Foods.can_overcharge) {
				player.addPotionEffect(new PotionEffect(MobEffects.SPEED, 30 * 20, 1));
				player.addPotionEffect(new PotionEffect(MobEffects.RESISTANCE, 30 * 20, 2));
				player.addPotionEffect(new PotionEffect(MobEffects.STRENGTH, 30 * 20, 0));
			}
			if(this == Foods.can_luna) {
				player.addPotionEffect(new PotionEffect(MobEffects.SPEED, 30 * 20, 1));
				player.addPotionEffect(new PotionEffect(MobEffects.RESISTANCE, 30 * 20, 2));
				player.addPotionEffect(new PotionEffect(MobEffects.STRENGTH, 30 * 20, 1));
				player.addPotionEffect(new PotionEffect(MobEffects.REGENERATION, 30 * 20, 2));
			}
			if(this == Foods.can_bepis) {
				player.addPotionEffect(new PotionEffect(MobEffects.SPEED, 30 * 20, 3));
				player.addPotionEffect(new PotionEffect(MobEffects.RESISTANCE, 30 * 20, 3));
			}
			if(this == Foods.can_breen) {
				player.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, 30 * 20, 0));
			}
			if(this == Foods.bottle_cherry) {
				player.heal(6F);
				player.addPotionEffect(new PotionEffect(MobEffects.SPEED, 30 * 20, 0));
				player.addPotionEffect(new PotionEffect(MobEffects.JUMP_BOOST, 30 * 20, 2));
				ContaminationUtil.contaminate(player, HazardType.RADIATION, ContaminationType.RAD_BYPASS, 5.0F);
				if(!player.capabilities.isCreativeMode) {
					Library.addToInventoryOrDrop(player, new ItemStack(Foods.cap_nuka));
					if(stack.isEmpty()) {
						return new ItemStack(Foods.bottle_empty);
					}

					Library.addToInventoryOrDrop(player, new ItemStack(Foods.bottle_empty));
				}
			}
			if(this == Foods.bottle_nuka) {
				player.heal(4F);
				player.addPotionEffect(new PotionEffect(MobEffects.SPEED, 30 * 20, 1));
				player.addPotionEffect(new PotionEffect(MobEffects.HASTE, 30 * 20, 1));
				ContaminationUtil.contaminate(player, HazardType.RADIATION, ContaminationType.RAD_BYPASS, 5.0F);
				if(!player.capabilities.isCreativeMode) {
					Library.addToInventoryOrDrop(player, new ItemStack(Foods.cap_nuka));
					if(stack.isEmpty()) {
						return new ItemStack(Foods.bottle_empty);
					}

					Library.addToInventoryOrDrop(player, new ItemStack(Foods.bottle_empty));
				}
			}
			if(this == Foods.bottle_sparkle) {
				player.heal(10F);
				player.addPotionEffect(new PotionEffect(MobEffects.SPEED, 120 * 20, 1));
				player.addPotionEffect(new PotionEffect(MobEffects.RESISTANCE, 120 * 20, 2));
				player.addPotionEffect(new PotionEffect(MobEffects.STRENGTH, 120 * 20, 2));
				player.addPotionEffect(new PotionEffect(MobEffects.HASTE, 120 * 20, 1));
				ContaminationUtil.contaminate(player, HazardType.RADIATION, ContaminationType.RAD_BYPASS, 5.0F);
				if(!player.capabilities.isCreativeMode){
                	Library.addToInventoryOrDrop(player, new ItemStack(Foods.cap_sparkle));
            		if (stack.isEmpty())
                	{
                    	return new ItemStack(Foods.bottle_empty);
                	}

                	Library.addToInventoryOrDrop(player, new ItemStack(Foods.bottle_empty));
                }
			}
			if(this == Foods.bottle_quantum) {
				player.heal(10F);
				player.addPotionEffect(new PotionEffect(MobEffects.SPEED, 30 * 20, 1));
				player.addPotionEffect(new PotionEffect(MobEffects.RESISTANCE, 30 * 20, 2));
				player.addPotionEffect(new PotionEffect(MobEffects.STRENGTH, 30 * 20, 1));
				ContaminationUtil.contaminate(player, HazardType.RADIATION, ContaminationType.RAD_BYPASS, 15.0F);
				if(!player.capabilities.isCreativeMode) {
					Library.addToInventoryOrDrop(player, new ItemStack(Foods.cap_quantum));
					if(stack.isEmpty()) {
						return new ItemStack(Foods.bottle_empty);
					}

					Library.addToInventoryOrDrop(player, new ItemStack(Foods.bottle_empty));
				}
			}
			
			if(this == Foods.bottle_rad)
        	{
        		player.heal(10F);
                player.addPotionEffect(new PotionEffect(MobEffects.SPEED, 120 * 20, 1));
                player.addPotionEffect(new PotionEffect(MobEffects.RESISTANCE, 120 * 20, 2));
                player.addPotionEffect(new PotionEffect(MobEffects.FIRE_RESISTANCE, 120 * 20, 0));
                player.addPotionEffect(new PotionEffect(MobEffects.STRENGTH, 120 * 20, 4));
                player.addPotionEffect(new PotionEffect(MobEffects.HASTE, 120 * 20, 1));
                ContaminationUtil.contaminate(player, HazardType.RADIATION, ContaminationType.RAD_BYPASS, 15.0F);
                
                if(!player.capabilities.isCreativeMode){
                	Library.addToInventoryOrDrop(player, new ItemStack(Foods.cap_rad));
            		if (stack.isEmpty())
                	{
                    	return new ItemStack(Foods.bottle_empty);
                	}

                	Library.addToInventoryOrDrop(player, new ItemStack(Foods.bottle_empty));
                }
        	}

			if(this == Foods.coffee) {
				player.heal(10);
				player.addPotionEffect(new PotionEffect(MobEffects.SPEED, 60 * 20, 2));
			}
			if(this == Foods.coffee_radium) {
				player.heal(10);
				player.addPotionEffect(new PotionEffect(MobEffects.SPEED, 60 * 20, 2));
				HbmLivingProps.incrementRadiation(player, 500F);
				//player.triggerAchievement(MainRegistry.achRadium);
			}
			
			if(this == Foods.bottle2_korl)
        	{
        		player.heal(6);
                player.addPotionEffect(new PotionEffect(MobEffects.SPEED, 30 * 20, 1));
                player.addPotionEffect(new PotionEffect(MobEffects.HASTE, 30 * 20, 2));
                player.addPotionEffect(new PotionEffect(MobEffects.STRENGTH, 30 * 20, 2));
                
                if(!player.capabilities.isCreativeMode){
                	Library.addToInventoryOrDrop(player, new ItemStack(Foods.cap_korl));
            		if (stack.isEmpty())
                	{
                    	return new ItemStack(Foods.bottle2_empty);
                	}

                	Library.addToInventoryOrDrop(player, new ItemStack(Foods.bottle2_empty));
                }
        	}
			
			if(this == Foods.bottle2_fritz)
        	{
        		player.heal(6);
                player.addPotionEffect(new PotionEffect(MobEffects.SPEED, 30 * 20, 1));
                player.addPotionEffect(new PotionEffect(MobEffects.RESISTANCE, 30 * 20, 2));
                player.addPotionEffect(new PotionEffect(MobEffects.JUMP_BOOST, 30 * 20, 2));
                
                if(!player.capabilities.isCreativeMode){
                	Library.addToInventoryOrDrop(player, new ItemStack(Foods.cap_fritz));
            		if (stack.isEmpty())
                	{
                    	return new ItemStack(Foods.bottle2_empty);
                	}

                	Library.addToInventoryOrDrop(player, new ItemStack(Foods.bottle2_empty));
                }
        	}
			
			if(this == Foods.bottle2_korl_special)
        	{
        		player.heal(16);
                player.addPotionEffect(new PotionEffect(MobEffects.SPEED, 120 * 20, 1));
                player.addPotionEffect(new PotionEffect(MobEffects.HASTE, 120 * 20, 2));
                player.addPotionEffect(new PotionEffect(MobEffects.STRENGTH, 120 * 20, 2));
                
                if(!player.capabilities.isCreativeMode){
                	Library.addToInventoryOrDrop(player, new ItemStack(Foods.cap_korl));
            		if (stack.isEmpty())
                	{
                    	return new ItemStack(Foods.bottle2_empty);
                	}

                	Library.addToInventoryOrDrop(player, new ItemStack(Foods.bottle2_empty));
                }
        	}
			
			if(this == Foods.bottle2_fritz_special)
        	{
        		player.heal(16);
                player.addPotionEffect(new PotionEffect(MobEffects.SPEED, 120 * 20, 1));
                player.addPotionEffect(new PotionEffect(MobEffects.RESISTANCE, 120 * 20, 2));
                player.addPotionEffect(new PotionEffect(MobEffects.JUMP_BOOST, 120 * 20, 2));
                
                if(!player.capabilities.isCreativeMode){
                	Library.addToInventoryOrDrop(player, new ItemStack(Foods.cap_fritz));
            		if (stack.isEmpty())
                	{
                    	return new ItemStack(Foods.bottle2_empty);
                	}

                	Library.addToInventoryOrDrop(player, new ItemStack(Foods.bottle2_empty));
                }
        	}
			
			if(this == Foods.bottle2_sunset)
        	{
        		player.heal(6);
                player.addPotionEffect(new PotionEffect(MobEffects.SPEED, 60 * 20, 1));
                player.addPotionEffect(new PotionEffect(MobEffects.RESISTANCE, 60 * 20, 2));
                player.addPotionEffect(new PotionEffect(MobEffects.STRENGTH, 60 * 20, 2));
                player.addPotionEffect(new PotionEffect(MobEffects.HASTE, 60 * 20, 2));
                
                if(!player.capabilities.isCreativeMode){
                	if(worldIn.rand.nextInt(10) == 0){
            			Library.addToInventoryOrDrop(player, new ItemStack(Foods.cap_star));
                	} else {
            			Library.addToInventoryOrDrop(player, new ItemStack(Foods.cap_sunset));
                	}
            		
            		if (stack.isEmpty())
                	{
                    	return new ItemStack(Foods.bottle2_empty);
                	}

                	Library.addToInventoryOrDrop(player, new ItemStack(Foods.bottle2_empty));
                }
        	}
			
			if(this == Foods.chocolate_milk)
        	{
        		ExplosionLarge.explode(worldIn, player, player.posX, player.posY, player.posZ, 50, true, false, false);
        	}

			if(!player.capabilities.isCreativeMode)
				if(this == Foods.can_creature || this == Foods.can_mrsugar || this == Foods.can_overcharge || this == Foods.can_redbomb || this == Foods.can_smart || this == Foods.can_luna || this == Foods.can_bepis || this == Foods.can_breen) {
					Library.addToInventoryOrDrop(player, new ItemStack(Foods.ring_pull));
					if(stack.isEmpty()) {
						return new ItemStack(Foods.can_empty);
					}
				}
			player.inventoryContainer.detectAndSendChanges();
		}
		return stack;
	}

	@Override
	public int getMaxItemUseDuration(ItemStack stack) {
		return 32;
	}

	@Override
	public EnumAction getItemUseAction(ItemStack stack) {
		return EnumAction.DRINK;
	}

	public static boolean hasOpener(EntityPlayer player){
		ItemStack stackR = player.getHeldItemMainhand();
		ItemStack stackL = player.getHeldItemOffhand();
		if(stackR.isEmpty() || stackL.isEmpty()) return false;
		return stackR.getItem() == Foods.bottle_opener || stackL.getItem() == Foods.bottle_opener;
	}	

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer player, EnumHand hand) {
		if(this.requiresOpener && !hasOpener(player))
			return ActionResult.newResult(EnumActionResult.PASS, player.getHeldItem(hand));

		player.setActiveHand(hand);
		return ActionResult.newResult(EnumActionResult.SUCCESS, player.getHeldItem(hand));
	}

	@Override
	public void addInformation(ItemStack stack, World worldIn, List<String> list, ITooltipFlag flagIn) {
		if(this == Foods.chocolate_milk)
    	{
            list.add("Regular chocolate milk. Safe to drink.");
            list.add("Totally not made from nitroglycerine.");
    	}
		if(this == Foods.bottle2_sunset)
    	{
    		if(MainRegistry.polaroidID == 11) {
    			list.add("\"Authentic Sunset Juice\"");
    			list.add("");
    			list.add("This smells like fish.");
    			list.add("*sip*");
    			list.add("Yup, that's pretty disugsting.");
    			list.add("...");
    			list.add("...");
    			list.add("*sip*");
    		} else {
    			list.add("The eternal #2. Screw you, Bradberton!");
    		}
    	}
		if(this == Foods.bottle2_fritz_special)
    	{
    		if(MainRegistry.polaroidID == 11)
    			list.add("ygrogr fgrof bf");
    		else
    			list.add("moremore caffeine");
    	}
		if(this == Foods.bottle2_korl_special)
    	{
    		if(MainRegistry.polaroidID == 11)
    			list.add("shgehgev u rguer");
    		else
                list.add("Contains actual orange juice!");
    	}
		if(this == Foods.bottle2_fritz)
    	{
            list.add("moremore caffeine");
    	}
		if(this == Foods.bottle2_korl)
    	{
            list.add("Contains actual orange juice!");
    	}
		if(this == Foods.bottle_quantum) {
			list.add("Comes with a colorful mix of over 70 isotopes!");
		}
		if(this == Foods.bottle_sparkle) {
			if(MainRegistry.polaroidID == 11)
				list.add("Contains trace amounts of taint.");
			else
				list.add("The most delicious beverage in the wasteland!");
		}
		if(this == Foods.can_smart) {
			list.add("Cheap and full of bubbles");
		}
		if(this == Foods.can_creature) {
			list.add("Basically gasoline in a tin can");
		}
		if(this == Foods.can_redbomb) {
			list.add("Liquefied explosives");
		}
		if(this == Foods.can_mrsugar) {
			list.add("An intellectual drink, for the chosen ones!");
		}
		if(this == Foods.can_overcharge) {
			list.add("Possible side effects include heart attacks, seizures or zombification");
		}
		if(this == Foods.can_luna) {
			list.add("Contains actual selenium and star metal. Tastes like night.");
		}
		if(this == Foods.can_bepis) {
			list.add("beppp");
		}
		if(this == Foods.can_breen) {
			list.add("Don't drink the water. They put something in it, to make you forget.");
			list.add("I don't even know how I got here.");
		}
		if(this == Foods.bottle_nuka) {
			list.add("Contains about 210 kcal and 1500 mSv.");
		}
		if(this == Foods.bottle_cherry) {
			list.add("Now with severe radiation poisoning in every seventh bottle!");
		}
	}
}
