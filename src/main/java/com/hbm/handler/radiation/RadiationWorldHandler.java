package com.hbm.handler.radiation;

import com.hbm.blocks.ModBlocks;
import com.hbm.config.GeneralConfig;
import com.hbm.config.RadiationConfig;
import com.hbm.main.MainRegistry;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

class RadiationWorldHandler {

    static void handleWorldDestruction(WorldServer world) {
        if (!RadiationConfig.worldRadEffects || !GeneralConfig.enableRads) return;
        handleAdvancedDestruction(world);
    }

    private static void handleAdvancedDestruction(WorldServer world) {
        if (GeneralConfig.enableDebugMode) {
            MainRegistry.logger.info("[Debug] Starting advanced world destruction processing");
        }
        RadiationSystemNT.handleWorldDestruction(world);

        if (GeneralConfig.enableDebugMode) {
            MainRegistry.logger.info("[Debug] Finished advanced world destruction processing");
        }
    }

    static void decayBlock(World world, BlockPos pos, IBlockState state, boolean isLegacy) {
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

        ResourceLocation registryName = block.getRegistryName();
        String namespace = registryName.getNamespace();
        String path = registryName.getPath();

        if ("hbm".equals(namespace) && "waste_leaves".equals(path)) {
            if (world.rand.nextInt(8) == 0) {
                world.setBlockToAir(pos);
            }
            return;
        }
        if (!"minecraft".equals(namespace)) return;
        IBlockState newState = switch (path) {
            case "grass" -> ModBlocks.waste_earth.getDefaultState();
            case "dirt", "farmland" -> ModBlocks.waste_dirt.getDefaultState();
            case "sandstone" -> ModBlocks.waste_sandstone.getDefaultState();
            case "red_sandstone" -> ModBlocks.waste_red_sandstone.getDefaultState();
            case "hardened_clay", "stained_hardened_clay" -> ModBlocks.waste_terracotta.getDefaultState();
            case "gravel" -> ModBlocks.waste_gravel.getDefaultState();
            case "mycelium" -> ModBlocks.waste_mycelium.getDefaultState();
            case "snow_layer" -> ModBlocks.waste_snow.getDefaultState();
            case "snow" -> ModBlocks.waste_snow_block.getDefaultState();
            case "ice" -> ModBlocks.waste_ice.getDefaultState();
            case "sand" -> {
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