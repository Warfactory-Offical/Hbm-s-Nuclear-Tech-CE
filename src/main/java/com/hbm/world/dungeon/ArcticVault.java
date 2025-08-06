package com.hbm.world.dungeon;

import com.hbm.blocks.ModBlocks;
import com.hbm.config.GeneralConfig;
import com.hbm.main.MainRegistry;
import com.hbm.world.generator.DungeonToolbox;
import com.hbm.world.phased.AbstractPhasedStructure;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.BlockStoneBrick;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class ArcticVault extends AbstractPhasedStructure {
	public static final ArcticVault INSTANCE = new ArcticVault();
	private ArcticVault() {}

	@Override
	protected void buildStructure(@NotNull LegacyBuilder builder, @NotNull Random rand) {
		build(builder, 0, 0, 0);
	}

	@Override
	public boolean checkSpawningConditions(@NotNull World world, @NotNull BlockPos pos) {
		return world.getBiome(pos.down()).getTemperature(pos) < 0.2 && world.getBlockState(pos.down()).getMaterial() == Material.ROCK;
	}

	@Override
	public List<@NotNull BlockPos> getValidationPoints(@NotNull BlockPos origin){
		return Collections.singletonList(origin);
	}

	@Override
	public void postGenerate(@NotNull World world, @NotNull Random rand, @NotNull BlockPos finalOrigin) {
		int x = finalOrigin.getX(), y = finalOrigin.getY(), z = finalOrigin.getZ();
		List<IBlockState> crates = Arrays.asList(ModBlocks.crate.getDefaultState(), ModBlocks.crate_metal.getDefaultState(), ModBlocks.crate_ammo.getDefaultState(), ModBlocks.crate_can.getDefaultState(), ModBlocks.crate_jungle.getDefaultState());

		for(int i = 0; i < 15; i++) {
			int ix = x - 4 + rand.nextInt(10);
			int iz = z - 4 + rand.nextInt(10);

			if(world.getBlockState(new BlockPos(ix, y + 1, iz)).getBlock() == Blocks.SNOW_LAYER) {
				IBlockState b = DungeonToolbox.getRandom(crates, rand);
				world.setBlockState(new BlockPos(ix, y + 1, iz), b, 2);
				world.setBlockState(new BlockPos(ix, y + 2, iz), Blocks.SNOW_LAYER.getDefaultState());
			}
		}

		int iy = world.getHeight(x, z);

		if(world.getBlockState(new BlockPos(x, iy - 1, z)).isSideSolid(world, new BlockPos(x, iy - 1, z), EnumFacing.UP)) {
			world.setBlockState(new BlockPos(x, iy, z), ModBlocks.tape_recorder.getDefaultState());
		}

		if(GeneralConfig.enableDebugMode)
			MainRegistry.logger.info("[Debug] Successfully spawned arctic code vault at " + x + " " + y + " " + z);
	}

	private void build(AbstractPhasedStructure.LegacyBuilder world, int x, int y, int z) {

		List<IBlockState> brick = Arrays.asList(Blocks.STONEBRICK.getDefaultState(), Blocks.STONEBRICK.getDefaultState().withProperty(BlockStoneBrick.VARIANT, BlockStoneBrick.EnumType.CRACKED));
		List<IBlockState> web = Arrays.asList(Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), Blocks.WEB.getDefaultState());

		DungeonToolbox.generateBox(world, x - 5, y, z - 5, 11, 1, 11, brick);
		DungeonToolbox.generateBox(world, x - 5, y + 6, z - 5, 11, 1, 11, brick);
		DungeonToolbox.generateBox(world, x - 5, y + 1, z - 5, 11, 5, 1, brick);
		DungeonToolbox.generateBox(world, x - 5, y + 1, z + 5, 11, 5, 1, brick);
		DungeonToolbox.generateBox(world, x - 5, y + 1, z - 5, 1, 5, 11, brick);
		DungeonToolbox.generateBox(world, x + 5, y + 1, z - 5, 1, 5, 11, brick);
		DungeonToolbox.generateBox(world, x - 4, y + 1, z - 4, 9, 3, 9, Blocks.AIR.getDefaultState());
		DungeonToolbox.generateBox(world, x - 4, y + 1, z - 4, 9, 1, 9, Blocks.SNOW_LAYER.getDefaultState());
		DungeonToolbox.generateBox(world, x - 2, y + 1, z - 2, 5, 2, 1, ModBlocks.tape_recorder.getDefaultState().withProperty(BlockHorizontal.FACING, EnumFacing.SOUTH));
		DungeonToolbox.generateBox(world, x - 2, y + 3, z - 2, 5, 1, 1, Blocks.SNOW_LAYER.getDefaultState());
		DungeonToolbox.generateBox(world, x - 2, y + 1, z + 2, 5, 2, 1, ModBlocks.tape_recorder.getDefaultState().withProperty(BlockHorizontal.FACING, EnumFacing.NORTH));
		DungeonToolbox.generateBox(world, x - 2, y + 3, z + 2, 5, 1, 1, Blocks.SNOW_LAYER.getDefaultState());
		DungeonToolbox.generateBox(world, x - 4, y + 4, z - 4, 9, 2, 9, web);
	}
}
