package com.hbm.world;

import com.hbm.blocks.ModBlocks;
import com.hbm.world.phased.AbstractPhasedStructure;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Random;

public class OilSandBubble extends AbstractPhasedStructure {
	private final int radius;
	private final List<ChunkPos> chunkOffsets;

	public OilSandBubble(int radius) {
		this.radius = radius;
		this.chunkOffsets = collectChunkOffsetsByRadius(radius);
	}

	@Override
	public boolean isCacheable() {
		return false;
	}

	@Override
	protected void buildStructure(@NotNull LegacyBuilder builder, @NotNull Random rand) {
	}

	@Override
	public List<ChunkPos> getAdditionalChunks(@NotNull BlockPos origin) {
		return translateOffsets(origin, chunkOffsets);
	}

	@Override
	public void postGenerate(@NotNull World world, @NotNull Random rand, @NotNull BlockPos finalOrigin) {
		OilSandBubble.spawnOil(world, rand, finalOrigin.getX(), finalOrigin.getY(), finalOrigin.getZ(), this.radius);
	}

	private static void spawnOil(World world, Random rand, int x, int y, int z, int radius) {
		int r = radius;
		int r2 = r * r;
		int r22 = r2 / 2;

		MutableBlockPos pos = new BlockPos.MutableBlockPos();
		for (int xx = -r; xx < r; xx++) {
			int X = xx + x;
			int XX = xx * xx;
			for (int yy = -r; yy < r; yy++) {
				int Y = yy + y;
				int YY = XX + yy * yy * 3;
				for (int zz = -r; zz < r; zz++) {
					int Z = zz + z;
					int ZZ = YY + zz * zz;
					if (ZZ < r22 + rand.nextInt(r22 / 3)) {
						pos.setPos(X, Y, Z);
						if(world.getBlockState(pos).getBlock() == Blocks.SAND)
							world.setBlockState(pos, ModBlocks.ore_oil_sand.getDefaultState());
					}
				}
			}
		}
	}
	
}
