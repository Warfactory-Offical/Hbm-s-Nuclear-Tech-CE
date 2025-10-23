package com.hbm.items.tool;

import baubles.api.BaubleType;
import baubles.api.IBauble;
import com.hbm.capability.HbmLivingProps;
import com.hbm.items.ModItems;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.util.ContaminationUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;

import java.util.ArrayList;
import java.util.List;

@Optional.InterfaceList({@Optional.Interface(iface = "baubles.api.IBauble", modid = "baubles")})
public class ItemDigammaDiagnostic extends Item implements IBauble {

	public ItemDigammaDiagnostic(String s) {
		this.setTranslationKey(s);
		this.setRegistryName(s);
		
		ModItems.ALL_ITEMS.add(this);
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand handIn) {
		
		if(!world.isRemote) {
			world.playSound(null, player.posX, player.posY, player.posZ, HBMSoundHandler.techBoop, SoundCategory.PLAYERS, 1.0F, 1.0F);
			ContaminationUtil.printDiagnosticData(player);
		}
		
		return super.onItemRightClick(world, player, handIn);
	}

	public static void playVoices(World world, EntityPlayer player){
		double x = HbmLivingProps.getDigamma(player);
		
		if(world.getTotalWorldTime() % 10 == 0 && world.rand.nextInt((int)(20/x)) == 0) {

			if(x > 0.01) {
				List<Integer> list = new ArrayList<Integer>();

				if(0.05 < x && x < 2){
					list.add(0);
				}
				if(0.25 < x && x < 3){
					list.add(1);
				}
				if(0.5 < x && x < 4){
					list.add(2);
				}
				if(1 < x && x < 5){
					list.add(3);
				}
				if(2 < x && x < 6){
					list.add(4);
				}
				if(3 < x && x < 7){
					list.add(5);
				}
				if(4 < x && x < 9){
					list.add(6);
				}
				if(5 < x){
					list.add(7);
				}
				if(6 < x){
					list.add(8);
				}
				if(list.size() > 0){
					int r = list.get(world.rand.nextInt(list.size()));
					
					if(r > 0){
						world.playSound(null, player.posX, player.posY, player.posZ, HBMSoundHandler.voiceSounds[r-1], SoundCategory.PLAYERS, (float)x*0.04F+0.04F, 1.0F);
					}
				}
			}
		}
	}

	@Override
	public BaubleType getBaubleType(ItemStack itemstack){
		return BaubleType.TRINKET;
	}
}
