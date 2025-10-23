package com.hbm.items.armor;

import com.hbm.capability.HbmCapability;
import com.hbm.capability.HbmCapability.IHBMData;
import com.hbm.handler.ArmorUtil;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.render.amlfrom1710.Vec3;
import com.hbm.render.model.ModelArmorBJ;
import com.hbm.util.I18nUtil;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class ArmorBJJetpack extends ArmorBJ {

	public ArmorBJJetpack(ArmorMaterial material, int layer, EntityEquipmentSlot slot, String texture, long maxPower, long chargeRate, long consumption, long drain, String s) {
		super(material, layer, slot, texture, maxPower, chargeRate, consumption, drain, s);
	}

	@SideOnly(Side.CLIENT)
	ModelArmorBJ model;

	@Override
	@SideOnly(Side.CLIENT)
	public ModelBiped getArmorModel(EntityLivingBase entityLiving, ItemStack itemStack, EntityEquipmentSlot armorSlot, ModelBiped _default){
		if(model == null) {
			model = new ModelArmorBJ(5);
		}
		return model;
	}

	public void onArmorTick(World world, EntityPlayer player, ItemStack stack) {
		
		super.onArmorTick(world, player, stack);
		
		IHBMData props = HbmCapability.getData(player);
		
		if(!world.isRemote) {
			
			if(hasFSBArmor(player) && props.isJetpackActive()) {

				NBTTagCompound data = new NBTTagCompound();
				data.setString("type", "jetpack_bj");
				data.setInteger("player", player.getEntityId());
				PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(data, player.posX, player.posY, player.posZ), new TargetPoint(world.provider.getDimension(), player.posX, player.posY, player.posZ, 100));
			}
		}

		if(hasFSBArmor(player)) {
			
			ArmorUtil.resetFlightTime(player);
			
			if(props.isJetpackActive()) {
				
				if(player.motionY < 0.4D)
					player.motionY += 0.1D;
				
				player.fallDistance = 0;
				
				world.playSound(null, player.posX, player.posY, player.posZ, HBMSoundHandler.immolatorShoot, SoundCategory.PLAYERS, 0.125F, 1.5F);
				
			} else if(player.isSneaking()) {
				
				if(player.motionY < -0.08) {
					
					double mo = player.motionY * -0.4;
					player.motionY += mo;
					
					Vec3 vec = new Vec3(player.getLookVec());
					vec.xCoord *= mo;
					vec.yCoord *= mo;
					vec.zCoord *= mo;

					player.motionX += vec.xCoord;
					player.motionY += vec.yCoord;
					player.motionZ += vec.zCoord;
				}
			}
		}
    }
    
	@Override
	public void addInformation(ItemStack stack, World worldIn, List<String> list, ITooltipFlag flagIn){
		super.addInformation(stack, worldIn, list, flagIn);
		list.add(TextFormatting.RED + "  + " + I18nUtil.resolveKey("armor.electricJetpack"));
    	list.add(TextFormatting.GRAY + "  + " + I18nUtil.resolveKey("armor.glider"));
	}
}