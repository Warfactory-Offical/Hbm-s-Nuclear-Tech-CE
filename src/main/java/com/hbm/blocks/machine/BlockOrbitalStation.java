package com.hbm.blocks.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ILookOverlay;
import com.hbm.dim.CelestialBody;
import com.hbm.handler.RocketStruct;
import com.hbm.handler.atmosphere.IBlockSealable;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.items.ModItems;
import com.hbm.items.ModItems.MissileParts;
import com.hbm.items.weapon.ItemCustomRocket;
import com.hbm.lib.ForgeDirection;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.TileEntityOrbitalStation;
import com.hbm.util.BobMathUtil;
import com.hbm.util.I18nUtil;
import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Pre;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

public class BlockOrbitalStation extends BlockDummyable implements IBlockSealable, ILookOverlay {

	public BlockOrbitalStation(Material mat, String s) {
		super(mat, s);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		if(meta >= 12) return new TileEntityOrbitalStation();
		if(meta >= 6) return new TileEntityProxyCombo(true, false, true);
		return null;
	}

	@Override
	public int[] getDimensions() {
		return new int[] {1, 0, 2, 2, 2, 2};
	}

	@Override
	public int getOffset() {
		return 2;
	}
	
	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		int[] posC = this.findCore(world, pos.getX(), pos.getY(), pos.getZ());

		if(posC == null)
			return false;

		// If activating the side blocks, ignore, to allow placing
		if(Math.abs(posC[0] - pos.getX()) >= 2 || Math.abs(posC[2] - pos.getZ()) >= 2)
			return false;

		if(world.isRemote) {
			return true;
		} else {
			TileEntity te = world.getTileEntity(new BlockPos(posC[0], posC[1], posC[2]));

			if(!(te instanceof TileEntityOrbitalStation))
				return false;

			TileEntityOrbitalStation station = (TileEntityOrbitalStation) te;

			if(station.hasStoredItems()) {
				station.giveStoredItems(player);
			} else if(station.hasDocked) {
				if(!station.hasRider) {
					if(player.isSneaking()) {
						if(player.getHeldItem(hand).isEmpty()) {
							station.despawnRocket();
							station.giveStoredItems(player);
						}
					} else {
						station.enterCapsule(player);
					}
				}
			} else {
				ItemStack held = player.getHeldItem(hand);
				if(held != ItemStack.EMPTY) {
					if(held.getItem() == ModItems.rocket_custom && ItemCustomRocket.hasFuel(held)) {
						station.spawnRocket(held);
						held.shrink(1);
					}
					if(held.getItem() == MissileParts.rp_capsule_20 || held.getItem() == MissileParts.rp_pod_20) {
						station.spawnRocket(ItemCustomRocket.build(new RocketStruct(held)));
						held.shrink(1);
					}
				}
			}
			
			return true;
		}
	}

	@Override
	public boolean isSealed(World world, int x, int y, int z) {
		return true;
	}

	@Override
	public void fillSpace(World world, int x, int y, int z, ForgeDirection dir, int o) {
		super.fillSpace(world, x, y, z, dir, o);

		x += dir.offsetX * o;
		z += dir.offsetZ * o;

		this.makeExtra(world, x + 2, y + 1, z - 1);
		this.makeExtra(world, x + 2, y + 1, z + 0);
		this.makeExtra(world, x + 2, y + 1, z + 1);
		this.makeExtra(world, x - 2, y + 1, z - 1);
		this.makeExtra(world, x - 2, y + 1, z + 0);
		this.makeExtra(world, x - 2, y + 1, z + 1);
		this.makeExtra(world, x - 1, y + 1, z + 2);
		this.makeExtra(world, x + 0, y + 1, z + 2);
		this.makeExtra(world, x + 1, y + 1, z + 2);
		this.makeExtra(world, x - 1, y + 1, z - 2);
		this.makeExtra(world, x + 0, y + 1, z - 2);
		this.makeExtra(world, x + 1, y + 1, z - 2);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void printHook(Pre event, World world, int x, int y, int z) {
		if(!CelestialBody.inOrbit(world)) {
			List<String> text = new ArrayList<String>();
			text.add("&[" + (BobMathUtil.getBlink() ? 0xff0000 : 0xffff00) + "&]! ! ! " + I18nUtil.resolveKey("atmosphere.noOrbit") + " ! ! !");
			ILookOverlay.printGeneric(event, I18nUtil.resolveKey(getTranslationKey() + ".name"), 0xffff00, 0x404000, text);
			return;
		}

		int[] posC = this.findCore(world, x, y, z);
		
		if(posC == null)
			return;

		if(Math.abs(posC[0] - x) >= 2 || Math.abs(posC[2] - z) >= 2)
			return;
		
		TileEntity te = world.getTileEntity(new BlockPos(posC[0], posC[1], posC[2]));
		
		if(!(te instanceof TileEntityOrbitalStation))
			return;
		
		TileEntityOrbitalStation station = (TileEntityOrbitalStation) te;
		EntityPlayer player = Minecraft.getMinecraft().player;

		List<String> text = new ArrayList<String>();

		for(int i = 0; i < station.inventory.getSlots(); i++) {
			if(!station.inventory.getStackInSlot(i).isEmpty()) {
				text.add(ChatFormatting.RED + "<- " + ChatFormatting.RESET + station.inventory.getStackInSlot(i).getDisplayName());
			}
		}

		if(!text.isEmpty()) {
			text.add(I18nUtil.resolveKey("station.retrieveRocket"));
		} else {
			if(station.hasDocked) {
				if(!station.hasRider) {
					if(player.isSneaking()) {
						if(player.getHeldItem(player.getActiveHand()).isEmpty()) {
							text.add(I18nUtil.resolveKey("station.removeRocket"));
						}
					} else {
						text.add(I18nUtil.resolveKey("station.enterRocket"));
					}
				} else {
					text.add(ChatFormatting.YELLOW + I18nUtil.resolveKey("station.occupiedRocket"));
				}

				if(station.needsFuel) {
					for(FluidTankNTM tank : station.getReceivingTanks()) {
						text.add(ChatFormatting.GREEN + "-> " + ChatFormatting.RESET + tank.getTankType().getLocalizedName() + ": " + tank.getFill() + "/" + tank.getMaxFill() + "mB");
					}

					if(!station.hasFuel) {
						text.add(ChatFormatting.RED + I18nUtil.resolveKey("station.emptyRocket"));
					}
				}
			} else if(!player.isSneaking()) {
				ItemStack held = player.getHeldItem(player.getActiveHand());
				if(!held.isEmpty()) {
					if(held.getItem() == ModItems.rocket_custom && ItemCustomRocket.hasFuel(held)) {
						text.add(I18nUtil.resolveKey("station.placeRocket"));
					}
					if(held.getItem() == MissileParts.rp_capsule_20 || held.getItem() == MissileParts.rp_pod_20) {
						text.add(I18nUtil.resolveKey("station.placeRocket"));
					}
				}
			}
		}

		if(text.isEmpty())
			return;
		
		ILookOverlay.printGeneric(event, I18nUtil.resolveKey(getTranslationKey() + ".name"), 0xffff00, 0x404000, text);
	}
	
}
