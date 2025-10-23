package com.hbm.items.tool;

import com.hbm.config.GeneralConfig;
import com.hbm.interfaces.IBomb;
import com.hbm.interfaces.IHoldableWeapon;
import com.hbm.items.ModItems;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.lib.Library;
import com.hbm.main.MainRegistry;
import com.hbm.render.misc.RenderScreenOverlay;
import com.hbm.util.I18nUtil;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import org.apache.logging.log4j.Level;

import java.util.List;

public class ItemLaserDetonator extends Item implements IHoldableWeapon {

	public ItemLaserDetonator(String s) {
		this.setRegistryName(s);
		this.setTranslationKey(s);
		this.setCreativeTab(MainRegistry.controlTab);
		
		ModItems.ALL_ITEMS.add(this);
	}
	
	@Override
	public void addInformation(ItemStack stack, World worldIn, List<String> list, ITooltipFlag flagIn) {
		list.add(I18nUtil.resolveKey("item.detonator_laser.desc"));
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
		RayTraceResult ray = Library.rayTrace(player, 500, 1);
		BlockPos pos = ray.getBlockPos();
		if(!world.isRemote)
		{
	    	if(world.getBlockState(pos).getBlock() instanceof IBomb) {
	    		((IBomb)world.getBlockState(pos).getBlock()).explode(world, pos, player);

	    		if(GeneralConfig.enableExtendedLogging)
	    			MainRegistry.logger.log(Level.INFO, "[DET] Tried to detonate block at " + pos.getX() + " / " + pos.getY() + " / " + pos.getZ() + " by " + player.getDisplayName() + "!");
	    		
	    		player.sendMessage(new TextComponentString("§2["+I18nUtil.resolveKey("chat.detonated")+"]"+"§r"));
	        	world.playSound(null, player.posX, player.posY, player.posZ, HBMSoundHandler.techBleep, SoundCategory.AMBIENT, 1.0F, 1.0F);
	        	
	    	} else {
	    		player.sendMessage(new TextComponentString("§c"+I18nUtil.resolveKey("chat.posbadrror")+"§r"));
	        	world.playSound(null, player.posX, player.posY, player.posZ, HBMSoundHandler.techBleep, SoundCategory.AMBIENT, 1.0F, 1.0F);
	    	}
		}
		return super.onItemRightClick(world, player, hand);
	}

	@Override
	public RenderScreenOverlay.Crosshair getCrosshair(){return RenderScreenOverlay.Crosshair.L_ARROWS;}
}
