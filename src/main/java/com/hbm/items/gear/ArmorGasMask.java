package com.hbm.items.gear;

import com.hbm.api.item.IGasMask;
import com.hbm.handler.ArmorUtil;
import com.hbm.items.ModItems;
import com.hbm.lib.RefStrings;
import com.hbm.render.NTMRenderHelper;
import com.hbm.render.model.ModelGasMask;
import com.hbm.render.model.ModelM65;
import com.hbm.util.ArmorRegistry.HazardClass;
import com.hbm.util.I18nUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ArmorGasMask extends ItemArmor implements IGasMask {

	@SideOnly(Side.CLIENT)
	private ModelGasMask modelGas;
	/*@SideOnly(Side.CLIENT)
	private ModelOxygenMask modelOxy;*/
	@SideOnly(Side.CLIENT)
	private ModelM65 modelM65;
	
	private ResourceLocation goggleBlur0 = new ResourceLocation(RefStrings.MODID + ":textures/misc/overlay_goggles_0.png");
	private ResourceLocation goggleBlur1 = new ResourceLocation(RefStrings.MODID + ":textures/misc/overlay_goggles_1.png");
	private ResourceLocation goggleBlur2 = new ResourceLocation(RefStrings.MODID + ":textures/misc/overlay_goggles_2.png");
	private ResourceLocation goggleBlur3 = new ResourceLocation(RefStrings.MODID + ":textures/misc/overlay_goggles_3.png");
	private ResourceLocation goggleBlur4 = new ResourceLocation(RefStrings.MODID + ":textures/misc/overlay_goggles_4.png");
	private ResourceLocation goggleBlur5 = new ResourceLocation(RefStrings.MODID + ":textures/misc/overlay_goggles_5.png");
	private ResourceLocation gasmaskBlur0 = new ResourceLocation(RefStrings.MODID + ":textures/misc/overlay_gasmask_0.png");
	private ResourceLocation gasmaskBlur1 = new ResourceLocation(RefStrings.MODID + ":textures/misc/overlay_gasmask_1.png");
	private ResourceLocation gasmaskBlur2 = new ResourceLocation(RefStrings.MODID + ":textures/misc/overlay_gasmask_2.png");
	private ResourceLocation gasmaskBlur3 = new ResourceLocation(RefStrings.MODID + ":textures/misc/overlay_gasmask_3.png");
	private ResourceLocation gasmaskBlur4 = new ResourceLocation(RefStrings.MODID + ":textures/misc/overlay_gasmask_4.png");
	private ResourceLocation gasmaskBlur5 = new ResourceLocation(RefStrings.MODID + ":textures/misc/overlay_gasmask_5.png");
	
	public ArmorGasMask(ArmorMaterial materialIn, int renderIndexIn, EntityEquipmentSlot equipmentSlotIn, String s) {
		super(materialIn, renderIndexIn, equipmentSlotIn);
		this.setTranslationKey(s);
		this.setRegistryName(s);
		this.setCreativeTab(CreativeTabs.COMBAT);
		
		ModItems.ALL_ITEMS.add(this);
	}
	
	@Override
	public boolean isValidArmor(ItemStack stack, EntityEquipmentSlot armorType, Entity entity) {
		return armorType == EntityEquipmentSlot.HEAD;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public ModelBiped getArmorModel(EntityLivingBase entityLiving, ItemStack itemStack, EntityEquipmentSlot armorSlot, ModelBiped _default) {
		if (this == ModItems.gas_mask) {
			if (armorSlot == EntityEquipmentSlot.HEAD) {
				if (this.modelGas == null) {
					this.modelGas = new ModelGasMask();
				}
				return this.modelGas;
			}
		}
		if (this == ModItems.gas_mask_m65 || this == ModItems.hazmat_helmet_red || this == ModItems.hazmat_helmet_grey || this == ModItems.gas_mask_mono || this == ModItems.hazmat_paa_helmet || this == ModItems.gas_mask_olde) {
			if (armorSlot == EntityEquipmentSlot.HEAD) {
				if (this.modelM65 == null) {
					this.modelM65 = new ModelM65();
				}
				return this.modelM65;
			}
		}
		return null;
	}
	
	@Override
	public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type) {
		if (stack.getItem() == ModItems.gas_mask) {
			return (RefStrings.MODID + ":textures/armor/GasMask.png");
		}
		if (stack.getItem() == ModItems.gas_mask_m65) {
			return (RefStrings.MODID + ":textures/armor/ModelM65.png");
		}
		if(stack.getItem() == ModItems.gas_mask_olde) {
			return "hbm:textures/armor/mask_olde.png";
		}
		if (stack.getItem() == ModItems.gas_mask_mono) {
			return (RefStrings.MODID + ":textures/armor/ModelM65Mono.png");
		}
		if(stack.getItem() == ModItems.hazmat_helmet) {
			return (RefStrings.MODID + ":textures/armor/hazmat_1.png");
		}
		if (stack.getItem() == ModItems.hazmat_helmet_red) {
			return (RefStrings.MODID + ":textures/armor/ModelHazRed.png");
		}
		if (stack.getItem() == ModItems.hazmat_helmet_grey) {
			return (RefStrings.MODID + ":textures/armor/ModelHazGrey.png");
		}
		if(stack.getItem() == ModItems.hazmat_paa_helmet) {
			return (RefStrings.MODID + ":textures/armor/ModelHazPaa.png");
		}
		return "hbm:textures/models/capes/CapeUnknown.png";
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public void renderHelmetOverlay(ItemStack stack, EntityPlayer player, ScaledResolution resolution, float partialTicks) {
		if(this != ModItems.gas_mask && this != ModItems.gas_mask_m65 && this != ModItems.hazmat_helmet_red && this != ModItems.hazmat_helmet_grey)
    		return;
    	

        GlStateManager.disableDepth();
        GlStateManager.depthMask(false);
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.disableAlpha();
        
        if(this == ModItems.gas_mask_m65 || this == ModItems.hazmat_helmet_red || this == ModItems.hazmat_helmet_grey) {
        	switch((int)((double)stack.getItemDamage() / (double)stack.getMaxDamage() * 6D)) {
        	case 0:
            	Minecraft.getMinecraft().getTextureManager().bindTexture(goggleBlur0); break;
        	case 1:
            	Minecraft.getMinecraft().getTextureManager().bindTexture(goggleBlur1); break;
        	case 2:
            	Minecraft.getMinecraft().getTextureManager().bindTexture(goggleBlur2); break;
        	case 3:
            	Minecraft.getMinecraft().getTextureManager().bindTexture(goggleBlur3); break;
        	case 4:
            	Minecraft.getMinecraft().getTextureManager().bindTexture(goggleBlur4); break;
        	case 5:
            	Minecraft.getMinecraft().getTextureManager().bindTexture(goggleBlur5); break;
        	default:
            	Minecraft.getMinecraft().getTextureManager().bindTexture(goggleBlur5); break;
        	}
        }
        if(this == ModItems.gas_mask) {
        	switch((int)((double)stack.getItemDamage() / (double)stack.getMaxDamage() * 6D)) {
        	case 0:
            	Minecraft.getMinecraft().getTextureManager().bindTexture(gasmaskBlur0); break;
        	case 1:
            	Minecraft.getMinecraft().getTextureManager().bindTexture(gasmaskBlur1); break;
        	case 2:
            	Minecraft.getMinecraft().getTextureManager().bindTexture(gasmaskBlur2); break;
        	case 3:
            	Minecraft.getMinecraft().getTextureManager().bindTexture(gasmaskBlur3); break;
        	case 4:
            	Minecraft.getMinecraft().getTextureManager().bindTexture(gasmaskBlur4); break;
        	case 5:
            	Minecraft.getMinecraft().getTextureManager().bindTexture(gasmaskBlur5); break;
        	default:
            	Minecraft.getMinecraft().getTextureManager().bindTexture(gasmaskBlur5); break;
        	}
        }
        
        NTMRenderHelper.startDrawingTexturedQuads();
        NTMRenderHelper.addVertexWithUV(0.0D, (double)resolution.getScaledHeight(), -90.0D, 0.0D, 1.0D);
        NTMRenderHelper.addVertexWithUV((double)resolution.getScaledWidth(), (double)resolution.getScaledHeight(), -90.0D, 1.0D, 1.0D);
        NTMRenderHelper.addVertexWithUV((double)resolution.getScaledWidth(), 0.0D, -90.0D, 1.0D, 0.0D);
        NTMRenderHelper.addVertexWithUV(0.0D, 0.0D, -90.0D, 0.0D, 0.0D);
        NTMRenderHelper.draw();
        GlStateManager.depthMask(true);
        GlStateManager.enableDepth();
        GlStateManager.enableAlpha();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
	}
	
	@Override
	public void addInformation(ItemStack stack, World worldIn, List<String> list, ITooltipFlag flagIn){
		super.addInformation(stack, worldIn, list, flagIn);
		ArmorUtil.addGasMaskTooltip(stack, worldIn, list, flagIn);
		List<HazardClass> haz = getBlacklist(stack);
	
		if(!haz.isEmpty()) {
			list.add("§c" + I18nUtil.resolveKey("hazard.neverProtects"));
			
			for(HazardClass clazz : haz) {
				list.add("§4 -" + I18nUtil.resolveKey(clazz.lang));
			}
		}
	}

	@Override
	public ArrayList<HazardClass> getBlacklist(ItemStack stack) {
		if(stack.getItem() == ModItems.gas_mask_mono) {
			return new ArrayList<HazardClass>(Arrays.asList(new HazardClass[] {HazardClass.GAS_CHLORINE, HazardClass.GAS_CORROSIVE, HazardClass.NERVE_AGENT, HazardClass.BACTERIA}));
		} else if(stack.getItem() == ModItems.gas_mask || stack.getItem() == ModItems.gas_mask_m65){
			return new ArrayList<HazardClass>(Arrays.asList(new HazardClass[] {HazardClass.GAS_CORROSIVE, HazardClass.NERVE_AGENT}));
		} else {
			return new ArrayList();
		}
	}

	@Override
	public ItemStack getFilter(ItemStack stack) {
		return ArmorUtil.getGasMaskFilter(stack);
	}

	@Override
	public void installFilter(ItemStack stack, ItemStack filter) {
		ArmorUtil.installGasMaskFilter(stack, filter);
	}

	@Override
	public void damageFilter(ItemStack stack, int damage) {
		ArmorUtil.damageGasMaskFilter(stack, damage);
	}

	@Override
	public boolean isFilterApplicable(ItemStack stack, ItemStack filter) {
		return true;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
		if(player.isSneaking()) {
			ItemStack stack = player.getHeldItem(hand);
			ItemStack filter = this.getFilter(stack);
			
			if(filter != null) {
				ArmorUtil.removeFilter(stack);
				
				if(!player.inventory.addItemStackToInventory(filter)) {
					player.dropItem(filter, true, false);
				}
			}
		}
		return super.onItemRightClick(world, player, hand);
	}
}
