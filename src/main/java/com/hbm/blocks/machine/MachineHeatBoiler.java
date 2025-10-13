package com.hbm.blocks.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ILookOverlay;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.items.ModItems;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.lib.ForgeDirection;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.TileEntityHeatBoiler;
import com.hbm.util.I18nUtil;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Pre;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MachineHeatBoiler extends BlockDummyable implements ILookOverlay, ITooltipProvider {

    public MachineHeatBoiler(Material materialIn, String s) {
        super(materialIn, s);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {

        if(meta >= 12) return new TileEntityHeatBoiler();
        if(meta >= 6) return new TileEntityProxyCombo(false, false, true);

        return null;
    }

    @Override
    public int[] getDimensions() {
        return new int[] {3, 0, 1, 1, 1, 1};
    }

    @Override
    public int getOffset() {
        return 1;
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return Item.getItemFromBlock(ModBlocks.machine_boiler);
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
        return new ItemStack(ModBlocks.machine_boiler);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos1, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {

        if(!world.isRemote && !player.isSneaking()) {

            if(!player.getHeldItem(hand).isEmpty() && player.getHeldItem(hand).getItem() instanceof IItemFluidIdentifier identifier) {
                int[] pos = this.findCore(world, pos1.getX(), pos1.getY(), pos1.getZ());
                if(pos == null)
                    return false;

                TileEntity te = world.getTileEntity(new BlockPos(pos[0], pos[1], pos[2]));

                if(!(te instanceof TileEntityHeatBoiler boiler))
                    return false;

                FluidType type = identifier.getType(world, pos[0], pos[1], pos[2], player.getHeldItem(hand));
                boiler.tanks[0].setTankType(type);
                boiler.markDirty();
                player.sendMessage(new TextComponentString("Changed type to ").setStyle(new Style().setColor(TextFormatting.YELLOW)).appendSibling(new TextComponentTranslation(type.getConditionalName())).appendSibling(new TextComponentString("!")));

                return true;
            }
            return false;

        } else {
            return true;
        }
    }

    @Override
    protected void fillSpace(World world, int x, int y, int z, ForgeDirection dir, int o) {
        super.fillSpace(world, x, y, z, dir, o);

        x = x + dir.offsetX * o;
        z = z + dir.offsetZ * o;

        ForgeDirection rot = dir.getRotation(ForgeDirection.UP);

        this.makeExtra(world, x + rot.offsetX, y, z + rot.offsetZ); //these add the side ports
        this.makeExtra(world, x - rot.offsetX, y, z - rot.offsetZ);
        this.makeExtra(world, x, y + 3, z); 
    }

    @Override
    public void addInformation(ItemStack stack, World worldIn, List<String> list, ITooltipFlag flagIn) {
        this.addStandardInfo(list);
        super.addInformation(stack, worldIn, list, flagIn);
    }

    @Override
    public void printHook(Pre event, World world, int x, int y, int z) {
        int[] pos = this.findCore(world, x, y, z);

        if(pos == null)
            return;

        TileEntity te = world.getTileEntity(new BlockPos(pos[0], pos[1], pos[2]));

        if(!(te instanceof TileEntityHeatBoiler))
            return;

        TileEntityHeatBoiler boiler = (TileEntityHeatBoiler) te;

        List<String> text = new ArrayList<>();

        for(int i = 0; i < boiler.tanks.length; i++)
            text.add((i < 1 ? "§a-> " : "§c<- ") + "§r" + boiler.tanks[i].getTankType().getLocalizedName() + ": " + boiler.tanks[i].getFill() + "/" + boiler.tanks[i].getMaxFill() + "mB");

        ILookOverlay.printGeneric(event, I18nUtil.resolveKey(getTranslationKey() + ".name"), 0xffff00, 0x404000, text);
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        super.onBlockPlacedBy(world, pos, state, placer, stack);

        if (stack.getMetadata() == 1) {
            int i = MathHelper.floor((double)(placer.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
            int o = -getOffset();

            ForgeDirection dir = ForgeDirection.NORTH;
            if (i == 0) dir = ForgeDirection.getOrientation(2);
            if (i == 1) dir = ForgeDirection.getOrientation(5);
            if (i == 2) dir = ForgeDirection.getOrientation(3);
            if (i == 3) dir = ForgeDirection.getOrientation(4);

            dir = getDirModified(dir);

            BlockPos corePos = pos.add(dir.offsetX * o, dir.offsetY * o, dir.offsetZ * o);
            TileEntity te = world.getTileEntity(corePos);

            if (te instanceof TileEntityHeatBoiler boiler) {
                boiler.hasExploded = true;
                boiler.markDirty();
            }
        }
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        boolean handled = false;

        int[] core = this.findCore(world, pos.getX(), pos.getY(), pos.getZ());

        if (core != null) {
            TileEntity te = world.getTileEntity(new BlockPos(core[0], core[1], core[2]));
            if (te instanceof TileEntityHeatBoiler boiler) {
                if (boiler.hasExploded) {
                    drops.add(new ItemStack(ModItems.ingot_steel, 4));
                    drops.add(new ItemStack(ModItems.plate_copper, 8));
                    handled = true;
                }
            }
        }

        if (!handled) drops.add(new ItemStack(ModBlocks.machine_boiler));
    }
}
