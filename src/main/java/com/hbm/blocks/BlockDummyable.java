package com.hbm.blocks;

import com.google.common.collect.ImmutableMap;
import com.hbm.handler.MultiblockHandlerXR;
import com.hbm.interfaces.ICopiable;
import com.hbm.items.IDynamicModels;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.InventoryHelper;
import com.hbm.main.MainRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.model.ModelRotation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class BlockDummyable extends BlockContainer implements ICustomBlockHighlight, ICopiable, IDynamicModels {

	//Drillgon200: I'm far to lazy to figure out what all the meta values should be translated to in properties
	public static final PropertyInteger META = PropertyInteger.create("meta", 0, 15);
	
	public BlockDummyable(Material materialIn, String s) {
		super(materialIn);
		this.setTranslationKey(s);
		this.setRegistryName(s);
		this.setTickRandomly(true);
		
		ModBlocks.ALL_BLOCKS.add(this);
		IDynamicModels.INSTANCES.add(this);
	}

	public BlockDummyable(Material materialIn, String s, boolean useBakedModel) {
		super(materialIn);
		this.setTranslationKey(s);
		this.setRegistryName(s);
		this.setTickRandomly(true);

		ModBlocks.ALL_BLOCKS.add(this);
	}
	
	/// BLOCK METADATA ///
	
	//0-5 		dummy rotation 		(for dummy neighbor checks)
	//6-11 		extra 				(6 rotations with flag, for pipe connectors and the like)
	//12-15 	block rotation 		(for rendering the TE)

	//meta offset from dummy to TE rotation
	public static final int offset = 10;
	//meta offset from dummy to extra rotation
	public static final int extra = 6;
		
	public static boolean safeRem = false;
	
	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos) {
		if(world.isRemote || safeRem)
    		return;
    	
    	int metadata = state.getValue(META);
    	
    	//if it's an extra, remove the extra-ness
    	if(metadata >= extra)
    		metadata -= extra;
    	
    	ForgeDirection dir = ForgeDirection.getOrientation(metadata).getOpposite();
    	Block b = world.getBlockState(new BlockPos(pos.getX() + dir.offsetX, pos.getY() + dir.offsetY, pos.getZ() + dir.offsetZ)).getBlock();
    	if(b.getClass() != this.getClass()) {
    		world.setBlockToAir(pos);
    	}
	}
	
	@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
		super.updateTick(world, pos, state, rand);
		if(world.isRemote)
    		return;
    	
    	int metadata = state.getValue(META);
    	
    	//if it's an extra, remove the extra-ness
    	if(metadata >= extra)
    		metadata -= extra;
    	
    	ForgeDirection dir = ForgeDirection.getOrientation(metadata).getOpposite();
    	Block b = world.getBlockState(new BlockPos(pos.getX() + dir.offsetX, pos.getY() + dir.offsetY, pos.getZ() + dir.offsetZ)).getBlock();
    	
    	if(b.getClass() != this.getClass()) {
    		world.setBlockToAir(pos);
    	}
	}

	@Nullable
	public BlockPos findCore(IBlockAccess world, BlockPos pos) {
    	positions.clear();
    	int[] p = findCoreRec(world, pos.getX(), pos.getY(), pos.getZ());
    	if(p == null) return null;
    	return new BlockPos(p[0], p[1], p[2]);
    }

	public int @Nullable [] findCore(IBlockAccess world, int x, int y, int z) {
    	positions.clear();
    	return findCoreRec(world, x, y, z);
    }
    
    List<BlockPos> positions = new ArrayList<BlockPos>();

	private int @Nullable [] findCoreRec(IBlockAccess world, int x, int y, int z) {
    	
    	BlockPos pos = new BlockPos(x, y, z);
    	IBlockState state = world.getBlockState(pos);
    	
    	if(state.getBlock().getClass() != this.getClass())
    		return null;
    	
    	int metadata = state.getValue(META);
    	
    	//if it's an extra, remove the extra-ness
    	if(metadata >= extra)
    		metadata -= extra;
    	
    	//if the block matches and the orientation is "UNKNOWN", it's the core
    	if(ForgeDirection.getOrientation(metadata) == ForgeDirection.UNKNOWN)
    		return new int[] { x, y, z };
    	
    	if(positions.contains(pos))
    		return null;

    	ForgeDirection dir = ForgeDirection.getOrientation(metadata).getOpposite();
    	
    	positions.add(pos);
    	
    	return findCoreRec(world, x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ);
    }
    
    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase player, ItemStack itemStack) {
    	if(!(player instanceof EntityPlayer))
			return;
		safeRem = true;
    	world.setBlockToAir(pos);
		safeRem = false;
    	
		EntityPlayer pl = (EntityPlayer) player;
		EnumHand hand = pl.getHeldItemMainhand() == itemStack ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND;
		
		int i = MathHelper.floor(player.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;
		int o = -getOffset();
		pos = new BlockPos(pos.getX(), pos.getY() + getHeightOffset(), pos.getZ());

		ForgeDirection dir = switch (i) {
            case 0 -> ForgeDirection.getOrientation(2);
            case 1 -> ForgeDirection.getOrientation(5);
            case 2 -> ForgeDirection.getOrientation(3);
            case 3 -> ForgeDirection.getOrientation(4);
            default -> ForgeDirection.NORTH;
        };
		
		dir = getDirModified(dir);
		
		int x = pos.getX();
		int y = pos.getY();
		int z = pos.getZ();
		
		if(!checkRequirement(world, x, y, z, dir, o)) {
			if(!pl.capabilities.isCreativeMode) {
				ItemStack stack = pl.inventory.mainInventory.get(pl.inventory.currentItem);
				Item item = Item.getItemFromBlock(this);
				
				if(stack.isEmpty()) {
					pl.inventory.mainInventory.set(pl.inventory.currentItem, new ItemStack(this));
				} else {
					if(stack.getItem() != item || stack.getCount() == stack.getMaxStackSize()) {
						pl.inventory.addItemStackToInventory(new ItemStack(this));
					} else {
						pl.getHeldItem(hand).grow(1);
					}
				}
			}
			
			return;
		}
		
		if(!world.isRemote){
			world.setBlockState(new BlockPos(x + dir.offsetX * o , y + dir.offsetY * o, z + dir.offsetZ * o), this.getDefaultState().withProperty(META, dir.ordinal() + offset), 3);
			fillSpace(world, x, y, z, dir, o);
		}
		pos = new BlockPos(pos.getX(), pos.getY() - getHeightOffset(), pos.getZ());
		world.scheduleUpdate(pos, this, 1);
		world.scheduleUpdate(pos, this, 2);

    	super.onBlockPlacedBy(world, pos, state, player, itemStack);
    }

	protected boolean standardOpenBehavior(World world, BlockPos pos, EntityPlayer player, int id){
		return this.standardOpenBehavior(world, pos.getX(), pos.getY(), pos.getZ(), player, id);
	}

    protected boolean standardOpenBehavior(World world, int x, int y, int z, EntityPlayer player, int id) {
		
		if(world.isRemote) {
			return true;
		} else if(!player.isSneaking()) {
			int[] pos = this.findCore(world, x, y, z);

			if(pos == null)
				return false;

			player.openGui(MainRegistry.instance, id, world, pos[0], pos[1], pos[2]);
			return true;
		} else {
			return true;
		}
	}
    public ForgeDirection getDirModified(ForgeDirection dir) {
		return dir;
	}

	protected EnumFacing getDirModified(EnumFacing dir) {
		return dir;
	}

    public boolean checkRequirement(World world, int x, int y, int z, ForgeDirection dir, int o) {
		return MultiblockHandlerXR.checkSpace(world, x + dir.offsetX * o , y + dir.offsetY * o, z + dir.offsetZ * o, getDimensions(), x, y, z, dir);
	}
	
	protected void fillSpace(World world, int x, int y, int z, ForgeDirection dir, int o) {
		MultiblockHandlerXR.fillSpace(world, x + dir.offsetX * o , y + dir.offsetY * o, z + dir.offsetZ * o, getDimensions(), this, dir);
	}
	
	//"upgrades" regular dummy blocks to ones with the extra flag
	public void makeExtra(World world, int x, int y, int z) {
		BlockPos pos = new BlockPos(x, y, z);
		if(world.getBlockState(pos).getBlock() != this)
			return;
		
		int meta = world.getBlockState(pos).getValue(META);
		
		if(meta > 5)
			return;
			
		//world.setBlockMetadataWithNotify(x, y, z, meta + extra, 3);
		safeRem = true;
		world.setBlockState(pos, this.getDefaultState().withProperty(META, meta + extra), 3);
		safeRem = false;
	}
	
	//Drillgon200: Removes the extra. I could have sworn there was already a method for this, but I can't find it.
	public void removeExtra(World world, int x, int y, int z) {
		BlockPos pos = new BlockPos(x, y, z);
		if(world.getBlockState(pos).getBlock() != this)
			return;
		
		int meta = world.getBlockState(pos).getValue(META);
		
		if(meta <= 5 || meta >= 12)
			return;
			
		//world.setBlockMetadataWithNotify(x, y, z, meta + extra, 3);
		safeRem = true;
		world.setBlockState(pos, this.getDefaultState().withProperty(META, meta - extra), 3);
		safeRem = false;
	}
		
	//checks if the dummy metadata is within the extra range
	public boolean hasExtra(int meta) {
		return meta > 5 && meta < 12;
	}
	
	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		int i = state.getValue(META);
		if(i >= 12) {
			//ForgeDirection d = ForgeDirection.getOrientation(world.getBlockMetadata(x, y, z) - offset);
			//MultiblockHandler.emptySpace(world, x, y, z, getDimensions(), this, d);
		} else if(!safeRem) {

			if(i >= extra)
				i -= extra;

			ForgeDirection dir = ForgeDirection.getOrientation(i).getOpposite();
			int[] pos1 = findCore(world, pos.getX() + dir.offsetX, pos.getY() + dir.offsetY, pos.getZ() + dir.offsetZ);

			if(pos1 != null) {

				//ForgeDirection d = ForgeDirection.getOrientation(world.getBlockMetadata(pos[0], pos[1], pos[2]) - offset);
				world.setBlockToAir(new BlockPos(pos1[0], pos1[1], pos1[2]));
			}
		}
		InventoryHelper.dropInventoryItems(world, pos, world.getTileEntity(pos));
		super.breakBlock(world, pos, state);
	}

	public boolean useDetailedHitbox() {
		return !bounding.isEmpty();
	}

	public List<AxisAlignedBB> bounding = new ArrayList<>();

	@Override
	public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean isActualState) {
		if (!this.useDetailedHitbox()) {
			super.addCollisionBoxToList(state, worldIn, pos, entityBox, collidingBoxes, entityIn, isActualState);
			return;
		}

		int[] corePos = this.findCore(worldIn, pos.getX(), pos.getY(), pos.getZ());

		if (corePos == null) {
			return;
		}

		BlockPos coreBlockPos = new BlockPos(corePos[0], corePos[1], corePos[2]);

		for (AxisAlignedBB aabb : this.bounding) {
			AxisAlignedBB rotatedBox = getAABBRotationOffset(
					aabb,
					coreBlockPos.getX() + 0.5,
					coreBlockPos.getY(),
					coreBlockPos.getZ() + 0.5,
					getRotationFromState(worldIn.getBlockState(coreBlockPos))
			);

			if (entityBox.intersects(rotatedBox)) {
				collidingBoxes.add(rotatedBox);
			}
		}
	}

	private ForgeDirection getRotationFromState(IBlockState state) {
		int meta = state.getValue(META);
		return ForgeDirection.getOrientation(meta - offset).getRotation(ForgeDirection.UP);
	}

	public static AxisAlignedBB getAABBRotationOffset(AxisAlignedBB aabb, double x, double y, double z, ForgeDirection dir) {
		AxisAlignedBB newBox = null;

		if (dir == ForgeDirection.NORTH) {
			newBox = new AxisAlignedBB(aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ);
		} else if (dir == ForgeDirection.EAST) {
			newBox = new AxisAlignedBB(-aabb.maxZ, aabb.minY, aabb.minX, -aabb.minZ, aabb.maxY, aabb.maxX);
		} else if (dir == ForgeDirection.SOUTH) {
			newBox = new AxisAlignedBB(-aabb.maxX, aabb.minY, -aabb.maxZ, -aabb.minX, aabb.maxY, -aabb.minZ);
		} else if (dir == ForgeDirection.WEST) {
			newBox = new AxisAlignedBB(aabb.minZ, aabb.minY, -aabb.maxX, aabb.maxZ, aabb.maxY, -aabb.minX);
		}

		if (newBox != null) {
			return newBox.offset(x, y, z);
		}

		return new AxisAlignedBB(aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ).offset(x + 0.5, y + 0.5, z + 0.5);
	}
	
	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.INVISIBLE;
	}
	
	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}
	
	@Override
	public boolean isBlockNormalCube(IBlockState state) {
		return false;
	}
	
	@Override
	public boolean isNormalCube(IBlockState state) {
		return false;
	}
	
	@Override
	public boolean isNormalCube(IBlockState state, IBlockAccess world, BlockPos pos) {
		return false;
	}
	@Override
	public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
		return false;
	}
	
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[]{META});
	}
	
	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(META);
	}
	
	@Override
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(META, meta);
	}
	
	public abstract int[] getDimensions();
	public abstract int getOffset();
	
	public int getHeightOffset() {
		return 0;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean shouldDrawHighlight(World world, BlockPos pos) {
		return !this.bounding.isEmpty();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void drawHighlight(DrawBlockHighlightEvent event, World world, BlockPos pos) {

		int[] posC = this.findCore(world, pos.getX(), pos.getY(), pos.getZ());
		if(posC == null) return;

		int coreX = posC[0];
		int coreY = posC[1];
		int coreZ = posC[2];

		EntityPlayer player = event.getPlayer();
		float interp = event.getPartialTicks();
		double dX = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double) interp;
		double dY = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double) interp;
		double dZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double)interp;
		float exp = 0.002F;

		int meta = world.getBlockState(new BlockPos(coreX, coreY, coreZ)).getValue(META);

		ICustomBlockHighlight.setup();
		for(AxisAlignedBB aabb : this.bounding) RenderGlobal.drawSelectionBoundingBox(getAABBRotationOffset(aabb.expand(exp, exp, exp), 0, 0, 0, ForgeDirection.getOrientation(meta - offset).getRotation(ForgeDirection.UP)).offset(coreX - dX + 0.5, coreY - dY, coreZ - dZ + 0.5), 0,0,0,1.0F);
		ICustomBlockHighlight.cleanup();
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		if (!this.useDetailedHitbox()) {
			return FULL_BLOCK_AABB;
		} else {
			return new AxisAlignedBB(0.0F, 0.0F, 0.0F, 1.0F, 0.999F, 1.0F);
		}
	}

	@Override
	public NBTTagCompound getSettings(World world, int x, int y, int z) {
		int[] pos = findCore(world, x, y, z);
		TileEntity tile = world.getTileEntity(new BlockPos(pos[0], pos[1], pos[2]));
		if (tile instanceof ICopiable)
			return ((ICopiable) tile).getSettings(world, pos[0], pos[1], pos[2]);
		else
			return null;
	}

	@Override
	public void pasteSettings(NBTTagCompound nbt, int index, World world, EntityPlayer player, int x, int y, int z) {
		int[] pos = findCore(world, x, y, z);
		TileEntity tile = world.getTileEntity(new BlockPos(pos[0], pos[1], pos[2]));
		if (tile instanceof ICopiable)
			((ICopiable) tile).pasteSettings(nbt, index, world, player, pos[0], pos[1], pos[2]);
	}

	@Override
	public String[] infoForDisplay(World world, int x, int y, int z) {
		int[] pos = findCore(world, x, y, z);
		TileEntity tile = world.getTileEntity(new BlockPos(pos[0], pos[1], pos[2]));
		if (tile instanceof ICopiable)
			return ((ICopiable) tile).infoForDisplay(world, x, y, z);
		return null;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void bakeModel(ModelBakeEvent event) {
		try {
			IModel blockBaseModel = ModelLoaderRegistry.getModel(new ResourceLocation("block/cube_all"));
			ImmutableMap<String, String> blockTextures = ImmutableMap.of("all", "hbm:blocks/block_steel");
			IModel blockRetextured = blockBaseModel.retexture(blockTextures);
			IBakedModel blockBaked = blockRetextured.bake(
					ModelRotation.X0_Y0,
					DefaultVertexFormats.BLOCK,
					ModelLoader.defaultTextureGetter()
			);
			ModelResourceLocation worldLocation = new ModelResourceLocation(getRegistryName(), "normal");
			event.getModelRegistry().putObject(worldLocation, blockBaked);
			IModel itemBaseModel = ModelLoaderRegistry.getModel(new ResourceLocation("item/generated"));
			ImmutableMap<String, String> itemTextures = ImmutableMap.of("layer0", "hbm:blocks/" + getRegistryName().getPath());
			IModel itemRetextured = itemBaseModel.retexture(itemTextures);
			IBakedModel itemBaked = itemRetextured.bake(
					ModelRotation.X0_Y0,
					DefaultVertexFormats.ITEM,
					ModelLoader.defaultTextureGetter()
			);
			ModelResourceLocation inventoryLocation = new ModelResourceLocation(getRegistryName(), "inventory");
			event.getModelRegistry().putObject(inventoryLocation, itemBaked);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	@Override
	@SideOnly(Side.CLIENT)
	public void registerModel() {
		ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), 0, new ModelResourceLocation(this.getRegistryName(), "inventory"));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerSprite(TextureMap map) {
	}

	@Override
	@SideOnly(Side.CLIENT)
	public StateMapperBase getStateMapper(ResourceLocation loc) {
		return new StateMapperBase() {
			@Override
			protected ModelResourceLocation getModelResourceLocation(IBlockState state) {
				return new ModelResourceLocation(loc, "normal");
			}
		};
	}

}
