package com.leafia.transformer;

import com.hbm.lib.RefStrings;
import com.hbm.util.I18nUtil;
import com.llib.math.SIPfx;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import java.util.List;

public class LeafiaGeneralLocal {
	public static void injectDebugInfoLeft(List<String> list) {
		int index = -1;
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).equals("")) {
				index = i;
				break;
			}
		}
		if (index >= 0) {
			//list.add(index,"NTM packet network: "+SIPfx.format("%01.2f",RecordablePacket.previousByteUsageSec,true).toLowerCase()+"bytes/sec");
			//list.add(index+1,"("+SIPfx.format("%01.2f",RecordablePacket.previousByteUsageMin,true).toLowerCase()+"bytes/min, "+SIPfx.format("%01.2f",RecordablePacket.previousByteUsage,true).toLowerCase()+"bytes/tick)");
		}
	}
	public static final ResourceLocation acidRain = new ResourceLocation(RefStrings.MODID, "textures/acidicrain.png");
	/*public static boolean acidRainParticles(Entity entity,Biome biome,IBlockState state,BlockPos down,double rx,double rz,AxisAlignedBB bb) {
		double x = (double)down.getX()+rx;
		double y = (double)((float)down.getY()+0.1F)+bb.maxY;
		double z = (double)down.getZ()+rz;
		World world = entity.world;
		if (biome instanceof HasAcidicRain) {
			if (world.rand.nextInt(10) == 0) {
				int rand = world.rand.nextInt(2)+1;
				for (int i = 0; i < rand; i++)
					Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleCloudSmall(world,x,y,z,0.25f));
			}
			if (world.rand.nextInt(900) == 0)
				world.playSound(Minecraft.getMinecraft().player,down,SoundEvents.ENTITY_GENERIC_EXTINGUISH_FIRE,SoundCategory.AMBIENT,0.1f,world.rand.nextFloat()*0.2f+0.6f);
			return false;
		}
		return true;
	}*/
	public static void injectWackySplashes(List<String> splash) {
		splash.add("Floppenheimer!");
		splash.add("i should dip my balls in sulfuric acid");
		splash.add("All answers are popbob!");
		splash.add("None may enter The Orb!");
		splash.add("Wacarb was here");
		splash.add("SpongeBoy me Bob I am overdosing on keramine agagagagaga");
		splash.add(TextFormatting.RED+I18nUtil.resolveKey("zzz.splash",System.getProperty("user.name")));
		splash.add("Nice toes, now hand them over.");
		splash.add("I smell burnt toast!");
		splash.add("There are bugs under your skin!");
		splash.add("Fentanyl!");
		splash.add("Don't do drugs!");
		splash.add("Imagine being scared by splash texts!");
		splash.add("Redditors aren't people!");
		splash.add("Can someone tell me what corrosive fumes the people on Reddit are huffing so I can avoid those more effectively?");
		splash.add("Extra information on F3 debug screen! Did you know?");
		splash.add("This mod sends string characters in just 5 bits!");
		splash.add("zally jumpscare");
		splash.add("Computer core community nowadays is really toxic!");
	}
}
