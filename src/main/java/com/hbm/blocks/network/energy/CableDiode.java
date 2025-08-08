package com.hbm.blocks.network.energy;

import com.hbm.api.block.IToolable;
import com.hbm.api.energymk2.IEnergyConnectorBlock;
import com.hbm.api.energymk2.IEnergyConnectorMK2;
import com.hbm.api.energymk2.IEnergyReceiverMK2;
import com.hbm.api.energymk2.IEnergyReceiverMK2.ConnectionPriority;
import com.hbm.api.energymk2.Nodespace;
import com.hbm.api.energymk2.Nodespace.PowerNode;
import com.hbm.blocks.ILookOverlay;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.lib.ForgeDirection;
import com.hbm.tileentity.TileEntityLoadedBase;
import com.hbm.util.BobMathUtil;
import com.hbm.util.Compat;
import com.hbm.util.I18nUtil;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Pre;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CableDiode extends BlockContainer implements IEnergyConnectorBlock, ILookOverlay, IToolable, ITooltipProvider {
	public static final PropertyDirection FACING = BlockDirectional.FACING;

	public CableDiode(Material materialIn, String s) {
		super(materialIn);
		this.setTranslationKey(s);
		this.setRegistryName(s);

		ModBlocks.ALL_BLOCKS.add(this);
	}

	@Override
	protected @NotNull BlockStateContainer createBlockState(){
		return new BlockStateContainer(this, FACING);
	}

	@Override
	public int getMetaFromState(IBlockState state){
		return state.getValue(FACING).getIndex();
	}

	@Override
	public @NotNull IBlockState getStateFromMeta(int meta) {
		EnumFacing enumfacing = EnumFacing.byIndex(meta);
		return this.getDefaultState().withProperty(FACING, enumfacing);
	}

	@Override
	public @NotNull IBlockState withRotation(IBlockState state, Rotation rot){
		return state.withProperty(FACING, rot.rotate(state.getValue(FACING)));
	}

	@NotNull
	@Override
	public IBlockState withMirror(IBlockState state, Mirror mirrorIn){
		return state.withRotation(mirrorIn.toRotation(state.getValue(FACING)));
	}

	@Override
	public void onBlockPlacedBy(World worldIn, @NotNull BlockPos pos, IBlockState state, @NotNull EntityLivingBase placer, @NotNull ItemStack stack) {
		worldIn.setBlockState(pos, state.withProperty(FACING, EnumFacing.getDirectionFromEntityLiving(pos, placer)));
	}

	@Override
	public boolean canConnect(IBlockAccess world, BlockPos pos, ForgeDirection dir) {
		return true;
	}

	@Override
	public boolean onScrew(World world, EntityPlayer player, int x, int y, int z, EnumFacing side, float fX, float fY, float fZ, EnumHand hand, ToolType tool) {
		BlockPos pos = new BlockPos(x, y, z);
		IBlockState state = world.getBlockState(pos);
		TileEntityDiode te = (TileEntityDiode) world.getTileEntity(pos);
		assert te != null;

		if(world.isRemote)
			return true;

		if(tool == ToolType.SCREWDRIVER) {
			if(te.level < 11)
				te.level++;
			te.markDirty();
			world.notifyBlockUpdate(pos, state, state, 3);
			return true;
		}

		if(tool == ToolType.HAND_DRILL) {
			if(te.level > 1)
				te.level--;
			te.markDirty();
			world.notifyBlockUpdate(pos, state, state, 3);
			return true;
		}

		if(tool == ToolType.DEFUSER) {
			int p = te.priority.ordinal() + 1;
			if(p > 4) p = 0;
			te.priority = ConnectionPriority.values()[p];
			te.markDirty();
			world.notifyBlockUpdate(pos, state, state, 3);
			return true;
		}

		return false;
	}

	@Override
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> list, ITooltipFlag flagIn) {
		list.add(TextFormatting.GOLD + "Limits throughput and restricts flow direction");
		list.add(TextFormatting.YELLOW + "Use screwdriver to increase throughput");
		list.add(TextFormatting.YELLOW + "Use hand drill to decrease throughput");
		list.add(TextFormatting.YELLOW + "Use defuser to change network priority");
	}

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.MODEL;
	}

	@Override
	public void printHook(Pre event, World world, int x, int y, int z) {

		TileEntity te = world.getTileEntity(new BlockPos(x, y, z));

		if(!(te instanceof TileEntityDiode))
			return;

		TileEntityDiode diode = (TileEntityDiode) te;

		List<String> text = new ArrayList<>();
		text.add("Max.: " + BobMathUtil.getShortNumber(diode.getMaxPower()) + "HE/t");
		text.add("Priority: " + diode.priority.name());

		ILookOverlay.printGeneric(event, I18nUtil.resolveKey(getTranslationKey() + ".name"), 0xffff00, 0x404000, text);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityDiode();
	}

	@AutoRegister
	public static class TileEntityDiode extends TileEntityLoadedBase implements IEnergyReceiverMK2, ITickable {

		@Override
		public void readFromNBT(NBTTagCompound nbt) {
			super.readFromNBT(nbt);
			level = nbt.getInteger("level");
			priority = ConnectionPriority.values()[nbt.getByte("p")];
		}

		@Override
		public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {
			super.writeToNBT(nbt);
			nbt.setInteger("level", level);
			nbt.setByte("p", (byte) this.priority.ordinal());
			return nbt;
		}

		@Override
		public @Nullable SPacketUpdateTileEntity getUpdatePacket() {
			NBTTagCompound nbt = new NBTTagCompound();
			this.writeToNBT(nbt);
			return new SPacketUpdateTileEntity(this.pos, 0, nbt);
		}

		@Override
		public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
			this.readFromNBT(pkt.getNbtCompound());
		}

		int level = 1;

		private ForgeDirection getDir() {
			return ForgeDirection.getOrientation(this.getBlockMetadata()).getOpposite();
		}

		@Override
		public void update() {

			if(!world.isRemote) {
				for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {

					if(dir == getDir())
						continue;

					this.trySubscribe(world, pos.getX() + dir.offsetX, pos.getY() + dir.offsetY, pos.getZ() + dir.offsetZ, dir);
				}

				pulses = 0;
				this.setPower(0); //tick is over, reset our allowed transfer
			}
		}

		@Override
		public boolean canConnect(ForgeDirection dir) {
			return dir != getDir();
		}

		/** Used as an intra-tick tracker for how much energy has been transmitted, resets to 0 each tick and maxes out based on transfer */
		private long power;
		private boolean recursionBrake = false;
		private int pulses = 0;
		public ConnectionPriority priority = ConnectionPriority.NORMAL;

		@Override
		public long transferPower(long power, boolean simulate) {

			if(recursionBrake)
				return power;

			pulses++;
			if(this.getPower() >= this.getMaxPower() || pulses > 10) return power; //if we have already maxed out transfer or max pulses, abort

			recursionBrake = true;

			ForgeDirection dir = getDir();
			PowerNode node = Nodespace.getNode(world, new BlockPos(pos.getX() + dir.offsetX, pos.getY() + dir.offsetY, pos.getZ() + dir.offsetZ));
			TileEntity te = Compat.getTileStandard(world, pos.getX() + dir.offsetX, pos.getY() + dir.offsetY, pos.getZ() + dir.offsetZ);

			if(node != null && !node.expired && node.hasValidNet() && te instanceof IEnergyConnectorMK2 && ((IEnergyConnectorMK2) te).canConnect(dir.getOpposite())) {
				long toTransfer = Math.min(power, this.getReceiverSpeed());
				long remainder = node.net.sendPowerDiode(toTransfer, simulate);
				long transferred = (toTransfer - remainder);
				if (simulate) this.power += transferred;
				power -= transferred;

			} else if(te instanceof IEnergyReceiverMK2 && te != this) {
				IEnergyReceiverMK2 rec = (IEnergyReceiverMK2) te;
				if(rec.canConnect(dir.getOpposite())) {
					long toTransfer = Math.min(power, rec.getReceiverSpeed());
					long remainder = rec.transferPower(toTransfer, simulate);
					power -= (toTransfer - remainder);
					recursionBrake = false;
					return power;
				}
			}

			recursionBrake = false;
			return power;
		}

		@Override
		public long getReceiverSpeed() {
			return this.getMaxPower() - this.getPower();
		}

		@Override
		public long getMaxPower() {
			return (long) Math.pow(10, level);
		}

		@Override
		public long getPower() {
			return Math.min(power, this.getMaxPower());
		}

		@Override
		public void setPower(long power) {
			this.power = power;
		}

		@Override
		public ConnectionPriority getPriority() {
			return this.priority;
		}
	}
}