package com.hbm.world.feature;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.BlockDeadPlant;
import com.hbm.blocks.generic.BlockPlantEnumMeta;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;

import static com.hbm.blocks.PlantEnums.EnumDeadPlantType;
import static com.hbm.blocks.PlantEnums.EnumFlowerPlantType.MUSTARD_WILLOW_0;
import static com.hbm.blocks.PlantEnums.EnumFlowerPlantType.MUSTARD_WILLOW_1;
import static com.hbm.blocks.PlantEnums.EnumTallPlantType.*;
import static com.hbm.blocks.generic.BlockMeta.META;

public class OilSpot {

    public static void generateOilSpot(World world, int x, int z, int width, int count, boolean addWillows) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for(int i = 0; i < count; i++) {
            int rX = x + (int)(world.rand.nextGaussian() * width);
            int rZ = z + (int)(world.rand.nextGaussian() * width);
            int rY = world.getHeight(rX, rZ);

            for(int y = rY; y > rY - 4; y--) {

                pos.setPos(rX, y - 1, rZ);
                IBlockState belowState = world.getBlockState(pos);
                Block below = belowState.getBlock();

                pos.setPos(rX, y, rZ);
                IBlockState groundState = world.getBlockState(pos);
                Block ground = groundState.getBlock();

                if(groundState.getBlock() instanceof BlockPlantEnumMeta) {
                    int meta = groundState.getValue(META);
                    if (ground == ModBlocks.plant_flower && (meta == MUSTARD_WILLOW_0.ordinal() || meta == MUSTARD_WILLOW_1.ordinal()))
                        continue;
                    if (ground == ModBlocks.plant_tall && (meta == MUSTARD_WILLOW_2_LOWER.ordinal() || meta == MUSTARD_WILLOW_3_LOWER.ordinal() || meta == MUSTARD_WILLOW_4_LOWER.ordinal()))
                        continue;
                }

                else if(below.isNormalCube(belowState, world, pos.setPos(rX, y - 1, rZ)) && !(ground instanceof BlockDeadPlant)) {
                    pos.setPos(rX, y, rZ);
                    if(ground instanceof BlockTallGrass) {
                        if(world.rand.nextInt(10) == 0) {
                            pos.setPos(rX, y + 1, rZ);
                            Block topBlock = world.getBlockState(pos).getBlock();
                            pos.setPos(rX, y, rZ);
                            if (topBlock.getMetaFromState(topBlock.getBlockState().getBaseState()) == 2) {
                                world.setBlockState(pos, ModBlocks.plant_dead.getStateFromMeta(EnumDeadPlantType.FERN.ordinal()));
                            } else {
                                world.setBlockState(pos, ModBlocks.plant_dead.getStateFromMeta(EnumDeadPlantType.GRASS.ordinal()));
                            }
                        } else {
                            world.setBlockState(pos, Blocks.AIR.getDefaultState());
                        }
                    } else if(ground instanceof BlockFlower) {
                        world.setBlockState(pos, ModBlocks.plant_dead.getStateFromMeta(EnumDeadPlantType.FLOWER.ordinal()));
                    } else if(ground instanceof BlockDoublePlant) {
                        world.setBlockState(pos, ModBlocks.plant_dead.getStateFromMeta(EnumDeadPlantType.BIG_FLOWER.ordinal()));
                    } else if(ground instanceof BlockBush) {
                        world.setBlockState(pos, ModBlocks.plant_dead.getStateFromMeta(EnumDeadPlantType.GENERIC.ordinal()));
                    } else if(ground instanceof IPlantable) {
                        world.setBlockState(pos, ModBlocks.plant_dead.getStateFromMeta(EnumDeadPlantType.GENERIC.ordinal()));
                    }
                }

                if(below == Blocks.GRASS || below == Blocks.DIRT) {
                    pos.setPos(rX, y - 1, rZ);
                    world.setBlockState(pos, world.rand.nextInt(10) == 0 ? ModBlocks.dirt_oily.getDefaultState() : ModBlocks.dirt_dead.getDefaultState());

                    if(addWillows && world.rand.nextInt(50) == 0) {
                        pos.setPos(rX, y, rZ);
                        if(ModBlocks.plant_flower.canPlaceBlockAt(world, pos)) {
                            world.setBlockState(pos, ModBlocks.plant_flower.getDefaultState().withProperty(META, MUSTARD_WILLOW_0.ordinal()), 3);
                        }
                    }

                    break;

                } else if(below == Blocks.SAND || below == ModBlocks.ore_oil_sand) {
                    pos.setPos(rX, y - 1, rZ);
                    if(belowState.getBlock() == Blocks.SAND && belowState.getValue(BlockSand.VARIANT) == BlockSand.EnumType.RED_SAND)
                        world.setBlockState(pos, ModBlocks.sand_dirty_red.getDefaultState());
                    else
                        world.setBlockState(pos, ModBlocks.sand_dirty.getDefaultState());
                    break;

                } else if(below == Blocks.STONE) {
                    pos.setPos(rX, y - 1, rZ);
                    world.setBlockState(pos, ModBlocks.stone_cracked.getDefaultState());
                    break;

                } else if(belowState.getMaterial() == Material.LEAVES) {
                    pos.setPos(rX, y - 1, rZ);
                    world.setBlockToAir(pos);
                    break;
                }
            }
        }
    }
}