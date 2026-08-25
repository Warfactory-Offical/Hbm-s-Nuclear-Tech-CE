package com.hbm.util;

import com.hbm.main.MainRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Loader;

import java.lang.reflect.Method;

/**
 * Dynamic Trees keeps a tree as a connected network of branch blocks anchored to a rooty block, so carving part of
 * one out with a raw {@code setBlockToAir} leaves the remainder floating and unrooted. Dynamic Trees cannot detect
 * that from its side, so explosions have to tell it.
 *
 * <p>Calls go through reflection because Dynamic Trees is an optional runtime dependency.</p>
 */
public class CompatDynamicTrees {

	private static final boolean LOADED = Loader.isModLoaded(Compat.ModIds.DYNAMIC_TREES);

	private static Class<?> branchClass;
	private static Method destroyBranchFromNode;
	private static boolean resolved;
	private static boolean available;

	private static boolean resolve() {
		if(resolved) return available;
		resolved = true;

		if(!LOADED) return false;

		try {
			branchClass = Class.forName("com.ferreusveritas.dynamictrees.blocks.BlockBranch");
			destroyBranchFromNode = branchClass.getMethod("destroyBranchFromNode", World.class, BlockPos.class, EnumFacing.class, boolean.class);
			available = true;
		} catch(ReflectiveOperationException e) {
			MainRegistry.logger.warn("Dynamic Trees is loaded but its branch API could not be resolved, explosions will leave trees floating", e);
		}

		return available;
	}

	public static boolean isBranch(Block block) {
		return resolve() && branchClass.isInstance(block);
	}

	/**
	 * Removes the branch network reachable from pos. Unlike Dynamic Trees' own explosion handler this spawns no
	 * falling tree entity and drops no logs, matching how NTM explosions treat every other block.
	 *
	 * @return true if a branch was found and removed
	 */
	public static boolean destroyTreeAt(World world, BlockPos pos) {
		if(!resolve()) return false;

		IBlockState state = world.getBlockState(pos);
		if(!branchClass.isInstance(state.getBlock())) return false;

		try {
			destroyBranchFromNode.invoke(state.getBlock(), world, pos.toImmutable(), EnumFacing.DOWN, false);
			return true;
		} catch(ReflectiveOperationException | RuntimeException e) {
			MainRegistry.logger.error("Dynamic Trees branch removal failed at {}", pos, e);
			return false;
		}
	}

	/**
	 * Cleans up whatever the blast left behind after the block at pos was carved out. Only the surviving neighbours
	 * still hold branches, so this is what reaches the part of the tree outside the blast.
	 */
	public static void destroyOrphanedNeighbors(World world, BlockPos pos) {
		if(!resolve()) return;

		for(EnumFacing facing : EnumFacing.VALUES) {
			BlockPos neighbor = pos.offset(facing);
			if(!world.isBlockLoaded(neighbor)) continue;
			destroyTreeAt(world, neighbor);
		}
	}
}
