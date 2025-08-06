package com.hbm.world;

import com.hbm.blocks.ModBlocks;
import com.hbm.world.phased.AbstractPhasedStructure;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class OilBubble extends AbstractPhasedStructure {
	public final int radius;

	public OilBubble( int radius) {
		this.radius = radius;
	}

	@Override
	public boolean isCacheable() {
		return false;
	}

	@Override
	protected void buildStructure(@NotNull LegacyBuilder builder, @NotNull Random rand) {
	}

	@Override
	public List<@NotNull BlockPos> getValidationPoints(@NotNull BlockPos origin) {
		return Arrays.asList(
				origin.add(-radius, 0, -radius),
				origin.add(radius, 0, -radius),
				origin.add(-radius, 0, radius),
				origin.add(radius, 0, radius)
		);
	}

	@Override
	public void postGenerate(@NotNull World world, @NotNull Random rand, @NotNull BlockPos finalOrigin) {
		OilBubble.spawnOil(world, finalOrigin.getX(), finalOrigin.getY(), finalOrigin.getZ(), this.radius);
	}

	private static void spawnOil(World world, int x, int y, int z, int radius) {
		int r = radius;
		int r2 = r * r;
		int r22 = r2 / 2;

		MutableBlockPos pos = new BlockPos.MutableBlockPos();
		for(int xx = -r; xx < r; xx++) {
			int X = xx + x;
			int XX = xx * xx;
			for(int yy = -r; yy < r; yy++) {
				int Y = yy + y;
				int YY = XX + yy * yy * 3;
				for(int zz = -r; zz < r; zz++) {
					int Z = zz + z;
					int ZZ = YY + zz * zz;
					if(ZZ < r22) {
						pos.setPos(X, Y, Z);
						if(world.getBlockState(pos).getBlock() == Blocks.STONE)
							world.setBlockState(pos, ModBlocks.ore_oil.getDefaultState());
					}
				}
			}
		}
	}

	public static void spawnOil(World world, int x, int y, int z, int radius, Block block, int meta, Block target) {
		int r = radius;
		int r2 = r * r;
		int r22 = r2 / 2;

		for (int xx = -r; xx < r; xx++) {
			int X = xx + x;
			int XX = xx * xx;
			for (int yy = -r; yy < r; yy++) {
				int Y = yy + y;
				int YY = XX + yy * yy * 3;
				for (int zz = -r; zz < r; zz++) {
					int Z = zz + z;
					int ZZ = YY + zz * zz;
					if (ZZ < r22) {
						if(world.getBlockState(new BlockPos(X, Y, Z)).getBlock() == target)
							world.setBlockState(new BlockPos(X, Y, Z), block.getDefaultState(), 2);
					}
				}
			}
		}
	}

}
