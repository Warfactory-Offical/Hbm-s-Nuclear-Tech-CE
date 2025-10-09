package com.hbm.blocks.bomb;

import com.hbm.blocks.ModBlocks;
import com.hbm.entity.effect.EntityEMPBlast;
import com.hbm.explosion.ExplosionChaos;
import com.hbm.explosion.ExplosionNukeGeneric;
import com.hbm.interfaces.IBomb;
import com.hbm.lib.HBMSoundHandler;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BombFloat extends Block implements IBomb {

	public BombFloat(Material materialIn, String s) {
		super(materialIn);
		this.setTranslationKey(s);
		this.setRegistryName(s);
		
		ModBlocks.ALL_BLOCKS.add(this);
	}
	
	@Override
	public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        if (worldIn.isBlockPowered(pos))
        {
        	explode(worldIn, pos, null);
        }
	}

	@Override
	public BombReturnCode explode(World world, BlockPos pos, Entity detonator) {
		world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), HBMSoundHandler.sparkShoot, SoundCategory.BLOCKS, 5.0f, world.rand.nextFloat() * 0.2F + 0.9F);
		
		if(!world.isRemote) {
			world.setBlockState(pos, Blocks.AIR.getDefaultState());
    		if(this == ModBlocks.float_bomb) {
            	ExplosionChaos.floater(world, detonator, pos, 15, 50);
            	ExplosionChaos.move(world, pos, 15, 0, 50, 0);
    		}
    		if(this == ModBlocks.emp_bomb) {
    			ExplosionNukeGeneric.empBlast(world, detonator, pos.getX(), pos.getY(), pos.getZ(), 50);
    			EntityEMPBlast wave = new EntityEMPBlast(world, 50);
    			wave.posX = pos.getX() + 0.5;
    			wave.posY = pos.getY() + 0.5;
    			wave.posZ = pos.getZ() + 0.5;
    			world.spawnEntity(wave);
    		}
		}

		return BombReturnCode.DETONATED;
	}

}
