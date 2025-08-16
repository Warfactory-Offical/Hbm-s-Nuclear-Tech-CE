package com.hbm.items.machine;

import com.hbm.items.ItemBakedBase;
import com.hbm.items.ModItems;
import com.hbm.main.MainRegistry;
import com.hbm.util.I18nUtil;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ItemStamp extends ItemBakedBase {

	protected StampType type;
	public static final HashMap<StampType, List<ItemStack>> stamps = new HashMap<>();

	public ItemStamp(String s, int dura, StampType type) {
		super(s);
		this.setMaxDamage(dura);
		this.setCreativeTab(MainRegistry.controlTab);
		this.setMaxStackSize(1);
		this.type = type;
		if(type != null) {
			this.addStampToList(this, 0, type);
		}
	}

	protected void addStampToList(Item item, int meta, StampType type) {
		List<ItemStack> list = stamps.get(type);

		if(list == null)
			list = new ArrayList<>();

		ItemStack stack = new ItemStack(item, 1, meta);

		list.add(stack);
		stamps.put(type, list);
	}
	
	@Override
	public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		if(this == ModItems.stamp_iron_circuit ||
				this == ModItems.stamp_iron_plate ||
				this == ModItems.stamp_iron_wire ||
				this == ModItems.stamp_obsidian_circuit ||
				this == ModItems.stamp_obsidian_plate ||
				this == ModItems.stamp_obsidian_wire ||
				this == ModItems.stamp_desh_circuit ||
				this == ModItems.stamp_desh_plate ||
				this == ModItems.stamp_desh_wire ||
				this == ModItems.stamp_steel_circuit ||
				this == ModItems.stamp_steel_plate ||
				this == ModItems.stamp_steel_wire ||
				this == ModItems.stamp_titanium_circuit ||
				this == ModItems.stamp_titanium_plate ||
				this == ModItems.stamp_titanium_wire ||
				this == ModItems.stamp_stone_circuit ||
				this == ModItems.stamp_stone_plate ||
				this == ModItems.stamp_stone_wire)
			tooltip.add("§e" + I18nUtil.resolveKey("info.templatefolder", I18nUtil.resolveKey("item.template_folder.name")));
		if(stack.getMaxDamage() > 0 && stack.getItemDamage() == 0) tooltip.add("Durability: "+ stack.getMaxDamage() + " / " + stack.getMaxDamage());
	}

	/** Params can't take an ItemStack, for some reason it crashes during init */
	public StampType getStampType(Item item, int meta) {
		return type;
	}

	public static enum StampType {
		FLAT,
		PLATE,
		WIRE,
		CIRCUIT,
		C357,
		C44,
		C50,
		C9,
		PRINTING1,
		PRINTING2,
		PRINTING3,
		PRINTING4,
		PRINTING5,
		PRINTING6,
		PRINTING7,
		PRINTING8;
	}
}
