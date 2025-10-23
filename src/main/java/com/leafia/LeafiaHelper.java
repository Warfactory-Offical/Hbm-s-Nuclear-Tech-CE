package com.leafia;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class LeafiaHelper {
	public static AxisAlignedBB getAABBRadius(Vec3d center,double radius) {
		return new AxisAlignedBB(center.x-radius,center.y-radius,center.z-radius,center.x+radius,center.y+radius,center.z+radius);
	}
	public static Vec3d getBlockPosCenter(BlockPos pos) {
		return new Vec3d(pos).add(0.5,0.5,0.5);
	}
}
