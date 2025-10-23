package com.leafia.transformer;

import com.hbm.blocks.generic.BlockCoalOil;
import com.hbm.entity.effect.EntityNukeTorex;
import com.hbm.util.Tuple.Pair;
import com.leafia.passive.LeafiaPassiveServer;
import com.llib.group.LeafiaSet;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.MinecraftException;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;

public class WorldServerLeafia {
	public static void player_onBreakBlockProgress(WorldServer world,int breakerId,BlockPos pos,int progress) {
		EntityPlayer doxxed = null;
		for (EntityPlayer entity : world.playerEntities) {
			if (entity.getEntityId() == breakerId)
				doxxed = entity;
		}
		if (doxxed != null) {
			IBlockState state = world.getBlockState(pos);
			Block block = state.getBlock();
			//if (block instanceof BlockCoalOil && Math.floorMod(progress,2) == 0)
				//((BlockCoalOil) block).onBreakBlockProgress(world,pos,doxxed);
		}
	}
	public static void fluid_onFilling(FluidStack stack,IFluidHandler inst) {
		World world = null;
		BlockPos pos = null;
		if (inst instanceof TileEntity) {
			world = ((TileEntity) inst).getWorld();
			pos = ((TileEntity) inst).getPos();
		} else if (inst instanceof IFluidHandlerItem) {
			ItemStack istack = ((IFluidHandlerItem) inst).getContainer();
			//istack. ah forget it
		} else if (inst != null) {
			for (Field field : inst.getClass().getFields()) {
				try {
					Object o = field.get(inst);
					if (o instanceof TileEntity) {
						world = ((TileEntity) o).getWorld();
						pos = ((TileEntity) o).getPos();
					}
				} catch (IllegalAccessException ignored) {}
			}
		}
		//if (!fluid_checkTraits(stack,inst,world,pos))
		//	stack.amount = 0; // fuck you
	}
	/*public static boolean fluid_canContinue(FluidStack stack,TileEntity te) {
		if (true) return true; // ah fuck it
		if (te != null)
			return fluid_checkTraits(stack,te,te.getWorld(),te.getPos());
		else
			return fluid_checkTraits(stack,null,null,null);
	}*/
	public static LeafiaSet<BlockPos> violatedPositions = new LeafiaSet<>();
	/*static boolean fluid_checkTraits(FluidStack stack,Object inst,World world,BlockPos pos) {
		LeafiaFluid fluid = LeafiaFluid.cast(stack);
		if (fluid != null) {
			List<LeafiaFluidTrait> hazards = new ArrayList<>();
			for (String trait : fluid.getTraits().fiaTraits) {
				LeafiaFluidTrait tr = LeafiaFluidTrait.reg.get(trait);
				if (tr.needsSpecializedContainer())
					hazards.add(tr);
			}
			if (!hazards.isEmpty()) {
				if (pos == null || world == null) return false;
				else {
					List<String> attributes = new ArrayList<>();
					if (inst instanceof ISpecializedContainer)
						attributes.addAll(Arrays.asList(((ISpecializedContainer) inst).protections()));
					boolean changed = true;
					while (changed) {
						changed = false;
						for (String trait : fluid.getTraits().fiaTraits) {
							for (Pair<String,String> re : LeafiaFluidTrait.reg.get(trait).redirections) {
								if (attributes.contains(re.getA()) && !attributes.contains(re.getB())) {
									attributes.add(re.getB());
									changed = true;
								}
							}
						}
					}
					if (!violatedPositions.contains(pos)) {
						for (LeafiaFluidTrait hazard : hazards) {
							boolean prevented = false;
							for (String preventation : hazard.preventations) {
								if (attributes.contains(preventation)) {
									prevented = true;
									break;
								}
							}
							if (!prevented) {
								Runnable callback = hazard.onViolation(world,pos,stack,inst);
								if (callback != null) {
									violatedPositions.add(pos);
									LeafiaPassiveServer.queueFunction(callback);
								}
							}
						}
					}
				}
			}
		}
		return true;
	}*/
}
