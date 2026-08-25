package com.hbm.saveddata.satellites;

import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemDrive.EnumDriveType;
import com.hbm.util.EnumUtil;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class Satellite {
	
	public static List<Class<? extends Satellite>> satellites = new ArrayList<Class<? extends Satellite>>();
	public static HashMap<Item, Class<? extends Satellite>> itemToClass = new HashMap<Item, Class<? extends Satellite>>();
	
	public static enum InterfaceActions {
		HAS_MAP,		//lets the interface display loaded chunks
		CAN_CLICK,		//enables onClick events
		SHOW_COORDS,	//enables coordinates as a mouse tooltip
		HAS_RADAR,		//lets the interface display loaded entities
		HAS_ORES		//like HAS_MAP but only shows ores
	}
	
	public static enum CoordActions {
		HAS_Y		//enables the Y-coord field which is disabled by default
	}
	
	public static enum Interfaces {
		NONE,		//does not interact with any sat interface (i.e. asteroid miners)
		SAT_PANEL,	//allows to interact with the sat interface panel (for graphical applications)
		SAT_COORD	//allows to interact with the sat coord remote (for teleportation or other coord related actions)
	}

	public List<InterfaceActions> ifaceAcs = new ArrayList<InterfaceActions>();
	public List<CoordActions> coordAcs = new ArrayList<CoordActions>();
	public Interfaces satIface = Interfaces.NONE;
	
	public static void register() {

		registerSatellite(SatelliteMapper.class, ModItems.sat_mapper);
		registerSatellite(SatelliteScanner.class, ModItems.sat_scanner);
		registerSatellite(SatelliteRadar.class, ModItems.sat_radar);
		registerSatellite(SatelliteLaser.class, ModItems.sat_laser);
		registerSatellite(SatelliteResonator.class, ModItems.sat_resonator);
		registerSatellite(SatelliteRelay.class, ModItems.sat_foeq);
		registerSatellite(SatelliteMiner.class, ModItems.sat_miner);
		registerSatellite(SatelliteLunarMiner.class, ModItems.sat_lunar_miner);
		registerSatellite(SatelliteHorizons.class, ModItems.sat_gerald);
	}
	
	private static void registerSatellite(Class<? extends Satellite> sat, Item item) {

		satellites.add(sat);
		itemToClass.put(item, sat);
	}
	
	public static void orbit(World world, int id, int freq, double x, double y, double z) {
		
		Satellite sat = create(id);
		if(sat != null && !world.isRemote) {
			SatelliteSavedData data = SatelliteSavedData.getData(world);
			data.sats.put(freq, sat);
			sat.onOrbit(world, x, y, z);
			data.markDirty();
		}
	}
	
	public static Satellite create(int id) {
		
		Satellite sat = null;
		
		try {
			Class<? extends Satellite> c = satellites.get(id);
			sat = c.newInstance();
		} catch(Exception ex) {
		}
		
		return sat;
	}
	
	public static int getIDFromItem(Item item) {
		
		Class<? extends Satellite> sat = itemToClass.get(item);
		int i = satellites.indexOf(sat);
		
		return i;
	}
	
	public int getID() {
		return satellites.indexOf(this.getClass());
	}
	
	/** Data currently loaded into the satellite, consumed by the tape drive over a sat link */
	public EnumDriveType driveInput = null;
	public EnumDriveType driveOutput = null;

	public void writeToNBT(NBTTagCompound nbt) {
		if(driveInput != null) nbt.setInteger("driveInput", driveInput.ordinal());
		if(driveOutput != null) nbt.setInteger("driveOutput", driveOutput.ordinal());
	}

	public void readFromNBT(NBTTagCompound nbt) {
		this.driveInput = nbt.hasKey("driveInput") ? EnumUtil.grabEnumSafely(EnumDriveType.VALUES, nbt.getInteger("driveInput")) : null;
		this.driveOutput = nbt.hasKey("driveOutput") ? EnumUtil.grabEnumSafely(EnumDriveType.VALUES, nbt.getInteger("driveOutput")) : null;
	}

	public String getType() {
		return this.getClass().getSimpleName();
	}

	public ITextComponent[] getInfo(World world) {
		return new ITextComponent[0];
	}

	/** The check for if there's data available, may also call produceData if a cooldown has elapsed */
	public boolean hasData(World world) {
		return this.driveInput != null && this.driveOutput != null;
	}

	public EnumDriveType getOutputData(EnumDriveType input) {
		if(input == this.driveInput) return this.driveOutput;
		return null;
	}

	public void produceData(EnumDriveType input, EnumDriveType output) {
		this.driveInput = input;
		this.driveOutput = output;
	}

	public void consumeData() {
		this.driveInput = null;
		this.driveOutput = null;
	}
	
	/**
	 * Called when the satellite reaches space, used to trigger achievements and other funny stuff.
	 * @param x posX of the rocket
	 * @param y ditto
	 * @param z ditto
	 */
	public void onOrbit(World world, double x, double y, double z) { }
	
	/**
	 * Called by the sat interface when clicking on the screen
	 *
	 * @param player
	 * @param x      the x-coordinate translated from the on-screen coords to actual world coordinates
	 * @param z      ditto
	 */
	public void onClick(World world, EntityPlayerMP player, int x, int z) { }
	
	/**
	 * Called by the coord sat interface
	 * @param x the specified x-coordinate
	 * @param y ditto
	 * @param z ditto
	 */
	public void onCoordAction(World world, EntityPlayerMP player, int x, int y, int z) { }

	public abstract float[] getColor();
}
