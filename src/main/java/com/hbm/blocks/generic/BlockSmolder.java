package com.hbm.blocks.generic;

import com.hbm.blocks.ModBlocks;
import com.hbm.items.ModItems.Materials.Powders;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Random;

public class BlockSmolder extends Block {

	public BlockSmolder(Material materialIn, String s) {
		super(materialIn);
		this.setTranslationKey(s);
		this.setRegistryName(s);

		ModBlocks.ALL_BLOCKS.add(this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random rand) {
		super.randomDisplayTick(state, world, pos, rand);
		if(world.getBlockState(pos.up()).getMaterial() == Material.AIR) {

    		world.spawnParticle(EnumParticleTypes.LAVA, pos.getX() + 0.25 + rand.nextDouble() * 0.5, pos.getY() + 1.1, pos.getZ() + 0.25 + rand.nextDouble() * 0.5, 0.0, 0.0, 0.0);
    		world.spawnParticle(EnumParticleTypes.FLAME, pos.getX() + 0.25 + rand.nextDouble() * 0.5, pos.getY() + 1.1, pos.getZ() + 0.25 + rand.nextDouble() * 0.5, 0.0, 0.0, 0.0);
    	}
	}

	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return Powders.powder_fire;
	}

	@Override
	public void onEntityWalk(World worldIn, BlockPos pos, Entity entityIn) {
		entityIn.setFire(3);
	}

}
