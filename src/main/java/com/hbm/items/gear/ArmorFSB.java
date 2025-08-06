package com.hbm.items.gear;

import com.hbm.config.PotionConfig;
import com.hbm.items.ModItems;
import com.hbm.items.tool.ItemGeigerCounter;
import com.hbm.render.NTMRenderHelper;
import com.hbm.render.amlfrom1710.Vec3;
import com.hbm.util.I18nUtil;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

public class ArmorFSB extends ItemArmor {


    public List<PotionEffect> effects = new ArrayList<PotionEffect>();
    public boolean noHelmet = false;
    public boolean vats = false;
    public boolean thermal = false;
    public boolean geigerSound = false;
    public boolean customGeiger = false;
    public boolean hardLanding = false;
    public int dashCount = 0;
    public int stepSize = 0;
    public SoundEvent step;
    public SoundEvent jump;
    public SoundEvent fall;
    private String texture = "";
    private ResourceLocation overlay = null;


    public ArmorFSB(ArmorMaterial materialIn, int renderIndexIn, EntityEquipmentSlot equipmentSlotIn, String texture, String name) {
        super(materialIn, renderIndexIn, equipmentSlotIn);
        this.setTranslationKey(name);
        this.setRegistryName(name);
        this.texture = texture;

        ModItems.ALL_ITEMS.add(this);
    }

    public static boolean hasFSBArmor(EntityLivingBase entity) {
        if (entity == null)
            return false;

        ItemStack plate = entity.getItemStackFromSlot(EntityEquipmentSlot.CHEST);

        if (plate != null && plate.getItem() instanceof ArmorFSB) {

            ArmorFSB chestplate = (ArmorFSB) plate.getItem();
            boolean noHelmet = chestplate.noHelmet;

            for (EntityEquipmentSlot slot : EntityEquipmentSlot.values()) {
                if (slot == EntityEquipmentSlot.MAINHAND || slot == EntityEquipmentSlot.OFFHAND)
                    continue;
                if (noHelmet && slot == EntityEquipmentSlot.HEAD)
                    continue;
                ItemStack armor = entity.getItemStackFromSlot(slot);

                if (armor == null || !(armor.getItem() instanceof ArmorFSB))
                    return false;

                if (((ArmorFSB) armor.getItem()).getArmorMaterial() != chestplate.getArmorMaterial())
                    return false;

                if (!((ArmorFSB) armor.getItem()).isArmorEnabled(armor))
                    return false;
            }
            return true;
        }

        return false;
    }

    public static boolean hasFSBArmorHelmet(EntityLivingBase entity) {
        ItemStack plate = entity.getItemStackFromSlot(EntityEquipmentSlot.CHEST);

        if (plate != null && plate.getItem() instanceof ArmorFSB) {
            return !((ArmorFSB) plate.getItem()).noHelmet && hasFSBArmor(entity);
        }
        return false;
    }

    public static boolean hasFSBArmorIgnoreCharge(EntityLivingBase entity) {
        if (entity == null)
            return false;

        ItemStack plate = entity.getItemStackFromSlot(EntityEquipmentSlot.CHEST);

        if (plate != null && plate.getItem() instanceof ArmorFSB) {

            ArmorFSB chestplate = (ArmorFSB) plate.getItem();
            boolean noHelmet = chestplate.noHelmet;

            for (EntityEquipmentSlot slot : EntityEquipmentSlot.values()) {
                if (slot == EntityEquipmentSlot.MAINHAND || slot == EntityEquipmentSlot.OFFHAND)
                    continue;
                if (noHelmet && slot == EntityEquipmentSlot.HEAD)
                    continue;
                ItemStack armor = entity.getItemStackFromSlot(slot);

                if (armor == null || !(armor.getItem() instanceof ArmorFSB))
                    return false;

                if (((ArmorFSB) armor.getItem()).getArmorMaterial() != chestplate.getArmorMaterial())
                    return false;
            }
            return true;
        }

        return false;
    }


    public static void handleAttack(LivingAttackEvent event) {
    }

    public static void handleHurt(LivingHurtEvent event) {
    }

    public static void handleTick(TickEvent.PlayerTickEvent event) {
        handleTick(event.player, event.phase == Phase.START);
    }

    public static void handleTick(EntityLivingBase entity) {
        handleTick(entity, true);
    }

    public static void handleTick(EntityLivingBase entity, boolean isStart) {
        if (ArmorFSB.hasFSBArmor(entity)) {

            ItemStack plate = entity.getItemStackFromSlot(EntityEquipmentSlot.CHEST);

            ArmorFSB chestplate = (ArmorFSB) plate.getItem();

            if (!chestplate.effects.isEmpty()) {

                for (PotionEffect i : chestplate.effects) {
                    entity.addPotionEffect(new PotionEffect(i.getPotion(), i.getDuration(), i.getAmplifier(), i.getIsAmbient(), false));//Norwood: I prefer not to have particles show with armor on
                }
            }


            if (chestplate.step != null && entity.world.isRemote && entity.onGround && isStart && !entity.isSneaking()) {
                if (entity.getEntityData().getFloat("hfr_nextStepDistance") == 0) {
                    entity.getEntityData().setFloat("hfr_nextStepDistance", entity.nextStepDistance);
                }

                int px = MathHelper.floor(entity.posX);
                int py = MathHelper.floor(entity.posY - 0.2D);
                int pz = MathHelper.floor(entity.posZ);
                IBlockState block = entity.world.getBlockState(new BlockPos(px, py, pz));
                if (block.getMaterial() != Material.AIR && entity.getEntityData().getFloat("hfr_nextStepDistance") <= entity.distanceWalkedOnStepModified) {
                    entity.playSound(chestplate.step, 1.0F, 1.0F);
                }

                entity.getEntityData().setFloat("hfr_nextStepDistance", entity.nextStepDistance);
            }
        }
    }

    public static void handleJump(EntityLivingBase entity) {

        if (ArmorFSB.hasFSBArmor(entity)) {

            ArmorFSB chestplate = (ArmorFSB) entity.getItemStackFromSlot(EntityEquipmentSlot.CHEST).getItem();

            if (chestplate.jump != null)
                entity.playSound(chestplate.jump, 1.0F, 1.0F);
        }
    }

    public static void handleFall(EntityLivingBase entity) {

        if (ArmorFSB.hasFSBArmor(entity)) {

            ArmorFSB chestplate = (ArmorFSB) entity.getItemStackFromSlot(EntityEquipmentSlot.CHEST).getItem();

            if (chestplate.hardLanding && entity.fallDistance > 10) {

                List<Entity> entities = entity.world.getEntitiesWithinAABBExcludingEntity(entity, entity.getEntityBoundingBox().grow(3, 0, 3));

                for (Entity e : entities) {

                    Vec3 vec = Vec3.createVectorHelper(entity.posX - e.posX, 0, entity.posZ - e.posZ);

                    if (vec.length() < 3) {

                        double intensity = 3 - vec.length();
                        e.motionX += vec.xCoord * intensity * -2;
                        e.motionY += 0.1D * intensity;
                        e.motionZ += vec.zCoord * intensity * -2;

                        e.attackEntityFrom(DamageSource.causeIndirectDamage(e, entity).setDamageBypassesArmor(), (float) (intensity * 10));
                    }
                }
                // return;
            }

            if (chestplate.fall != null && entity.fallDistance > 0.25 && !entity.isSneaking()) {
                entity.playSound(chestplate.fall, 1.0F, 1.0F);
            }
        }
    }

    public void handleAttack(LivingAttackEvent event, ArmorFSB chestplate) {
    }

    public void handleHurt(LivingHurtEvent event, ArmorFSB chestplate) {
    }

    public boolean isArmorEnabled(ItemStack stack) {
        return true;
    }


    @Override
    public void onUpdate(ItemStack stack, World world, Entity e, int itemSlot, boolean isSelected) {

        if (this.armorType != EntityEquipmentSlot.CHEST || !(e instanceof EntityLivingBase))
            return;
        EntityLivingBase entity = (EntityLivingBase) e;
        if (!hasFSBArmor(entity))
            return;
        ArmorFSB fsbarmor = (ArmorFSB) entity.getItemStackFromSlot(EntityEquipmentSlot.CHEST).getItem();


        if (!fsbarmor.geigerSound || !(entity instanceof EntityPlayer))
            return;

        ItemGeigerCounter.playGeiger(world, (EntityPlayer) entity);
    }

    //For crazier stuff not possible without hooking the event
    @SideOnly(Side.CLIENT)
    public void handleOverlay(RenderGameOverlayEvent.Pre event, EntityPlayer player) {
    }

    public ArmorFSB enableThermalSight(boolean thermal) {
        this.thermal = thermal;
        return this;
    }

    public ArmorFSB setHasGeigerSound(boolean geiger) {
        this.geigerSound = geiger;
        return this;
    }

    public ArmorFSB setHasCustomGeiger(boolean geiger) {
        this.customGeiger = geiger;
        return this;
    }

    public ArmorFSB setHasHardLanding(boolean hardLanding) {
        this.hardLanding = hardLanding;
        return this;
    }


    public ArmorFSB setDashCount(int dashCount) {
        this.dashCount = dashCount;
        return this;
    }

    public ArmorFSB setStepSize(int stepSize) {
        this.stepSize = stepSize;
        return this;
    }

    public ArmorFSB setStep(SoundEvent step) {
        this.step = step;
        return this;
    }

    public ArmorFSB setJump(SoundEvent jump) {
        this.jump = jump;
        return this;
    }

    public ArmorFSB setFall(SoundEvent fall) {
        this.fall = fall;
        return this;
    }

    public ArmorFSB addEffect(PotionEffect effect) {
        if (!PotionConfig.doJumpBoost && effect.getPotion() == MobEffects.JUMP_BOOST)
            return this;
        effects.add(effect);
        return this;
    }

    public ArmorFSB setNoHelmet(boolean noHelmet) {
        this.noHelmet = noHelmet;
        return this;
    }

    public ArmorFSB enableVATS(boolean vats) {
        this.vats = vats;
        return this;
    }


    public ArmorFSB setOverlay(String path) {
        this.overlay = new ResourceLocation(path);
        return this;
    }

    public ArmorFSB cloneStats(ArmorFSB original) {
        //lists aren't being modified after instantiation, so there's no need to dereference
        this.effects = original.effects;
        this.noHelmet = original.noHelmet;
        this.vats = original.vats;
        this.thermal = original.thermal;
        this.geigerSound = original.geigerSound;
        this.customGeiger = original.customGeiger;
        this.hardLanding = original.hardLanding;
        this.dashCount = original.dashCount;
        this.stepSize = original.stepSize;
        this.step = original.step;
        this.jump = original.jump;
        this.fall = original.fall;
        //overlay doesn't need to be copied because it's helmet exclusive
        return this;
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type) {
        return texture;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, World worldIn, List<String> list, ITooltipFlag flagIn) {

        List toAdd = new ArrayList();

        if (!effects.isEmpty()) {
            List potionList = new ArrayList();
            for (PotionEffect effect : effects) {
                potionList.add(I18n.format(effect.getEffectName()));
            }

            toAdd.add(TextFormatting.AQUA + String.join(", ", potionList));
        }

        if (geigerSound) toAdd.add(TextFormatting.GOLD + "  " + I18nUtil.resolveKey("armor.geigerSound"));
        if (customGeiger) toAdd.add(TextFormatting.GOLD + "  " + I18nUtil.resolveKey("armor.geigerHUD"));
        if (vats) toAdd.add(TextFormatting.RED + "  " + I18nUtil.resolveKey("armor.vats"));
        if (thermal) toAdd.add(TextFormatting.RED + "  " + I18nUtil.resolveKey("armor.thermal"));
        if (hardLanding) toAdd.add(TextFormatting.RED + "  " + I18nUtil.resolveKey("armor.hardLanding"));
        if (stepSize != 0) toAdd.add(TextFormatting.BLUE + "  " + I18nUtil.resolveKey("armor.stepSize", stepSize));
        if (dashCount > 0) toAdd.add(TextFormatting.AQUA + "  " + I18nUtil.resolveKey("armor.dash", dashCount));

        if (!toAdd.isEmpty()) {
            list.add(TextFormatting.GOLD + I18nUtil.resolveKey("armor.fullSetBonus"));
            list.addAll(toAdd);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderHelmetOverlay(ItemStack stack, EntityPlayer player, ScaledResolution resolution, float partialTicks) {
        if (overlay == null)
            return;
        GlStateManager.disableDepth();
        GlStateManager.depthMask(false);
        GlStateManager.tryBlendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.disableAlpha();
        Minecraft.getMinecraft().getTextureManager().bindTexture(overlay);
        NTMRenderHelper.startDrawingTexturedQuads();
        NTMRenderHelper.addVertexWithUV(0.0D, (double) resolution.getScaledHeight(), -90.0D, 0.0D, 1.0D);
        NTMRenderHelper.addVertexWithUV((double) resolution.getScaledWidth(), (double) resolution.getScaledHeight(), -90.0D, 1.0D, 1.0D);
        NTMRenderHelper.addVertexWithUV((double) resolution.getScaledWidth(), 0.0D, -90.0D, 1.0D, 0.0D);
        NTMRenderHelper.addVertexWithUV(0.0D, 0.0D, -90.0D, 0.0D, 0.0D);
        NTMRenderHelper.draw();
        GlStateManager.depthMask(true);
        GlStateManager.enableDepth();
        GlStateManager.enableAlpha();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

}
