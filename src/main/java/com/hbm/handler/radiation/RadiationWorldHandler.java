package com.hbm.handler.radiation;

import com.hbm.blocks.ModBlocks;
import com.hbm.config.GeneralConfig;
import com.hbm.config.RadiationConfig;
import com.hbm.handler.radiation.RadiationSystemNT.RadPocket;
import com.hbm.main.MainRegistry;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import java.util.Set;

public class RadiationWorldHandler {

    public static void handleWorldDestruction(WorldServer world) {
        if (!RadiationConfig.worldRadEffects || !GeneralConfig.enableRads) return;
        handleAdvancedDestruction(world);
    }

    private static void handleAdvancedDestruction(WorldServer world) {
        if (GeneralConfig.enableDebugMode) {
            MainRegistry.logger.info("[Debug] Starting advanced world destruction processing");
        }

        Set<RadPocket> activePockets = RadiationSystemNT.getActiveSetView(world);
        if (activePockets.isEmpty()) {
            return;
        }

        RadPocket[] pockets = activePockets.toArray(new RadPocket[0]);
        RadPocket p = pockets[world.rand.nextInt(pockets.length)];

        float threshold = 5.0F;

        if (p.radiation.get() < threshold) {
            return;
        }

        BlockPos startPos = p.parent.subChunkPos;

        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < 16; j++) {
                for (int k = 0; k < 16; k++) {
                    if (world.rand.nextInt(3) != 0) continue;
                    BlockPos pos = startPos.add(i, j, k);
                    if (p.parent.getPocket(pos) != p) continue;
                    if (world.isAirBlock(pos)) continue;

                    IBlockState state = world.getBlockState(pos);
                    decayBlock(world, pos, state, false);
                }
            }
        }

        if (GeneralConfig.enableDebugMode) {
            MainRegistry.logger.info("[Debug] Finished advanced world destruction processing");
        }
    }

    private static void decayBlock(World world, BlockPos pos, IBlockState state, boolean isLegacy) {
        Block block = state.getBlock();
        if (block.getRegistryName() == null) return;

        if (block instanceof BlockDoublePlant) {
            BlockPos lowerPos;
            BlockPos upperPos;
            if (state.getValue(BlockDoublePlant.HALF) == BlockDoublePlant.EnumBlockHalf.LOWER) {
                lowerPos = pos;
                upperPos = pos.up();
            } else {
                lowerPos = pos.down();
                upperPos = pos;
            }
            world.setBlockState(upperPos, Blocks.AIR.getDefaultState(), 2);
            world.setBlockState(lowerPos, ModBlocks.waste_grass_tall.getDefaultState(), 2);
            return;
        }

        String registryName = block.getRegistryName().toString();

        if ("hbm:waste_leaves".equals(registryName)) {
            if (world.rand.nextInt(8) == 0) {
                world.setBlockToAir(pos);
            }
            return;
        }

        IBlockState newState = switch (registryName) {
            case "minecraft:grass" -> ModBlocks.waste_earth.getDefaultState();
            case "minecraft:dirt", "minecraft:farmland" -> ModBlocks.waste_dirt.getDefaultState();
            case "minecraft:sandstone" -> ModBlocks.waste_sandstone.getDefaultState();
            case "minecraft:red_sandstone" -> ModBlocks.waste_red_sandstone.getDefaultState();
            case "minecraft:hardened_clay", "minecraft:stained_hardened_clay" -> ModBlocks.waste_terracotta.getDefaultState();
            case "minecraft:gravel" -> ModBlocks.waste_gravel.getDefaultState();
            case "minecraft:mycelium" -> ModBlocks.waste_mycelium.getDefaultState();
            case "minecraft:snow_layer" -> ModBlocks.waste_snow.getDefaultState();
            case "minecraft:snow" -> ModBlocks.waste_snow_block.getDefaultState();
            case "minecraft:ice" -> ModBlocks.waste_ice.getDefaultState();
            case "minecraft:sand" -> {
                BlockSand.EnumType meta = state.getValue(BlockSand.VARIANT);
                if (isLegacy && world.rand.nextInt(60) == 0) {
                    yield meta == BlockSand.EnumType.SAND ? ModBlocks.waste_trinitite.getDefaultState() :
                            ModBlocks.waste_trinitite_red.getDefaultState();
                } else {
                    yield meta == BlockSand.EnumType.SAND ? ModBlocks.waste_sand.getDefaultState() : ModBlocks.waste_sand_red.getDefaultState();
                }
            }
            default -> {
                if (block instanceof BlockLeaves) {
                    yield ModBlocks.waste_leaves.getDefaultState();
                } else if (block instanceof BlockBush) {
                    yield ModBlocks.waste_grass_tall.getDefaultState();
                }
                yield null;
            }
        };

        if (newState != null) world.setBlockState(pos, newState, 2);
    }
}