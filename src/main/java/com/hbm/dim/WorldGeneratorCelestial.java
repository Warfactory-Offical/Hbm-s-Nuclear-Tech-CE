package com.hbm.dim;

import com.google.common.base.Predicate;
import com.hbm.blocks.ModBlocks;
import com.hbm.config.GeneralConfig;
import com.hbm.config.WorldConfig;
import com.hbm.dim.laythe.biome.BiomeGenBaseLaythe;
import com.hbm.main.MainRegistry;
import com.hbm.world.feature.DepthDeposit;
import com.hbm.world.generator.CellularDungeonFactory;
import com.hbm.world.generator.DungeonToolbox;
import com.hbm.world.generator.MeteorDungeonStructure;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.pattern.BlockMatcher;
import net.minecraft.init.Biomes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.feature.WorldGenMinable;
import net.minecraftforge.event.terraingen.OreGenEvent.GenerateMinable;
import net.minecraftforge.fml.common.IWorldGenerator;
import net.minecraftforge.fml.common.eventhandler.Event;

import java.util.Random;

public class WorldGeneratorCelestial implements IWorldGenerator {

    @Override
    public void generate(Random rand, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
        if(!(world.provider instanceof WorldProviderCelestial))
            return;

        if(world.provider.getDimension() == 0)
            return;

        WorldProviderCelestial celestialProvider = (WorldProviderCelestial)world.provider;
        Block blockToReplace = celestialProvider.getStone();
        int meta = CelestialBody.getMeta(world);

        generateStructures(world, rand, chunkX * 16, chunkZ * 16);

        // Generate vanilla ores too
        // Ho-ho-ho! No.
        //if(blockToReplace != Blocks.STONE) {
            //generateVanillaOres(world, rand, chunkX * 16, chunkZ * 16, blockToReplace, meta);
        //}

        generateNTMOres(world, rand, chunkX * 16, chunkZ * 16, blockToReplace, meta);
        //generateBedrockOres(world, rand, chunkX * 16, chunkZ * 16, blockToReplace);
    }

    public void generateStructures(World world, Random rand, int x, int z) {
		Biome biome = world.getBiomeProvider().getBiome(new BlockPos(x, 150, z));

		if(WorldConfig.meteorStructure > 0 && rand.nextInt(WorldConfig.meteorStructure) == 0 && biome != Biomes.OCEAN && biome != Biomes.DEEP_OCEAN && biome != BiomeGenBaseLaythe.laytheOcean) {
			int px = x + rand.nextInt(16) + 8;
			int pz = z + rand.nextInt(16) + 8;
			
			MeteorDungeonStructure.INSTANCE.generate(world, rand, new BlockPos(px, 10, pz));
			
			if(GeneralConfig.enableDebugMode)
				MainRegistry.logger.info("[Debug] Successfully spawned meteor dungeon at " + px + " 10 " + pz);
			
			int y = world.getHeight(px, pz);
			
			for(int f = 0; f < 3; f++)
				world.setBlockState(new BlockPos(px, y + f, pz), ModBlocks.meteor_pillar.getDefaultState());
			world.setBlockState(new BlockPos(px, y + 3, pz), ModBlocks.meteor_brick_chiseled.getDefaultState());
		}
    }

    public void generateNTMOres(World world, Random rand, int x, int z, Block planetStone, int meta) {

        DepthDeposit.generateCondition(world, x, 0, 3, z, 5, 0.6D, ModBlocks.cluster_depth_iron, rand, 24, planetStone, ModBlocks.stone_depth);
        DepthDeposit.generateCondition(world, x, 0, 3, z, 5, 0.6D, ModBlocks.cluster_depth_titanium, rand, 32, planetStone, ModBlocks.stone_depth);
        DepthDeposit.generateCondition(world, x, 0, 3, z, 5, 0.6D, ModBlocks.cluster_depth_tungsten, rand, 32, planetStone, ModBlocks.stone_depth);
        DepthDeposit.generateCondition(world, x, 0, 3, z, 5, 0.8D, ModBlocks.ore_depth_cinnabar, rand, 16, planetStone, ModBlocks.stone_depth);
        DepthDeposit.generateCondition(world, x, 0, 3, z, 5, 0.8D, ModBlocks.ore_depth_zirconium, rand, 16, planetStone, ModBlocks.stone_depth);
        DepthDeposit.generateCondition(world, x, 0, 3, z, 5, 0.8D, ModBlocks.ore_depth_borax, rand, 16, planetStone, ModBlocks.stone_depth);

        DungeonToolbox.generateOre(world, rand, x, z, WorldConfig.uraniumSpawn, 5, 5, 20, ModBlocks.ore_uranium, planetStone);
        DungeonToolbox.generateOre(world, rand, x, z, WorldConfig.thoriumSpawn, 5, 5, 25, ModBlocks.ore_thorium, planetStone);
        DungeonToolbox.generateOre(world, rand, x, z, WorldConfig.titaniumSpawn, 6, 5, 30, ModBlocks.ore_titanium, planetStone);
        DungeonToolbox.generateOre(world, rand, x, z, WorldConfig.sulfurSpawn, 8, 5, 30, ModBlocks.ore_sulfur, planetStone);
        DungeonToolbox.generateOre(world, rand, x, z, WorldConfig.aluminiumSpawn, 6, 5, 40, ModBlocks.ore_aluminium, planetStone);
        DungeonToolbox.generateOre(world, rand, x, z, WorldConfig.copperSpawn, 6, 5, 45, ModBlocks.ore_copper, planetStone);
        //DungeonToolbox.generateOre(world, rand, x, z, WorldConfig.mineralSpawn, 10, 12, 32, ModBlocks.ore_mineral, meta, planetStone);
        DungeonToolbox.generateOre(world, rand, x, z, WorldConfig.fluoriteSpawn, 4, 5, 45, ModBlocks.ore_fluorite, planetStone);
        DungeonToolbox.generateOre(world, rand, x, z, WorldConfig.niterSpawn, 6, 5, 30, ModBlocks.ore_niter, planetStone);
        DungeonToolbox.generateOre(world, rand, x, z, WorldConfig.tungstenSpawn, 8, 5, 30, ModBlocks.ore_tungsten, planetStone);
        DungeonToolbox.generateOre(world, rand, x, z, WorldConfig.leadSpawn, 9, 5, 30, ModBlocks.ore_lead, planetStone);
        DungeonToolbox.generateOre(world, rand, x, z, WorldConfig.berylliumSpawn, 4, 5, 30, ModBlocks.ore_beryllium, planetStone);
        DungeonToolbox.generateOre(world, rand, x, z, WorldConfig.rareSpawn, 5, 5, 20, ModBlocks.ore_rare, planetStone);
        // DungeonToolbox.generateOre(world, rand, x, z, WorldConfig.asbestosSpawn, 4, 16, 16, ModBlocks.ore_asbestos, meta, planetStone);
        DungeonToolbox.generateOre(world, rand, x, z, WorldConfig.cinnabarSpawn, 4, 8, 16, ModBlocks.ore_cinnabar, planetStone);
        DungeonToolbox.generateOre(world, rand, x, z, WorldConfig.cobaltSpawn, 4, 4, 8, ModBlocks.ore_cobalt, planetStone);

        DungeonToolbox.generateOre(world, rand, x, z, WorldConfig.ironClusterSpawn, 6, 15, 45, ModBlocks.cluster_iron, planetStone);
        DungeonToolbox.generateOre(world, rand, x, z, WorldConfig.titaniumClusterSpawn, 6, 15, 30, ModBlocks.cluster_titanium, planetStone);
        DungeonToolbox.generateOre(world, rand, x, z, WorldConfig.aluminiumClusterSpawn, 6, 15, 35, ModBlocks.cluster_aluminium, planetStone);
        //DungeonToolbox.generateOre(world, rand, x, z, WorldConfig.malachiteSpawn, 16, 6, 40, ModBlocks.stone_resource, EnumStoneType.MALACHITE.ordinal(), planetStone);
    }

    // We don't have the new bedrock ore generation. For now.
    // KAPITULIEREN WERDEN WIR NICHT
    //public void generateBedrockOres(World world, Random rand, int x, int z, Block planetStone) {
        //if(rand.nextInt(3) == 0) {
            //List<WeightedRandomGeneric<BedrockOreDefinition>> list = BedrockOre.weightedOres;
            //SolarSystem.Body bodyEnum = CelestialBody.getEnum(world);

            // If we haven't got any defined body bedrock ores, default to earth bedrock ores
            //if(BedrockOre.weightedPlanetOres.containsKey(bodyEnum))
                //list = BedrockOre.weightedPlanetOres.get(bodyEnum);

            @SuppressWarnings("unchecked")
            //WeightedRandomGeneric<BedrockOreDefinition> item = (WeightedRandomGeneric<BedrockOreDefinition>) WeightedRandom.getRandomItem(rand, list);
            //BedrockOreDefinition def = item.get();
            
            //int randPosX = x + rand.nextInt(2) + 8;
            //int randPosZ = z + rand.nextInt(2) + 8;
            //BedrockOre.generate(world, randPosX, randPosZ, def.stack, def.acid, def.color, def.tier, ModBlocks.stone_depth, planetStone);
        //}
    //}

    // This will generate vanilla ores for the chunk when the biome decorator fails to find any regular stone
    //public void generateVanillaOres(World world, Random rand, int x, int z, Block planetStone, int meta) {
        //genVanillaOre(world, rand, x, z, 0, 64, 20, 8, ModBlocks.ore_iron, planetStone, meta);
        //genVanillaOre(world, rand, x, z, 0, 32, 2, 8, ModBlocks.ore_gold, planetStone, meta);
        //genVanillaOre(world, rand, x, z, 0, 16, 8, 7, ModBlocks.ore_redstone, planetStone, meta);
        //genVanillaOre(world, rand, x, z, 0, 16, 1, 7, ModBlocks.ore_diamond, planetStone, meta);
        // what the fuck is a lapis lazuli
        // emeralds also spawn in a special way but... like... fuck emeralds
            // haha no, fuck all ores. for now.
    //}

    // Called by ModEventHandler to handle vanilla ore generation events
    public static void onGenerateOre(GenerateMinable event) {
        // No coal on celestial bodies, no dead dinodoys :(
        if(event.getType() == GenerateMinable.EventType.COAL) {
            event.setResult(Event.Result.DENY);
        }
    }

    // A simple reimplementation of `genStandardOre1` without needing to instance a BiomeDecorator
    private void genVanillaOre(World world, Random rand, int x, int z, int yMin, int yMax, int count, int numberOfBlocks, Block ore, Block target, int meta) {
        IBlockState oreState = ore.getDefaultState();
        Predicate<IBlockState> targetPredicate = BlockMatcher.forBlock(target);

        WorldGenMinable worldGenMinable = new WorldGenMinable(oreState, numberOfBlocks, targetPredicate);

        for (int l = 0; l < count; ++l) {
            int genX = x + rand.nextInt(16);
            int genY = rand.nextInt(yMax - yMin) + yMin; // millenial supremacy
            int genZ = z + rand.nextInt(16);
            worldGenMinable.generate(world, rand, new BlockPos(genX, genY, genZ));
        }
    }

}
