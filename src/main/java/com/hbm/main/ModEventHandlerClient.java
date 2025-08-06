package com.hbm.main;

import baubles.api.BaublesApi;
import com.google.common.collect.Queues;
import com.hbm.blocks.ILookOverlay;
import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.TrappedBrick.Trap;
import com.hbm.capability.HbmCapability;
import com.hbm.entity.mob.EntityHunterChopper;
import com.hbm.entity.projectile.EntityChopperMine;
import com.hbm.entity.siege.SiegeTier;
import com.hbm.forgefluid.SpecialContainerFillLists.EnumCanister;
import com.hbm.forgefluid.SpecialContainerFillLists.EnumCell;
import com.hbm.forgefluid.SpecialContainerFillLists.EnumGasCanister;
import com.hbm.handler.*;
import com.hbm.hazard.HazardSystem;
import com.hbm.interfaces.*;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.RecipesCommon.NbtComparableStack;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.gui.GUIArmorTable;
import com.hbm.inventory.recipes.ChemplantRecipes;
import com.hbm.inventory.recipes.SerializableRecipe;
import com.hbm.items.IDynamicModels;
import com.hbm.items.IModelRegister;
import com.hbm.items.ModItems;
import com.hbm.items.RBMKItemRenderers;
import com.hbm.items.armor.ItemArmorMod;
import com.hbm.items.armor.JetpackBase;
import com.hbm.items.gear.ArmorFSB;
import com.hbm.items.gear.RedstoneSword;
import com.hbm.items.machine.*;
import com.hbm.items.machine.ItemCassette.TrackType;
import com.hbm.items.special.*;
import com.hbm.items.special.weapon.GunB92;
import com.hbm.items.tool.ItemCanister;
import com.hbm.items.tool.ItemGasCanister;
import com.hbm.items.tool.ItemGuideBook;
import com.hbm.items.weapon.*;
import com.hbm.items.weapon.sedna.GunConfig;
import com.hbm.items.weapon.sedna.ItemGunBaseNT;
import com.hbm.items.weapon.sedna.ItemGunBaseSedna;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.lib.Library;
import com.hbm.lib.RecoilHandler;
import com.hbm.lib.RefStrings;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toserver.AuxButtonPacket;
import com.hbm.packet.toserver.GunButtonPacket;
import com.hbm.packet.toserver.MeathookJumpPacket;
import com.hbm.particle.ParticleBatchRenderer;
import com.hbm.particle.ParticleFirstPerson;
import com.hbm.particle.gluon.ParticleGluonBurnTrail;
import com.hbm.physics.ParticlePhysicsBlocks;
import com.hbm.render.LightRenderer;
import com.hbm.render.NTMRenderHelper;
import com.hbm.render.anim.HbmAnimations;
import com.hbm.render.anim.HbmAnimations.Animation;
import com.hbm.render.anim.HbmAnimations.BlenderAnimation;
import com.hbm.render.anim.sedna.HbmAnimationsSedna;
import com.hbm.render.item.BakedModelCustom;
import com.hbm.render.item.BakedModelNoGui;
import com.hbm.render.item.TEISRBase;
import com.hbm.render.item.TemplateBakedModel;
import com.hbm.render.item.weapon.*;
import com.hbm.render.item.weapon.sedna.ItemRenderWeaponBase;
import com.hbm.render.misc.BeamPronter;
import com.hbm.render.misc.RenderAccessoryUtility;
import com.hbm.render.misc.RenderScreenOverlay;
import com.hbm.render.misc.SoyuzPronter;
import com.hbm.render.modelrenderer.EgonBackpackRenderer;
import com.hbm.render.tileentity.*;
import com.hbm.render.util.RenderOverhead;
import com.hbm.sound.*;
import com.hbm.sound.MovingSoundPlayerLoop.EnumHbmSound;
import com.hbm.tileentity.bomb.TileEntityNukeCustom;
import com.hbm.tileentity.bomb.TileEntityNukeCustom.CustomNukeEntry;
import com.hbm.tileentity.bomb.TileEntityNukeCustom.EnumEntryType;
import com.hbm.tileentity.machine.rbmk.TileEntityRBMKBase;
import com.hbm.util.*;
import com.hbm.util.ArmorRegistry.HazardClass;
import com.hbm.wiaj.GuiWorldInAJar;
import com.hbm.wiaj.cannery.CanneryBase;
import com.hbm.wiaj.cannery.Jars;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelBiped.ArmPose;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumHand;
import net.minecraft.util.MovementInput;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.*;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.registry.IRegistry;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.event.*;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.RenderTickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.Project;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.*;

public class ModEventHandlerClient {

    public static final int flashDuration = 5_000;
    public static final int shakeDuration = 1_500;
    private static final ResourceLocation poster = new ResourceLocation(RefStrings.MODID + ":textures/models/misc/poster.png");
    public static Set<EntityLivingBase> specialDeathEffectEntities = new HashSet<>();
    public static ArrayDeque<ParticleFirstPerson> firstPersonAuxParticles = Queues.newArrayDeque();
    public static float deltaMouseX;
    public static float deltaMouseY;
    public static float currentFOV = 70;
    public static long flashTimestamp;
    public static long shakeTimestamp;
    public static TextureAtlasSprite contrail;
    public static TextureAtlasSprite particle_base;
    public static TextureAtlasSprite fog;
    public static TextureAtlasSprite uv_debug;
    public static TextureAtlasSprite debugPower;
    public static TextureAtlasSprite debugFluid;
    private static long canneryTimestamp;
    private static ComparableStack lastCannery = null;
    FloatBuffer MODELVIEW = GLAllocation.createDirectFloatBuffer(16);
    FloatBuffer PROJECTION = GLAllocation.createDirectFloatBuffer(16);
    IntBuffer VIEWPORT = GLAllocation.createDirectIntBuffer(16);
    FloatBuffer POSITION = GLAllocation.createDirectFloatBuffer(4);

    public static void updateMouseDelta() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.inGameHasFocus && Display.isActive()) {
            mc.mouseHelper.mouseXYChange();
            float f = mc.gameSettings.mouseSensitivity * 0.6F + 0.2F;
            float f1 = f * f * f * 8.0F;
            deltaMouseX = (float) mc.mouseHelper.deltaX * f1;
            deltaMouseY = (float) mc.mouseHelper.deltaY * f1;
        } else {
            deltaMouseX = 0;
            deltaMouseY = 0;
        }
    }

    public static void registerBedrockOreModels() {
        ResourceLocation[] list = new ResourceLocation[300];
        for (int i = 0; i < list.length; i++) {
            ModelLoader.setCustomModelResourceLocation(ModItems.ore_bedrock, i, new ModelResourceLocation(ModItems.ore_bedrock.getRegistryName(), "inventory"));
            ModelLoader.setCustomModelResourceLocation(ModItems.ore_bedrock_centrifuged, i, new ModelResourceLocation(ModItems.ore_bedrock_centrifuged.getRegistryName(), "inventory"));
            ModelLoader.setCustomModelResourceLocation(ModItems.ore_bedrock_cleaned, i, new ModelResourceLocation(ModItems.ore_bedrock_cleaned.getRegistryName(), "inventory"));
            ModelLoader.setCustomModelResourceLocation(ModItems.ore_bedrock_separated, i, new ModelResourceLocation(ModItems.ore_bedrock_separated.getRegistryName(), "inventory"));
            ModelLoader.setCustomModelResourceLocation(ModItems.ore_bedrock_purified, i, new ModelResourceLocation(ModItems.ore_bedrock_purified.getRegistryName(), "inventory"));
            ModelLoader.setCustomModelResourceLocation(ModItems.ore_bedrock_nitrated, i, new ModelResourceLocation(ModItems.ore_bedrock_nitrated.getRegistryName(), "inventory"));
            ModelLoader.setCustomModelResourceLocation(ModItems.ore_bedrock_nitrocrystalline, i, new ModelResourceLocation(ModItems.ore_bedrock_nitrocrystalline.getRegistryName(), "inventory"));
            ModelLoader.setCustomModelResourceLocation(ModItems.ore_bedrock_deepcleaned, i, new ModelResourceLocation(ModItems.ore_bedrock_deepcleaned.getRegistryName(), "inventory"));
            ModelLoader.setCustomModelResourceLocation(ModItems.ore_bedrock_seared, i, new ModelResourceLocation(ModItems.ore_bedrock_seared.getRegistryName(), "inventory"));
            ModelLoader.setCustomModelResourceLocation(ModItems.ore_bedrock_enriched, i, new ModelResourceLocation(ModItems.ore_bedrock_enriched.getRegistryName(), "inventory"));
            ModelLoader.setCustomModelResourceLocation(ModItems.ore_bedrock_exquisite, i, new ModelResourceLocation(ModItems.ore_bedrock_exquisite.getRegistryName(), "inventory"));
            ModelLoader.setCustomModelResourceLocation(ModItems.ore_bedrock_perfect, i, new ModelResourceLocation(ModItems.ore_bedrock_perfect.getRegistryName(), "inventory"));
        }

    }

    public static void swapModels(Item item, IRegistry<ModelResourceLocation, IBakedModel> reg) {
        ModelResourceLocation loc = new ModelResourceLocation(item.getRegistryName(), "inventory");
        IBakedModel model = reg.getObject(loc);
        TileEntityItemStackRenderer render = item.getTileEntityItemStackRenderer();
        if (render instanceof TEISRBase) {
            ((TEISRBase) render).itemModel = model;
            reg.putObject(loc, new BakedModelCustom((TEISRBase) render));
        }
    }

    public static void swapModelsNoGui(Item item, IRegistry<ModelResourceLocation, IBakedModel> reg) {
        ModelResourceLocation loc = new ModelResourceLocation(item.getRegistryName(), "inventory");
        IBakedModel model = reg.getObject(loc);
        TileEntityItemStackRenderer render = item.getTileEntityItemStackRenderer();
        if (render instanceof TEISRBase) {
            ((TEISRBase) render).itemModel = model;
            reg.putObject(loc, new BakedModelNoGui((TEISRBase) render));
        }
    }


    public static ItemStack getMouseOverStack() {

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.currentScreen instanceof GuiContainer container) {

            ScaledResolution scaledresolution = new ScaledResolution(mc);
            int width = scaledresolution.getScaledWidth();
            int height = scaledresolution.getScaledHeight();
            int mouseX = Mouse.getX() * width / mc.displayWidth;
            int mouseY = height - Mouse.getY() * height / mc.displayHeight - 1;

            for (Slot slot : container.inventorySlots.inventorySlots) {
                if (slot.getHasStack() && container.isMouseOverSlot(slot, mouseX, mouseY)) {
                    return slot.getStack();
                }
            }
        }

        return null;
    }

    @SubscribeEvent
    public void registerModels(ModelRegistryEvent event) {
        int i = 0;
        ResourceLocation[] list = new ResourceLocation[EnumCell.values().length];
        for (EnumCell e : EnumCell.values()) {
            list[i] = e.getResourceLocation();
            i++;
        }
        ModelLoader.registerItemVariants(ModItems.cell, list);

        ModelResourceLocation[] locations = new ModelResourceLocation[ItemAmmoArty.itemTypes.length];
        for (i = 0; i < ItemAmmoArty.itemTypes.length; i++) {
            locations[i] = new ModelResourceLocation(ModItems.ammo_arty.getRegistryName() + "_" + i, "inventory");
        }
        ModelLoader.registerItemVariants(ModItems.ammo_arty, locations);

        FluidType[] order = Fluids.getInNiceOrder();
        for (i = 0; i < order.length; i++) {
            if (!order[i].hasNoID()) {
                ModelLoader.setCustomModelResourceLocation(ModItems.forge_fluid_identifier, order[i].getID(),
                        ItemForgeFluidIdentifier.identifierModel);
                if (order[i].getContainer(Fluids.CD_Canister.class) != null) {
//                    ModelLoader.setCustomModelResourceLocation(ModItems.canister_generic, order[i].getID(),
//                            FluidCanisterRender.INSTANCE.setModelLocation(ItemCanister.getStackFromFluid(order[i])));
                }

                ModelLoader.setCustomModelResourceLocation(ModItems.ff_fluid_duct, order[i].getID(), ItemFFFluidDuct.ductLoc);
                if (order[i].getContainer(Fluids.CD_Gastank.class) != null) {
                    ModelLoader.setCustomModelResourceLocation(ModItems.gas_full, order[i].getID(), ItemGasCanister.gasCansiterFullModel);
                }
                ModelLoader.setCustomModelResourceLocation(ModItems.fluid_tank_lead_full, order[i].getID(),
                        ItemFluidTank.fluidTankLeadFullModel);
                ModelLoader.setCustomModelResourceLocation(ModItems.fluid_tank_full, order[i].getID(),
                        ItemFluidTank.fluidTankModel);
                ModelLoader.setCustomModelResourceLocation(ModItems.fluid_barrel_full, order[i].getID(),
                        ItemFluidTank.fluidBarrelModel);
                ModelLoader.setCustomModelResourceLocation(ModItems.fluid_icon, order[i].getID(),
                        ItemFluidIcon.fluidIconModel);
            }
        }
        ModelLoader.setCustomModelResourceLocation(ModItems.canister_empty, 0, ItemCanister.fluidCanisterModel);
        ModelLoader.setCustomModelResourceLocation(ModItems.icf_pellet, 0, new ModelResourceLocation(ModItems.icf_pellet.getRegistryName(),
"inventory"));

        for (Item item : ModItems.ALL_ITEMS) {
            try {
                registerModel(item, 0);
            } catch (NullPointerException e) {
                e.printStackTrace();
                MainRegistry.logger.info("Failed to register model for " + item.getRegistryName());
            }
        }
        for (Block block : ModBlocks.ALL_BLOCKS) {
            registerBlockModel(block, 0);
        }


        ((ItemBedrockOreNew) ModItems.bedrock_ore).registerModels();
        ((ItemAmmoArty) ModItems.ammo_arty).registerModels();
        ((ItemMold) ModItems.mold).registerModels();
        IDynamicModels.registerModels();
        IDynamicModels.registerCustomStateMappers();
        IMetaItemTesr.redirectModels();
        //FIXME: this is a dogshit solution


        for (ItemAutogen item : ItemAutogen.INSTANCES) {
            item.registerModels();
        }
        registerBedrockOreModels();
    }

    private void registerBlockModel(Block block, int meta) {
        registerModel(Item.getItemFromBlock(block), meta);
    }

    private void registerModel(Item item, int meta) {
        if (item == Items.AIR)
            return;

        //Drillgon200: I hate myself for making this
        //Th3_Sl1ze: Don't worry, I hate myself too
        if (item == ModItems.chemistry_template) {
            ChemplantRecipes.register();
        }


        if (item instanceof ItemDepletedFuel) {
            for (int i = 0; i <= 1; i++) {
                ModelLoader.setCustomModelResourceLocation(item, i,
                        new ModelResourceLocation(Objects.requireNonNull(item.getRegistryName()), "inventory"));
            }
        }
        if (item instanceof IModelRegister)
            ((IModelRegister) item).registerModels();

        if (item == ModItems.chemistry_icon) {
            for (int i : ChemplantRecipes.recipeNames.keySet()) {
                ModelLoader.setCustomModelResourceLocation(item, i, new ModelResourceLocation(RefStrings.MODID + ":chem_icon_" + ChemplantRecipes.getName(i).toLowerCase(), "inventory"));
            }
        } else if (item == ModItems.chemistry_template) {
            for (int i : ChemplantRecipes.recipeNames.keySet()) {
                ModelLoader.setCustomModelResourceLocation(item, i, new ModelResourceLocation(item.getRegistryName(), "inventory"));
            }
        } else if (item == ModItems.crucible_template) {
            for (int i = 0; i < 32; i++) { // FIXME: figure out a better way of doing this
                ModelLoader.setCustomModelResourceLocation(item, i, new ModelResourceLocation(item.getRegistryName(), "inventory"));
            }
        } else if (item == ModItems.siren_track) {
            for (int i = 0; i < TrackType.values().length; i++) {
                ModelLoader.setCustomModelResourceLocation(item, i, new ModelResourceLocation(item.getRegistryName(), "inventory"));
            }
        } else if (item == ModItems.ingot_u238m2) {
            ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
            ModelLoader.setCustomModelResourceLocation(item, 1, new ModelResourceLocation(RefStrings.MODID + ":hs-elements", "inventory"));
            ModelLoader.setCustomModelResourceLocation(item, 2, new ModelResourceLocation(RefStrings.MODID + ":hs-arsenic", "inventory"));
            ModelLoader.setCustomModelResourceLocation(item, 3, new ModelResourceLocation(RefStrings.MODID + ":hs-vault", "inventory"));
        } else if (item == ModItems.polaroid || item == ModItems.glitch) {
            ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName() + "_" + MainRegistry.polaroidID, "inventory"));
        } else if (item == Item.getItemFromBlock(ModBlocks.brick_jungle_glyph)) {
            for (int i = 0; i < 16; i++)
                ModelLoader.setCustomModelResourceLocation(item, i, new ModelResourceLocation(item.getRegistryName().toString() + i, "inventory"));
        } else if (item == Item.getItemFromBlock(ModBlocks.brick_jungle_trap)) {
            for (int i = 0; i < Trap.values().length; i++)
                ModelLoader.setCustomModelResourceLocation(item, i, new ModelResourceLocation(item.getRegistryName(), "inventory"));
        } else if (item instanceof ItemGuideBook) {
            for (int i = 0; i < ItemGuideBook.BookType.values().length; i++)
                ModelLoader.setCustomModelResourceLocation(item, i, new ModelResourceLocation(item.getRegistryName(), "inventory"));
        } else if (item instanceof ItemHot) {
            for (int i = 0; i < 15; i++)
                ModelLoader.setCustomModelResourceLocation(item, i, new ModelResourceLocation(item.getRegistryName(), "inventory"));
            ModelLoader.setCustomModelResourceLocation(item, 15, new ModelResourceLocation(item.getRegistryName() + "_hot", "inventory"));
        } else if (item instanceof ItemRBMKPellet) {
            for (int xe = 0; xe < 2; xe++) {
                for (int en = 0; en < 5; en++) {
                    ModelLoader.setCustomModelResourceLocation(item, en + xe * 5, new ModelResourceLocation(item.getRegistryName() + "_e" + en + (xe > 0 ? "_xe" : ""), "inventory"));
                }
            }
        } else if (item instanceof ItemWasteLong) {
            for (int i = 0; i < ItemWasteLong.WasteClass.values().length; i++) {
                ModelLoader.setCustomModelResourceLocation(item, i, new ModelResourceLocation(item.getRegistryName(), "inventory"));
            }
        } else if (item instanceof ItemWasteShort) {
            for (int i = 0; i < ItemWasteShort.WasteClass.values().length; i++) {
                ModelLoader.setCustomModelResourceLocation(item, i, new ModelResourceLocation(item.getRegistryName(), "inventory"));
            }
        } else if (item == ModItems.coin_siege) {
            for (int i = 0; i < SiegeTier.getLength(); i++) {
                ModelLoader.setCustomModelResourceLocation(item, i, new ModelResourceLocation(RefStrings.MODID + ":coin_siege_" + SiegeTier.tiers[i].name, "inventory"));
            }
        } else if (item == Item.getItemFromBlock(ModBlocks.volcano_core)) {
            for (int i = 0; i < 4; i++) {
                ModelLoader.setCustomModelResourceLocation(item, i, new ModelResourceLocation(item.getRegistryName(), "inventory"));
            }
        } else if (item == ModItems.fluid_identifier_multi) {
            ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
        } else if (item instanceof IHasCustomModel) {
            ModelLoader.setCustomModelResourceLocation(item, meta, ((IHasCustomModel) item).getResourceLocation());
        } else if (item instanceof IDynamicModels) { // we are literally registering them manually, why do it twice?..
        } else {
            ModelLoader.setCustomModelResourceLocation(item, meta, new ModelResourceLocation(item.getRegistryName(), "inventory"));
        }
    }

    @SubscribeEvent
    public void modelBaking(ModelBakeEvent evt) {
        ItemBedrockOreNew.bakeModels(evt);
        ItemAutogen.bakeModels(evt);
        ItemMold.bakeModels(evt);
        IDynamicModels.bakeModels(evt);

        for (EnumCanister e : EnumCanister.values()) {
            Object o = evt.getModelRegistry().getObject(e.getResourceLocation());
            if (o instanceof IBakedModel)
                e.putRenderModel((IBakedModel) o);
        }
        for (EnumCell cellType : EnumCell.values()) {
            FluidType fluid = cellType.getFluid();
            int meta = (fluid == null) ? 0 : fluid.getID();
            ModelLoader.setCustomModelResourceLocation(
                    ModItems.cell,
                    meta,
                    cellType.getResourceLocation()
            );
        }
        for (EnumGasCanister e : EnumGasCanister.values()) {
            Object o = evt.getModelRegistry().getObject(e.getResourceLocation());
            if (o instanceof IBakedModel)
                e.putRenderModel((IBakedModel) o);
        }

        // Drillgon200: Sigh... find a better custom model loading system.
        // Drillgon200: Removed todo, found a better way. Now I just have to
        // deal with all these ugly things. That can wait.
        ResourceManager.init();
        Object object1 = evt.getModelRegistry().getObject(RedstoneSword.rsModel);
        if (object1 instanceof IBakedModel) {
            IBakedModel model = (IBakedModel) object1;
            ItemRedstoneSwordRender.INSTANCE.itemModel = model;
            evt.getModelRegistry().putObject(RedstoneSword.rsModel, new ItemRenderRedstoneSword());
        }
        wrapModel(evt, ItemAssemblyTemplate.location);
        wrapModel(evt, ItemChemistryTemplate.location);
        wrapModel(evt, ItemCrucibleTemplate.location);
        Object object3 = evt.getModelRegistry().getObject(GunB92.b92Model);
        if (object3 instanceof IBakedModel) {
            IBakedModel model = (IBakedModel) object3;
            ItemRenderGunAnim.INSTANCE.b92ItemModel = model;
            evt.getModelRegistry().putObject(GunB92.b92Model, new B92BakedModel());
        }
        /*
Object object6 = evt.getModelRegistry().getObject(com.hbm.items.tool.ItemCanister.fluidCanisterModel);
        if (object6 instanceof IBakedModel) {
            IBakedModel model = (IBakedModel) object6;
            FluidCanisterRender.INSTANCE.itemModel = model;
            evt.getModelRegistry().putObject(ItemCanister.fluidCanisterModel, new FluidCanisterBakedModel());
        }
*/

        IRegistry<ModelResourceLocation, IBakedModel> reg = evt.getModelRegistry();
        swapModelsNoGui(ModItems.gun_b93, reg);
        swapModelsNoGui(ModItems.gun_supershotgun, reg);
        swapModels(ModItems.cell, reg);
        swapModels(ModItems.gas_empty, reg);
        swapModelsNoGui(ModItems.multitool_dig, reg);
        swapModelsNoGui(ModItems.multitool_silk, reg);
        swapModelsNoGui(ModItems.multitool_ext, reg);
        swapModelsNoGui(ModItems.multitool_miner, reg);
        swapModelsNoGui(ModItems.multitool_hit, reg);
        swapModelsNoGui(ModItems.multitool_beam, reg);
        swapModelsNoGui(ModItems.multitool_sky, reg);
        swapModelsNoGui(ModItems.multitool_mega, reg);
        swapModelsNoGui(ModItems.multitool_joule, reg);
        swapModelsNoGui(ModItems.multitool_decon, reg);
        swapModelsNoGui(ModItems.big_sword, reg);
        swapModelsNoGui(ModItems.shimmer_sledge, reg);
        swapModelsNoGui(ModItems.shimmer_axe, reg);
        swapModels(ModItems.ff_fluid_duct, reg);
        swapModels(ModItems.fluid_icon, reg);
        swapModelsNoGui(ModItems.stopsign, reg);
        swapModelsNoGui(ModItems.sopsign, reg);
        swapModelsNoGui(ModItems.chernobylsign, reg);
        swapModels(Item.getItemFromBlock(ModBlocks.radiorec), reg);
        swapModels(ModItems.gun_vortex, reg);
        swapModelsNoGui(ModItems.wood_gavel, reg);
        swapModelsNoGui(ModItems.lead_gavel, reg);
        swapModelsNoGui(ModItems.diamond_gavel, reg);
        swapModelsNoGui(ModItems.mese_gavel, reg);
        swapModels(ModItems.ingot_steel_dusted, reg);
        swapModels(ModItems.ingot_chainsteel, reg);
        swapModels(ModItems.ingot_meteorite, reg);
        swapModels(ModItems.ingot_meteorite_forged, reg);
        swapModels(ModItems.blade_meteorite, reg);
        swapModels(ModItems.crucible, reg);
        swapModels(ModItems.hs_sword, reg);
        swapModels(ModItems.hf_sword, reg);
        swapModels(ModItems.gun_egon, reg);
        swapModels(ModItems.jshotgun, reg);

        swapModels(ModItems.meteorite_sword_seared, reg);
        swapModels(ModItems.meteorite_sword_reforged, reg);
        swapModels(ModItems.meteorite_sword_hardened, reg);
        swapModels(ModItems.meteorite_sword_alloyed, reg);
        swapModels(ModItems.meteorite_sword_machined, reg);
        swapModels(ModItems.meteorite_sword_treated, reg);
        swapModels(ModItems.meteorite_sword_etched, reg);
        swapModels(ModItems.meteorite_sword_bred, reg);
        swapModels(ModItems.meteorite_sword_irradiated, reg);
        swapModels(ModItems.meteorite_sword_fused, reg);
        swapModels(ModItems.meteorite_sword_baleful, reg);
        swapModels(ModItems.meteorite_sword_warped, reg);
        swapModels(ModItems.meteorite_sword_demonic, reg);

        swapModels(ModItems.ore_bedrock, reg);
        swapModels(ModItems.ore_bedrock_centrifuged, reg);
        swapModels(ModItems.ore_bedrock_cleaned, reg);
        swapModels(ModItems.ore_bedrock_separated, reg);
        swapModels(ModItems.ore_bedrock_purified, reg);
        swapModels(ModItems.ore_bedrock_nitrated, reg);
        swapModels(ModItems.ore_bedrock_nitrocrystalline, reg);
        swapModels(ModItems.ore_bedrock_deepcleaned, reg);
        swapModels(ModItems.ore_bedrock_seared, reg);
        swapModels(ModItems.ore_bedrock_enriched, reg);
        swapModels(ModItems.ore_bedrock_exquisite, reg);
        swapModels(ModItems.ore_bedrock_perfect, reg);
        swapModelsNoGui(ModItems.bedrock_ore, reg);
        swapModels(ModItems.detonator_laser, reg);
        swapModels(ModItems.boltgun, reg);

        swapModels(ModItems.forge_fluid_identifier, reg);
        swapModels(ModItems.fluid_barrel_full, reg);
        swapModels(ModItems.fluid_tank_full, reg);
        swapModels(ModItems.fluid_tank_lead_full, reg);

        swapModels(ModItems.ammo_himars, reg);
        swapModels(ModItems.jetpack_glider, reg);
        swapModels(ModItems.gear_large, reg);

        for (Item item : RBMKItemRenderers.itemRenderers.keySet()) {
            swapModels(item, reg);
        }

        for (Object renderer : TileEntityRendererDispatcher.instance.renderers.values()) {
            if (renderer instanceof IItemRendererProvider prov) {
                for (Item item : prov.getItemsForRenderer()) {
                    swapModels(item, reg);
                }
            }
        }

        MainRegistry.proxy.registerMissileItems(reg);
    }

    @SubscribeEvent
    public void itemColorsEvent(ColorHandlerEvent.Item evt) {
        evt.getItemColors().registerItemColorHandler((ItemStack stack, int tintIndex) -> {
            if (tintIndex == 1) {
                int j = TrackType.getEnum(stack.getItemDamage()).getColor();

                if (j < 0) {
                    j = 0xFFFFFF;
                }

                return j;
            }
            return 0xFFFFFF;
        }, ModItems.siren_track);
        evt.getItemColors().registerItemColorHandler((stack, tintIndex) -> {
            if (tintIndex == 0) {
                ItemICFPellet.EnumICFFuel type1 = ItemICFPellet.getType(stack, true);
                ItemICFPellet.EnumICFFuel type2 = ItemICFPellet.getType(stack, false);
                int r = (((type1.color & 0xff0000) >> 16) + ((type2.color & 0xff0000) >> 16)) / 2;
                int g = (((type1.color & 0x00ff00) >> 8) + ((type2.color & 0x00ff00) >> 8)) / 2;
                int b = ((type1.color & 0x0000ff) + (type2.color & 0x0000ff)) / 2;
                return (r << 16) | (g << 8) | b;
            }
            return 0xFFFFFF;
        }, ModItems.icf_pellet);
        evt.getItemColors().registerItemColorHandler((stack, tintIndex) -> {
            if (tintIndex == 1) {
                return Fluids.fromID(stack.getMetadata()).getColor();
            }
            return 0xFFFFFF;
        }, ModItems.fluid_tank_full);
        evt.getItemColors().registerItemColorHandler((stack, tintIndex) -> {
            if (tintIndex == 1) {
                return Fluids.fromID(stack.getMetadata()).getColor();
            }
            return 0xFFFFFF;
        }, ModItems.fluid_tank_lead_full);
        evt.getItemColors().registerItemColorHandler((stack, tintIndex) -> {
            if (tintIndex == 1) {
                return Fluids.fromID(stack.getMetadata()).getColor();
            }
            return 0xFFFFFF;
        }, ModItems.fluid_barrel_full);
        evt.getItemColors().registerItemColorHandler((stack, tintIndex) -> {
            if (tintIndex == 1) {
                return Fluids.fromID(stack.getMetadata()).getColor();
            }
            return 0xFFFFFF;
        }, ModItems.forge_fluid_identifier);
        // mlbv: this is far easier than writing TEISR for each item. replace all color related rendering with this if applicable.
    }

    @SubscribeEvent
    public void textureStitch(TextureStitchEvent.Pre evt) {
        TextureMap map = evt.getMap();
        ItemBedrockOreNew.registerSprites(map);
        ItemMold.registerSprites(map);
        ItemAutogen.registerSprites(map);

        IDynamicModels.registerSprites(map);


        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/steam_still"));
        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/steam_flowing"));
        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/hotsteam_still"));
        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/hotsteam_flowing"));
        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/superhotsteam_still"));
        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/superhotsteam_flowing"));
        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/ultrahotsteam_still"));
        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/ultrahotsteam_flowing"));
        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/coolant_still"));
        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/coolant_flowing"));
        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/hotcoolant_still"));
        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/hotcoolant_flowing"));

        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/heavywater_still"));
        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/heavywater_flowing"));
        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/deuterium_still"));
        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/deuterium_flowing"));
        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/tritium_still"));
        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/tritium_flowing"));

        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/oil_still"));
        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/oil_flowing"));
        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/hotoil_still"));
        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/hotoil_flowing"));

        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/crackoil_still"));
        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/crackoil_flowing"));
        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/hotcrackoil_still"));
        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/hotcrackoil_flowing"));

        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/heavyoil_still"));
        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/heavyoil_flowing"));
        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/bitumen_still"));
        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/bitumen_flowing"));
        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/smear_still"));
        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/smear_flowing"));
        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/heatingoil_still"));
        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/heatingoil_flowing"));

        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/reclaimed_still"));
        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/reclaimed_flowing"));
        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/petroil_still"));
        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/petroil_flowing"));

        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/fracksol_still"));
        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/fracksol_flowing"));

        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/lubricant_still"));
        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/lubricant_flowing"));

        // Yes yes I know, I spelled 'naphtha' wrong.
        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/napatha_still"));
        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/napatha_flowing"));
        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/diesel_still"));
        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/diesel_flowing"));

        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/lightoil_still"));
        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/lightoil_flowing"));
        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/kerosene_still"));
        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/kerosene_flowing"));

        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/gas_still"));
        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/gas_flowing"));
        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/petroleum_still"));
        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/petroleum_flowing"));

        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/aromatics_still"));
        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/aromatics_flowing"));
        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/unsaturateds_still"));
        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/unsaturateds_flowing"));

        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/biogas_still"));
        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/biogas_flowing"));
        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/biofuel_still"));
        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/biofuel_flowing"));

        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/ethanol_still"));
        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/ethanol_flowing"));
        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/fishoil_still"));
        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/fishoil_flowing"));
        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/sunfloweroil_still"));
        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/sunfloweroil_flowing"));
        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/colloid_still"));
        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/colloid_flowing"));

        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/nitan_still"));
        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/nitan_flowing"));

        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/uf6_still"));
        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/uf6_flowing"));
        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/puf6_still"));
        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/puf6_flowing"));
        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/sas3_still"));
        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/sas3_flowing"));

        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/amat_still"));
        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/amat_flowing"));
        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/aschrab_still"));
        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/aschrab_flowing"));

        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/acid_still"));
        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/acid_flowing"));
        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/sulfuric_acid_still"));
        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/sulfuric_acid_flowing"));
        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/nitric_acid_still"));
        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/nitric_acid_flowing"));
        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/solvent_still"));
        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/solvent_flowing"));
        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/radiosolvent_still"));
        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/radiosolvent_flowing"));
        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/nitroglycerin_still"));
        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/nitroglycerin_flowing"));
        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/liquid_osmiridium_still"));
        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/liquid_osmiridium_flowing"));
        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/watz_still"));
        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/watz_flowing"));
        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/cryogel_still"));
        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/cryogel_flowing"));

        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/hydrogen_still"));
        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/hydrogen_flowing"));
        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/oxygen_still"));
        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/oxygen_flowing"));
        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/xenon_still"));
        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/xenon_flowing"));
        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/balefire_still"));
        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/balefire_flowing"));

        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/mercury_still"));
        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/mercury_flowing"));

        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/plasma_dt_still"));
        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/plasma_dt_flowing"));
        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/plasma_hd_still"));
        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/plasma_hd_flowing"));
        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/plasma_ht_still"));
        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/plasma_ht_flowing"));
        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/plasma_put_still"));
        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/plasma_put_flowing"));
        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/plasma_xm_still"));
        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/plasma_xm_flowing"));
        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/plasma_bf_still"));
        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/plasma_bf_flowing"));
        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/uu_still"));
        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/uu_flowing"));

        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/gasoline_still"));
        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/gasoline_flowing"));
        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/experience_still"));
        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/experience_flowing"));
        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/spentsteam_still"));
        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/spentsteam_flowing"));
        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/pain_still"));
        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/pain_flowing"));
        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/wastefluid_still"));
        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/wastefluid_flowing"));
        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/wastegas_still"));
        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/wastegas_flowing"));
        //More shit to the pile
        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/gas_default"));
        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/fluid_default_still"));
        map.registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/fluid_viscous_default_still"));

        //Debug stuff
        debugPower = map.registerSprite(new ResourceLocation(RefStrings.MODID, "particle/debug_power"));
        debugFluid = map.registerSprite(new ResourceLocation(RefStrings.MODID, "particle/debug_fluid"));
        uv_debug = map.registerSprite(new ResourceLocation(RefStrings.MODID, "misc/uv_debug"));


        contrail = map.registerSprite(new ResourceLocation(RefStrings.MODID + ":particle/contrail"));
        particle_base = map.registerSprite(new ResourceLocation(RefStrings.MODID, "particle/particle_base"));
        fog = map.registerSprite(new ResourceLocation(RefStrings.MODID, "particle/fog"));

        map.registerSprite(new ResourceLocation(RefStrings.MODID, "items/ore_bedrock_layer"));
        map.registerSprite(new ResourceLocation(RefStrings.MODID, "items/fluid_identifier_overlay"));
        map.registerSprite(new ResourceLocation(RefStrings.MODID, "items/fluid_barrel_overlay"));
        map.registerSprite(new ResourceLocation(RefStrings.MODID, "items/fluid_tank_overlay"));
        map.registerSprite(new ResourceLocation(RefStrings.MODID, "items/fluid_tank_lead_overlay"));
        map.registerSprite(new ResourceLocation(RefStrings.MODID, "items/fluid_icon"));
    }

    @SubscribeEvent
    public void textureStitchPost(TextureStitchEvent.Post evt) {
        RenderStructureMarker.fac_ti[0][0] = evt.getMap().getAtlasSprite(RefStrings.MODID + ":blocks/factory_titanium_hull");
        RenderStructureMarker.fac_ti[0][1] = evt.getMap().getAtlasSprite(RefStrings.MODID + ":blocks/factory_titanium_hull");
        RenderStructureMarker.fac_ti[1][0] = evt.getMap().getAtlasSprite(RefStrings.MODID + ":blocks/factory_titanium_hull");
        RenderStructureMarker.fac_ti[1][1] = evt.getMap().getAtlasSprite(RefStrings.MODID + ":blocks/factory_titanium_furnace");
        RenderStructureMarker.fac_ti[2][0] = evt.getMap().getAtlasSprite(RefStrings.MODID + ":blocks/factory_titanium_core");
        RenderStructureMarker.fac_ti[2][1] = evt.getMap().getAtlasSprite(RefStrings.MODID + ":blocks/factory_titanium_core");


        RenderStructureMarker.fusion[0][0] = evt.getMap().getAtlasSprite(RefStrings.MODID + ":blocks/block_steel");
        RenderStructureMarker.fusion[0][1] = evt.getMap().getAtlasSprite(RefStrings.MODID + ":blocks/fusion_conductor_side_alt3");
        RenderStructureMarker.fusion[1][0] = evt.getMap().getAtlasSprite(RefStrings.MODID + ":blocks/fusion_heater_top");
        RenderStructureMarker.fusion[1][1] = evt.getMap().getAtlasSprite(RefStrings.MODID + ":blocks/fusion_heater_side");
        RenderStructureMarker.fusion[2][0] = evt.getMap().getAtlasSprite(RefStrings.MODID + ":blocks/block_tungsten");
        RenderStructureMarker.fusion[2][1] = evt.getMap().getAtlasSprite(RefStrings.MODID + ":blocks/fusion_hatch");
        RenderStructureMarker.fusion[3][0] = evt.getMap().getAtlasSprite(RefStrings.MODID + ":blocks/fusion_motor_top_alt");
        RenderStructureMarker.fusion[3][1] = evt.getMap().getAtlasSprite(RefStrings.MODID + ":blocks/fusion_motor_side_alt");
        RenderStructureMarker.fusion[4][0] = evt.getMap().getAtlasSprite(RefStrings.MODID + ":blocks/fusion_center_top_alt");
        RenderStructureMarker.fusion[4][1] = evt.getMap().getAtlasSprite(RefStrings.MODID + ":blocks/fusion_center_side_alt");
        RenderStructureMarker.fusion[5][0] = evt.getMap().getAtlasSprite(RefStrings.MODID + ":blocks/fusion_center_top_alt");
        RenderStructureMarker.fusion[5][1] = evt.getMap().getAtlasSprite(RefStrings.MODID + ":blocks/fusion_core_side_alt");
        RenderStructureMarker.fusion[6][0] = evt.getMap().getAtlasSprite(RefStrings.MODID + ":blocks/block_tungsten");
        RenderStructureMarker.fusion[6][1] = evt.getMap().getAtlasSprite(RefStrings.MODID + ":blocks/block_tungsten");

        RenderStructureMarker.watz[0][0] = evt.getMap().getAtlasSprite(RefStrings.MODID + ":blocks/reinforced_brick");
        RenderStructureMarker.watz[0][1] = evt.getMap().getAtlasSprite(RefStrings.MODID + ":blocks/reinforced_brick");
        RenderStructureMarker.watz[1][0] = evt.getMap().getAtlasSprite(RefStrings.MODID + ":blocks/reinforced_brick");
        RenderStructureMarker.watz[1][1] = evt.getMap().getAtlasSprite(RefStrings.MODID + ":blocks/watz_hatch");
        RenderStructureMarker.watz[2][0] = evt.getMap().getAtlasSprite(RefStrings.MODID + ":blocks/watz_control_top");
        RenderStructureMarker.watz[2][1] = evt.getMap().getAtlasSprite(RefStrings.MODID + ":blocks/watz_control_side");
        RenderStructureMarker.watz[3][0] = evt.getMap().getAtlasSprite(RefStrings.MODID + ":blocks/watz_end");
        RenderStructureMarker.watz[3][1] = evt.getMap().getAtlasSprite(RefStrings.MODID + ":blocks/watz_end");
        RenderStructureMarker.watz[4][0] = evt.getMap().getAtlasSprite(RefStrings.MODID + ":blocks/watz_conductor_top");
        RenderStructureMarker.watz[4][1] = evt.getMap().getAtlasSprite(RefStrings.MODID + ":blocks/watz_conductor_side");
        RenderStructureMarker.watz[5][0] = evt.getMap().getAtlasSprite(RefStrings.MODID + ":blocks/watz_computer");
        RenderStructureMarker.watz[5][1] = evt.getMap().getAtlasSprite(RefStrings.MODID + ":blocks/watz_computer");
        RenderStructureMarker.watz[6][0] = evt.getMap().getAtlasSprite(RefStrings.MODID + ":blocks/watz_cooler");
        RenderStructureMarker.watz[6][1] = evt.getMap().getAtlasSprite(RefStrings.MODID + ":blocks/watz_cooler");
        RenderStructureMarker.watz[7][0] = evt.getMap().getAtlasSprite(RefStrings.MODID + ":blocks/watz_element_top");
        RenderStructureMarker.watz[7][1] = evt.getMap().getAtlasSprite(RefStrings.MODID + ":blocks/watz_element_side");

        RenderStructureMarker.fwatz[0][0] = evt.getMap().getAtlasSprite(RefStrings.MODID + ":blocks/fwatz_scaffold");
        RenderStructureMarker.fwatz[0][1] = evt.getMap().getAtlasSprite(RefStrings.MODID + ":blocks/fwatz_scaffold");
        RenderStructureMarker.fwatz[1][0] = evt.getMap().getAtlasSprite(RefStrings.MODID + ":blocks/fwatz_scaffold");
        RenderStructureMarker.fwatz[1][1] = evt.getMap().getAtlasSprite(RefStrings.MODID + ":blocks/fwatz_hatch");
        RenderStructureMarker.fwatz[2][0] = evt.getMap().getAtlasSprite(RefStrings.MODID + ":blocks/fwatz_cooler_top");
        RenderStructureMarker.fwatz[2][1] = evt.getMap().getAtlasSprite(RefStrings.MODID + ":blocks/fwatz_cooler");
        RenderStructureMarker.fwatz[3][0] = evt.getMap().getAtlasSprite(RefStrings.MODID + ":blocks/fwatz_tank");
        RenderStructureMarker.fwatz[3][1] = evt.getMap().getAtlasSprite(RefStrings.MODID + ":blocks/fwatz_tank");
        RenderStructureMarker.fwatz[4][0] = evt.getMap().getAtlasSprite(RefStrings.MODID + ":blocks/block_combine_steel");
        RenderStructureMarker.fwatz[4][1] = evt.getMap().getAtlasSprite(RefStrings.MODID + ":blocks/fwatz_conductor_side");
        RenderStructureMarker.fwatz[5][0] = evt.getMap().getAtlasSprite(RefStrings.MODID + ":blocks/fwatz_computer");
        RenderStructureMarker.fwatz[5][1] = evt.getMap().getAtlasSprite(RefStrings.MODID + ":blocks/fwatz_computer");
        RenderStructureMarker.fwatz[6][0] = evt.getMap().getAtlasSprite(RefStrings.MODID + ":blocks/fwatz_core");
        RenderStructureMarker.fwatz[6][1] = evt.getMap().getAtlasSprite(RefStrings.MODID + ":blocks/fwatz_core");

        RenderMultiblock.structLauncher = evt.getMap().getAtlasSprite(RefStrings.MODID + ":blocks/struct_launcher");
        RenderMultiblock.structScaffold = evt.getMap().getAtlasSprite(RefStrings.MODID + ":blocks/struct_scaffold");

        RenderSoyuzMultiblock.blockIcons[0] = evt.getMap().getAtlasSprite(RefStrings.MODID + ":blocks/struct_launcher");
        RenderSoyuzMultiblock.blockIcons[1] = evt.getMap().getAtlasSprite(RefStrings.MODID + ":blocks/concrete");
        RenderSoyuzMultiblock.blockIcons[2] = evt.getMap().getAtlasSprite(RefStrings.MODID + ":blocks/struct_scaffold");

        RenderWatzMultiblock.casingSprite = evt.getMap().getAtlasSprite(RefStrings.MODID + ":blocks/watz_casing_tooled");
        RenderWatzMultiblock.coolerSpriteSide = evt.getMap().getAtlasSprite(RefStrings.MODID + ":blocks/watz_cooler_side");
        RenderWatzMultiblock.coolerSpriteTop = evt.getMap().getAtlasSprite(RefStrings.MODID + ":blocks/watz_cooler_top");
        RenderWatzMultiblock.elementSpriteSide = evt.getMap().getAtlasSprite(RefStrings.MODID + ":blocks/watz_element_side");
        RenderWatzMultiblock.elementSpriteTop = evt.getMap().getAtlasSprite(RefStrings.MODID + ":blocks/watz_element_top");

        RenderICFMultiblock.componentSprite0 = evt.getMap().getAtlasSprite("hbm:blocks/icf_component");
        RenderICFMultiblock.componentSprite2 = evt.getMap().getAtlasSprite("hbm:blocks/icf_component.vessel_welded");
        RenderICFMultiblock.componentSprite4 = evt.getMap().getAtlasSprite("hbm:blocks/icf_component.structure_bolted");
    }

    @SubscribeEvent
    public void renderTick(RenderTickEvent e) {
        EntityPlayer player = Minecraft.getMinecraft().player;
        if (player != null && player.getHeldItemMainhand().getItem() instanceof ItemSwordCutter && ItemSwordCutter.clicked) {
            updateMouseDelta();
            player.turn(deltaMouseX, deltaMouseY);
            float oldPitch = player.rotationPitch;
            float oldYaw = player.rotationYaw;
            float y = player.rotationYaw - ItemSwordCutter.yaw;
            if (y > ItemSwordCutter.MAX_DYAW) {
                player.rotationYaw = ItemSwordCutter.yaw + ItemSwordCutter.MAX_DYAW;
            }
            if (y < -ItemSwordCutter.MAX_DYAW) {
                player.rotationYaw = ItemSwordCutter.yaw - ItemSwordCutter.MAX_DYAW;
            }
            float p = player.rotationPitch - ItemSwordCutter.pitch;
            if (p > ItemSwordCutter.MAX_DPITCH) {
                player.rotationPitch = ItemSwordCutter.pitch + ItemSwordCutter.MAX_DPITCH;
            }
            if (p < -ItemSwordCutter.MAX_DPITCH) {
                player.rotationPitch = ItemSwordCutter.pitch - ItemSwordCutter.MAX_DPITCH;
            }
            player.prevRotationYaw += player.rotationYaw - oldYaw;
            player.prevRotationPitch += player.rotationPitch - oldPitch;
        }
    }

    @SubscribeEvent
    public void fovUpdate(FOVUpdateEvent e) {
        EntityPlayer player = e.getEntity();
        if (player.getHeldItemMainhand().getItem() == ModItems.gun_supershotgun && ItemGunShotty.hasHookedEntity(player.world, player.getHeldItemMainhand())) {
            e.setNewfov(e.getFov() * 1.1F);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void fovModifier(EntityViewRenderEvent.FOVModifier e) {
        currentFOV = e.getFOV();
    }

    @SubscribeEvent
    public void setupNewFOV(FOVUpdateEvent event) {

        EntityPlayer player = Minecraft.getMinecraft().player;
        ItemStack held = player.getHeldItemMainhand();

        if(held.isEmpty()) return;

        TileEntityItemStackRenderer customRenderer = held.getItem().getTileEntityItemStackRenderer();
        if(!(customRenderer instanceof ItemRenderWeaponBase renderGun)) return;
        event.setNewfov(renderGun.getViewFOV(held, event.getFov()));
    }

    @SubscribeEvent
    public void inputUpdate(InputUpdateEvent e) {
        EntityPlayer player = e.getEntityPlayer();
        if (player.getHeldItemMainhand().getItem() == ModItems.gun_supershotgun && ItemGunShotty.hasHookedEntity(player.world, player.getHeldItemMainhand())) {
            MovementInput m = e.getMovementInput();
            //To make it extra responsive, swings faster if the player is swinging in the opposite direction.
            float coeff = 0.25F;
            if ((ItemGunShotty.motionStrafe < 0 && m.moveStrafe > 0) || (ItemGunShotty.motionStrafe > 0 && m.moveStrafe < 0))
                coeff *= 2;
            ItemGunShotty.motionStrafe += m.moveStrafe * coeff;
            m.moveStrafe = 0;
            m.moveForward = 0;
            //If the player jumps, add some velocity in their look direction (don't want to add velocity down though, so always increase y velocity by at least 1)
            if (m.jump) {
                Vec3d look = player.getLookVec().scale(0.75);
                player.motionX += look.x * 1.5;
                player.motionY = 1 + MathHelper.clamp(look.y, 0, 1);
                player.motionZ += look.z * 1.5;
                ItemGunShotty.setHookedEntity(player, player.getHeldItemMainhand(), null);
                PacketDispatcher.wrapper.sendToServer(new MeathookJumpPacket());
                m.jump = false;
            }
        }
        JetpackHandler.inputUpdate(e);
    }

    @SubscribeEvent
    public void clientTick(ClientTickEvent e) {
        if (e.phase == Phase.END) {
            if (!firstPersonAuxParticles.isEmpty()) {
                Iterator<ParticleFirstPerson> i = firstPersonAuxParticles.iterator();
                while (i.hasNext()) {
                    Particle p = i.next();
                    p.onUpdate();
                    if (!p.isAlive()) {
                        i.remove();
                    }
                }
            }
            specialDeathEffectEntities.removeIf(ent -> ent.isDead);
            EntityPlayer player = Minecraft.getMinecraft().player;
            if (player != null) {
                boolean isHooked = player.getHeldItemMainhand().getItem() == ModItems.gun_supershotgun && ItemGunShotty.hasHookedEntity(player.world, player.getHeldItemMainhand());
                if (isHooked)
                    player.distanceWalkedModified = player.prevDistanceWalkedModified; //Stops the held shotgun from bobbing when hooked
            }
        } else {

            if (Minecraft.getMinecraft().world != null) {
                //Drillgon200: If I add more guns like this, I'll abstract it.
                for (EntityPlayer player : Minecraft.getMinecraft().world.playerEntities) {
                    if (player.getHeldItemMainhand().getItem() == ModItems.gun_egon && !ItemGunEgon.soundsByPlayer.containsKey(player)) {
                        boolean firing = player == Minecraft.getMinecraft().player ? ItemGunEgon.m1 && Library.countInventoryItem(player.inventory, ItemGunEgon.getBeltType(player, player.getHeldItemMainhand(), true)) >= 2 : ItemGunEgon.getIsFiring(player.getHeldItemMainhand());
                        if (firing) {
                            ItemGunEgon.soundsByPlayer.put(player, new GunEgonSoundHandler(player));
                        }
                    }
                }
            }
            Iterator<GunEgonSoundHandler> itr = ItemGunEgon.soundsByPlayer.values().iterator();
            while (itr.hasNext()) {
                GunEgonSoundHandler g = itr.next();
                g.update();
                if (g.ticks == -1)
                    itr.remove();
            }
        }
        if (Minecraft.getMinecraft().player != null) {
            JetpackHandler.clientTick(e);
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_F1) && Minecraft.getMinecraft().currentScreen != null) {

            ComparableStack comp = canneryTimestamp > System.currentTimeMillis() - 100 ? lastCannery : null;

            if (comp == null) {
                ItemStack stack = getMouseOverStack();
                if (stack != null) comp = new ComparableStack(stack).makeSingular();
            }

            if (comp != null) {
                CanneryBase cannery = Jars.canneries.get(comp);
                if (cannery != null) {
                    FMLCommonHandler.instance().showGuiScreen(new GuiWorldInAJar(cannery.createScript(), cannery.getName(), cannery.getIcon(), cannery.seeAlso()));
                }
            }
        }
    }

    //Sus
    @SubscribeEvent
    public void onArmorRenderEvent(RenderPlayerEvent.Pre event) {
        EntityPlayer player = event.getEntityPlayer();
        GlStateManager.pushMatrix();
        GlStateManager.translate(0, player.isSneaking() ? 1.1 : 1.4, 0);
        GL11.glRotated(180, 0, 0, 1);

        for (int i = 0; i < 4; i++) {

            ItemStack armor = player.inventory.armorItemInSlot(i);

            if (armor != null && ArmorModHandler.hasMods(armor)) {

                for (ItemStack mod : ArmorModHandler.pryMods(armor)) {

                    if (mod != null && mod.getItem() instanceof ItemArmorMod) {
                        ((ItemArmorMod) mod.getItem()).modRender(event, armor);
                    }
                }
            }

            //because armor that isn't ItemArmor doesn't render at all
            if (armor != null && armor.getItem() instanceof JetpackBase) {
                ((ItemArmorMod) armor.getItem()).modRender(event, armor);
            }
        }
        GlStateManager.popMatrix();
    }

    @SubscribeEvent
    public void renderSpecificHand(RenderSpecificHandEvent e) {
        if (Minecraft.getMinecraft().player.getHeldItem(e.getHand()).getItem() == ModItems.crucible) {
            e.setCanceled(true);
            Minecraft.getMinecraft().getItemRenderer().renderItemInFirstPerson(Minecraft.getMinecraft().player, e.getPartialTicks(), e.getInterpolatedPitch(), EnumHand.MAIN_HAND, 0, Minecraft.getMinecraft().player.getHeldItem(e.getHand()), 0);
        } else if (e.getHand() == EnumHand.MAIN_HAND && Minecraft.getMinecraft().player.getHeldItem(EnumHand.MAIN_HAND).getItem() instanceof ItemSwordCutter) {
            Animation anim = HbmAnimations.getRelevantAnim(EnumHand.MAIN_HAND);
            if (anim != null && anim.animation != null) {
                e.setCanceled(true);
                Minecraft.getMinecraft().getItemRenderer().renderItemInFirstPerson(Minecraft.getMinecraft().player, e.getPartialTicks(), e.getInterpolatedPitch(), EnumHand.MAIN_HAND, 0, Minecraft.getMinecraft().player.getHeldItem(e.getHand()), 0);
            }
        }
    }

    @SubscribeEvent
    public void cameraSetup(EntityViewRenderEvent.CameraSetup e) {
        RecoilHandler.modifiyCamera(e);
        JetpackHandler.handleCameraTransform(e);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void renderWorld(RenderWorldLastEvent evt) {
        HbmShaderManager2.createInvMVP();
        GlStateManager.enableDepth();
        List<Entity> list = Minecraft.getMinecraft().world.loadedEntityList;
        ClientProxy.renderingConstant = true;

        Entity entity = Minecraft.getMinecraft().getRenderViewEntity();
        float partialTicks = Minecraft.getMinecraft().getRenderPartialTicks();
        double d3 = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * (double) partialTicks;
        double d4 = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * (double) partialTicks;
        double d5 = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * (double) partialTicks;
        for (Entity e : list) {
            if (e instanceof IConstantRenderer) {
                double d0 = e.lastTickPosX + (e.posX - e.lastTickPosX) * (double) partialTicks;
                double d1 = e.lastTickPosY + (e.posY - e.lastTickPosY) * (double) partialTicks;
                double d2 = e.lastTickPosZ + (e.posZ - e.lastTickPosZ) * (double) partialTicks;
                float f = e.prevRotationYaw + (e.rotationYaw - e.prevRotationYaw) * partialTicks;

                Render<Entity> r = Minecraft.getMinecraft().getRenderManager().getEntityRenderObject(e);
                r.doRender(e, d0 - d3, d1 - d4, d2 - d5, f, partialTicks);
            }
        }
        ClientProxy.renderingConstant = false;

        //SSG meathook icon projection
        if (ItemGunShotty.rayTrace != null) {
            GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, MODELVIEW);
            GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, PROJECTION);
            GL11.glGetInteger(GL11.GL_VIEWPORT, VIEWPORT);

            Project.gluProject((float) (ItemGunShotty.rayTrace.x - d3), (float) (ItemGunShotty.rayTrace.y - d4), (float) (ItemGunShotty.rayTrace.z - d5), MODELVIEW, PROJECTION, VIEWPORT, POSITION);

            ItemGunShotty.screenPos = new Vec2f(POSITION.get(0), POSITION.get(1));
        } else {
            ItemGunShotty.screenPos = new Vec2f( (float)Minecraft.getMinecraft().displayWidth / 2, (float) Minecraft.getMinecraft().displayHeight / 2);
        }

        //SSG meathook chain rendering
        ItemStack stack = Minecraft.getMinecraft().player.getHeldItemMainhand();
        if (ItemGunShotty.hasHookedEntity(Minecraft.getMinecraft().world, stack)) {
            Entity e = ItemGunShotty.getHookedEntity(Minecraft.getMinecraft().world, stack);

            //Left/right, up/down, forward/backward
            Vec3d ssgChainPos = new Vec3d(-0.08, -0.1, 0.35);
            ssgChainPos = ssgChainPos.rotatePitch((float) Math.toRadians(-(entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks)));
            ssgChainPos = ssgChainPos.rotateYaw((float) Math.toRadians(-(entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks)));

            ssgChainPos = ssgChainPos.add(0, entity.getEyeHeight(), 0);

            double d0 = e.lastTickPosX + (e.posX - e.lastTickPosX) * (double) partialTicks;
            double d1 = e.lastTickPosY + (e.posY - e.lastTickPosY) * (double) partialTicks;
            double d2 = e.lastTickPosZ + (e.posZ - e.lastTickPosZ) * (double) partialTicks;
            Vec3d tester = new Vec3d(d0 - d3, d1 + e.getEyeHeight() * 0.75 - d4, d2 - d5).subtract(ssgChainPos);

            double yaw = Math.toDegrees(Math.atan2(tester.x, tester.z));
            double sqrt = MathHelper.sqrt(tester.x * tester.x + tester.z * tester.z);
            double pitch = Math.toDegrees(Math.atan2(tester.y, sqrt));

            GlStateManager.pushMatrix();
            GlStateManager.translate(ssgChainPos.x, ssgChainPos.y, ssgChainPos.z);
            GL11.glRotated(yaw + 90, 0, 1, 0);
            GL11.glRotated(-pitch + 90, 0, 0, 1);
            GL11.glScaled(0.125, 0.25, 0.125);

            double len = MathHelper.clamp(tester.length() * 2, 0, 40);
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();

            NTMRenderHelper.bindTexture(ResourceManager.universal);
            GlStateManager.enableLighting();
            buffer.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_TEX_NORMAL);
            for (int i = 0; i < Math.ceil(len); i++) {
                float offset = 0;
                if (ItemGunShotty.motionStrafe != 0) {
                    if (i < len * 0.75) {
                        offset = (float) Library.smoothstep(i, 0, len * 0.5);
                    } else {
                        offset = 1 - (float) Library.smoothstep(i, len * 0.5, len);
                    }
                    if (ItemGunShotty.motionStrafe > 0)
                        offset = -offset;
                }
                float scale = (float) (len / 20F);
                buffer.setTranslation(0, i, offset * scale);


                ResourceManager.n45_chain.renderAll();
            }

            tessellator.draw();
            GlStateManager.popMatrix();
        }

        int dist = 300;
        int x = 0;
        int y = 500;
        int z = 0;

        Vec3d vec = new Vec3d(x - d3, y - d4, z - d5);

        if (vec.length() < dist) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(vec.x, vec.y, vec.z);


            net.minecraft.client.renderer.RenderHelper.enableStandardItemLighting();
            GL11.glRotated(80, 0, 0, 1);
            GL11.glRotated(30, 0, 1, 0);

            double sine = Math.sin(System.currentTimeMillis() * 0.0005) * 5;
            double sin3 = Math.sin(System.currentTimeMillis() * 0.0005 + Math.PI * 0.5) * 5;
            GL11.glRotated(sine, 0, 0, 1);
            GL11.glRotated(sin3, 1, 0, 0);

            GlStateManager.translate(0, -3, 0);
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 6500F, 30F);
            SoyuzPronter.prontCapsule();

            GL11.glRotated(System.currentTimeMillis() * 0.025 % 360, 0, -1, 0);

            String msg = HTTPHandler.capsule;

            GlStateManager.translate(0, 3.75, 0);
            GL11.glRotated(180, 1, 0, 0);

            float rot = 0F;

            //looks dumb but we'll use this technology for the cyclotron
            for (char c : msg.toCharArray()) {
                GlStateManager.pushMatrix();

                GlStateManager.rotate(rot, 0, 1, 0);

                float width = Minecraft.getMinecraft().fontRenderer.getStringWidth(msg);
                float scale = 5 / width;

                rot -= Minecraft.getMinecraft().fontRenderer.getCharWidth(c) * scale * 50;

                GlStateManager.translate(2, 0, 0);

                GlStateManager.rotate(-90, 0, 1, 0);

                GlStateManager.scale(scale, scale, scale);
                GlStateManager.disableCull();
                Minecraft.getMinecraft().fontRenderer.drawString(String.valueOf(c), 0, 0, 0xff00ff);
                GlStateManager.enableCull();
                GlStateManager.popMatrix();
            }

            net.minecraft.client.renderer.RenderHelper.disableStandardItemLighting();

            GlStateManager.popMatrix();
        }

        boolean hudOn = HbmCapability.getData(Minecraft.getMinecraft().player).getEnableHUD();
        if (hudOn) {
            RenderOverhead.renderMarkers(evt.getPartialTicks());

            if (ArmorFSB.hasFSBArmor(Minecraft.getMinecraft().player)) {
                ItemStack plate = Minecraft.getMinecraft().player.inventory.armorInventory.get(2);
                ArmorFSB chestplate = (ArmorFSB) plate.getItem();

                if (chestplate.thermal)
                    RenderOverhead.renderThermalSight(evt.getPartialTicks());
            }
        }

        if (entity instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) entity;
            net.minecraft.client.renderer.Tessellator tes = net.minecraft.client.renderer.Tessellator.getInstance();
            BufferBuilder buf = tes.getBuffer();
            if (player.getHeldItemMainhand().getItem() instanceof ItemSwordCutter && ItemSwordCutter.clicked) {
                if (Mouse.isButtonDown(1) && ItemSwordCutter.startPos != null) {
					/*ItemSwordCutter.x += deltaMouseX*0.01F;
					ItemSwordCutter.y -= deltaMouseY*0.01F;
					if(ItemSwordCutter.x + ItemSwordCutter.y == 0){
						ItemSwordCutter.x = 1F;
					}
					double lenRcp = 1D/Math.sqrt(ItemSwordCutter.x*ItemSwordCutter.x+ItemSwordCutter.y*ItemSwordCutter.y);
					ItemSwordCutter.x *= lenRcp;
					ItemSwordCutter.y *= lenRcp;
					double angle = Math.atan2(ItemSwordCutter.y, ItemSwordCutter.x);
					GlStateManager.pushMatrix();
					GlStateManager.translate(0, player.getEyeHeight(), 0);
					GL11.glRotated(-player.rotationYaw-90, 0, 1, 0);
					GL11.glRotated(-player.rotationPitch, 0, 0, 1);
					GlStateManager.translate(-0.3, 0, 0);
					GL11.glRotated(Math.toDegrees(angle), 1, 0, 0);
					GlStateManager.translate(0, 0.2, 0);
					GlStateManager.disableCull();
					GlStateManager.disableTexture2D();
					GlStateManager.enableBlend();
					GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
					GlStateManager.color(0.7F, 0.7F, 0.7F, 0.4F);
					buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
					buf.pos(0, 0, -2).endVertex();
					buf.pos(3, 0, -2).endVertex();
					buf.pos(3, 0, 2).endVertex();
					buf.pos(0, 0, 2).endVertex();
					tes.draw();
					GlStateManager.enableTexture2D();
					GlStateManager.disableBlend();
					GlStateManager.enableCull();

					Vec3d[] positions = BobMathUtil.worldFromLocal(new Vector4f(0, 0, -2, 1), new Vector4f(3, 0, -2, 1), new Vector4f(3, 0, 2, 1));
					Vec3d norm = positions[1].subtract(positions[0]).crossProduct(positions[2].subtract(positions[0])).normalize();
					ItemSwordCutter.plane = new Vec3d[]{positions[0], norm};
					GlStateManager.popMatrix();
					GlStateManager.disableTexture2D();
					GlStateManager.color(1F, 0F, 0F, 1F);
					buf.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);
					Vec3d pos1 = positions[1].subtract(player.getPositionEyes(partialTicks));
					buf.pos(pos1.x, pos1.y+player.getEyeHeight(), pos1.z).endVertex();
					buf.pos(pos1.x+norm.x, pos1.y+norm.y+player.getEyeHeight(), pos1.z+norm.z).endVertex();
					tes.draw();
					GlStateManager.enableTexture2D();
					player.turn(deltaMouseX, deltaMouseY);
					GlStateManager.color(1F, 1F, 1F, 1F);*/
                    if (!(player.getHeldItemMainhand().getItem() instanceof ItemCrucible && ItemCrucible.getCharges(player.getHeldItemMainhand()) == 0)) {
                        Vec3d pos1 = ItemSwordCutter.startPos;
                        Vec3d pos2 = player.getLook(partialTicks);
                        Vec3d norm = ItemSwordCutter.startPos.crossProduct(player.getLook(partialTicks));
                        GlStateManager.disableTexture2D();
                        GlStateManager.color(0, 0, 0, 1);
                        GlStateManager.pushMatrix();
                        GlStateManager.loadIdentity();

                        GL11.glRotated(player.rotationPitch, 1, 0, 0);
                        GL11.glRotated(player.rotationYaw + 180, 0, 1, 0);

                        buf.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);
                        buf.pos(pos1.x, pos1.y, pos1.z).endVertex();
                        buf.pos(pos2.x, pos2.y, pos2.z).endVertex();
                        tes.draw();
                        GlStateManager.popMatrix();
                        GlStateManager.color(1, 1, 1, 1);
                        GlStateManager.enableTexture2D();
                        if (norm.lengthSquared() > 0.001F) {
                            ItemSwordCutter.planeNormal = norm.normalize();
                        }
                    } else {
                        ItemSwordCutter.clicked = false;
                        ItemSwordCutter.planeNormal = null;
                    }
                } else {
                    ItemSwordCutter.clicked = false;
                    ItemSwordCutter.planeNormal = null;
                }
            }
			/*Vec3d euler = BobMathUtil.getEulerAngles(player.getLookVec());
			javax.vecmath.Matrix3f rot = BakedModelUtil.eulerToMat((float)Math.toRadians(euler.x), (float)Math.toRadians(euler.y+90), player.world.rand.nextFloat()*2F*(float)Math.PI);
			Vec3d c1 = new Vec3d(rot.m00, rot.m01, rot.m02);
			Vec3d c2 = new Vec3d(rot.m10, rot.m11, rot.m12);
			Vec3d c3 = new Vec3d(rot.m20, rot.m21, rot.m22);
			GlStateManager.pushMatrix();
			GlStateManager.translate(0, player.getEyeHeight(), 0);
			Vec3d look = player.getLook(partialTicks).scale(2);
			GlStateManager.translate(look.x, look.y, look.z);
			GlStateManager.disableTexture2D();
			buf.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);
			buf.pos(0, 0, 0).endVertex();
			buf.pos(c1.x, c1.y, c1.z).endVertex();
			buf.pos(0, 0, 0).endVertex();
			buf.pos(c2.x, c2.y, c2.z).endVertex();
			buf.pos(0, 0, 0).endVertex();
			buf.pos(c3.x, c3.y, c3.z).endVertex();
			tes.draw();
			GlStateManager.enableTexture2D();
			GlStateManager.popMatrix();*/

            //GLUON GUN//
            if (player.getHeldItemMainhand().getItem() == ModItems.gun_egon && ItemGunEgon.activeTicks > 0 && Minecraft.getMinecraft().gameSettings.thirdPersonView == 0) {
                GlStateManager.pushMatrix();
                float[] angles = ItemGunEgon.getBeamDirectionOffset(player.world.getTotalWorldTime() + partialTicks);
                Vec3d look = Library.changeByAngle(player.getLook(partialTicks), angles[0], angles[1]);
                RayTraceResult r = Library.rayTraceIncludeEntitiesCustomDirection(player, look, 50, partialTicks);
                Vec3d pos = player.getPositionEyes(partialTicks);
                Vec3d hitPos = pos.add(look.scale(50));
                if (r == null || r.typeOfHit == Type.MISS) {
                } else {
                    hitPos = r.hitVec.add(look.scale(-0.1));
                }
                float[] offset = ItemRenderGunEgon.getOffset(player.world.getTotalWorldTime() + partialTicks);
                //I'll at least attempt to make it look consistent at different fovs
                float fovDiff = (currentFOV - 70) * 0.0002F;
                Vec3d start = new Vec3d(-0.18 + offset[0] * 0.075F - fovDiff, -0.2 + offset[1] * 0.1F, 0.35 - fovDiff * 30);
                start = start.rotatePitch((float) Math.toRadians(-(player.prevRotationPitch + (player.rotationPitch - player.prevRotationPitch) * partialTicks)));
                start = start.rotateYaw((float) Math.toRadians(-(player.prevRotationYaw + (player.rotationYaw - player.prevRotationYaw) * partialTicks)));

                start = start.add(0, player.getEyeHeight(), 0);
                GlStateManager.translate(start.x, start.y, start.z);
                BeamPronter.gluonBeam(new Vec3d(0, 0, 0), new Vec3d(Vec3dUtil.convertToVec3i(hitPos.subtract(pos).subtract(start.subtract(0, player.getEyeHeight(), 0)))), 0.4F);
                GlStateManager.popMatrix();

            }
        }

        for (EntityPlayer player : Minecraft.getMinecraft().world.playerEntities) {

            //FSB world rendering
//            if (ArmorFSB.hasFSBArmor(player)) {
//                ItemStack plate = player.inventory.armorInventory.get(2);
//                ArmorFSB chestplate = (ArmorFSB) plate.getItem();
//                if (chestplate.flashlightPosition != null && plate.hasTagCompound() && plate.getTagCompound().getBoolean("flActive")) {
//                    Vec3d start = chestplate.flashlightPosition.rotatePitch(-(float) Math.toRadians(player.rotationPitch)).rotateYaw(-(float) Math.toRadians(player.rotationYaw)).add(player.getPositionEyes(partialTicks));
//                    boolean volume = true;
//                    if (player == Minecraft.getMinecraft().player && Minecraft.getMinecraft().gameSettings.thirdPersonView == 0)
//                        volume = false;
//                    LightRenderer.addFlashlight(start, start.add(player.getLook(partialTicks).scale(30)), 30, 200, ResourceManager.fl_cookie, volume, true, true, true);
//                }
//            }

            //Gun world rendering
            if (player.getHeldItemMainhand().getItem() instanceof ItemGunBase) {
                ((ItemGunBase) player.getHeldItemMainhand().getItem()).playerWorldRender(player, evt, EnumHand.MAIN_HAND);
            }
            if (player.getHeldItemOffhand().getItem() instanceof ItemGunBase) {
                ((ItemGunBase) player.getHeldItemOffhand().getItem()).playerWorldRender(player, evt, EnumHand.OFF_HAND);
            }

            //Gluon gun world rendering
            if (player.getHeldItemMainhand().getItem() != ModItems.gun_egon) {
                ItemGunEgon.activeTrailParticles.remove(player);
                continue;
            }
            boolean firing = player == Minecraft.getMinecraft().player ? ItemGunEgon.m1 && Library.countInventoryItem(player.inventory, ItemGunEgon.getBeltType(player, player.getHeldItemMainhand(), true)) >= 2 : ItemGunEgon.getIsFiring(player.getHeldItemMainhand());
            if (!firing) {
                ItemGunEgon.activeTrailParticles.remove(player);
                continue;
            }
            float[] angles = ItemGunEgon.getBeamDirectionOffset(player.world.getTotalWorldTime() + partialTicks);
            Vec3d look = Library.changeByAngle(player.getLook(partialTicks), angles[0], angles[1]);
            RayTraceResult r = Library.rayTraceIncludeEntitiesCustomDirection(player, look, 50, partialTicks);
            if (r != null && r.hitVec != null && r.typeOfHit == Type.BLOCK) {
                ParticleGluonBurnTrail currentTrailParticle = null;
                if (!ItemGunEgon.activeTrailParticles.containsKey(player)) {
                    currentTrailParticle = new ParticleGluonBurnTrail(player.world, 0.4F, player);
                    Minecraft.getMinecraft().effectRenderer.addEffect(currentTrailParticle);
                    ItemGunEgon.activeTrailParticles.put(player, currentTrailParticle);
                } else {
                    currentTrailParticle = ItemGunEgon.activeTrailParticles.get(player);
                }
                Vec3d normal = Library.normalFromRayTrace(r);
                if (!currentTrailParticle.tryAddNewPosition(r.hitVec.add(normal.scale(0.02)), normal)) {
                    currentTrailParticle = null;
                    ItemGunEgon.activeTrailParticles.remove(player);
                }
            } else {
                ItemGunEgon.activeTrailParticles.remove(player);
            }
        }

        for (Runnable r : ClientProxy.deferredRenderers) {
            r.run();
        }
        ClientProxy.deferredRenderers.clear();

        HbmShaderManager2.blitDepth();

        ParticleBatchRenderer.renderLast(evt);

        LightRenderer.worldRender();

       WorldSpaceFPRender.doHandRendering(evt);

		for(Particle p : firstPersonAuxParticles){
			if(p instanceof ParticlePhysicsBlocks)
				p.renderParticle(null, Minecraft.getMinecraft().getRenderViewEntity(), MainRegistry.proxy.partialTicks(), 0, 0, 0, 0, 0);
		}
        if (!(Minecraft.getMinecraft().player.getHeldItemMainhand().getItem() instanceof IPostRender || Minecraft.getMinecraft().player.getHeldItemOffhand().getItem() instanceof IPostRender)) {
            HbmShaderManager2.postProcess();
        }
    }

    @SubscribeEvent
    public void renderHand(RenderHandEvent e) {
        if (Minecraft.getMinecraft().player.getHeldItemMainhand().getItem() instanceof IPostRender || Minecraft.getMinecraft().player.getHeldItemOffhand().getItem() instanceof IPostRender) {
            e.setCanceled(true);
            Minecraft mc = Minecraft.getMinecraft();
            boolean flag = mc.getRenderViewEntity() instanceof EntityLivingBase && ((EntityLivingBase) mc.getRenderViewEntity()).isPlayerSleeping();
            if (mc.gameSettings.thirdPersonView == 0 && !flag && !mc.gameSettings.hideGUI && !mc.playerController.isSpectator()) {
                mc.entityRenderer.enableLightmap();
                mc.entityRenderer.itemRenderer.renderItemInFirstPerson(e.getPartialTicks());
                mc.entityRenderer.disableLightmap();
            }
            HbmShaderManager2.postProcess();
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void cancelVanished(RenderLivingEvent.Pre<EntityLivingBase> event) {
        if (MainRegistry.proxy.isVanished(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void preRenderEvent(RenderLivingEvent.Pre<EntityLivingBase> event) {
        EntityPlayer player = Minecraft.getMinecraft().player;
        if (ArmorFSB.hasFSBArmor(player) && HbmCapability.getData(player).getEnableHUD()) {
            ItemStack plate = player.inventory.armorInventory.get(2);
            ArmorFSB chestplate = (ArmorFSB) plate.getItem();

            if (chestplate.vats) {

                int count = (int) Math.min(event.getEntity().getMaxHealth(), 100);

                int bars = (int) Math.ceil(event.getEntity().getHealth() * count / event.getEntity().getMaxHealth());

                String bar = TextFormatting.RED + "";

                for (int i = 0; i < count; i++) {

                    if (i == bars)
                        bar += TextFormatting.RESET + "";

                    bar += "|";
                }
                RenderOverhead.renderTag(event.getEntity(), event.getX(), event.getY(), event.getZ(), event.getRenderer(), bar, chestplate.thermal);
            }
        }
    }

    public boolean hasBauble(EntityPlayer player, Item bauble) {
        try {
            if (BaublesApi.isBaubleEquipped(player, bauble) != -1) {
                return true;
            }
        } catch (Throwable t) {
        }
        return false;
    }

    @SubscribeEvent
    public void onOverlayRender(RenderGameOverlayEvent.Pre event) {
        EntityPlayer player = Minecraft.getMinecraft().player;

        /// NUKE FLASH ///
        if (event.getType() == ElementType.CROSSHAIRS && (flashTimestamp + flashDuration - System.currentTimeMillis()) > 0) {
            int width = event.getResolution().getScaledWidth();
            int height = event.getResolution().getScaledHeight();
            int buff = -200; // that's for the shake effect - so the flash won't look like offset
            net.minecraft.client.renderer.Tessellator tess = net.minecraft.client.renderer.Tessellator.getInstance();
            BufferBuilder buffer = tess.getBuffer();
            GlStateManager.disableTexture2D();
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
            GlStateManager.alphaFunc(516, 0.0F);
            GlStateManager.depthMask(false);
            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
            float brightness = (flashTimestamp + flashDuration - System.currentTimeMillis()) / (float) flashDuration;
            buffer.pos(width - buff, buff, 0).color(1F, 1F, 1F, brightness * 1F).endVertex();
            buffer.pos(buff, buff, 0).color(1F, 1F, 1F, brightness * 1F).endVertex();
            buffer.pos(buff, height - buff, 0).color(1F, 1F, 1F, brightness * 1F).endVertex();
            buffer.pos(width - buff, height - buff, 0).color(1F, 1F, 1F, brightness * 1F).endVertex();
            tess.draw();
            OpenGlHelper.glBlendFunc(770, 771, 1, 0);
            GlStateManager.enableTexture2D();
            GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
            GL11.glDepthMask(true);
            return;
        }

        if (event.getType() == ElementType.CROSSHAIRS && player.getHeldItemMainhand().getItem() == ModItems.gun_supershotgun && !ItemGunShotty.hasHookedEntity(player.world, player.getHeldItemMainhand())) {
            float x1 = ItemGunShotty.prevScreenPos.x + (ItemGunShotty.screenPos.x - ItemGunShotty.prevScreenPos.x) * event.getPartialTicks();
            float y1 = ItemGunShotty.prevScreenPos.y + (ItemGunShotty.screenPos.y - ItemGunShotty.prevScreenPos.y) * event.getPartialTicks();
            float x = BobMathUtil.remap(x1, 0, Minecraft.getMinecraft().displayWidth, 0, event.getResolution().getScaledWidth());
            float y = event.getResolution().getScaledHeight() - BobMathUtil.remap(y1, 0, Minecraft.getMinecraft().displayHeight, 0, event.getResolution().getScaledHeight());
            NTMRenderHelper.bindTexture(ResourceManager.meathook_marker);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(SourceFactor.ONE_MINUS_DST_COLOR, DestFactor.ONE_MINUS_SRC_COLOR, SourceFactor.ONE, DestFactor.ZERO);
            NTMRenderHelper.drawGuiRect(x - 2.5F, y - 2.5F, 0, 0, 5, 5, 1, 1);
            GlStateManager.tryBlendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO);
            GlStateManager.disableBlend();
        }
        /// HANDLE GUN AND AMMO OVERLAYS ///
        if (player.getHeldItem(EnumHand.MAIN_HAND) != null && player.getHeldItem(EnumHand.MAIN_HAND).getItem() instanceof IItemHUD) {
            ((IItemHUD) player.getHeldItem(EnumHand.MAIN_HAND).getItem()).renderHUD(event, event.getType(), player, player.getHeldItem(EnumHand.MAIN_HAND), EnumHand.MAIN_HAND);
        }

        if (player.getHeldItem(EnumHand.OFF_HAND) != null && player.getHeldItem(EnumHand.OFF_HAND).getItem() instanceof IItemHUD) {
            ((IItemHUD) player.getHeldItem(EnumHand.OFF_HAND).getItem()).renderHUD(event, event.getType(), player, player.getHeldItem(EnumHand.OFF_HAND), EnumHand.OFF_HAND);
        }

        /// HANDLE GEIGER COUNTER AND JETPACK HUD ///
        if (event.getType() == ElementType.HOTBAR) {
            if (!(ArmorFSB.hasFSBArmorHelmet(player) && ((ArmorFSB) player.inventory.armorInventory.get(3).getItem()).customGeiger)) {
                if (Library.hasInventoryItem(player.inventory, ModItems.geiger_counter) || hasBauble(player, ModItems.geiger_counter)) {

                    float rads = (float) Library.getEntRadCap(player).getRads();

                    RenderScreenOverlay.renderRadCounter(event.getResolution(), rads, Minecraft.getMinecraft().ingameGUI);
                }
            }
            if (Library.hasInventoryItem(player.inventory, ModItems.digamma_diagnostic) || hasBauble(player, ModItems.digamma_diagnostic)) {

                float digamma = (float) Library.getEntRadCap(player).getDigamma();

                RenderScreenOverlay.renderDigCounter(event.getResolution(), digamma, Minecraft.getMinecraft().ingameGUI);
            }
            if (JetpackHandler.hasJetpack(player)) {
                JetpackHandler.renderHUD(player, event.getResolution());
            }
        }

        /// DODD DIAG HOOK FOR RBMK
        if (event.getType() == ElementType.CROSSHAIRS) {
            Minecraft mc = Minecraft.getMinecraft();
            World world = mc.world;
            RayTraceResult mop = mc.objectMouseOver;

            if (mop != null && mop.typeOfHit == mop.typeOfHit.BLOCK) {
                if (world.getBlockState(mop.getBlockPos()).getBlock() instanceof ILookOverlay) {
                    ((ILookOverlay) world.getBlockState(mop.getBlockPos()).getBlock()).printHook(event, world, mop.getBlockPos().getX(), mop.getBlockPos().getY(), mop.getBlockPos().getZ());
                }
            }
            TileEntityRBMKBase.diagnosticPrintHook(event);
        }

        /// HANLDE SEDNA ANIMATION BUSES ///

        for(int i = 0; i < HbmAnimationsSedna.hotbar.length; i++) {
            for(int j = 0; j < HbmAnimationsSedna.hotbar[i].length; j++) {

                HbmAnimationsSedna.Animation animation = HbmAnimationsSedna.hotbar[i][j];

                if(animation == null)
                    continue;

                if(animation.holdLastFrame)
                    continue;

                long time = System.currentTimeMillis() - animation.startMillis;

                if(time > animation.animation.getDuration())
                    HbmAnimationsSedna.hotbar[i][j] = null;
            }
        }

        /// HANDLE SCOPE OVERLAY ///
        ItemStack held = player.getHeldItemMainhand();

        if(player.isSneaking() && !held.isEmpty() && held.getItem() instanceof ItemGunBaseSedna && event.getType() == ElementType.HOTBAR)  {
            GunConfigurationSedna config = ((ItemGunBaseSedna) held.getItem()).mainConfig;

            if(config.scopeTexture != null) {
                ScaledResolution resolution = event.getResolution();
                RenderScreenOverlay.renderScope(resolution, config.scopeTexture);
            }
        }

        if(!held.isEmpty() && held.getItem() instanceof ItemGunBaseNT && ItemGunBaseNT.aimingProgress == ItemGunBaseNT.prevAimingProgress && ItemGunBaseNT.aimingProgress == 1F && event.getType() == ElementType.HOTBAR)  {
            ItemGunBaseNT gun = (ItemGunBaseNT) held.getItem();
            GunConfig cfg = gun.getConfig(held, 0);
            if(cfg.getScopeTexture(held) != null) {
                ScaledResolution resolution = event.getResolution();
                RenderScreenOverlay.renderScope(resolution, cfg.getScopeTexture(held));
            }
        }

        //prevents NBT changes (read: every fucking tick) on guns from bringing up the item's name over the hotbar
        ItemStack highlightedItem = ObfuscationReflectionHelper.getPrivateValue(GuiIngame.class, Minecraft.getMinecraft().ingameGUI, "field_92016_l");
        if(!held.isEmpty() && held.getItem() instanceof ItemGunBaseNT && !highlightedItem.isEmpty() && highlightedItem.getItem() == held.getItem()) {
            ObfuscationReflectionHelper.setPrivateValue(GuiIngame.class, Minecraft.getMinecraft().ingameGUI, held, "field_92016_l");
        }

        /// HANDLE ANIMATION BUSES ///

        if (event.getType() == ElementType.ALL) {
            for (int i = 0; i < HbmAnimations.hotbar.length; i++) {
                Animation animation = HbmAnimations.hotbar[i];

                if (animation == null)
                    continue;

                long time = System.currentTimeMillis() - animation.startMillis;

                int duration = 0;
                if (animation instanceof BlenderAnimation) {
                    BlenderAnimation banim = ((BlenderAnimation) animation);
                    //duration = (int) Math.ceil(banim.wrapper.anim.length * (1F/Math.abs(banim.wrapper.speedScale)));
                    EnumHand hand = i < 9 ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND;
                    if (!Minecraft.getMinecraft().player.getHeldItem(hand).getTranslationKey().equals(banim.key))
                        HbmAnimations.hotbar[i] = null;
                    if (animation.animation != null) {
                        if (time > animation.animation.getDuration()) {
                            animation.animation = null;
                        }
                    }
                } else {
                    duration = animation.animation.getDuration();
                    if (time > duration)
                        HbmAnimations.hotbar[i] = null;
                }

            }
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_O) && Minecraft.getMinecraft().currentScreen == null) {
            PacketDispatcher.wrapper.sendToServer(new AuxButtonPacket(0, 0, 0, 999, 0));
        }
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        ItemStack helmet = player.inventory.armorInventory.get(3);

        if (helmet.getItem() instanceof ArmorFSB) {
            ((ArmorFSB) helmet.getItem()).handleOverlay(event, player);
        }

        // NUKE GUI SHAKE //
        if (event.getType() == ElementType.HOTBAR && (ModEventHandlerClient.shakeTimestamp + ModEventHandlerClient.shakeDuration - System.currentTimeMillis()) > 0) {
            double mult = (ModEventHandlerClient.shakeTimestamp + ModEventHandlerClient.shakeDuration - System.currentTimeMillis()) / (double) ModEventHandlerClient.shakeDuration * 2;
            double horizontal = MathHelper.clamp(Math.sin(System.currentTimeMillis() * 0.02), -0.7, 0.7) * 15;
            double vertical = MathHelper.clamp(Math.sin(System.currentTimeMillis() * 0.01 + 2), -0.7, 0.7) * 3;
            GlStateManager.translate(horizontal * mult, vertical * mult, 0);
        }

        /// HANDLE FSB HUD ///

        if (helmet.getItem() instanceof ArmorFSB) {
            ((ArmorFSB) helmet.getItem()).handleOverlay(event, player);
        }
        if(!event.isCanceled() && event.getType() == ElementType.HOTBAR) {

            HbmCapability.IHBMData props  = HbmCapability.getData(player);
            if(props.getDashCount() > 0) {
                RenderScreenOverlay.renderDashBar(event.getResolution(), Minecraft.getMinecraft().ingameGUI, props);

            }
        }
    }

    @SubscribeEvent(receiveCanceled = true)
    public void onHUDRenderShield(RenderGameOverlayEvent.Pre event) {

        EntityPlayer player = Minecraft.getMinecraft().player;

        if(event.getType().equals(ElementType.ARMOR)) {
            HbmCapability.IHBMData props = HbmCapability.getData(player);
            if(props.getEffectiveMaxShield(player) > 0) {
                RenderScreenOverlay.renderShieldBar(event.getResolution(), Minecraft.getMinecraft().ingameGUI);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void preRenderPlayer(RenderPlayerEvent.Pre evt) {
        PotionEffect invis = evt.getEntityPlayer().getActivePotionEffect(MobEffects.INVISIBILITY);

        if (invis != null && invis.getAmplifier() >= 0) {
            evt.setCanceled(true);
            return;
        }
        // event.setCanceled(true);
        AbstractClientPlayer player = (AbstractClientPlayer) evt.getEntityPlayer();

        ModelPlayer renderer = evt.getRenderer().getMainModel();

        if (player.getHeldItem(EnumHand.MAIN_HAND) != null && player.getHeldItem(EnumHand.MAIN_HAND).getItem() instanceof IHoldableWeapon) {
            renderer.rightArmPose = ArmPose.BOW_AND_ARROW;
            // renderer.getMainModel().bipedLeftArm.rotateAngleY = 90;
        }
        if (player.getHeldItem(EnumHand.OFF_HAND) != null && player.getHeldItem(EnumHand.OFF_HAND).getItem() instanceof IHoldableWeapon) {
            renderer.leftArmPose = ArmPose.BOW_AND_ARROW;
        }
        JetpackHandler.preRenderPlayer(player);
        if (player.getHeldItemMainhand().getItem() == ModItems.gun_egon) {
            EgonBackpackRenderer.showBackpack = true;
        }

        ResourceLocation cloak = RenderAccessoryUtility.getCloakFromPlayer(player);
        // GL11.glRotated(180, 1, 0, 0);
        NetworkPlayerInfo info = Minecraft.getMinecraft().getConnection().getPlayerInfo(player.getUniqueID());
        if (cloak != null)
            RenderAccessoryUtility.loadCape(info, cloak);
    }

    @SubscribeEvent
    public void preRenderLiving(RenderLivingEvent.Pre<AbstractClientPlayer> event) {

        if (specialDeathEffectEntities.contains(event.getEntity())) {
            event.setCanceled(true);
        }
        if (event.getEntity() instanceof AbstractClientPlayer && event.getRenderer().getMainModel() instanceof ModelBiped) {
            AbstractClientPlayer player = (AbstractClientPlayer) event.getEntity();

            ModelBiped renderer = (ModelBiped) event.getRenderer().getMainModel();

            if (player.getHeldItem(EnumHand.MAIN_HAND) != null && player.getHeldItem(EnumHand.MAIN_HAND).getItem() instanceof IHoldableWeapon) {
                renderer.rightArmPose = ArmPose.BOW_AND_ARROW;
            }
            if (player.getHeldItem(EnumHand.OFF_HAND) != null && player.getHeldItem(EnumHand.OFF_HAND).getItem() instanceof IHoldableWeapon) {
                renderer.leftArmPose = ArmPose.BOW_AND_ARROW;
            }
        }
    }

    @SubscribeEvent
    public void postRenderPlayer(RenderPlayerEvent.Post event) {
        JetpackHandler.postRenderPlayer(event.getEntityPlayer());
        EntityPlayer player = event.getEntityPlayer();
        //GLUON GUN//
        boolean firing = player == Minecraft.getMinecraft().player ? ItemGunEgon.m1 && Library.countInventoryItem(player.inventory, ItemGunEgon.getBeltType(player, player.getHeldItemMainhand(), true)) >= 2 : ItemGunEgon.getIsFiring(player.getHeldItemMainhand());
        EgonBackpackRenderer.showBackpack = false;
        if (player.getHeldItemMainhand().getItem() == ModItems.gun_egon && firing) {
            GlStateManager.pushMatrix();
            float partialTicks = event.getPartialRenderTick();
            float[] angles = ItemGunEgon.getBeamDirectionOffset(player.world.getTotalWorldTime() + partialTicks);
            Vec3d look = Library.changeByAngle(player.getLook(partialTicks), angles[0], angles[1]);
            RayTraceResult r = Library.rayTraceIncludeEntitiesCustomDirection(player, look, 50, event.getPartialRenderTick());
            Vec3d pos = player.getPositionEyes(event.getPartialRenderTick());
            Vec3d hitPos = pos.add(look.scale(50));
            if (r == null || r.typeOfHit == Type.MISS) {
            } else {
                hitPos = r.hitVec.add(look.scale(-0.1));
            }
            Vec3d start = new Vec3d(-0.18, -0.1, 0.35);
            start = start.rotatePitch((float) Math.toRadians(-(player.prevRotationPitch + (player.rotationPitch - player.prevRotationPitch) * partialTicks)));
            start = start.rotateYaw((float) Math.toRadians(-(player.prevRotationYaw + (player.rotationYaw - player.prevRotationYaw) * partialTicks)));

            Vec3d diff = player.getPositionEyes(partialTicks).subtract(TileEntityRendererDispatcher.staticPlayerX, TileEntityRendererDispatcher.staticPlayerY, TileEntityRendererDispatcher.staticPlayerZ);
            GlStateManager.translate(start.x + diff.x, start.y + diff.y, start.z + diff.z);
            BeamPronter.gluonBeam(new Vec3d(0, 0, 0), new Vec3d(Vec3dUtil.convertToVec3i(hitPos.subtract(pos))), 0.4F);
            GlStateManager.popMatrix();
        }
    }

    @SubscribeEvent
    public void clickHandler(MouseEvent event) {
        EntityPlayer player = Minecraft.getMinecraft().player;
        if (event.getButton() == 1 && !event.isButtonstate())
            ItemSwordCutter.canClick = true;

        boolean m1 = ItemGunBase.m1;
        boolean m2 = ItemGunBase.m2;
        if (player.getHeldItem(EnumHand.MAIN_HAND) != null && player.getHeldItem(EnumHand.MAIN_HAND).getItem() instanceof ItemGunBase) {

            if (event.getButton() == 0)
                event.setCanceled(true);

            ItemGunBase item = (ItemGunBase) player.getHeldItem(EnumHand.MAIN_HAND).getItem();
            if (event.getButton() == 0 && !m1 && !m2) {
                ItemGunBase.m1 = true;
                PacketDispatcher.wrapper.sendToServer(new GunButtonPacket(true, (byte) 0, EnumHand.MAIN_HAND));
                item.startActionClient(player.getHeldItemMainhand(), player.world, player, true, EnumHand.MAIN_HAND);
            } else if (event.getButton() == 1 && !m2 && !m1) {
                ItemGunBase.m2 = true;
                PacketDispatcher.wrapper.sendToServer(new GunButtonPacket(true, (byte) 1, EnumHand.MAIN_HAND));
                item.startActionClient(player.getHeldItemMainhand(), player.world, player, false, EnumHand.MAIN_HAND);
            }
        }
        if (player.getHeldItem(EnumHand.OFF_HAND) != null && player.getHeldItem(EnumHand.OFF_HAND).getItem() instanceof ItemGunBase) {

            if (event.getButton() == 0)
                event.setCanceled(true);

            ItemGunBase item = (ItemGunBase) player.getHeldItem(EnumHand.OFF_HAND).getItem();
            if (event.getButton() == 0 && !m1 && !m2) {
                ItemGunBase.m1 = true;
                PacketDispatcher.wrapper.sendToServer(new GunButtonPacket(true, (byte) 0, EnumHand.OFF_HAND));
                item.startActionClient(player.getHeldItemOffhand(), player.world, player, true, EnumHand.OFF_HAND);
            } else if (event.getButton() == 1 && !m2 && !m1) {
                ItemGunBase.m2 = true;
                PacketDispatcher.wrapper.sendToServer(new GunButtonPacket(true, (byte) 1, EnumHand.OFF_HAND));
                item.startActionClient(player.getHeldItemOffhand(), player.world, player, false, EnumHand.OFF_HAND);
            }
        }
    }

    // FIXME: This is absolutely fucked up logic, get rid of this asap
    @Spaghetti("please get this shit out of my face")
    @SubscribeEvent
    public void onPlaySound(PlaySoundEvent e) {
        ResourceLocation r = e.getSound().getSoundLocation();

        WorldClient wc = Minecraft.getMinecraft().world;

        // Alright, alright, I give the fuck up, you've wasted my time enough
        // with this bullshit. You win.
        // A winner is you.
        // Conglaturations.
        // Fuck you.

        if (r.toString().equals("hbm:misc.nulltau") && Library.getClosestPlayerForSound(wc, e.getSound().getXPosF(), e.getSound().getYPosF(), e.getSound().getZPosF(), 2) != null) {
            EntityPlayer ent = Library.getClosestPlayerForSound(wc, e.getSound().getXPosF(), e.getSound().getYPosF(), e.getSound().getZPosF(), 2);

            if (MovingSoundPlayerLoop.getSoundByPlayer(ent, EnumHbmSound.soundTauLoop) == null) {
                MovingSoundPlayerLoop.globalSoundList.add(new MovingSoundXVL1456(HBMSoundHandler.tauChargeLoop2, SoundCategory.PLAYERS, ent, EnumHbmSound.soundTauLoop));
                MovingSoundPlayerLoop.getSoundByPlayer(ent, EnumHbmSound.soundTauLoop).setPitch(0.5F);
            } else {
                if (MovingSoundPlayerLoop.getSoundByPlayer(ent, EnumHbmSound.soundTauLoop).getPitch() < 1.5F)
                    MovingSoundPlayerLoop.getSoundByPlayer(ent, EnumHbmSound.soundTauLoop).setPitch(MovingSoundPlayerLoop.getSoundByPlayer(ent, EnumHbmSound.soundTauLoop).getPitch() + 0.01F);
            }
        }
//        if (r.toString().equals("hbm:misc.nullradar") && Library.getClosestPlayerForSound(wc, e.getSound().getXPosF(), e.getSound().getYPosF(), e.getSound().getZPosF(), 2) != null) {
//            EntityPlayer ent = Library.getClosestPlayerForSound(wc, e.getSound().getXPosF(), e.getSound().getYPosF(), e.getSound().getZPosF(), 2);
//            if (MovingSoundPlayerLoop.getSoundByPlayer(ent, EnumHbmSound.soundRadarLoop) == null) {
//                MovingSoundPlayerLoop.globalSoundList.add(new MovingSoundRadarLoop(HBMSoundHandler.alarmAirRaid, SoundCategory.PLAYERS, ent, EnumHbmSound.soundRadarLoop));
//                MovingSoundPlayerLoop.getSoundByPlayer(ent, EnumHbmSound.soundRadarLoop).setVolume(1.0F);
//            }
//        }

        if (r.toString().equals("hbm:misc.nullchopper") && Library.getClosestChopperForSound(wc, e.getSound().getXPosF(), e.getSound().getYPosF(), e.getSound().getZPosF(), 2) != null) {
            EntityHunterChopper ent = Library.getClosestChopperForSound(wc, e.getSound().getXPosF(), e.getSound().getYPosF(), e.getSound().getZPosF(), 2);

            if (MovingSoundPlayerLoop.getSoundByPlayer(ent, EnumHbmSound.soundChopperLoop) == null) {
                MovingSoundPlayerLoop.globalSoundList.add(new MovingSoundChopper(HBMSoundHandler.chopperFlyingLoop, SoundCategory.HOSTILE, ent, EnumHbmSound.soundChopperLoop));
                MovingSoundPlayerLoop.getSoundByPlayer(ent, EnumHbmSound.soundChopperLoop).setVolume(10.0F);
            }
        }

        if (r.toString().equals("hbm:misc.nullcrashing") && Library.getClosestChopperForSound(wc, e.getSound().getXPosF(), e.getSound().getYPosF(), e.getSound().getZPosF(), 2) != null) {
            EntityHunterChopper ent = Library.getClosestChopperForSound(wc, e.getSound().getXPosF(), e.getSound().getYPosF(), e.getSound().getZPosF(), 2);

            if (MovingSoundPlayerLoop.getSoundByPlayer(ent, EnumHbmSound.soundCrashingLoop) == null) {
                MovingSoundPlayerLoop.globalSoundList.add(new MovingSoundCrashing(HBMSoundHandler.chopperCrashingLoop, SoundCategory.HOSTILE, ent, EnumHbmSound.soundCrashingLoop));
                MovingSoundPlayerLoop.getSoundByPlayer(ent, EnumHbmSound.soundCrashingLoop).setVolume(10.0F);
            }
        }

        if (r.toString().equals("hbm:misc.nullmine") && Library.getClosestMineForSound(wc, e.getSound().getXPosF(), e.getSound().getYPosF(), e.getSound().getZPosF(), 2) != null) {
            EntityChopperMine ent = Library.getClosestMineForSound(wc, e.getSound().getXPosF(), e.getSound().getYPosF(), e.getSound().getZPosF(), 2);

            if (MovingSoundPlayerLoop.getSoundByPlayer(ent, EnumHbmSound.soundMineLoop) == null) {
                MovingSoundPlayerLoop.globalSoundList.add(new MovingSoundChopperMine(HBMSoundHandler.chopperMineLoop, SoundCategory.HOSTILE, ent, EnumHbmSound.soundMineLoop));
                MovingSoundPlayerLoop.getSoundByPlayer(ent, EnumHbmSound.soundMineLoop).setVolume(10.0F);
            }
        }

        for (MovingSoundPlayerLoop sounds : MovingSoundPlayerLoop.globalSoundList) {
            if (!sounds.init || sounds.isDonePlaying()) {
                sounds.init = true;
                sounds.setDone(false);
                Minecraft.getMinecraft().getSoundHandler().playSound(sounds);
            }
        }
    }

    @SubscribeEvent
    public void onPlayerLeaveServer(ClientDisconnectionFromServerEvent event) {
        SerializableRecipe.clearReceivedRecipes();
    }

    @SubscribeEvent
    public void drawBlockSelectionBox(DrawBlockHighlightEvent evt) {
        if (evt.getTarget().typeOfHit == RayTraceResult.Type.BLOCK) {
            BlockPos pos = evt.getTarget().getBlockPos();
            IBlockState state = Minecraft.getMinecraft().world.getBlockState(pos);
            Block block = state.getBlock();
            if (block instanceof ICustomSelectionBox) {
                GlStateManager.enableBlend();
                GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
                GlStateManager.glLineWidth(2.0F);
                GlStateManager.disableTexture2D();
                GlStateManager.depthMask(false);
                EntityPlayer player = evt.getPlayer();
                float partialTicks = evt.getPartialTicks();
                double d3 = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double) partialTicks;
                double d4 = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double) partialTicks;
                double d5 = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double) partialTicks;
                if (((ICustomSelectionBox) block).renderBox(Minecraft.getMinecraft().world, player, state, evt.getTarget().getBlockPos(), pos.getX() - d3, pos.getY() - d4, pos.getZ() - d5, partialTicks)) {
                    evt.setCanceled(true);
                }
                GlStateManager.depthMask(true);
                GlStateManager.enableTexture2D();
                GlStateManager.disableBlend();
            }
        }
    }

    @SubscribeEvent
    public void drawTooltip(ItemTooltipEvent event) {

        ItemStack stack = event.getItemStack();
        List<String> list = event.getToolTip();

        /// DAMAGE RESISTANCE ///
        DamageResistanceHandler.addInfo(stack, list);

        /// HAZMAT INFO ///
        List<HazardClass> hazInfo = ArmorRegistry.hazardClasses.get(stack.getItem());

        if (hazInfo != null) {

            if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
                list.add(TextFormatting.GOLD + I18nUtil.resolveKey("hazard.prot"));
                for (HazardClass clazz : hazInfo) {
                    list.add(TextFormatting.YELLOW + "  " + I18nUtil.resolveKey(clazz.lang));
                }
            } else {

                list.add(I18nUtil.resolveKey("desc.tooltip.hold", "LSHIFT"));
            }
        }

        /// CLADDING ///
        double rad = HazmatRegistry.getResistance(stack);
        rad = ((int) (rad * 100)) / 100D;
        if (rad > 0)
            list.add(TextFormatting.YELLOW + I18nUtil.resolveKey("trait.radResistance", rad));


        /// ARMOR MODS ///
        if (stack.getItem() instanceof ItemArmor && ArmorModHandler.hasMods(stack)) {

            if (!Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) && !(Minecraft.getMinecraft().currentScreen instanceof GUIArmorTable)) {

                list.add(I18nUtil.resolveKey("desc.tooltip.holdarmor", "LSHIFT"));

            } else {

                list.add(TextFormatting.YELLOW + I18nUtil.resolveKey("desc.mods"));

                ItemStack[] mods = ArmorModHandler.pryMods(stack);

                for (int i = 0; i < 8; i++) {

                    if (mods[i] != null && mods[i].getItem() instanceof ItemArmorMod) {

                        ((ItemArmorMod) mods[i].getItem()).addDesc(list, mods[i], stack);
                    }
                }
            }
        }

        /// NEUTRON RADS ///
        ContaminationUtil.addNeutronRadInfo(stack, event.getEntityPlayer(), list, event.getFlags());

        /// HAZARDS ///
        HazardSystem.addHazardInfo(stack, event.getEntityPlayer(), list, event.getFlags());

        /// CUSTOM NUKE ///
        ComparableStack comp = new NbtComparableStack(stack).makeSingular();
        CustomNukeEntry entry = TileEntityNukeCustom.entries.get(comp);

        if (entry != null) {

            if (!list.isEmpty())
                list.add("");

            if (entry.entry == EnumEntryType.ADD)
                list.add(TextFormatting.GOLD + I18nUtil.resolveKey("desc.nstageadd", entry.value, entry.type));

            if (entry.entry == EnumEntryType.MULT)
                list.add(TextFormatting.GOLD + I18nUtil.resolveKey("desc.nstagemult", entry.value, entry.type));
        }

        /// CREATE-ISH HELP (WIAJ) ///
        try {
            CanneryBase cannery = Jars.canneries.get(comp);
            if (cannery != null) {
                list.add(TextFormatting.GREEN + I18nUtil.resolveKey("cannery.f1"));
                lastCannery = comp;
                canneryTimestamp = System.currentTimeMillis();
            }
        } catch (Exception ex) {
            list.add(TextFormatting.RED + "Error loading cannery: " + ex.getLocalizedMessage());
        }
        /// NEUTRON RADS ///
        if (event.getFlags().isAdvanced()) {
            List<String> names = ItemStackUtil.getOreDictNames(stack);
            if (names.size() > 0) {
                list.add("§bOre Dict:");
                for (String s : names) {
                    list.add("§3 - " + s);
                }
            }
        }

        //MKU
        if (stack.hasTagCompound()) {
            if (stack.getTagCompound().getBoolean("ntmContagion"))
                list.add("§4§l[" + I18nUtil.resolveKey("trait.mkuinfected") + "§4§l]");
        }
    }

    @SubscribeEvent
    public void renderFrame(RenderItemInFrameEvent event) {

        if (event.getItem() != null && event.getItem().getItem() == ModItems.flame_pony) {
            event.setCanceled(true);

            double p = 0.0625D;
            double o = p * 2.75D;

            GlStateManager.disableLighting();
            Minecraft.getMinecraft().renderEngine.bindTexture(poster);
            net.minecraft.client.renderer.Tessellator tess = net.minecraft.client.renderer.Tessellator.getInstance();
            BufferBuilder buf = tess.getBuffer();
            buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
            buf.pos(0.5, 0.5 + o, p * 0.5).tex(1, 0).endVertex();
            buf.pos(-0.5, 0.5 + o, p * 0.5).tex(0, 0).endVertex();
            buf.pos(-0.5, -0.5 + o, p * 0.5).tex(0, 1).endVertex();
            buf.pos(0.5, -0.5 + o, p * 0.5).tex(1, 1).endVertex();
            tess.draw();
            GlStateManager.enableLighting();
        }
    }

    private void wrapModel(ModelBakeEvent event, ModelResourceLocation location) {
        IBakedModel existingModel = event.getModelRegistry().getObject(location);
        if (existingModel != null && !(existingModel instanceof TemplateBakedModel)) {
            TemplateBakedModel wrapper = new TemplateBakedModel(existingModel);
            event.getModelRegistry().putObject(location, wrapper);
        }
    }
}
