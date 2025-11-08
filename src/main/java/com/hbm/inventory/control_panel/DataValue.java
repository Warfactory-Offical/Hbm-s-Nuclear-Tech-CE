package com.hbm.inventory.control_panel;

import com.hbm.main.MainRegistry;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagString;

public interface DataValue {
	float getNumber();
	boolean getBoolean();
	DataType getType();
	<E extends Enum<E>> E getEnum(Class<E> clazz);
	NBTBase writeToNBT();
	void readFromNBT(NBTBase nbt);
	
	static DataValue newFromNBT(NBTBase base){
		DataValue val = null;
		try {
			if(base instanceof NBTTagCompound) {
				val = new DataValueEnum<>(null);
				val.readFromNBT(base);
			} else if(base instanceof NBTTagFloat) {
				val = new DataValueFloat(0);
				val.readFromNBT(base);
			} else if(base instanceof NBTTagString) {
				val = new DataValueString("");
				val.readFromNBT(base);
			}
		} catch(Exception x) {
			MainRegistry.logger.catching(x);
			return null;
		}
		return val;
	}
	
	enum DataType {
		GENERIC(new float[]{0.5F, 0.5F, 0.5F}),
		NUMBER(new float[]{0.4F, 0.6F, 0}),
		STRING(new float[]{0, 1, 1}),
		ENUM(new float[]{0.29F, 0, 0.5F});
		
		private final float[] color;
		
		DataType(float[] color){
			this.color = color;
		}
		
		public float[] getColor(){
			return color;
		}
	}
}
