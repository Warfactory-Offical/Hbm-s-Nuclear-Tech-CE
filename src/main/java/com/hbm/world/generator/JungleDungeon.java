package com.hbm.world.generator;

import com.hbm.blocks.ModBlocks;
import com.hbm.world.phased.AbstractPhasedStructure;
import net.minecraft.util.math.BlockPos;

import java.util.Random;

public class JungleDungeon extends CellularDungeon {

	public boolean hasHole = false;
	
	public JungleDungeon(int width, int height, int dimX, int dimZ, int tries, int branches) {
		super(width, height, dimX, dimZ, tries, branches);

		this.floor.add(ModBlocks.brick_jungle.getDefaultState());
		this.floor.add(ModBlocks.brick_jungle_cracked.getDefaultState());
		this.wall.add(ModBlocks.brick_jungle.getDefaultState());
		this.wall.add(ModBlocks.brick_jungle_cracked.getDefaultState());
		this.ceiling.add(ModBlocks.brick_jungle.getDefaultState());
		this.ceiling.add(ModBlocks.brick_jungle_cracked.getDefaultState());
	}
	
	@Override
	public void generate(final AbstractPhasedStructure.LegacyBuilder world, final int x, final int y, final int z, final Random rand) {
		super.generate(world, x, y, z, rand);
		JungleDungeon that = JungleDungeon.this;

		//A hole has not been made -> this is the bottom floor
		if(!that.hasHole) {
			world.setBlockState(new BlockPos(x, y, z), ModBlocks.brick_jungle_circle.getDefaultState());
		}

		that.hasHole = false;

		//since all the building is timed jobs, this has to be as well. timed jobs are ordered so this works!
		//is it shitty coding? is it not? who knows?
		// mlbv: It is. Removed.
	}

}