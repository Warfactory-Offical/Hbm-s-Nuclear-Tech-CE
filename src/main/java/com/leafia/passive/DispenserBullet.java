package com.leafia.passive;

import com.hbm.entity.projectile.EntityBulletBase;
import com.hbm.handler.BulletConfigSyncingUtil;
import com.hbm.lib.HBMSoundEvents;
import net.minecraft.block.BlockDispenser;
import net.minecraft.dispenser.BehaviorProjectileDispense;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.dispenser.IPosition;
import net.minecraft.entity.Entity;
import net.minecraft.entity.IProjectile;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;

public class DispenserBullet extends BehaviorProjectileDispense {
    public static void register(Item item,int id) {
        DispenserBullet behavior = new DispenserBullet();
        behavior.id = id;
        BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(item,behavior);
    }
    protected int id = -1;
    @Override
    public ItemStack dispenseStack(IBlockSource source,ItemStack stack)
    {
        World world = source.getWorld();
        IPosition iposition = BlockDispenser.getDispensePosition(source);
        EnumFacing enumfacing = (EnumFacing)source.getBlockState().getValue(BlockDispenser.FACING);
        EntityBulletBase iprojectile = (EntityBulletBase)this.getProjectileEntity(world, iposition, stack);
        iprojectile.setPosition(iprojectile.posX+enumfacing.getXOffset()*1.25,iprojectile.posY+enumfacing.getYOffset()*1.25,iprojectile.posZ+enumfacing.getZOffset()*1.25);
        iprojectile.shoot((double)enumfacing.getXOffset(), (double)((float)enumfacing.getYOffset() + 0.1F), (double)enumfacing.getZOffset(), this.getProjectileVelocity(), this.getProjectileInaccuracy());
        world.spawnEntity((Entity)iprojectile);
        stack.shrink(1);
        return stack;
    }
    @Override
    protected void playDispenseSound(IBlockSource source)
    {
        source.getWorld().playSound(null,source.getBlockPos(),HBMSoundEvents.fatmanShoot,SoundCategory.BLOCKS,1,1);
    }
    @Override
    protected IProjectile getProjectileEntity(World world,IPosition position,ItemStack stackIn) {
        EntityBulletBase projectile = new EntityBulletBase(world,BulletConfigSyncingUtil.NUKE_HIGH);
        projectile.setPosition(position.getX(),position.getY(),position.getZ());
        return projectile;
    }
    protected float getProjectileInaccuracy()
    {
        return super.getProjectileInaccuracy() * 0.2F;
    }
    protected float getProjectileVelocity()
    {
        return super.getProjectileVelocity() * 9.0F;
    }
}
