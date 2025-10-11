package com.hbm.world.feature;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.BlockGlyphid;
import com.hbm.blocks.generic.BlockGlyphid.Type;
import com.hbm.blocks.generic.BlockGlyphidSpawner;
import com.hbm.util.LootGenerator;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Random;

public class GlyphidHive {
    public static final int[][][] schematicSmall = new int[][][] {
            {
                    {0,0,0,0,0,0,0,0,0,0,0},
                    {0,0,0,0,0,0,0,0,0,0,0},
                    {0,0,0,0,0,0,0,0,0,0,0},
                    {0,0,0,0,0,0,0,0,0,0,0},
                    {0,0,0,0,1,1,1,0,0,0,0},
                    {0,0,0,0,1,1,1,0,0,0,0},
                    {0,0,0,0,1,1,1,0,0,0,0},
                    {0,0,0,0,0,0,0,0,0,0,0},
                    {0,0,0,0,0,0,0,0,0,0,0},
                    {0,0,0,0,0,0,0,0,0,0,0},
                    {0,0,0,0,0,0,0,0,0,0,0},
            },
            {
                    {0,0,0,0,0,0,0,0,0,0,0},
                    {0,0,0,0,0,0,0,0,0,0,0},
                    {0,0,0,0,1,1,1,0,0,0,0},
                    {0,0,0,1,1,1,1,1,0,0,0},
                    {0,0,1,1,1,1,1,1,1,0,0},
                    {0,0,1,1,1,1,1,1,1,0,0},
                    {0,0,1,1,1,1,1,1,1,0,0},
                    {0,0,0,1,1,1,1,1,0,0,0},
                    {0,0,0,0,1,1,1,0,0,0,0},
                    {0,0,0,0,0,0,0,0,0,0,0},
                    {0,0,0,0,0,0,0,0,0,0,0},
            },
            {
                    {0,0,0,0,0,0,0,0,0,0,0},
                    {0,0,0,0,1,1,1,0,0,0,0},
                    {0,0,1,1,1,1,1,1,1,0,0},
                    {0,0,1,1,1,1,1,1,1,0,0},
                    {0,1,1,1,3,3,3,1,1,1,0},
                    {0,1,1,1,3,3,3,1,1,1,0},
                    {0,1,1,1,3,3,3,1,1,1,0},
                    {0,0,1,1,1,1,1,1,1,0,0},
                    {0,0,1,1,1,1,1,1,1,0,0},
                    {0,0,0,0,1,1,1,0,0,0,0},
                    {0,0,0,0,0,0,0,0,0,0,0},
            },
            {
                    {0,0,0,0,0,0,0,0,0,0,0},
                    {0,0,0,0,1,1,1,0,0,0,0},
                    {0,0,1,1,1,1,1,1,1,0,0},
                    {0,0,1,1,2,2,2,1,1,0,0},
                    {0,1,1,2,2,2,2,2,1,1,0},
                    {0,1,1,2,2,2,2,2,1,1,0},
                    {0,1,1,2,2,2,2,2,1,1,0},
                    {0,0,1,1,2,2,2,1,1,0,0},
                    {0,0,1,1,1,1,1,1,1,0,0},
                    {0,0,0,0,1,1,1,0,0,0,0},
                    {0,0,0,0,0,0,0,0,0,0,0},
            },
            {
                    {0,0,0,0,0,0,0,0,0,0,0},
                    {0,0,0,0,1,1,1,0,0,0,0},
                    {0,0,1,1,1,1,1,1,1,0,0},
                    {0,0,1,1,1,1,1,1,1,0,0},
                    {0,1,1,1,1,1,1,1,1,1,0},
                    {0,1,1,1,1,1,1,1,1,1,0},
                    {0,1,1,1,1,1,1,1,1,1,0},
                    {0,0,1,1,1,1,1,1,1,0,0},
                    {0,0,1,1,1,1,1,1,1,0,0},
                    {0,0,0,0,1,1,1,0,0,0,0},
                    {0,0,0,0,0,0,0,0,0,0,0},
            }
    };

    public static void generateSmall(World world, int x, int y, int z, Random rand, boolean infected, boolean loot) {

        for(int i = 0; i < 11; i++) {
            for(int j = 0; j < 5; j++) {
                for(int k = 0; k < 11; k++) {

                    int block = schematicSmall[4 - j][i][k];
                    int iX = x + i - 5;
                    int iY = y + j - 2;
                    int iZ = z + k - 5;
                    BlockPos pos = new BlockPos(iX, iY, iZ);

                    switch(block) {
                        case 1: world.setBlockState(pos, ModBlocks.glyphid_base.getDefaultState().withProperty(BlockGlyphid.TYPE, infected ? Type.INFESTED : Type.BASE), 2); break;
                        case 2: world.setBlockState(pos, rand.nextInt(3) == 0
                                ? ModBlocks.glyphid_spawner.getDefaultState().withProperty(BlockGlyphidSpawner.TYPE, infected ? BlockGlyphidSpawner.Type.INFESTED : BlockGlyphidSpawner.Type.BASE)
                                : ModBlocks.glyphid_base.getDefaultState().withProperty(BlockGlyphid.TYPE, infected ? Type.INFESTED : Type.BASE), 2); break;
                        case 3:
                            int r = rand.nextInt(3);
                            if(r == 0) {
                                world.setBlockState(pos, Blocks.SKULL.getStateFromMeta(1), 3);
                                TileEntitySkull skull = (TileEntitySkull) world.getTileEntity(pos);
                                if(skull != null) skull.setSkullRotation(rand.nextInt(16));
                            } else if(r == 1) {
                                world.setBlockState(new BlockPos(iX, iY, z + k - 5), ModBlocks.deco_loot.getDefaultState(), 2);
                                LootGenerator.lootBones(world, iX, iY, iZ);
                            } else {
                                if(loot) {
                                    world.setBlockState(pos, ModBlocks.deco_loot.getDefaultState(), 2);
                                    LootGenerator.lootGlyphidHive(world, iX, iY, iZ);
                                } else {
                                    world.setBlockState(pos, ModBlocks.glyphid_base.getDefaultState().withProperty(BlockGlyphid.TYPE, infected ? Type.INFESTED : Type.BASE), 2);
                                }
                            }
                            break;
                    }
                }
            }
        }
    }
}
