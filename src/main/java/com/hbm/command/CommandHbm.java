package com.hbm.command;

import com.google.common.collect.Lists;
import com.hbm.blocks.ModBlocks;
import com.hbm.handler.HbmShaderManager2;
import com.hbm.lib.RefStrings;
import com.hbm.main.ModEventHandlerClient;
import com.hbm.main.ResourceManager;
import com.hbm.render.GLCompat;
import com.hbm.world.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.init.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class CommandHbm extends CommandBase {

	@Override
	public String getName() {
		return "hbm";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "[WIP] Usage: /hbm <subcommand> <args>\nDo /hbm subcommands for a list of subcommands";
	}
	
	@Override
	public int getRequiredPermissionLevel() {
		//Level 2 ops can do commands like setblock, gamemode, and give. They can't kick/ban or stop the server.
		return 2;
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos targetPos) {
		if(args.length == 1) {
			return getSubCommands().stream().filter(s -> s.startsWith(args[0])).collect(Collectors.toList());
		} else if(args.length == 2) {
			if("subcommands".equals(args[0])) {
				return Lists.newArrayList("gen").stream().filter(s -> s.startsWith(args[1])).collect(Collectors.toList());
			} else if("gen".equals(args[0])) {
				return Lists.newArrayList("antenna", "relay", "dud", "silo", "factory", "barrel", "vertibird", "vertibird_crashed", "satellite", "spaceship", "sellafield", "radio", "bunker", "desert_atom", "library", "geysir_water", "geysir_vapor", "geysir_chlorine").stream().filter(s ->  s.startsWith(args[1])).collect(Collectors.toList());
			}
		}
		return Collections.emptyList();
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if(args.length == 0) {
			throw new CommandException(getUsage(sender));
		} else if(args.length > 0) {
			if("subcommands".equals(args[0])) {
				doSubcommandCommand(server, sender, args);
				return;
			} else if("gen".equals(args[0])) {
				doGenCommand(server, sender, args);
				return;
			} else if("reloadCollada".equals(args[0])){
				if(FMLCommonHandler.instance().getSide() == Side.CLIENT){
					Minecraft.getMinecraft().addScheduledTask(() -> {
						ResourceManager.loadAnimatedModels();
						ResourceManager.lit_particles = HbmShaderManager2.loadShader(new ResourceLocation(RefStrings.MODID, "shaders/lit_particles"), shader -> {
							GLCompat.bindAttribLocation(shader, 0, "pos");
							GLCompat.bindAttribLocation(shader, 1, "offsetPos");
							GLCompat.bindAttribLocation(shader, 2, "scale");
							GLCompat.bindAttribLocation(shader, 3, "texData");
							GLCompat.bindAttribLocation(shader, 4, "color");
							GLCompat.bindAttribLocation(shader, 5, "lightmap");
						}).withUniforms(HbmShaderManager2.MODELVIEW_MATRIX, HbmShaderManager2.PROJECTION_MATRIX, HbmShaderManager2.INV_PLAYER_ROT_MATRIX, HbmShaderManager2.LIGHTMAP);
						
						ResourceManager.gluon_beam = HbmShaderManager2.loadShader(new ResourceLocation(RefStrings.MODID, "shaders/gluon_beam"))
								.withUniforms(shader -> {
									GLCompat.activeTexture(GLCompat.GL_TEXTURE0+3);
									Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.noise_1);
									shader.uniform1i("noise_1", 3);
									GLCompat.activeTexture(GLCompat.GL_TEXTURE0+4);
									Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.noise_2);
									shader.uniform1i("noise_1", 4);
									GLCompat.activeTexture(GLCompat.GL_TEXTURE0);
									
									float time = (System.currentTimeMillis()%10000000)/1000F;
									shader.uniform1f("time", time);
								});
						
						ResourceManager.gluon_spiral = HbmShaderManager2.loadShader(new ResourceLocation(RefStrings.MODID, "shaders/gluon_spiral"))
								.withUniforms(shader -> {
									//Well, I accidentally uniformed the same noise sampler twice. That explains why the second noise didn't work.
									GLCompat.activeTexture(GLCompat.GL_TEXTURE0+3);
									Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.noise_1);
									shader.uniform1i("noise_1", 3);
									GLCompat.activeTexture(GLCompat.GL_TEXTURE0+4);
									Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.noise_2);
									shader.uniform1i("noise_1", 4);
									GLCompat.activeTexture(GLCompat.GL_TEXTURE0);
									
									float time = (System.currentTimeMillis()%10000000)/1000F;
									shader.uniform1f("time", time);
								});
						
						//Drillgon200: Did I need a shader for this? No, not really, but it's somewhat easier to create a sin wave pattern programmatically than to do it in paint.net.
						ResourceManager.tau_ray = HbmShaderManager2.loadShader(new ResourceLocation(RefStrings.MODID, "shaders/tau_ray"));
						
						ResourceManager.book_circle = HbmShaderManager2.loadShader(new ResourceLocation(RefStrings.MODID, "shaders/book/circle"));
						
						ResourceManager.normal_fadeout = HbmShaderManager2.loadShader(new ResourceLocation(RefStrings.MODID, "shaders/normal_fadeout"));
						
						ResourceManager.heat_distortion = HbmShaderManager2.loadShader(new ResourceLocation(RefStrings.MODID, "shaders/heat_distortion"))
								.withUniforms(shader -> {
									Framebuffer buffer = Minecraft.getMinecraft().getFramebuffer();
									GLCompat.activeTexture(GLCompat.GL_TEXTURE0+3);
									GlStateManager.bindTexture(buffer.framebufferTexture);
									shader.uniform1i("fbo_tex", 3);
									GLCompat.activeTexture(GLCompat.GL_TEXTURE0+4);
									Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.noise_2);
									shader.uniform1i("noise", 4);
									GLCompat.activeTexture(GLCompat.GL_TEXTURE0);
									
									float time = (System.currentTimeMillis()%10000000)/1000F;
									shader.uniform1f("time", time);
									shader.uniform2f("windowSize", Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight);
								});
						
						ResourceManager.desaturate = HbmShaderManager2.loadShader(new ResourceLocation(RefStrings.MODID, "shaders/desaturate"));
						ResourceManager.test_trail = HbmShaderManager2.loadShader(new ResourceLocation(RefStrings.MODID, "shaders/trail"), shader ->{
							GLCompat.bindAttribLocation(shader, 0, "pos");
							GLCompat.bindAttribLocation(shader, 1, "tex");
							GLCompat.bindAttribLocation(shader, 2, "color");
						});
						ResourceManager.blit = HbmShaderManager2.loadShader(new ResourceLocation(RefStrings.MODID, "shaders/blit"));
						ResourceManager.downsample = HbmShaderManager2.loadShader(new ResourceLocation(RefStrings.MODID, "shaders/downsample"));
						ResourceManager.bloom_h = HbmShaderManager2.loadShader(new ResourceLocation(RefStrings.MODID, "shaders/bloom_h"));
						ResourceManager.bloom_v = HbmShaderManager2.loadShader(new ResourceLocation(RefStrings.MODID, "shaders/bloom_v"));
						ResourceManager.bloom_test = HbmShaderManager2.loadShader(new ResourceLocation(RefStrings.MODID, "shaders/bloom_test"));
						ResourceManager.lightning = HbmShaderManager2.loadShader(new ResourceLocation(RefStrings.MODID, "shaders/lightning"), shader ->{
							GLCompat.bindAttribLocation(shader, 0, "pos");
							GLCompat.bindAttribLocation(shader, 1, "tex");
							GLCompat.bindAttribLocation(shader, 2, "color");
						}).withUniforms(shader -> {
							GLCompat.activeTexture(GLCompat.GL_TEXTURE0+4);
							Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.noise_2);
							shader.uniform1i("noise", 4);
							GLCompat.activeTexture(GLCompat.GL_TEXTURE0);
						});
						ResourceManager.maxdepth = HbmShaderManager2.loadShader(new ResourceLocation(RefStrings.MODID, "shaders/maxdepth"));
						ResourceManager.lightning_gib = HbmShaderManager2.loadShader(new ResourceLocation(RefStrings.MODID, "shaders/lightning_gib")).withUniforms(HbmShaderManager2.LIGHTMAP, shader -> {
							GLCompat.activeTexture(GLCompat.GL_TEXTURE0+4);
							Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.noise_2);
							shader.uniform1i("noise", 4);
							GLCompat.activeTexture(GLCompat.GL_TEXTURE0);
						});
						ResourceManager.testlut = HbmShaderManager2.loadShader(new ResourceLocation(RefStrings.MODID, "shaders/testlut"));
						ResourceManager.flashlight_nogeo = HbmShaderManager2.loadShader(new ResourceLocation(RefStrings.MODID, "shaders/flashlight_nogeo"));
						ResourceManager.flashlight_deferred = HbmShaderManager2.loadShader(new ResourceLocation(RefStrings.MODID, "shaders/flashlight_deferred")).withUniforms(shader -> {
							shader.uniform2f("windowSize", Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight);
						});
						
						
						//The actual shaders used in flashlight rendering, not experimental
						ResourceManager.albedo = HbmShaderManager2.loadShader(new ResourceLocation(RefStrings.MODID, "shaders/lighting/albedo"));
						ResourceManager.flashlight_depth = HbmShaderManager2.loadShader(new ResourceLocation(RefStrings.MODID, "shaders/lighting/flashlight_depth"));
						ResourceManager.flashlight_post = HbmShaderManager2.loadShader(new ResourceLocation(RefStrings.MODID, "shaders/lighting/flashlight_post")).withUniforms(shader -> {
							shader.uniform2f("windowSize", Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight);
						});
						ResourceManager.pointlight_post = HbmShaderManager2.loadShader(new ResourceLocation(RefStrings.MODID, "shaders/lighting/pointlight_post")).withUniforms(shader -> {
							shader.uniform2f("windowSize", Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight);
						});
						ResourceManager.cone_volume = HbmShaderManager2.loadShader(new ResourceLocation(RefStrings.MODID, "shaders/lighting/cone_volume")).withUniforms(shader -> {
							shader.uniform2f("windowSize", Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight);
						});
						ResourceManager.flashlight_blit = HbmShaderManager2.loadShader(new ResourceLocation(RefStrings.MODID, "shaders/lighting/blit"));
						ResourceManager.volume_upscale = HbmShaderManager2.loadShader(new ResourceLocation(RefStrings.MODID, "shaders/lighting/volume_upscale")).withUniforms(shader -> {
							shader.uniform2f("windowSize", Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight);
						});
						
						ResourceManager.heat_distortion_post = HbmShaderManager2.loadShader(new ResourceLocation(RefStrings.MODID, "shaders/heat_distortion_post")).withUniforms(shader ->{
							shader.uniform2f("windowSize", Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight);
							GlStateManager.setActiveTexture(GLCompat.GL_TEXTURE0+4);
							Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.noise_2);
							shader.uniform1i("noise", 4);
							GlStateManager.setActiveTexture(GLCompat.GL_TEXTURE0);
							float time = (System.currentTimeMillis()%10000000)/1000F;
							shader.uniform1f("time", time);
						});
						
						ResourceManager.heat_distortion_new = HbmShaderManager2.loadShader(new ResourceLocation(RefStrings.MODID, "shaders/heat_distortion_new"));
						ResourceManager.crucible_lightning = HbmShaderManager2.loadShader(new ResourceLocation(RefStrings.MODID, "shaders/crucible_lightning"), shader ->{
							GLCompat.bindAttribLocation(shader, 0, "pos");
							GLCompat.bindAttribLocation(shader, 1, "tex");
							GLCompat.bindAttribLocation(shader, 2, "in_color");
						}).withUniforms(shader -> {
							GLCompat.activeTexture(GLCompat.GL_TEXTURE0+4);
							Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.noise_2);
							shader.uniform1i("noise", 4);
							GLCompat.activeTexture(GLCompat.GL_TEXTURE0);
						});
						ResourceManager.flash_lmap = HbmShaderManager2.loadShader(new ResourceLocation(RefStrings.MODID, "shaders/flash_lmap")).withUniforms(HbmShaderManager2.LIGHTMAP);
						ResourceManager.bimpact = HbmShaderManager2.loadShader(new ResourceLocation(RefStrings.MODID, "shaders/bimpact"), shader -> {
							GLCompat.bindAttribLocation(shader, 0, "pos");
							GLCompat.bindAttribLocation(shader, 1, "vColor");
							GLCompat.bindAttribLocation(shader, 3, "tex");
							GLCompat.bindAttribLocation(shader, 4, "lightTex");
							GLCompat.bindAttribLocation(shader, 5, "projTex");
						}).withUniforms(HbmShaderManager2.LIGHTMAP, HbmShaderManager2.WINDOW_SIZE);
						ResourceManager.blood_dissolve = HbmShaderManager2.loadShader(new ResourceLocation(RefStrings.MODID, "shaders/blood/blood")).withUniforms(HbmShaderManager2.LIGHTMAP);
						ResourceManager.gravitymap_render = HbmShaderManager2.loadShader(new ResourceLocation(RefStrings.MODID, "shaders/blood/gravitymap"));
						ResourceManager.blood_flow_update = HbmShaderManager2.loadShader(new ResourceLocation(RefStrings.MODID, "shaders/blood/blood_flow_update"));
						
						ResourceManager.gpu_particle_render = HbmShaderManager2.loadShader(new ResourceLocation(RefStrings.MODID, "shaders/gpu_particle_render")).withUniforms(HbmShaderManager2.MODELVIEW_MATRIX, HbmShaderManager2.PROJECTION_MATRIX, HbmShaderManager2.INV_PLAYER_ROT_MATRIX, shader -> {
							shader.uniform1i("lightmap", 1);
							shader.uniform1i("particleData0", 2);
							shader.uniform1i("particleData1", 3);
							shader.uniform1i("particleData2", 4);
							shader.uniform4f("particleTypeTexCoords[0]", ModEventHandlerClient.contrail.getMinU(), ModEventHandlerClient.contrail.getMinV(), ModEventHandlerClient.contrail.getMaxU() - ModEventHandlerClient.contrail.getMinU(), ModEventHandlerClient.contrail.getMaxV() - ModEventHandlerClient.contrail.getMinV());
						});

						ResourceManager.gpu_particle_udpate = HbmShaderManager2.loadShader(new ResourceLocation(RefStrings.MODID, "shaders/gpu_particle_update")).withUniforms(shader -> {
							shader.uniform1i("particleData0", 2);
							shader.uniform1i("particleData1", 3);
							shader.uniform1i("particleData2", 4);
						});
						sender.sendMessage(new TextComponentString("Reloaded resources!"));
					});
					
				}
				return;
			}
		}
	}

	protected List<String> getSubCommands() {
		return Lists.newArrayList("subcommands", "gen", "reloadCollada");
	}

	protected void doSubcommandCommand(MinecraftServer server, ICommandSender sender, String[] args) {
		if(args.length == 1) {
			//If no subcommand is specified, list available subcommands.
			StringBuilder builder = new StringBuilder();
			builder.append("Hbm command list [DEBUG]\n\n");
			for(String s : getSubCommands()) {
				builder.append(s).append("\n");
			}
			builder.delete(builder.length() - 1, builder.length());
			sender.sendMessage(new TextComponentTranslation(builder.toString()));
		} else if(args.length > 1){
			//If a subcommand is specified, try to give info about that command. If it doesn't exist, send an error message.
			if("gen".equals(args[1])){
				StringBuilder builder = new StringBuilder();
				builder.append("Info for command: gen\n\n");
				builder.append("Generates a structure at the block under your current position. Generation can be forced.\n\n");
				builder.append("Available structures:\n\n");
				builder.append("antenna      relay\ndud           silo\nfactory      barrel\nvertibird     vertibird_crashed\nsatellite      spaceship\nsellafield     radio\nbunker       desert_atom\nlibrary      geysir_water\ngeysir_vapor      geysir_chlorine");
				//builder.delete(builder.length() - 1, builder.length());
				sender.sendMessage(new TextComponentTranslation(builder.toString()));
			} else {
				sender.sendMessage(new TextComponentTranslation("Unknown command: " + args[1]));
			}
		}
	}

	protected void doGenCommand(MinecraftServer server, ICommandSender sender, String[] args) {
		if(args.length > 1) {
			boolean force = false;
			World world = sender.getEntityWorld();
			Random rand = world.rand;
			Vec3d senderPos = sender.getPositionVector();
			BlockPos genPos = new BlockPos(senderPos.x, world.getHeight((int) senderPos.x, (int) senderPos.z), senderPos.z);
			
			if(args.length > 2 && "f".equals(args[2]))
				force = true;

            switch (args[1]) {
                case "antenna" -> Antenna.INSTANCE.generate(world, rand, genPos, force);
                case "relay" -> Relay.INSTANCE.generate(world, rand, genPos, force);
                case "dud" -> new Dud().generate(world, rand, genPos, force);
                case "silo" -> Silo.INSTANCE.generate(world, rand, genPos, force);
                case "factory" -> Factory.INSTANCE.generate(world, rand, genPos, force);
                case "barrel" -> Barrel.INSTANCE.generate(world, rand, genPos, force);
                case "vertibird" -> Vertibird.INSTANCE.generate(world, rand, genPos, force);
                case "vertibird_crashed" -> CrashedVertibird.INSTANCE.generate(world, rand, genPos, force);
                case "satellite" -> Satellite.INSTANCE.generate(world, rand, genPos, force);
                case "spaceship" -> Spaceship.INSTANCE.generate(world, rand, genPos, force);
                case "sellafield" -> {
                    double r = rand.nextInt(15) + 10;
                    if (rand.nextInt(50) == 0)
                        r = 50;

					new Sellafield(r, r * 0.35D).generate(world, rand, genPos, true);
                }
                case "radio" -> Radio01.INSTANCE.generate(world, rand, genPos, force);
                case "bunker" -> Bunker.INSTANCE.generate(world, rand, genPos, force);
                case "desert_atom" -> DesertAtom001.INSTANCE.generate(world, rand, genPos, force);
                case "library" -> LibraryDungeon.INSTANCE.generate(world, rand, genPos, force);
                case "geysir_water" -> {
                    if (force) {
                        GeyserLarge.INSTANCE.generate(world, rand, genPos);
                    } else {
                        if (world.getBlockState(genPos.down()).getBlock() == Blocks.SAND)
                            GeyserLarge.INSTANCE.generate(world, rand, genPos);
                    }
                }
                case "geysir_vapor" -> {
                    if (force) {
                        world.setBlockState(genPos.down(), ModBlocks.geysir_vapor.getDefaultState());
                    } else {
                        if (world.getBlockState(genPos.down()).getBlock() == Blocks.STONE)
                            world.setBlockState(genPos.down(), ModBlocks.geysir_vapor.getDefaultState());
                    }
                }
                case "geysir_chlorine" -> {
                    if (force) {
                        Geyser.INSTANCE.generate(world, rand, genPos);
                    } else {
                        if (world.getBlockState(genPos.down()).getBlock() == Blocks.GRASS)
                            Geyser.INSTANCE.generate(world, rand, genPos);
                    }
                }
            }
		}
	}

}
