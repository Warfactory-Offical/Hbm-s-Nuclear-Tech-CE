package com.hbm.world;

import com.hbm.blocks.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDirectional;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.ApiStatus;

import java.util.Random;

// mlbv: not naturally generated, schedule for removal
@Deprecated
@ApiStatus.ScheduledForRemoval(inVersion = "1.0")
public class ParticleAccelerator {

	public static String[][] array = new String[][] {
		{
			"                 ",
			" YYYYYYYYYYYYYYY ",
			" YYYYYYYYYYYYYYY ",
			" YYYYYYYYYYYYYYY ",
			" YYY         YYY ",
			" YYY         YYY ",
			"DDDDD       DDDDD",
			" YYY        WWWWW",
			" YYY        WWWWW",
			" YYY        WWWWW",
			"DDDDD       DDDDD",
			" YYY         YYY ",
			" YYY         YYY ",
			" YYYYYYYYYYYYYYY ",
			" YYYYYYYYYYYYYYY ",
			" YYYYYYYYYYYYYYY ",
			"                 "
		},
		{
			" BBBBBBBBBBBBBBB ",
			"BAAAMMMMMMMMMAAAB",
			"BAAAMMMMMMMMMAAAB",
			"BAAAMMMMMMMMMAAAB",
			"BMMMBBBBBBBBBMMMB",
			"BMMMB       BMMMB",
			"DMMMD       DAAAD",
			"YMMMY       W   W",
			"YMMMY       O   O",
			"YMMMY       W   W",
			"DMMMD       DAAAD",
			"BMMMB       BMMMB",
			"BMMMBBBBBBBBBMMMB",
			"BAAAMMMMMMMMMAAAB",
			"BAAAMMMMMMMMMAAAB",
			"BAAAMMMMMMMMMAAAB",
			" BBBBBBBBBBBBBBB "
		},
		{
 			" GGGGGGGGGGGGGGG ",
			"GAAAMMMMMMMMMAAAG",
			"GA             AG",
			"GA AMMMMMMMMMA AG",
			"GM MGGGGGGGGGM MG",
			"BM MB       BM MB",
			"DM MD       DA AD",
			"YM MY       W   W",
			"YM MY       O   O",
			"YM MY       W   W",
			"DM MD       DA AD",
			"BM MB       BM MB",
			"GM MGGGGGGGGGM MG",
			"GA AMMMMMMMMMA AG",
			"GA             AG",
			"GAAAMMMMMMMMMAAAG",
			" GGGGGGGGGGGGGGG "
		},
		{
			" BBBBBBBBBBBBBBB ",
			"BAAAMMMMMMMMMAAAB",
			"BAAAMMMMMMMMMAAAB",
			"BAAAMMMMMMMMMAAAB",
			"BMMMBBBBBBBBBMMMB",
			"BMMMB       BMMMB",
			"DMMMD       DAAAD",
			"YMMMY       W   W",
			"YMMMY       O   O",
			"YMMMY       W   W",
			"DMMMD       DAAAD",
			"BMMMB       BMMMB",
			"BMMMBBBBBBBBBMMMB",
			"BAAAMMMMMMMMMAAAB",
			"BAAAMMMMMMMMMAAAB",
			"BAAAMMMMMMMMMAAAB",
			" BBBBBBBBBBBBBBB "
		},
		{
			"                 ",
			" YYYYYYYYYYYYYYY ",
			" YPPPPPPPPPPPPPY ",
			" YPYYYYYYYYYYYPY ",
			" YPY         YPY ",
			" YPY         YPY ",
			"DDPDD       DDDDD",
			" YPY        WWWWW",
			" YPY        WWWWW",
			" YPY        WWWWW",
			"DDPDD       DDDDD",
			" YPY         YPY ",
			" YPY         YPY ",
			" YPYYYYYYYYYYYPY ",
			" YPPPPPPPPPPPPPY ",
			" YYYYYYYYYYYYYYY ",
			"                 "
		},
		{
			"                 ",
			"                 ",
			"  HHHHHHHHHHHHH  ",
			"  H           H  ",
			"  H           H  ",
			"  H           H  ",
			"  H              ",
			"  H              ",
			"  H              ",
			"  H              ",
			"  H              ",
			"  H           H  ",
			"  H           H  ",
			"  H           H  ",
			"  HHHHHHHHHHHHH  ",
			"                 ",
			"                 "
		}
	};
	
	public void generate(World world, Random rand, BlockPos pos) {
		MutableBlockPos mPos = new BlockPos.MutableBlockPos();
		int x = pos.getX() - 8;
		int y = pos.getY();
		int z = pos.getZ() - 8;
		
		for(int i = 0; i < 17; i++) {
			for(int j = 0; j < 6; j++) {
				for(int k = 0; k < 17; k++) {
					String c = array[j][i].substring(k, k + 1);
					Block b = Blocks.AIR;

					if(c.equals("M"))//Mese
						b = ModBlocks.hadron_coil_mese;
					if(c.equals("A"))//Alloy
						b = ModBlocks.hadron_coil_alloy;
					if(c.equals("B"))//Black
						b = ModBlocks.hadron_plating_black;
					if(c.equals("Y"))//Yellow
						b = ModBlocks.hadron_plating_yellow;
					if(c.equals("D"))//Danger
						b = ModBlocks.hadron_plating_striped;
					if(c.equals("G"))//Glass
						b = ModBlocks.hadron_plating_glass;
					if(c.equals("P"))//Plug
						b = ModBlocks.hadron_power;
					if(c.equals("H"))//yeah
						b = ModBlocks.red_cable;
					if(c.equals("W"))//Chamber Walls
						b = ModBlocks.hadron_analysis;
					if(c.equals("O"))//Observation glass
						b = ModBlocks.hadron_analysis_glass;
					
					world.setBlockState(mPos.setPos(x + i, y + j, z + k), b.getDefaultState());
				}
			}
		}		
		
		world.setBlockState(mPos.setPos(x + 8, y + 2, z + 2), ModBlocks.hadron_core.getDefaultState().withProperty(BlockDirectional.FACING, EnumFacing.WEST), 3);
		world.setBlockState(mPos.setPos(x + 8, y + 2, z), ModBlocks.hadron_access.getDefaultState().withProperty(BlockDirectional.FACING, EnumFacing.SOUTH), 3);
	}
}
