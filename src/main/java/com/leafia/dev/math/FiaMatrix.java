package com.leafia.dev.math;

import com.llib.exceptions.LeafiaDevFlaw;
import net.minecraft.util.math.Vec3d;

// Basically CFrame for minecraft except it uses degrees instead of radians owo
public class FiaMatrix {
	public Vec3d position = new Vec3d(0,0,0);
	public Vec3d frontVector = new Vec3d(0,0,-1);
	public Vec3d upVector = new Vec3d(0,1,0);
	public Vec3d rightVector = new Vec3d(1,0,0);
	public FiaMatrix() {}
	public FiaMatrix(Vec3d pos) { position = pos; }
	public FiaMatrix(Vec3d position,Vec3d rightVector,Vec3d upVector,Vec3d frontVector) {
		this.position = position;
		this.frontVector = frontVector;
		this.upVector = upVector;
		this.rightVector = rightVector;
	}
	public FiaMatrix(FiaMatrix reflectFrom) { reflect(reflectFrom); }
	public enum RotationOrder { XYZ,XZY,YXZ,YZX,ZYX,ZXY;
		final int[] order = new int[]{0,1,2};
		RotationOrder() {
			int swap = Math.floorDiv(this.ordinal(),2);
			order[0] = order[swap];
			order[swap] = 0;
			if (Math.floorMod(this.ordinal(),2) == 1) {
				int axis = order[1];
				order[1] = order[2];
				order[2] = axis;
			}
		}
	}
	public static class TupleRotation {
		public RotationOrder order;
		public double angleX;
		public double angleY;
		public double angleZ;
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof TupleRotation) {
				TupleRotation tuple = (TupleRotation)obj;
				return tuple.order == this.order && tuple.angleX == this.angleX
						&& tuple.angleY == this.angleY && tuple.angleZ == this.angleZ;
			}
			return super.equals(obj);
		}
		@Override
		public String toString() {
			return String.format("(["+order.name()+"]% 01.3f°,% 01.3f°,% 01.3f°)",angleX,angleY,angleZ);
		}
	}
	public FiaMatrix(RotationOrder order,double angleX,double angleY,double angleZ) {
		this.reflect(rotate(order,angleX,angleY,angleZ));
	}
	public FiaMatrix(TupleRotation rotation) { this.reflect(this.rotate(rotation)); }
	public FiaMatrix(Vec3d position,Vec3d lookAt) {
		this.position = position;
		Vec3d relative = lookAt.subtract(position);
		this.reflect(
				this.rotateY(Math.toDegrees(Math.atan2(-relative.x,-relative.z)))
						.rotateX(Math.toDegrees(Math.atan2(relative.y,Math.sqrt(relative.x*relative.x+relative.z*relative.z))))
		);
	}

	public double getX() { return position.x; }
	public double getY() { return position.y; }
	public double getZ() { return position.z; }

	public FiaMatrix add(Vec3d vector) {
		FiaMatrix matrix = new FiaMatrix(this);
		matrix.position = this.position.add(vector);
		return matrix;
	}
	public FiaMatrix subtract(Vec3d vector) {
		FiaMatrix matrix = new FiaMatrix(this);
		matrix.position = this.position.add(vector);
		return matrix;
	}
	public FiaMatrix scale(double factor) {
		FiaMatrix matrix = new FiaMatrix(this);
		matrix.position = this.position.scale(factor);
		return matrix;
	}
	public FiaMatrix translateWorld(double x,double y,double z) { return add(new Vec3d(x,y,z)); }
	public FiaMatrix translate(double x,double y,double z) {
		FiaMatrix matrix = new FiaMatrix(this);
		matrix.position = position.add(frontVector.scale(-z)).add(rightVector.scale(x)).add(upVector.scale(y));
		return matrix;
	}
	public FiaMatrix translateWorld(Vec3d vector) { return add(vector); }
	public FiaMatrix translate(Vec3d vector) { return translate(vector.x,vector.y,vector.z); }
	public FiaMatrix rotate(RotationOrder order,double angleX,double angleY,double angleZ) {
		FiaMatrix matrix = this;
		for (int axis : order.order) {
			if (axis == 0) matrix = matrix.rotateX(angleX);
			else if (axis == 1) matrix = matrix.rotateY(angleY);
			else matrix = matrix.rotateZ(angleZ);
		}
		return matrix;
	}
	public FiaMatrix rotate(RotationOrder order,Vec3d vector) { return rotate(order,vector.x,vector.y,vector.z); }
	public FiaMatrix rotate(TupleRotation rotation) { return rotate(rotation.order,rotation.angleX,rotation.angleY,rotation.angleZ); }
	public FiaMatrix travel(FiaMatrix other) {
		return new FiaMatrix(
				position.add(frontVector.scale(-other.position.z)).add(rightVector.scale(other.position.x)).add(upVector.scale(other.position.y)),
				rightVector.scale(other.rightVector.x).add(upVector.scale(other.rightVector.y)).subtract(frontVector.scale(other.rightVector.z)),
				rightVector.scale(other.upVector.x).add(upVector.scale(other.upVector.y)).subtract(frontVector.scale(other.upVector.z)),
				rightVector.scale(other.frontVector.x).add(upVector.scale(other.frontVector.y)).subtract(frontVector.scale(other.frontVector.z))
		);
	}
	public FiaMatrix rotateAlong(FiaMatrix other) {
		return new FiaMatrix(
				position,
				rightVector.scale(other.rightVector.x).add(upVector.scale(other.rightVector.y)).subtract(frontVector.scale(other.rightVector.z)),
				rightVector.scale(other.upVector.x).add(upVector.scale(other.upVector.y)).subtract(frontVector.scale(other.upVector.z)),
				rightVector.scale(other.frontVector.x).add(upVector.scale(other.frontVector.y)).subtract(frontVector.scale(other.frontVector.z))
		);
	}

	/**
	 * Copies the other FiaMatrix's data into this one
	 * <h3>This operation modifies <tt>this</tt> instance
	 * @param template The matrix to copy from
	 * @return Itself, for chaining
	 */
	public FiaMatrix reflect(FiaMatrix template) {
		position = template.position;
		frontVector = template.frontVector;
		upVector = template.upVector;
		rightVector = template.rightVector;
		return this;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof FiaMatrix) {
			FiaMatrix other = (FiaMatrix)obj;
			return position.equals(other.position) && frontVector.equals(other.frontVector)
					&& upVector.equals(other.upVector) && rightVector.equals(other.rightVector);
		}
		return super.equals(obj);
	}

	public FiaMatrix radian_rotateX(double rad) {
		double cos = Math.cos(rad); double sin = Math.sin(rad);
		return new FiaMatrix(position,
				rightVector,
				upVector.scale(cos).subtract(frontVector.scale(sin)),
				frontVector.scale(cos).add(upVector.scale(sin))
		);
	}
	public FiaMatrix radian_rotateY(double rad) {
		double cos = Math.cos(rad); double sin = Math.sin(rad);
		return new FiaMatrix(position,
				rightVector.scale(cos).add(frontVector.scale(sin)),
				upVector,
				frontVector.scale(cos).subtract(rightVector.scale(sin))
		);
	}
	public FiaMatrix radian_rotateZ(double rad) {
		double cos = Math.cos(rad); double sin = Math.sin(rad);
		return new FiaMatrix(position,
				rightVector.scale(cos).add(upVector.scale(sin)),
				upVector.scale(cos).subtract(rightVector.scale(sin)),
				frontVector
		);
	}
	public FiaMatrix rotateX(double angle) { return radian_rotateX(Math.toRadians(angle)); }
	public FiaMatrix rotateY(double angle) { return radian_rotateY(Math.toRadians(angle)); }
	public FiaMatrix rotateZ(double angle) { return radian_rotateZ(Math.toRadians(angle)); }
	public FiaMatrix inverse() {
		/* broken code lol
		return new FiaMatrix(position.scale(-1),
				new Vec3d(rightVector.x,upVector.x,-frontVector.x),
				new Vec3d(rightVector.y,upVector.y,-frontVector.y),
				new Vec3d(-rightVector.z,-upVector.z,frontVector.z)
		);*/
		return new FiaMatrix(new Vec3d(0,0,0),
				new Vec3d(rightVector.x,upVector.x,-frontVector.x),
				new Vec3d(rightVector.y,upVector.y,-frontVector.y),
				new Vec3d(-rightVector.z,-upVector.z,frontVector.z)
		).travel(new FiaMatrix(position.scale(-1)));
	}

	////////////////////////////////////////////// TECHNICAL FEATURES //////////////////////////////////////////////
	enum AxesHelper {
		X,Y,Z,_X,_Y,_Z;
		final int sign;
		AxesHelper() { sign = this.ordinal() >= 3 ? -1 : 1; }
		AxesHelper getAbsolute() { return AxesHelper.values()[Math.floorMod(this.ordinal(),3)]; }
		AxesHelper getOpposite() { return AxesHelper.values()[Math.floorMod(this.ordinal()+3,6)]; }
		AxesHelper getAtanPrimary() {
			switch(getAbsolute()) {
				case X: return AxesHelper._Z;
				case Y: case Z: return AxesHelper.X;
				default: throw new LeafiaDevFlaw("how");
			}
		}
		AxesHelper getAtanSecondary() {
			switch(getAbsolute()) {
				case X: case Z: return AxesHelper.Y;
				case Y: return AxesHelper._Z;
				default: throw new LeafiaDevFlaw("how");
			}
		}
		Vec3d getCorrespondingVector(FiaMatrix matrix) {
			switch(getAbsolute()) {
				case X: return matrix.rightVector;
				case Y: return matrix.upVector;
				case Z: return matrix.frontVector;
				default: throw new LeafiaDevFlaw("how");
			}
		}
		double getValue(Vec3d vector) {
			switch(getAbsolute()) {
				case X: return vector.x*sign;
				case Y: return vector.y*sign;
				case Z: return vector.z*sign;
				default: throw new LeafiaDevFlaw("how");
			}
		}
	}
	double getAngle(AxesHelper demandedAxis,AxesHelper pivotAxis) {
		/*AxesHelper secondaryAxis = null;
		for (int i = 0; secondaryAxis == null; i++) {
			if (i >= 3) throw new LeafiaDevFlaw("how!!");
			AxesHelper ax = AxesHelper.values()[i];
			if (demandedAxis != ax && pivotAxis != ax)
				secondaryAxis = ax;
		}*/ // wasn't necessary
		AxesHelper atanPrimary = demandedAxis.getAtanPrimary();
		AxesHelper atanSecondary;
		// if pivot already matches the template directions, good to go
		if (atanPrimary.getAbsolute() == pivotAxis) atanSecondary = demandedAxis.getAtanSecondary();
		else {
			// otherwise, rotate it to match
			atanPrimary = demandedAxis.getAtanSecondary();
			atanSecondary = demandedAxis.getAtanPrimary().getOpposite();
		}
		return Math.atan2(
				atanSecondary.getValue(pivotAxis.getCorrespondingVector(this)),
				atanPrimary.getValue(pivotAxis.getCorrespondingVector(this))
		);
	}
	TupleRotation getRotation(RotationOrder order) {
		TupleRotation tuple = new TupleRotation();
		tuple.order = order;
		FiaMatrix copy = new FiaMatrix(this);
		for (int i : order.order) {
			AxesHelper axis = AxesHelper.values()[i];
			AxesHelper pivot = AxesHelper.values()[order.order[2]];
			if (pivot == axis) pivot = AxesHelper.values()[order.order[0]];
			double angle = copy.getAngle(axis,pivot);
			switch(i) {
				case 0: copy = new FiaMatrix().radian_rotateX(-angle).travel(copy); tuple.angleX = Math.toDegrees(angle); break;
				case 1: copy = new FiaMatrix().radian_rotateY(-angle).travel(copy); tuple.angleY = Math.toDegrees(angle); break;
				case 2: copy = new FiaMatrix().radian_rotateZ(-angle).travel(copy); tuple.angleZ = Math.toDegrees(angle); break;
				default: throw new LeafiaDevFlaw("how!!!!!!");
			}
		}
		return tuple;
	}

	public FiaMatrix toWorldSpace(FiaMatrix other) {
		return travel(other);
	}
	public FiaMatrix toObjectSpace(FiaMatrix other) {
		return inverse().travel(other);
	}
}
