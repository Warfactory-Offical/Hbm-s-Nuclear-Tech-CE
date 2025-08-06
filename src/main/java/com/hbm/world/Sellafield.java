package com.hbm.world;

import com.hbm.blocks.ModBlocks;
import com.hbm.world.phased.AbstractPhasedStructure;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Deprecated()
public class Sellafield extends AbstractPhasedStructure {
	private final double radius;
	private final double depth;

	public Sellafield(double radius, double depth) {
		this.radius = radius;
		this.depth = depth;
	}

	@Override
	protected boolean isCacheable(){
		return false;
	}

	@Override
	protected void buildStructure(@NotNull LegacyBuilder builder, @NotNull Random rand) {
	}

	@Override
	public List<@NotNull BlockPos> getValidationPoints(@NotNull BlockPos origin) {
		int iRad = (int) Math.ceil(this.radius);
		return Arrays.asList(
				origin.add(-iRad, 0, -iRad),
				origin.add(iRad, 0, -iRad),
				origin.add(-iRad, 0, iRad),
				origin.add(iRad, 0, iRad)
		);
	}

	@Override
	public void postGenerate(@NotNull World world, @NotNull Random rand, @NotNull BlockPos finalOrigin) {
		generate(world, finalOrigin.getX(), finalOrigin.getZ(), this.radius, this.depth);
	}

	private static double depthFunc(double x, double rad, double depth) {
		
		return -Math.pow(x, 2) / Math.pow(rad, 2) * depth + depth;
	}
	
	private static void generate(World world, int x, int z, double radius, double depth) {
		
		if(world.isRemote)
			return;
		
		Random rand = new Random();
		
		int iRad = (int)Math.round(radius);
		
		for(int a = -iRad - 5; a <= iRad + 5; a++) {
			
			for(int b = -iRad - 5; b <= iRad + 5; b++) {
				
				double r = Math.sqrt(Math.pow(a, 2) + Math.pow(b, 2));
				
				if(r - rand.nextInt(3) <= radius) {
					
					int dep = (int)depthFunc(r, radius, depth);
					dig(world, x + a, z + b, dep);

					if(r + rand.nextInt(3) <= radius / 6D) {
						place(world, x + a, z + b, 3, ModBlocks.sellafield_4);
					} else if(r - rand.nextInt(3) <= radius / 6D * 2D) {
						place(world, x + a, z + b, 3, ModBlocks.sellafield_3);
					} else if(r - rand.nextInt(3) <= radius / 6D * 3D) {
						place(world, x + a, z + b, 3, ModBlocks.sellafield_2);
					} else if(r - rand.nextInt(3) <= radius / 6D * 4D) {
						place(world, x + a, z + b, 3, ModBlocks.sellafield_1);
					} else if(r - rand.nextInt(3) <= radius / 6D * 5D) {
						place(world, x + a, z + b, 3, ModBlocks.sellafield_0);
					} else {
						place(world, x + a, z + b, 3, ModBlocks.sellafield_slaked);
					}
				}
			}
		}

		placeCore(world, x, z, radius * 0.3D);
	}
	
	private static void dig(World world, int x, int z, int depth) {
		
		int y = world.getHeight(x, z) - 1;
		
		if(y < depth * 2)
			return;
		
		for(int i = 0; i < depth; i++)
			world.setBlockState(new BlockPos(x, y - i, z), Blocks.AIR.getDefaultState(), 2 | 16);
	}
	
	private static void place(World world, int x, int z, int depth, Block block) {
		
		int y = world.getHeight(x, z) - 1;
		
		for(int i = 0; i < depth; i++)
			world.setBlockState(new BlockPos(x, y - i, z), block.getDefaultState(), 2 | 16);
	}
	
	private static void placeCore(World world, int x, int z, double rad) {
		
		int y = world.getHeight(x, z) - 1;
		
		world.setBlockState(new BlockPos(x, y, z), ModBlocks.sellafield_core.getDefaultState(), 2 | 16);
		//Drillgon200: This tile entity is never actually created anywhere
		/*try {
			
			TileEntitySellafield te = (TileEntitySellafield) world.getTileEntity(x, y, z);
			te.radius = rad;
			
		} catch(Exception ex) { }*/
	}
}