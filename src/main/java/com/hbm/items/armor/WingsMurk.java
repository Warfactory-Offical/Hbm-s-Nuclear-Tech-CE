package com.hbm.items.armor;

import com.hbm.capability.HbmCapability;
import com.hbm.capability.HbmCapability.IHBMData;
import com.hbm.handler.ArmorUtil;
import com.hbm.items.ModItems;
import com.hbm.main.ResourceManager;
import com.hbm.render.model.ModelArmorWings;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class WingsMurk extends JetpackBase {

	public WingsMurk( String s) {
		super(s);
	}

	@SideOnly(Side.CLIENT)
	ModelArmorWings model;

	@Override
	@SideOnly(Side.CLIENT)
	public ModelBiped getArmorModel(EntityLivingBase entityLiving, ItemStack itemStack, EntityEquipmentSlot armorSlot, ModelBiped _default){
		if(model == null) {
			model = new ModelArmorWings(this == ModItems.wings_murk ? 0 : 1);
		}
		return model;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type){
		return ResourceManager.wings_murk.toString();
	}
	
	public void onArmorTick(World world, EntityPlayer player, ItemStack stack) {
		
		if(player.onGround)
			return;
		
		ArmorUtil.resetFlightTime(player);
		
		if(player.fallDistance > 0)
			player.fallDistance = 0;
		
		if(player.motionY < -0.4D)
			player.motionY = -0.4D;
		
		if(this == ModItems.wings_limp) {
			
			 if(player.isSneaking()) {
					
				if(player.motionY < -0.08) {

					double mo = player.motionY * -0.2;
					player.motionY += mo;

					Vec3d vec = player.getLookVec();
                    vec = vec.scale(mo);

					player.motionX += vec.x;
					player.motionY += vec.y;
					player.motionZ += vec.z;
				}
			}
		}

		IHBMData props = HbmCapability.getData(player);
		
		if(this == ModItems.wings_murk) {

			if(props.isJetpackActive()) {

				if(player.motionY < 0.6D)
					player.motionY += 0.2D;
				else
					player.motionY = 0.8D;
				
			} else if(props.getEnableBackpack() && !player.isSneaking()) {
				
				if(player.motionY < -1)
					player.motionY += 0.4D;
				else if(player.motionY < -0.1)
					player.motionY += 0.2D;
				else if(player.motionY < 0)
					player.motionY = 0;
			}
			
			if(props.getEnableBackpack()) {
				
				Vec3d orig = player.getLookVec();
				Vec3d look = new Vec3d(orig.x, 0, orig.z).normalize();
				double mod = player.isSneaking() ? 0.25D : 1D;
				
				if(player.moveForward != 0) {
					player.motionX += look.x * 0.35 * player.moveForward * mod;
					player.motionZ += look.z * 0.35 * player.moveForward * mod;
				}
				
				if(player.moveStrafing != 0) {
					look = look.rotateYaw((float) Math.PI * 0.5F);
					player.motionX += look.x * 0.15 * player.moveStrafing * mod;
					player.motionZ += look.z * 0.15 * player.moveStrafing * mod;
				}
			}
		}
	}
}