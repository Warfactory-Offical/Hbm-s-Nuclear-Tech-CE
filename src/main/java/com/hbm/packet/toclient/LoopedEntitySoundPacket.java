package com.hbm.packet.toclient;

import com.hbm.entity.logic.EntityBomber;
import com.hbm.entity.missile.EntityMissileBaseAdvanced;
import com.hbm.entity.missile.EntityMissileCustom;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.sound.MovingSoundBomber;
import com.hbm.sound.MovingSoundRocket;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class LoopedEntitySoundPacket implements IMessage {

	int entityID;

	public LoopedEntitySoundPacket(){
		
	}

	public LoopedEntitySoundPacket(int entityID){
		this.entityID = entityID;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		entityID = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(entityID);
	}

	public static class Handler implements IMessageHandler<LoopedEntitySoundPacket, IMessage> {
		
		@Override
		//Tamaized, I love you!
		@SideOnly(Side.CLIENT)
		public IMessage onMessage(LoopedEntitySoundPacket m, MessageContext ctx) {
			Minecraft.getMinecraft().addScheduledTask(() -> {
				Entity e = Minecraft.getMinecraft().world.getEntityByID(m.entityID);

				if(e instanceof EntityMissileCustom || e instanceof EntityMissileBaseAdvanced){
					boolean startNew = true;
					for(int i = 0; i < MovingSoundRocket.globalSoundList.size(); i++)  {
						if(MovingSoundRocket.globalSoundList.get(i).rocket == e && !MovingSoundRocket.globalSoundList.get(i).isDonePlaying()){
							startNew = false;
							break;
						}
					}
					if(startNew){
						Minecraft.getMinecraft().getSoundHandler().playSound(new MovingSoundRocket(HBMSoundHandler.rocketEngine, e));
					}
				}
						
				if(e instanceof EntityBomber) {

					int n;
			        int x = (int) e.getDataManager().get(EntityBomber.STYLE);

                    n = switch (x) {
                        case 0, 1, 2, 3, 4 -> 2;
                        case 5, 6, 7, 8 -> 1;
                        default -> 2;
                    };
			        
					boolean flag = true;
					for(int i = 0; i < MovingSoundBomber.globalSoundList.size(); i++)  {
						if(MovingSoundBomber.globalSoundList.get(i).bomber == e && !MovingSoundBomber.globalSoundList.get(i).isDonePlaying()){
							flag = false;
							break;
						}
					}
					
					if(flag) {
						if(n == 2)
							Minecraft.getMinecraft().getSoundHandler().playSound(new MovingSoundBomber(HBMSoundHandler.bomberSmallLoop, SoundCategory.HOSTILE, (EntityBomber)e));
						if(n == 1)
							Minecraft.getMinecraft().getSoundHandler().playSound(new MovingSoundBomber(HBMSoundHandler.bomberLoop, SoundCategory.HOSTILE, (EntityBomber)e));
					}
				}
			});
			
			
			return null;
		}
	}
}
