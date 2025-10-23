package com.leafia.passive;

import com.hbm.items.ModItems;
import com.leafia.eventbuses.LeafiaServerListener;
import com.leafia.transformer.WorldServerLeafia;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class LeafiaPassiveServer {
	static final List<Runnable> queue = new ArrayList<>();
	public static void onTick(World world) {
		//PWRDiagnosis.preventScan.clear();
		//Tracker.postTick(world);
		//PWRMeshedWreck.rmCache.clear();
	}
	public static void priorTick(World world) {
		//if (ModItems.wand_leaf.darnit != null)
		//	ModItems.wand_leaf.darnit.run();
		//Tracker.preTick(world);
		//LeafiaServerListener.SharpEdges.damageCache.clear();
		List<Runnable> running = new ArrayList<>(queue);
		queue.clear();
		for (Runnable callback : running)
			callback.run();
		WorldServerLeafia.violatedPositions.clear();
	}
	public static void queueFunction(Runnable callback) {
		queue.add(callback);
	}
}
