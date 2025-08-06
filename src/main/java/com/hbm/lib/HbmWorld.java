package com.hbm.lib;

import com.hbm.world.phased.PhasedStructureGenerator;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class HbmWorld {

	public static void mainRegistry()
	{
		initWorldGen();
	}
	
	public static void initWorldGen()
	{
		registerWorldGen(new HbmWorldGen(), 1);
	}
	
	public static void registerWorldGen(HbmWorldGen nukerWorldGen, int weightedProbability)
	{
		 GameRegistry.registerWorldGenerator(nukerWorldGen, weightedProbability);
		 GameRegistry.registerWorldGenerator(PhasedStructureGenerator.INSTANCE, weightedProbability);
	}
}
