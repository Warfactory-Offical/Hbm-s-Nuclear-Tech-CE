package com.leafia.dev.math;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;

public class FiaBB {
	public FiaMatrix mat;
	public double x0;
	public double y0;
	public double x1;
	public double y1;
	double depth;
	public FiaBB(FiaMatrix mat,double x0,double y0,double x1,double y1,double depth) {
		this.mat = mat;
		this.x0 = x0;
		this.y0 = y0;
		this.x1 = x1;
		this.y1 = y1;
		this.depth = depth;
	}
	public FiaBB(FiaBB bb) {
		this.mat = bb.mat;
		this.x0 = bb.x0;
		this.x1 = bb.x1;
		this.y0 = bb.y0;
		this.y1 = bb.y1;
		this.depth = bb.depth;
	}
	public AxisAlignedBB toAABB() {
		Vec3d a = mat.translate(x0,y0,0).position;
		Vec3d b = mat.translate(x1,y1,depth).position;
		return new AxisAlignedBB(
				Math.min(a.x,b.x),Math.min(a.y,b.y),Math.min(a.z,b.z),
				Math.max(a.x,b.x),Math.max(a.y,b.y),Math.max(a.z,b.z)
		);
	};
}
