package com.hbm.wiaj.cannery;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.items.ModItems;
import com.hbm.main.ResourceManager;
import com.hbm.tileentity.network.TileEntityPipeBaseNT;
import com.hbm.util.I18nUtil;
import com.hbm.wiaj.JarScene;
import com.hbm.wiaj.JarScript;
import com.hbm.wiaj.WorldInAJar;
import com.hbm.wiaj.actions.*;
import com.hbm.wiaj.actors.ActorFancyPanel;
import com.hbm.wiaj.actors.ActorFancyPanel.Orientation;
import com.hbm.wiaj.actors.ActorTileEntity;
import com.hbm.wiaj.actors.ITileActorRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.lwjgl.opengl.GL11; import net.minecraft.client.renderer.GlStateManager;

public class CanneryCentrifuge extends CanneryBase {

	@Override
	public ItemStack getIcon() {
		return new ItemStack(ModBlocks.machine_gascent);
	}

	@Override
	public String getName() {
		return "cannery.centrifuge";
	}

	public JarScript createScript() {
		WorldInAJar world = new WorldInAJar(9, 5, 5);
		JarScript script = new JarScript(world);
		
		JarScene scene0 = new JarScene(script);
		
		scene0.add(new ActionSetZoom(2, 0));
		
		for(int x = world.sizeX - 1; x >= 0 ; x--) {
			for(int z = 0; z < world.sizeZ; z++) {
				scene0.add(new ActionSetBlock(x, 0, z, ModBlocks.concrete_smooth));
			}
			
			if(x == 7) {
				scene0.add(new ActionSetTile(7, 1, 2, new Dummies.JarDummyConnector()));
				scene0.add(new ActionSetBlock(7, 1, 2, ModBlocks.barrel_tcalloy));
			}
			
			if(x == 6) {
				TileEntityPipeBaseNT duct = new TileEntityPipeBaseNT();
				duct.setType(Fluids.UF6);
				scene0.add(new ActionSetTile(6, 1, 2, duct));
				scene0.add(new ActionSetBlock(6, 1, 2, ModBlocks.fluid_duct_neo, 0));
			}
			
			if(x == 5) {
				scene0.add(new ActionSetTile(5, 1, 2, new Dummies.JarDummyConnector()));
				NBTTagCompound cent = new NBTTagCompound();
				cent.setDouble("x", 5);
				cent.setDouble("y", 1);
				cent.setDouble("z", 2);
				cent.setInteger("rotation", 2);
				scene0.add(new ActionCreateActor(0, new ActorTileEntity(new ActorGasCent(), cent)));
			}
			
			scene0.add(new ActionWait(2));
		}

		scene0.add(new ActionCreateActor(1, new ActorFancyPanel(Minecraft.getMinecraft().fontRenderer, -15, -50, new Object[][] {{I18nUtil.resolveKey("cannery.centrifuge.0")}}, 200)
				.setColors(colorCopper).setOrientation(Orientation.BOTTOM)));
		
		scene0.add(new ActionWait(60));
		scene0.add(new ActionRemoveActor(1));
		
		JarScene scene1 = new JarScene(script);

		scene1.add(new ActionCreateActor(1, new ActorFancyPanel(Minecraft.getMinecraft().fontRenderer, -15, 10, new Object[][] {{I18nUtil.resolveKey("cannery.centrifuge.1")}}, 200)
				.setColors(colorCopper).setOrientation(Orientation.CENTER)));
		
		scene1.add(new ActionWait(60));
		scene1.add(new ActionRemoveActor(1));

		scene1.add(new ActionSetZoom(4, 20));
		
		scene1.add(new ActionCreateActor(1, new ActorFancyPanel(Minecraft.getMinecraft().fontRenderer, 0, 40, new Object[][] {{I18nUtil.resolveKey("cannery.centrifuge.2")}}, 150)
				.setColors(colorCopper).setOrientation(Orientation.LEFT)));
		
		scene1.add(new ActionWait(60));
		scene1.add(new ActionRemoveActor(1));
		scene1.add(new ActionSetZoom(-2, 20));
		scene1.add(new ActionWait(20));
		
		NBTTagCompound c2 = new NBTTagCompound(); c2.setDouble("x", 4); c2.setDouble("y", 1); c2.setDouble("z", 2); c2.setInteger("rotation", 2);
		scene1.add(new ActionCreateActor(1, new ActorTileEntity(new ActorGasCent(), c2)));
		scene1.add(new ActionWait(10));
		
		scene1.add(new ActionCreateActor(2, new ActorFancyPanel(Minecraft.getMinecraft().fontRenderer, 0, 0, new Object[][] {{I18nUtil.resolveKey("cannery.centrifuge.3")}}, 200)
				.setColors(colorCopper).setOrientation(Orientation.CENTER)));
		
		scene1.add(new ActionWait(100));
		scene1.add(new ActionRemoveActor(2));
		scene1.add(new ActionSetZoom(-2, 20));
		
		scene1.add(new ActionCreateActor(2, new ActorFancyPanel(Minecraft.getMinecraft().fontRenderer, 0, 0, new Object[][] {{I18nUtil.resolveKey("cannery.centrifuge.4")}}, 200)
				.setColors(colorCopper).setOrientation(Orientation.CENTER)));
		
		scene1.add(new ActionWait(60));
		scene1.add(new ActionRemoveActor(2));

		NBTTagCompound c3 = new NBTTagCompound(); c3.setDouble("x", 3); c3.setDouble("y", 1); c3.setDouble("z", 2); c3.setInteger("rotation", 2);
		scene1.add(new ActionCreateActor(2, new ActorTileEntity(new ActorGasCent(), c3)));
		scene1.add(new ActionWait(10));
		NBTTagCompound c4 = new NBTTagCompound(); c4.setDouble("x", 2); c4.setDouble("y", 1); c4.setDouble("z", 2); c4.setInteger("rotation", 2);
		scene1.add(new ActionCreateActor(3, new ActorTileEntity(new ActorGasCent(), c4)));
		scene1.add(new ActionWait(10));
		
		scene1.add(new ActionCreateActor(4, new ActorFancyPanel(Minecraft.getMinecraft().fontRenderer, 0, 0, new Object[][] {{I18nUtil.resolveKey("cannery.centrifuge.5")}}, 200)
				.setColors(colorCopper).setOrientation(Orientation.CENTER)));
		
		scene1.add(new ActionWait(60));
		scene1.add(new ActionRemoveActor(4));

		scene1.add(new ActionCreateActor(4, new ActorFancyPanel(Minecraft.getMinecraft().fontRenderer, 45, 35, new Object[][] {{" = ", new ItemStack(ModItems.nugget_u238, 11), new ItemStack(ModItems.nugget_u235)}}, 0)
				.setColors(colorCopper).setOrientation(Orientation.LEFT)));
		
		script.addScene(scene0).addScene(scene1);
		return script;
	}
	
	public static class ActorGasCent implements ITileActorRenderer {

		@Override
		public void renderActor(WorldInAJar world, int ticks, float interp, NBTTagCompound data) {
			double x = data.getDouble("x");
			double y = data.getDouble("y");
			double z = data.getDouble("z");
			int rotation = data.getInteger("rotation");
			
			GlStateManager.translate(x + 0.5D, y, z + 0.5D);
			GlStateManager.enableLighting();
			GlStateManager.shadeModel(GL11.GL_SMOOTH);
			
			switch(rotation) {
			case 3: GlStateManager.rotate(0, 0F, 1F, 0F); break;
			case 5: GlStateManager.rotate(90, 0F, 1F, 0F); break;
			case 2: GlStateManager.rotate(180, 0F, 1F, 0F); break;
			case 4: GlStateManager.rotate(270, 0F, 1F, 0F); break;
			}
			
			ITileActorRenderer.bindTexture(ResourceManager.centrifuge_gas_tex);
			ResourceManager.centrifuge_gas.renderPart("Centrifuge");
			ResourceManager.centrifuge_gas.renderPart("Flag");
			
			GlStateManager.shadeModel(GL11.GL_FLAT);
		}

		@Override
		public void updateActor(int ticks, NBTTagCompound data) { }
	}
}
