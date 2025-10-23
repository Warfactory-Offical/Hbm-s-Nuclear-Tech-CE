package com.leafia.passive;

import com.hbm.tileentity.machine.TileEntityCore;
import com.llib.group.LeafiaSet;
import com.llib.math.MathLeafia;
import net.minecraft.client.Minecraft;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class LeafiaPassiveLocal {
	static final List<Runnable> queue = new ArrayList<>();
	// "LeaviaPassiveLoval" (my typing is awesome)
	static short t0 = 0;
	static int packetRecordTimer1s = 1_000;
	static int packetRecordTimer1m = 60;

	public static LeafiaSet<TileEntityCore> trackingCores = new LeafiaSet<>();

	public static void onTick(World world) {
		//TrackerLocal.localTick(Minecraft.getMinecraft().player);
		for (Runnable callback : queue)
			callback.run();
		queue.clear();
		if (Minecraft.getMinecraft().isGamePaused()) {
			for (TileEntityCore core : trackingCores);
				//core.explosionClock = System.currentTimeMillis();
		}
	}
	public static void priorTick(World world) {
		/*RecordablePacket.previousByteUsage = RecordablePacket.bytesUsage;
		RecordablePacket.bytesUsage = 0;
		short t1 = MathLeafia.getTime32s();
		int dT = MathLeafia.getTimeDifference32s(t0,t1);
		t0 = t1;
		packetRecordTimer1s -= dT;
		if (packetRecordTimer1s < 0) {
			packetRecordTimer1s = Math.floorMod(packetRecordTimer1s,1000);
			packetRecordTimer1m--;
			RecordablePacket.previousByteUsageSec = RecordablePacket.bytesUsageSec;
			RecordablePacket.bytesUsageSec = 0;
			if (packetRecordTimer1m <= 0) {
				packetRecordTimer1m = 60;
				RecordablePacket.previousByteUsageMin = RecordablePacket.bytesUsageMin;
				RecordablePacket.bytesUsageMin = 0;
			}
		}*/
	}
	public static void queueFunctionPost(Runnable callback) {
		queue.add(callback);
	}
}
