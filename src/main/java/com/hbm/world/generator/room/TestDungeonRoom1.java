package com.hbm.world.generator.room;

import com.hbm.blocks.ModBlocks;
import com.hbm.world.generator.CellularDungeon;
import com.hbm.world.generator.CellularDungeonRoom;
import com.hbm.world.generator.DungeonToolbox;
import com.hbm.world.phased.AbstractPhasedStructure;
import net.minecraft.block.BlockRotatedPillar;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TestDungeonRoom1 extends CellularDungeonRoom {

	public TestDungeonRoom1(CellularDungeon parent) {
		super(parent);
	}

	public void generateMain(AbstractPhasedStructure.LegacyBuilder world, int x, int y, int z) {
		
		super.generateMain(world, x, y, z);
		DungeonToolbox.generateBox(world, x + parent.width / 2 - 3, y + 1, z + parent.width / 2 - 3, 1, parent.height - 2, 1, ModBlocks.meteor_pillar.getDefaultState().withProperty(BlockRotatedPillar.AXIS, EnumFacing.Axis.Y));
		DungeonToolbox.generateBox(world, x + parent.width / 2 + 3, y + 1, z + parent.width / 2 - 3, 1, parent.height - 2, 1, ModBlocks.meteor_pillar.getDefaultState().withProperty(BlockRotatedPillar.AXIS, EnumFacing.Axis.Y));
		DungeonToolbox.generateBox(world, x + parent.width / 2 + 3, y + 1, z + parent.width / 2 + 3, 1, parent.height - 2, 1, ModBlocks.meteor_pillar.getDefaultState().withProperty(BlockRotatedPillar.AXIS, EnumFacing.Axis.Y));
		DungeonToolbox.generateBox(world, x + parent.width / 2 - 3, y + 1, z + parent.width / 2 + 3, 1, parent.height - 2, 1, ModBlocks.meteor_pillar.getDefaultState().withProperty(BlockRotatedPillar.AXIS, EnumFacing.Axis.Y));
		world.setBlockState(new BlockPos(x + parent.width / 2 - 3, y + 3, z + parent.width / 2 - 3), ModBlocks.meteor_brick_chiseled.getDefaultState(), 2);
		world.setBlockState(new BlockPos(x + parent.width / 2 + 3, y + 3, z + parent.width / 2 - 3), ModBlocks.meteor_brick_chiseled.getDefaultState(), 2);
		world.setBlockState(new BlockPos(x + parent.width / 2 + 3, y + 3, z + parent.width / 2 + 3), ModBlocks.meteor_brick_chiseled.getDefaultState(), 2);
		world.setBlockState(new BlockPos(x + parent.width / 2 - 3, y + 3, z + parent.width / 2 + 3), ModBlocks.meteor_brick_chiseled.getDefaultState(), 2);
		world.setBlockState(new BlockPos(x + parent.width / 2, y + 1, z + parent.width / 2), ModBlocks.meteor_pillar.getDefaultState().withProperty(BlockRotatedPillar.AXIS, EnumFacing.Axis.Y), 2);
		world.setBlockState(new BlockPos(x + parent.width / 2, y + 2, z + parent.width / 2), Blocks.GLOWSTONE.getDefaultState(), 3);
		
		/*world.setBlock(x + parent.width / 2, y, z + parent.width / 2, Blocks.mob_spawner, 0, 2);
        TileEntityMobSpawner tileentitymobspawner2 = (TileEntityMobSpawner)world.getTileEntity(x + parent.width / 2, y, z + parent.width / 2);

        if (tileentitymobspawner2 != null)
        {
            tileentitymobspawner2.func_145881_a().setEntityName("entity_cyber_crab");
        }*/
	}
}