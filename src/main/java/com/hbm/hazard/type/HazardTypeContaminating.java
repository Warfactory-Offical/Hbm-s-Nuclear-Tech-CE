package com.hbm.hazard.type;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.BlockClean;
import com.hbm.config.RadiationConfig;
import com.hbm.entity.effect.EntityFalloutUnderGround;
import com.hbm.hazard.modifier.HazardModifier;
import com.hbm.util.I18nUtil;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class HazardTypeContaminating extends HazardTypeBase {

    private static final int MAX_RADIUS = 500;

    private static int computeRadius(double level) {
        return (int) Math.min(Math.sqrt(level) + 0.5D, MAX_RADIUS);
    }

    @Override
    public void onUpdate(EntityLivingBase target, double level, ItemStack stack) {
    }

    @Override
    public void updateEntity(EntityItem item, double level) {
        if(!RadiationConfig.enableContaminationOnGround) return;
        if (item == null) return;
        World world = item.world;
        if (world == null || world.isRemote) return;

        if (item.onGround) {
            if(world.getBlockState(item.getPosition().down()).getBlock() instanceof BlockClean clean){
                getUsed(clean, item.getPosition().down(), world);
                return;
            }
            int radius = computeRadius(level);
            if (radius > 1) {
                EntityFalloutUnderGround falloutBall = new EntityFalloutUnderGround(world);
                falloutBall.setPosition(item.posX, item.posY, item.posZ);
                falloutBall.setScale(radius);
                world.spawnEntity(falloutBall);
            }
            item.setDead();
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addHazardInformation(EntityPlayer player, List list, double level, ItemStack stack, List<HazardModifier> modifiers) {
        int radius = computeRadius(level);
        if (radius > 1) {
            list.add(TextFormatting.DARK_GREEN + "[" + I18nUtil.resolveKey("trait.contaminating") + "]");
            list.add(TextFormatting.GREEN + " " + I18nUtil.resolveKey("trait.contaminating.radius", radius));
        }
    }

    protected static void getUsed(Block b, BlockPos pos, World world) {
        if (b == ModBlocks.tile_lab && world.rand.nextInt(2000) == 0) {
            world.setBlockState(pos, ModBlocks.tile_lab_cracked.getDefaultState());
        } else if (b == ModBlocks.tile_lab_cracked && world.rand.nextInt(10000) == 0) {
            world.setBlockState(pos, ModBlocks.tile_lab_broken.getDefaultState());
        }
    }
}
