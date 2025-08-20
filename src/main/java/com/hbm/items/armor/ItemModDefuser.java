package com.hbm.items.armor;

import com.hbm.handler.ArmorModHandler;
import com.hbm.items.ModItems;
import com.hbm.lib.HBMSoundHandler;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAICreeperSwell;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import java.util.List;

public class ItemModDefuser extends ItemArmorMod {

    public ItemModDefuser(String s) {
        super(ArmorModHandler.extra, true, true, true, true, s);
    }

    @Override
    public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flagIn) {

        list.add(TextFormatting.YELLOW + "Defuses nearby creepers");
        list.add("");
        super.addInformation(stack, world, list, flagIn);
    }

    @Override
    public void addDesc(List list, ItemStack stack, ItemStack armor) {
        list.add(TextFormatting.YELLOW + "  " + stack.getDisplayName() + " (Defuses creepers)");
    }

    @Override
    public void modUpdate(EntityLivingBase entity, ItemStack armor) {

        if(entity.world.isRemote || entity.world.getTotalWorldTime() % 20 != 0) return;

        List<EntityCreeper> creepers = entity.world.getEntitiesWithinAABB(EntityCreeper.class, entity.getEntityBoundingBox().expand(5, 5, 5));

        for(EntityCreeper creeper : creepers) {

            if(creeper.getCreeperState() == 1 || creeper.hasIgnited()) {
                creeper.setCreeperState(-1);
                creeper.getDataManager().set(creeper.IGNITED, false);

                EntityAICreeperSwell toRem = null;
                for(EntityAITasks.EntityAITaskEntry o : creeper.tasks.taskEntries) {

                    if(o.action instanceof EntityAICreeperSwell) {
                        toRem = (EntityAICreeperSwell) o.action;
                        break;
                    }
                }

                if(toRem != null) {
                    creeper.tasks.removeTask(toRem);
                    creeper.world.playSound(creeper.posX, creeper.posY, creeper.posZ, HBMSoundHandler.pinBreak, SoundCategory.PLAYERS, 1.0F, 1.0F, false);
                    creeper.dropItem(ModItems.safety_fuse, 1);
                    creeper.attackEntityFrom(DamageSource.causeMobDamage(entity), 1.0F);
                    creeper.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 0, 200));
                }
            }
        }
    }
}
