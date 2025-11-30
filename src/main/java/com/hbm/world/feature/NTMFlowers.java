package com.hbm.world.feature;

import com.hbm.blocks.BlockEnumMeta;
import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.PlantEnums;
import com.hbm.blocks.generic.BlockFlowerPlant;
import com.hbm.world.phased.AbstractPhasedStructure;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Biomes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class NTMFlowers extends AbstractPhasedStructure {

    public static final NTMFlowers INSTANCE_FOXGLOVE = new NTMFlowers(BiomeDictionary.Type.FOREST, PlantEnums.EnumFlowerPlantType.FOXGLOVE);
    public static final NTMFlowers INSTANCE_HEMP = new NTMFlowers((Biome) null, PlantEnums.EnumFlowerPlantType.HEMP);
    public static final NTMFlowers INSTANCE_TOBACCO = new NTMFlowers(BiomeDictionary.Type.JUNGLE, PlantEnums.EnumFlowerPlantType.TOBACCO);
    public static final NTMFlowers INSTANCE_NIGHTSHADE = new NTMFlowers(Biomes.ROOFED_FOREST, PlantEnums.EnumFlowerPlantType.NIGHTSHADE);

    private static final int HORIZONTAL_RADIUS = 7;
    private static final LongArrayList CHUNK_OFFSETS = collectChunkOffsetsByRadius(HORIZONTAL_RADIUS);

    private Biome spawnBiome;
    private BiomeDictionary.Type biomeType;
    private final IBlockState plantType;

    public NTMFlowers(Biome biome, PlantEnums.EnumFlowerPlantType plantType) {
        this.spawnBiome = biome;
        this.plantType = BlockEnumMeta.stateFromEnum(ModBlocks.plant_flower, plantType);
    }

    public NTMFlowers(BiomeDictionary.Type biome, PlantEnums.EnumFlowerPlantType plantType) {
        this.biomeType = biome;
        this.plantType = BlockEnumMeta.stateFromEnum(ModBlocks.plant_flower, plantType);
    }

    @Override
    protected boolean useDynamicScheduler() {
        return true;
    }

    @Override
    public LongArrayList getWatchedChunkOffsets(@NotNull BlockPos origin) {
        return CHUNK_OFFSETS;
    }

    @Override
    protected boolean isCacheable() {
        return false; //It sploches flowers everywehre
    }

    @Override
    protected void buildStructure(@NotNull LegacyBuilder builder, @NotNull Random rand) {
    }

    @Override
    public void postGenerate(@NotNull World world, @NotNull Random rand, @NotNull BlockPos finalOrigin) {
        for (int i = 0; i < 64; ++i) {
            BlockPos blockpos = finalOrigin.add(rand.nextInt(8) - rand.nextInt(8), rand.nextInt(4) - rand.nextInt(4), rand.nextInt(8) - rand.nextInt(8));

            if (world.isAirBlock(blockpos) && blockpos.getY() < 255 && ((BlockFlowerPlant) plantType.getBlock()).canBlockStay(world, blockpos, plantType)) {
                world.setBlockState(blockpos, plantType, 18);
            }
        }
    }

    @Override
    public boolean checkSpawningConditions(@NotNull World world, @NotNull BlockPos origin) {
        return (spawnBiome == null && biomeType == null) || (biomeType != null && BiomeDictionary.hasType(world.getBiome(origin), biomeType)) || world.getBiome(origin) == spawnBiome;
    }
}
