package com.hbm.blocks.generic;

import com.hbm.blocks.ILookOverlay;
import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.tileentity.TileEntityLoadedBase;
import com.hbm.util.I18nUtil;
import com.hbm.util.Tuple.Pair;
import com.hbm.world.gen.nbt.NBTStructure;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BlockWandStructure extends BlockContainer implements ILookOverlay {

	public BlockWandStructure(String s) {
		super(Material.IRON);
		this.setTranslationKey(s);
		this.setRegistryName(s);
		this.setCreativeTab(null);

		ModBlocks.ALL_BLOCKS.add(this);
	}

	@Override
	public TileEntity createNewTileEntity(@NotNull World world, int meta) {
		return new TileEntityWandStructure();
	}

	@Override
	public @NotNull EnumBlockRenderType getRenderType(@NotNull IBlockState state) {
		return EnumBlockRenderType.MODEL;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void printHook(RenderGameOverlayEvent.Pre event, World world, BlockPos pos) {

		TileEntity te = world.getTileEntity(pos);
		if(!(te instanceof TileEntityWandStructure)) return;
		TileEntityWandStructure wand = (TileEntityWandStructure) te;

		List<String> text = new ArrayList<>();
		text.add("Name: " + wand.name);
		text.add("Size: " + wand.sizeX + " / " + wand.sizeY + " / " + wand.sizeZ);

		ILookOverlay.printGeneric(event, I18nUtil.resolveKey(getTranslationKey() + ".name"), 0xffff00, 0x404000, text);
	}

	@AutoRegister(name = "tileentity_wand_structure")
	public static class TileEntityWandStructure extends TileEntityLoadedBase implements IControlReceiver, ITickable {

		public String name = "";

		public int sizeX = 1;
		public int sizeY = 1;
		public int sizeZ = 1;

		public Set<Pair<Block, Integer>> blacklist = new HashSet<>();

		@Override
		public void update() {
			if(!world.isRemote) {
				networkPackNT(256);
			}
		}

		public void saveStructure(EntityPlayer player) {

			if(name.isEmpty()) {
				player.sendMessage(new TextComponentString(TextFormatting.RED + "Could not save: invalid name"));
				return;
			}

			if(sizeX <= 0 || sizeY <= 0 || sizeZ <= 0) {
				player.sendMessage(new TextComponentString(TextFormatting.RED + "Could not save: invalid dimensions"));
				return;
			}

			Pair<Block, Integer> air = new Pair<>(Blocks.AIR, 0);
			blacklist.add(air);

			NBTStructure.saveArea(name + ".nbt", world,
					pos.getX(), pos.getY() + 1, pos.getZ(),
					pos.getX() + sizeX - 1, pos.getY() + sizeY, pos.getZ() + sizeZ - 1, blacklist);

			blacklist.remove(air);

			player.sendMessage(new TextComponentString("Saved structure as " + name + ".nbt"));
		}

		@Override
		public boolean hasPermission(EntityPlayer player) {
			return player.getDistanceSq(pos) < 100;
		}

		@Override
		public void receiveControl(NBTTagCompound data) {

			if(data.hasKey("name")) this.name = data.getString("name");
			if(data.hasKey("sizeX")) this.sizeX = data.getInteger("sizeX");
			if(data.hasKey("sizeY")) this.sizeY = data.getInteger("sizeY");
			if(data.hasKey("sizeZ")) this.sizeZ = data.getInteger("sizeZ");

			this.markDirty();
		}

		@Override
		public void serialize(ByteBuf buf) {
			com.hbm.util.BufferUtil.writeString(buf, name);
			buf.writeInt(sizeX);
			buf.writeInt(sizeY);
			buf.writeInt(sizeZ);
		}

		@Override
		public void deserialize(ByteBuf buf) {
			this.name = com.hbm.util.BufferUtil.readString(buf);
			this.sizeX = buf.readInt();
			this.sizeY = buf.readInt();
			this.sizeZ = buf.readInt();
		}

		@Override
		public void readFromNBT(NBTTagCompound nbt) {
			super.readFromNBT(nbt);
			this.name = nbt.getString("name");
			this.sizeX = nbt.getInteger("sizeX");
			this.sizeY = nbt.getInteger("sizeY");
			this.sizeZ = nbt.getInteger("sizeZ");
		}

		@Override
		public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {
			nbt.setString("name", name);
			nbt.setInteger("sizeX", sizeX);
			nbt.setInteger("sizeY", sizeY);
			nbt.setInteger("sizeZ", sizeZ);
			return super.writeToNBT(nbt);
		}
	}
}
