package com.hbm.items.tool;

import com.hbm.api.item.IDepthRockTool;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.BlockBedrockOre;
import com.hbm.blocks.generic.BlockBedrockOreTE;
import com.hbm.handler.ToolAbility;
import com.hbm.handler.ToolAbility.SilkAbility;
import com.hbm.handler.WeaponAbility;
import com.hbm.items.ModItems;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toclient.PlayerInformPacketLegacy;
import com.hbm.util.I18nUtil;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.server.SPacketBlockChange;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.*;

public class ItemToolAbility extends ItemTool implements IItemAbility, IDepthRockTool {

	private EnumToolType toolType;
	private EnumRarity rarity = EnumRarity.COMMON;
	//was there a reason for this to be private?
    protected float damage;
    protected double movement;
    private List<ToolAbility> breakAbility = new ArrayList<ToolAbility>() {
		private static final long serialVersionUID = 153867601249309418L;
	{ add(null); }};
    private List<WeaponAbility> hitAbility = new ArrayList<WeaponAbility>();

    private boolean rockBreaker = false;
    
	
	public static enum EnumToolType {
		
		PICKAXE(
				Sets.newHashSet(new Material[] { Material.IRON, Material.ANVIL, Material.ROCK }),
				Sets.newHashSet(Blocks.ACTIVATOR_RAIL, Blocks.COAL_ORE, Blocks.COBBLESTONE, Blocks.DETECTOR_RAIL, Blocks.DIAMOND_BLOCK, Blocks.DIAMOND_ORE, Blocks.DOUBLE_STONE_SLAB, Blocks.GOLDEN_RAIL, Blocks.GOLD_BLOCK, Blocks.GOLD_ORE, Blocks.ICE, Blocks.IRON_BLOCK, Blocks.IRON_ORE, Blocks.LAPIS_BLOCK, Blocks.LAPIS_ORE, Blocks.LIT_REDSTONE_ORE, Blocks.MOSSY_COBBLESTONE, Blocks.NETHERRACK, Blocks.PACKED_ICE, Blocks.RAIL, Blocks.REDSTONE_ORE, Blocks.SANDSTONE, Blocks.RED_SANDSTONE, Blocks.STONE, Blocks.STONE_SLAB, Blocks.STONE_BUTTON, Blocks.STONE_PRESSURE_PLATE)
		),
		AXE(
				Sets.newHashSet(new Material[] { Material.WOOD, Material.PLANTS, Material.VINE }),
				Sets.newHashSet(Blocks.PLANKS, Blocks.BOOKSHELF, Blocks.LOG, Blocks.LOG2, Blocks.CHEST, Blocks.PUMPKIN, Blocks.LIT_PUMPKIN, Blocks.MELON_BLOCK, Blocks.LADDER, Blocks.WOODEN_BUTTON, Blocks.WOODEN_PRESSURE_PLATE)
		),
		SHOVEL(
				Sets.newHashSet(new Material[] { Material.CLAY, Material.SAND, Material.GROUND, Material.SNOW, Material.CRAFTED_SNOW }),
				Sets.newHashSet(Blocks.CLAY, Blocks.DIRT, Blocks.FARMLAND, Blocks.GRASS, Blocks.GRAVEL, Blocks.MYCELIUM, Blocks.SAND, Blocks.SNOW, Blocks.SNOW_LAYER, Blocks.SOUL_SAND, Blocks.GRASS_PATH, Blocks.CONCRETE_POWDER)
		),
		MINER(
				Sets.newHashSet(new Material[] { Material.GRASS, Material.IRON, Material.ANVIL, Material.ROCK, Material.CLAY, Material.SAND, Material.GROUND, Material.SNOW, Material.CRAFTED_SNOW })
		);
		
		private EnumToolType(Set<Material> materials) {
			this.materials = materials;
		}
		
		private EnumToolType(Set<Material> materials, Set<Block> blocks) {
			this.materials = materials;
			this.blocks = blocks;
		}

		public Set<Material> materials = new HashSet<Material>();
		public Set<Block> blocks = new HashSet<Block>();
	}
	
	public ItemToolAbility(float damage, float attackSpeedIn, double movement, ToolMaterial material, EnumToolType type, String s) {
		super(0, attackSpeedIn, material, type.blocks);
		this.setTranslationKey(s);
		this.setRegistryName(s);
		this.damage = damage;
		this.movement = movement;
		this.toolType = type;
		if(type == EnumToolType.MINER){
			this.setHarvestLevel("shovel", material.getHarvestLevel());
			this.setHarvestLevel("pickaxe", material.getHarvestLevel());
			this.setHarvestLevel("axe", material.getHarvestLevel());
		} else {
			this.setHarvestLevel(type.toString().toLowerCase(), material.getHarvestLevel());
		}

		ModItems.ALL_ITEMS.add(this);
	}
	
	public ItemToolAbility addBreakAbility(ToolAbility breakAbility) {
		this.breakAbility.add(breakAbility);
		return this;
	}
	
	public ItemToolAbility addHitAbility(WeaponAbility weaponAbility) {
		this.hitAbility.add(weaponAbility);
		return this;
	}
	
	//<insert obvious Rarity joke here>
	//Drillgon200: What?
	public ItemToolAbility setRarity(EnumRarity rarity) {
		this.rarity = rarity;
		return this;
	}
	
	//Drillgon200: Dang it bob, override annotations matter!
	@SuppressWarnings("deprecation")
	@Override
    public EnumRarity getRarity(ItemStack stack) {
        return this.rarity != EnumRarity.COMMON ? this.rarity : super.getRarity(stack);
    }
	
	@Override
	public boolean hitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker) {
		if(!attacker.world.isRemote && !this.hitAbility.isEmpty() && attacker instanceof EntityPlayer && canOperate(stack)) {
    		
    		for(WeaponAbility ability : this.hitAbility) {
				ability.onHit(attacker.world, (EntityPlayer) attacker, target, this);
    		}
    	}
		stack.damageItem(2, attacker);
        return true;
	}
	
	@Override
	public boolean onBlockStartBreak(ItemStack stack, BlockPos pos, EntityPlayer player) {
		World world = player.world;
    	IBlockState block = world.getBlockState(pos);
    	
    	if(!world.isRemote && canHarvestBlock(block, stack) && this.getCurrentAbility(stack) != null && canOperate(stack))
    		this.getCurrentAbility(stack).onDig(world, pos.getX(), pos.getY(), pos.getZ(), player, block, this, player.getHeldItemMainhand() == stack ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND);
    	
    	return false;
	}
	
	@Override
	public float getDestroySpeed(ItemStack stack, IBlockState state) {
		if(!canOperate(stack))
    		return 1;
    	
    	if(toolType == null)
            return super.getDestroySpeed(stack, state);
    	
    	if(toolType.blocks.contains(state.getBlock()) || toolType.materials.contains(state.getMaterial()))
    		return this.efficiency;
    	
        return super.getDestroySpeed(stack, state);
	}
	
	@Override
	public boolean canHarvestBlock(IBlockState state, ItemStack stack) {
		if(!canOperate(stack)) return false;

		if(isForbiddenBlock(state.getBlock())) return false;
    	
		if(this.getCurrentAbility(stack) instanceof SilkAbility)
    		return true;
		
    	return getDestroySpeed(stack, state) > 1;
	}

	public static boolean isForbiddenBlock(Block b){
		return (b == Blocks.BARRIER || b == Blocks.BEDROCK || b == Blocks.COMMAND_BLOCK || b == Blocks.CHAIN_COMMAND_BLOCK || b == Blocks.REPEATING_COMMAND_BLOCK || b == ModBlocks.ore_bedrock_oil || b instanceof BlockBedrockOre || b instanceof BlockBedrockOreTE );
	}
	
	@Override
	public Multimap<String, AttributeModifier> getItemAttributeModifiers(EntityEquipmentSlot slot) {
		Multimap<String, AttributeModifier> map = HashMultimap.<String, AttributeModifier>create();
		if(slot == EntityEquipmentSlot.MAINHAND){
			map.put(SharedMonsterAttributes.MOVEMENT_SPEED.getName(), new AttributeModifier(UUID.fromString("91AEAA56-376B-4498-935B-2F7F68070635"), "Tool modifier", movement, 1));
			map.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Tool modifier", this.damage, 0));
			map.put(SharedMonsterAttributes.ATTACK_SPEED.getName(), new AttributeModifier(ATTACK_SPEED_MODIFIER, "Tool modifier", this.attackSpeed, 0));
		}
        return map;
	}
	
	//that's slimelad's code
    //creative commons 3 and all that jazz
	@Override
    public void breakExtraBlock(World world, int x, int y, int z, EntityPlayer playerEntity, int refX, int refY, int refZ, EnumHand hand) {
    	BlockPos pos = new BlockPos(x, y, z);
        if (world.isAirBlock(pos))
            return;

        if(!(playerEntity instanceof EntityPlayerMP))
            return;
        
        EntityPlayerMP player = (EntityPlayerMP) playerEntity;
        ItemStack stack = player.getHeldItem(hand);

        IBlockState block = world.getBlockState(pos);

        if(!canHarvestBlock(block, stack))
            return;

        IBlockState refBlock = world.getBlockState(new BlockPos(refX, refY, refZ));
        float refStrength = ForgeHooks.blockStrength(refBlock, player, world, new BlockPos(refX, refY, refZ));
        float strength = ForgeHooks.blockStrength(block, player, world, pos);

        if (!ForgeHooks.canHarvestBlock(block.getBlock(), player, world, pos) || refStrength/strength > 10f)
            return;

        int event = ForgeHooks.onBlockBreakEvent(world, player.interactionManager.getGameType(), player, pos);
        if(event < 0)
            return;

        if (player.capabilities.isCreativeMode) {
            block.getBlock().onBlockHarvested(world, pos, block, player);
            if (block.getBlock().removedByPlayer(block, world, pos, player, false))
                block.getBlock().onPlayerDestroy(world, pos, block);

            if (!world.isRemote) {
                player.connection.sendPacket(new SPacketBlockChange(world, pos));
            }
            return;
        }

        player.getHeldItem(hand).onBlockDestroyed(world, block, pos, player);

        if (!world.isRemote) {
        	
            block.getBlock().onBlockHarvested(world, pos, block, player);

            if(block.getBlock().removedByPlayer(block, world, pos, player, true))
            {
                block.getBlock().onPlayerDestroy(world, pos, block);
                block.getBlock().harvestBlock(world, player, pos, block, world.getTileEntity(pos), stack);
                block.getBlock().dropXpOnBlockBreak(world, pos, event);
            }

            player.connection.sendPacket(new SPacketBlockChange(world, pos));
            
        } else {
            world.playEvent(2001, pos, Block.getStateId(block));
            if(block.getBlock().removedByPlayer(block, world, pos, player, true))
            {
                block.getBlock().onPlayerDestroy(world, pos, block);
            }
            ItemStack itemstack = player.getHeldItem(hand);
            if (itemstack != null)
            {
                itemstack.onBlockDestroyed(world, block, new BlockPos(x, y, z), player);

                if (itemstack.isEmpty())
                {
                    player.inventory.setInventorySlotContents(player.inventory.currentItem, ItemStack.EMPTY);
                }
            }

            Minecraft.getMinecraft().getConnection().sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, new BlockPos(x, y, z), Minecraft.getMinecraft().objectMouseOver.sideHit));
        }
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public boolean hasEffect(ItemStack stack) {
    	return getCurrentAbility(stack) != null ? true : super.hasEffect(stack);
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, World worldIn, List<String> list, ITooltipFlag flagIn) {
    	if(this.breakAbility.size() > 1) {
    		list.add(I18nUtil.resolveKey("tool.ability.list"));
    		
    		for(ToolAbility ability : this.breakAbility) {
    			
    			if(ability != null) {
    				
    				if(getCurrentAbility(stack) == ability)
    					list.add(" §e§l>" + ability.getFullName());
    				else
    					list.add("  §6" + ability.getFullName());
    			}
    		}

    		list.add(I18nUtil.resolveKey("tool.ability.rightclick"));
    		list.add(I18nUtil.resolveKey("tool.ability.shiftclick"));
    	}
    	
    	if(!this.hitAbility.isEmpty()) {
    		
    		list.add(I18nUtil.resolveKey("tool.ability.weaponlist"));
    		
    		for(WeaponAbility ability : this.hitAbility) {
				list.add("  " + TextFormatting.RED + ability.getFullName());
    		}
    	}

    	if(this.rockBreaker){
    		list.add("§5["+I18nUtil.resolveKey("trait.unmineable")+"]§d " + I18nUtil.resolveKey("tool.ability.canmine"));
    	}
    }
    
    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
    	ItemStack stack = player.getHeldItem(hand);
    	if(this.breakAbility.size() < 2 || !canOperate(stack))
    		return EnumActionResult.PASS;
    	if(!worldIn.isRemote){
    		switchMode(player, stack);
    	}
    	return EnumActionResult.SUCCESS;
    }
    
    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
    	ItemStack stack = player.getHeldItem(hand);
    	
    	if(world.isRemote || this.breakAbility.size() < 2 || !canOperate(stack))
    		return super.onItemRightClick(world, player, hand);
    	
    	switchMode(player, stack);
    	
    	return ActionResult.newResult(EnumActionResult.SUCCESS, stack);
    }
    
    private void switchMode(EntityPlayer player, ItemStack stack){
    	int i = getAbility(stack);
    	i++;
    	
    	if(player.isSneaking())
    		i = 0;
    	
    	setAbility(stack, i % this.breakAbility.size());
    	
    	while(getCurrentAbility(stack) != null && !getCurrentAbility(stack).isAllowed()) {

    		player.sendMessage(
				new TextComponentString("[")
				.appendSibling(new TextComponentTranslation("chat.ability"))
				.appendSibling(new TextComponentString(" "))
				.appendSibling(new TextComponentTranslation(getCurrentAbility(stack).getName(), new Object[0]))
				.appendSibling(new TextComponentString(getCurrentAbility(stack).getExtension() + " ")
				.appendSibling(new TextComponentTranslation("chat.blacklist"))
				.appendSibling(new TextComponentString("]")))
				.setStyle(new Style().setColor(TextFormatting.RED)));

        	i++;
        	setAbility(stack, i % this.breakAbility.size());
    	}
    	
    	if(getCurrentAbility(stack) != null) {
			PacketDispatcher.wrapper.sendTo(new PlayerInformPacketLegacy(new TextComponentString("[").appendSibling(new TextComponentTranslation("chat.abilenabled")).appendSibling(new TextComponentString(" ")).appendSibling(new TextComponentTranslation(getCurrentAbility(stack).getName())).appendSibling(new TextComponentString("]")).setStyle(new Style().setColor(TextFormatting.YELLOW)), 9), (EntityPlayerMP) player);
    	} else {
			PacketDispatcher.wrapper.sendTo(new PlayerInformPacketLegacy(new TextComponentString("[").appendSibling(new TextComponentTranslation("chat.abildisabled")).appendSibling(new TextComponentString("]")).setStyle(new Style().setColor(TextFormatting.GOLD)), 9), (EntityPlayerMP) player);
    	}

    	//Drillgon200: I hope "random.orb" referred to the experience orb sound
        player.world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 0.25F, getCurrentAbility(stack) == null ? 0.75F : 1.25F);
    }
    
    private ToolAbility getCurrentAbility(ItemStack stack) {
    	
    	int ability = getAbility(stack) % this.breakAbility.size();
    	
    	return this.breakAbility.get(ability);
    }
    
    private int getAbility(ItemStack stack) {
    	
    	if(stack.hasTagCompound())
    		return stack.getTagCompound().getInteger("ability");
    	
    	return 0;
    }
    
    private void setAbility(ItemStack stack, int ability) {

    	if(!stack.hasTagCompound())
    		stack.setTagCompound(new NBTTagCompound());
    	
    	stack.getTagCompound().setInteger("ability", ability);
    }
    
    protected boolean canOperate(ItemStack stack) {
    	return true;
    }

    public ItemToolAbility setDepthRockBreaker() {
		this.rockBreaker = true;
		return this;
	}
	
	
	@Override
	public boolean canBreakRock(World world, EntityPlayer player, ItemStack tool, IBlockState block, BlockPos pos){
		return canOperate(tool) && this.rockBreaker;
	}
}
