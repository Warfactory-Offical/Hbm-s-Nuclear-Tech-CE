package com.hbm.inventory.control_panel;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;

public class DataValueEnum<T extends Enum<T>> implements DataValue {

	public Enum<T> value;
	public Class<T> enumClass;

	@SuppressWarnings("unchecked")
	public DataValueEnum(Enum<T> e){
		value = e;
		if(e != null)
			enumClass = (Class<T>)e.getClass();
	}

	@Override
	public float getNumber(){
		return value.ordinal();
	}

	@Override
	public boolean getBoolean(){
		return value.toString().toLowerCase().equals("true") ? true : false;
	}

	@Override
	public String toString(){
		return value.toString();
	}

	@Override
	@SuppressWarnings("unchecked")
	public <E extends Enum<E>> E getEnum(Class<E> clazz){
		if(clazz == enumClass) {
			return (E)value;
		} else {
			return clazz.getEnumConstants()[0];
		}
	}

	@Override
	public DataType getType(){
		return DataType.ENUM;
	}

	public T[] getPossibleValues(){
		return enumClass.getEnumConstants();
	}

	@Override
	public NBTBase writeToNBT(){
		NBTTagCompound tag = new NBTTagCompound();
		tag.setString("clazz", enumClass.getName());
		tag.setInteger("ordinal", value.ordinal());
		return tag;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void readFromNBT(NBTBase nbt){
		NBTTagCompound tag = (NBTTagCompound)nbt;
		try {
			this.enumClass = (Class<T>)Class.forName(tag.getString("clazz"));
		} catch(ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
		this.value = this.enumClass.getEnumConstants()[tag.getInteger("ordinal")];
	}

}
