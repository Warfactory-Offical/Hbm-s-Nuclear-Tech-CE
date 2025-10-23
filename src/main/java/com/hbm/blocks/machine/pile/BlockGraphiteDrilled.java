package com.hbm.blocks.machine.pile;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.BlockMeta;
import com.hbm.blocks.machine.pile.BlockGraphiteDrilledBase;
import com.hbm.blocks.machine.pile.BlockGraphiteRod;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.material.Mats;
import com.hbm.items.ModItems;
import com.hbm.items.special.ItemCell;
import com.hbm.lib.HBMSoundHandler;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockGraphiteDrilled extends BlockGraphiteDrilledBase {
	
	public BlockGraphiteDrilled(String s){
		super(s);
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ){
		if(!player.getHeldItem(hand).isEmpty()) {

			int meta = getMetaFromState(world.getBlockState(pos));
			int cfg = meta & 3;

			if(facing.getIndex() == cfg * 2 || facing.getIndex() == cfg * 2 + 1) {
				int x = pos.getX();
				int y = pos.getY();
				int z = pos.getZ();
				if(checkInteraction(world, x, y, z, meta, player, hand, ModItems.pile_rod_uranium, ModBlocks.block_graphite_fuel)) return true;
				if(checkInteraction(world, x, y, z, meta | 8, player, hand, ModItems.pile_rod_pu239, ModBlocks.block_graphite_fuel)) return true;
				if(checkInteraction(world, x, y, z, meta, player, hand, ModItems.pile_rod_plutonium, ModBlocks.block_graphite_plutonium)) return true;
				if(checkInteraction(world, x, y, z, meta, player, hand, ModItems.pile_rod_source, ModBlocks.block_graphite_source)) return true;
				if(checkInteraction(world, x, y, z, meta, player, hand, ModItems.pile_rod_boron, ModBlocks.block_graphite_rod)) return true;
				if(checkInteraction(world, x, y, z, meta, player, hand, ModItems.pile_rod_lithium, ModBlocks.block_graphite_lithium)) return true;
				if(checkInteraction(world, x, y, z, meta, player, hand, ItemCell.getFullCell(Fluids.TRITIUM).getItem(), ModBlocks.block_graphite_tritium)) return true; //if you want to i guess?
				if(checkInteraction(world, x, y, z, meta, player, hand, ModItems.pile_rod_detector, ModBlocks.block_graphite_detector)) return true;
				if(meta >> 2 != 1) {
					if(checkInteraction(world, x, y, z, meta | 4, player, hand, ModItems.shell, ModBlocks.block_graphite_drilled)) return true;
					return checkInteraction(world, x, y, z, 0, player, hand, ModItems.ingot_graphite, ModBlocks.block_graphite);
				}
			}
		}

		return false;
	}
	
	private boolean checkInteraction(World world, int x, int y, int z, int meta, EntityPlayer player, EnumHand hand, Item item, Block block) {
		
		if(player.getHeldItem(hand).getItem() == item) {
			player.getHeldItem(hand).shrink(1);
			if(item == ModItems.shell && player.getHeldItem(hand).getItemDamage() != Mats.MAT_ALUMINIUM.id) return false; //shitty workaround
			if(item == ModItems.cell && player.getHeldItem(hand).getItemDamage() != Fluids.TRITIUM.getID()) return false; //shitty workaround x2
			world.setBlockState(new BlockPos(x, y, z), block.getDefaultState().withProperty(BlockMeta.META, meta), 3);
			world.playSound(null, x + 0.5, y + 1.5, z + 0.5, HBMSoundHandler.upgradePlug, SoundCategory.BLOCKS, 1.0F, 1.0F);
			
			return true;
		}
		
		return false;
	}

	@Override
	public boolean onScrew(World world, EntityPlayer player, int x, int y, int z, EnumFacing side, float fX, float fY, float fZ, EnumHand hand, ToolType tool) {
		BlockPos pos = new BlockPos(x, y, z);
		int meta = getMetaFromState(world.getBlockState(pos));
		int cfg = meta & 3;

		if(tool != ToolType.SCREWDRIVER)
			return false;

		if(!world.isRemote && (side.getIndex() == cfg * 2 || side.getIndex() == cfg * 2 + 1) && meta >> 2 == 1) {
			world.setBlockState(pos, ModBlocks.block_graphite_drilled.getDefaultState().withProperty(BlockMeta.META, cfg), 3);
			world.playSound(null, x + 0.5, y + 1.5, z + 0.5, HBMSoundHandler.upgradePlug, SoundCategory.BLOCKS, 1.0F, 0.85F);

			BlockGraphiteRod.ejectItem(world, x, y, z, side, new ItemStack(ModItems.shell, 1, Mats.MAT_ALUMINIUM.id));
		}

		return true;
	}
}