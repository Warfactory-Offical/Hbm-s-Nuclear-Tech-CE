package com.hbm.blocks.generic;

import com.hbm.blocks.ModBlocks;
import com.hbm.items.ModItems;
import com.hbm.items.ModItems.Armory;
import com.hbm.items.ModItems.Foods;
import com.hbm.items.weapon.sedna.factory.GunFactory;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import java.util.Random;

public class BlockAmmoCrate extends Block {

	public BlockAmmoCrate(Material materialIn, String s) {
		super(materialIn);
		this.setTranslationKey(s);
		this.setRegistryName(s);

		ModBlocks.ALL_BLOCKS.add(this);
	}

	Random rand = new Random();

	@Override
	public void getDrops(NonNullList<ItemStack> ret, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
		ret.add(new ItemStack(Foods.cap_nuka, 12 + rand.nextInt(21)));
        ret.add(new ItemStack(ModItems.syringe_metal_stimpak, 1 + rand.nextInt(3)));

		if(rand.nextBoolean()) ret.add(new ItemStack(Armory.ammo_standard, 16 + rand.nextInt(17), GunFactory.EnumAmmo.P9_SP.ordinal()));
		if(rand.nextBoolean()) ret.add(new ItemStack(Armory.ammo_standard, 16 + rand.nextInt(17), GunFactory.EnumAmmo.P9_FMJ.ordinal()));
		if(rand.nextBoolean()) ret.add(new ItemStack(Armory.ammo_standard, 16 + rand.nextInt(17), GunFactory.EnumAmmo.M357_SP.ordinal()));
		if(rand.nextBoolean()) ret.add(new ItemStack(Armory.ammo_standard, 16 + rand.nextInt(17), GunFactory.EnumAmmo.M357_FMJ.ordinal()));
		if(rand.nextBoolean()) ret.add(new ItemStack(Armory.ammo_standard, 16 + rand.nextInt(17), GunFactory.EnumAmmo.M44_SP.ordinal()));
		if(rand.nextBoolean()) ret.add(new ItemStack(Armory.ammo_standard, 16 + rand.nextInt(17), GunFactory.EnumAmmo.M44_FMJ.ordinal()));
		if(rand.nextBoolean()) ret.add(new ItemStack(Armory.ammo_standard, 16 + rand.nextInt(17), GunFactory.EnumAmmo.R556_SP.ordinal()));
		if(rand.nextBoolean()) ret.add(new ItemStack(Armory.ammo_standard, 16 + rand.nextInt(17), GunFactory.EnumAmmo.R556_FMJ.ordinal()));
		if(rand.nextBoolean()) ret.add(new ItemStack(Armory.ammo_standard, 16 + rand.nextInt(17), GunFactory.EnumAmmo.R762_SP.ordinal()));
		if(rand.nextBoolean()) ret.add(new ItemStack(Armory.ammo_standard, 16 + rand.nextInt(17), GunFactory.EnumAmmo.R762_FMJ.ordinal()));
		if(rand.nextBoolean()) ret.add(new ItemStack(Armory.ammo_standard, 16 + rand.nextInt(17), GunFactory.EnumAmmo.G12.ordinal()));
		if(rand.nextBoolean()) ret.add(new ItemStack(Armory.ammo_standard, 16 + rand.nextInt(17), GunFactory.EnumAmmo.G12_SLUG.ordinal()));
		if(rand.nextBoolean()) ret.add(new ItemStack(Armory.ammo_standard, 2 + rand.nextInt(3), GunFactory.EnumAmmo.G40_HE.ordinal()));
		if(rand.nextBoolean()) ret.add(new ItemStack(Armory.ammo_standard, 2 + rand.nextInt(3), GunFactory.EnumAmmo.ROCKET_HE.ordinal()));

        if(rand.nextInt(10) == 0) ret.add(new ItemStack(ModItems.syringe_metal_super, 2));
	}

}
