package com.hbm.saveddata.satellites;

import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemDrive.EnumDriveType;
import com.hbm.items.machine.ItemSatellite;
import com.hbm.items.machine.ItemSatellite.EnumSatType;
import com.hbm.util.BobMathUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class SatelliteScience extends Satellite {

	public static final int COOLDOWN = 15 * 60 * 20;
	public long lastScience;

	public static final int SENSOR_DURATION = 100 * 60 * 60 * 20;
	public int sensorProgress;
	public int sensorCount;

	@Override
	public String getType() {
		return "SCIENCE_PROBE";
	}

	@Override
	public boolean hasData(World world) {
		if(super.hasData(world)) return true;

		if(world.getTotalWorldTime() > this.lastScience + COOLDOWN) {
			this.produceData(EnumDriveType.DISK_EMPTY, EnumDriveType.DISK_FLIGHTDATA);
			this.lastScience = world.getTotalWorldTime();
		}

		return super.hasData(world);
	}

	@Override
	public void onPartDelivered(World world, ItemStack part) {

		if(!part.isEmpty() && part.getItem() == ModItems.satellite && part.getItemDamage() == EnumSatType.SCIENCE_SENSOR.ordinal()) {
			this.sensorCount++;
			this.markDirty();
		}
	}

	@Override
	public void onUpdateTick(World world) {

		if(this.sensorProgress < SENSOR_DURATION) {
			this.sensorProgress += this.sensorCount;
		} else {
			this.sensorProgress = 0;
			this.produceData(EnumDriveType.DISK_EMPTY, EnumDriveType.DISK_ORBITDATA);
			this.markDirty();
		}
	}

	@Override
	public ITextComponent[] getInfo(World world) {

		int cooldown = (int) ((lastScience + COOLDOWN) - world.getTotalWorldTime());
		int seconds = cooldown / 20;

		List<ITextComponent> info = new ArrayList<>();
		info.add(new TextComponentTranslation(ItemSatellite.make(EnumSatType.SCIENCE).getTranslationKey() + ".name"));
		info.add(cooldown <= 0 ? new TextComponentTranslation("satellite.ready") : new TextComponentTranslation("satellite.cooldown", (seconds / 60) + "m" + (seconds % 60) + "s"));
		if(this.sensorCount > 0) {
			info.add(new TextComponentTranslation("satellite.sensors", this.sensorCount));
			info.add(new TextComponentTranslation("satellite.pending", BobMathUtil.getShortNumber(SENSOR_DURATION - sensorProgress)));
		}
		if(this.driveOutput == EnumDriveType.DISK_ORBITDATA) info.add(new TextComponentTranslation("satellite.data"));

		return info.toArray(new ITextComponent[0]);
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setLong("lastScience", lastScience);
		nbt.setInteger("sensorProgress", sensorProgress);
		nbt.setInteger("sensorCount", sensorCount);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		lastScience = nbt.getLong("lastScience");
		sensorProgress = nbt.getInteger("sensorProgress");
		sensorCount = nbt.getInteger("sensorCount");
	}

	@Override
	public float[] getColor() {
		return new float[] { 1.0F, 1.0F, 0.4F };
	}
}
