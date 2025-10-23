package com.hbm.items.tool;

import com.google.common.collect.Multimap;
import com.hbm.entity.projectile.EntityLaserBeam;
import com.hbm.entity.projectile.EntityMinerBeam;
import com.hbm.entity.projectile.EntityRubble;
import com.hbm.explosion.ExplosionChaos;
import com.hbm.items.ModItems;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.lib.Library;
import java.util.List;
import java.util.Random;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class ItemMultitoolPassive extends Item {

	Random rand = new Random();

	public ItemMultitoolPassive(String s) {
		this.setTranslationKey(s);
		this.setRegistryName(s);
		this.setMaxDamage(5000);

		ModItems.ALL_ITEMS.add(this);
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
		ItemStack stack = player.getHeldItem(hand);
		if(player.isSneaking()) {
			world.playSound(null, player.posX, player.posY, player.posZ, HBMSoundHandler.techBoop, SoundCategory.PLAYERS, 2.0F, 1.0F);

			if(this == ModItems.multitool_ext) {
				return ActionResult.newResult(EnumActionResult.SUCCESS, new ItemStack(ModItems.multitool_miner, 1, stack.getItemDamage()));
			} else if(this == ModItems.multitool_miner) {
				ItemStack item = new ItemStack(ModItems.multitool_hit, 1, stack.getItemDamage());
				item.addEnchantment(Enchantments.LOOTING, 3);
				item.addEnchantment(Enchantments.KNOCKBACK, 3);
				return ActionResult.newResult(EnumActionResult.SUCCESS, item);
			} else if(this == ModItems.multitool_hit) {
				return ActionResult.newResult(EnumActionResult.SUCCESS, new ItemStack(ModItems.multitool_beam, 1, stack.getItemDamage()));
			} else if(this == ModItems.multitool_beam) {
				return ActionResult.newResult(EnumActionResult.SUCCESS, new ItemStack(ModItems.multitool_sky, 1, stack.getItemDamage()));
			} else if(this == ModItems.multitool_sky) {
				ItemStack item = new ItemStack(ModItems.multitool_mega, 1, stack.getItemDamage());
				item.addEnchantment(Enchantments.KNOCKBACK, 5);
				return ActionResult.newResult(EnumActionResult.SUCCESS, item);
			} else if(this == ModItems.multitool_mega) {
				ItemStack item = new ItemStack(ModItems.multitool_joule, 1, stack.getItemDamage());
				item.addEnchantment(Enchantments.KNOCKBACK, 3);
				return ActionResult.newResult(EnumActionResult.SUCCESS, item);
			} else if(this == ModItems.multitool_joule) {
				ItemStack item = new ItemStack(ModItems.multitool_decon, 1, stack.getItemDamage());
				return ActionResult.newResult(EnumActionResult.SUCCESS, item);
			} else if(this == ModItems.multitool_decon) {
				ItemStack item = new ItemStack(ModItems.multitool_dig, 1, stack.getItemDamage());
				item.addEnchantment(Enchantments.LOOTING, 3);
				item.addEnchantment(Enchantments.FORTUNE, 3);
				return ActionResult.newResult(EnumActionResult.SUCCESS, item);
			}

		} else {
			if(this == ModItems.multitool_ext) {
				return ActionResult.newResult(EnumActionResult.PASS, stack);
			} else if(this == ModItems.multitool_miner) {

				EntityMinerBeam plasma = new EntityMinerBeam(world, player, 0.75F);

				world.playSound(null, player.posX, player.posY, player.posZ, HBMSoundHandler.immolatorIgnite, SoundCategory.PLAYERS, 1.0F, 1F);
				// world.playSoundAtEntity(player, "hbm:weapon.immolatorShoot",
				// 1.0F, 1F);

				if(!world.isRemote)
					world.spawnEntity(plasma);

				return ActionResult.newResult(EnumActionResult.PASS, stack);
			} else if(this == ModItems.multitool_hit) {
				return ActionResult.newResult(EnumActionResult.PASS, stack);
			} else if(this == ModItems.multitool_beam) {

				EntityLaserBeam plasma = new EntityLaserBeam(world, player, 1F);

				world.playSound(null, player.posX, player.posY, player.posZ, HBMSoundHandler.immolatorIgnite, SoundCategory.PLAYERS, 1.0F, 1F);
				// world.playSoundAtEntity(player, "hbm:weapon.immolatorShoot",
				// 1.0F, 1F);

				if(!world.isRemote)
					world.spawnEntity(plasma);

				return ActionResult.newResult(EnumActionResult.PASS, stack);
			} else if(this == ModItems.multitool_sky) {
				for(int i = 0; i < 15; i++) {
					int a = (int) player.posX - 15 + rand.nextInt(31);
					int b = (int) player.posZ - 15 + rand.nextInt(31);
					// if(!world.isRemote) {
					EntityLightningBolt blitz = new EntityLightningBolt(world, a, world.getHeight(a, b), b, false);
					world.spawnEntity(blitz);
					// }
				}
				return ActionResult.newResult(EnumActionResult.PASS, stack);
			} else if(this == ModItems.multitool_mega) {
				return ActionResult.newResult(EnumActionResult.PASS, stack);
			} else if(this == ModItems.multitool_joule) {
				return ActionResult.newResult(EnumActionResult.PASS, stack);
			} else if(this == ModItems.multitool_decon) {
				return ActionResult.newResult(EnumActionResult.PASS, stack);
			}
		}

		return ActionResult.newResult(EnumActionResult.PASS, stack);
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if(this == ModItems.multitool_ext) {
			IBlockState b = world.getBlockState(pos);
			ItemStack s = FurnaceRecipes.instance().getSmeltingResult(new ItemStack(Item.getItemFromBlock(b.getBlock()), 1, b.getBlock().getMetaFromState(b)));
			if(!s.isEmpty()) {
				ItemStack t = s.copy();
				if(!world.isRemote)
					world.setBlockState(pos, Blocks.AIR.getDefaultState());

				if(!player.inventory.addItemStackToInventory(t))
					player.dropItem(t, false);
				player.swingArm(hand);
			}
		} else if(this == ModItems.multitool_miner) {
		} else if(this == ModItems.multitool_hit) {
		} else if(this == ModItems.multitool_beam) {
		} else if(this == ModItems.multitool_sky) {
		} else if(this == ModItems.multitool_mega) {

			ExplosionChaos.levelDown(world, pos.getX(), pos.getY(), pos.getZ(), 2);
			return EnumActionResult.SUCCESS;

		} else if(this == ModItems.multitool_joule) {

			int l = 25;
			float part = -1F / 16F;

			Vec3d v = player.getLookVec();
			Vec3d vec0 = new Vec3d(v.x, v.y, v.z);
			vec0 = vec0.rotateYaw(.25F);
			List<int[]> list = Library.getBlockPosInPath(pos, l, vec0);
			vec0 = vec0.rotateYaw(part);
			list.addAll(Library.getBlockPosInPath(pos, l, vec0));
			vec0 = vec0.rotateYaw(part);
			list.addAll(Library.getBlockPosInPath(pos, l, vec0));
			vec0 = vec0.rotateYaw(part);
			list.addAll(Library.getBlockPosInPath(pos, l, vec0));
			vec0 = vec0.rotateYaw(part);
			list.addAll(Library.getBlockPosInPath(pos, l, vec0));
			vec0 = vec0.rotateYaw(part);
			list.addAll(Library.getBlockPosInPath(pos, l, vec0));
			vec0 = vec0.rotateYaw(part);
			list.addAll(Library.getBlockPosInPath(pos, l, vec0));
			vec0 = vec0.rotateYaw(part);
			list.addAll(Library.getBlockPosInPath(pos, l, vec0));
			vec0 = vec0.rotateYaw(part);
			list.addAll(Library.getBlockPosInPath(pos, l, vec0));

			if(!world.isRemote)
				for(int j = 0; j < list.size(); j++) {

					int x1 = list.get(j)[0];
					int y1 = list.get(j)[1];
					int z1 = list.get(j)[2];
					int w1 = list.get(j)[3];
					
					BlockPos pos2 = new BlockPos(x1, y1, z1);

					IBlockState b = world.getBlockState(pos2);
					float k = b.getBlockHardness(world, pos2);

					if(k < 6000 && k > 0 && b.getBlock() != Blocks.AIR) {

						EntityRubble rubble = new EntityRubble(world);
						rubble.posX = x1 + 0.5F;
						rubble.posY = y1;
						rubble.posZ = z1 + 0.5F;

						rubble.motionY = 0.025F * w1 + 0.15F;
						rubble.setMetaBasedOnBlock(b.getBlock(), b.getBlock().getMetaFromState(b));

						world.spawnEntity(rubble);

						world.setBlockState(pos2, Blocks.AIR.getDefaultState());
					}
				}

			return EnumActionResult.SUCCESS;

		} else if(this == ModItems.multitool_decon) {

			if(!world.isRemote)
				ExplosionChaos.decontaminate(world, pos);
			return EnumActionResult.SUCCESS;

		}

		return EnumActionResult.PASS;
	}

	@Override
	public Multimap<String, AttributeModifier> getAttributeModifiers(EntityEquipmentSlot slot, ItemStack stack) {
		Multimap<String, AttributeModifier> map = super.getAttributeModifiers(slot, stack);
		if(slot == EntityEquipmentSlot.MAINHAND) {
			map.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weapon modifier", 4, 0));
			if(this == ModItems.multitool_ext) {
				map.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weapon modifier", 7, 0));
			} else if(this == ModItems.multitool_miner) {
				map.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weapon modifier", 8, 0));
			} else if(this == ModItems.multitool_hit) {
				map.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weapon modifier", 16, 0));
			} else if(this == ModItems.multitool_beam) {
				map.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weapon modifier", 8, 0));
			} else if(this == ModItems.multitool_sky) {
				map.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weapon modifier", 5, 0));
			} else if(this == ModItems.multitool_mega) {
				map.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weapon modifier", 12, 0));
			} else if(this == ModItems.multitool_joule) {
				map.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weapon modifier", 12, 0));
			} else if(this == ModItems.multitool_decon) {
				map.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weapon modifier", 5, 0));
			}
		}
		return map;
	}
	
	@Override
	public void addInformation(ItemStack stack, World worldIn, List<String> list, ITooltipFlag flagIn) {
		if(this == ModItems.multitool_ext) {
			list.add("Right click instantly destroys smeltable blocks");
			list.add("Mined blocks will be smelted and put in the player's inventory");
		}
		if(this == ModItems.multitool_miner) {
			list.add("Shoots lasers which destroy smeltable blocks");
			list.add("These blocks will drop the smelted item");
		}
		if(this == ModItems.multitool_hit) {
			list.add("Very high damage against mobs");
			list.add("Strong knock back");
		}
		if(this == ModItems.multitool_beam) {
			list.add("Shoots lasers which ignite blocks and mobs");
			list.add("Lasers are destroyed by water");
		}
		if(this == ModItems.multitool_sky) {
			list.add("Right click summons a lightning storm around the player");
			list.add("Lightning can also hit the player using the fist");
		}
		if(this == ModItems.multitool_mega) {
			list.add("Right click will level down blocks with a powerful punch");
			list.add("Immense knockback against mobs");
		}
		if(this == ModItems.multitool_joule) {
			list.add("Right click will break blocks in the line of sight");
			list.add("These blocks will be flung up as rubble");
		}
		if(this == ModItems.multitool_decon) {
			list.add("Right click will remove radiation effect from blocks");
			list.add("Blocks like nuclear waste turn into lead");
		}
	}
}
