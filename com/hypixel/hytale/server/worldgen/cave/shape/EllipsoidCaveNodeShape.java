package com.hypixel.hytale.server.worldgen.cave.shape;

import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.procedurallib.supplier.IDoubleRange;
import com.hypixel.hytale.server.worldgen.cave.CaveNodeType;
import com.hypixel.hytale.server.worldgen.cave.CaveType;
import com.hypixel.hytale.server.worldgen.cave.element.CaveNode;
import com.hypixel.hytale.server.worldgen.util.bounds.IWorldBounds;
import java.util.Random;
import javax.annotation.Nonnull;

public class EllipsoidCaveNodeShape extends AbstractCaveNodeShape implements IWorldBounds {
   private final CaveType caveType;
   @Nonnull
   private final Vector3d o;
   private final double rx;
   private final double ry;
   private final double rz;
   private final int lowBoundX;
   private final int lowBoundY;
   private final int lowBoundZ;
   private final int highBoundX;
   private final int highBoundY;
   private final int highBoundZ;

   public EllipsoidCaveNodeShape(CaveType caveType, @Nonnull Vector3d o, double rx, double ry, double rz) {
      this.caveType = caveType;
      this.o = o;
      this.rx = rx;
      this.ry = ry;
      this.rz = rz;
      this.lowBoundX = MathUtil.floor(Math.min(o.x - rx, o.x + rx));
      this.lowBoundY = MathUtil.floor(Math.min(o.y - ry, o.y + ry));
      this.lowBoundZ = MathUtil.floor(Math.min(o.z - rz, o.z + rz));
      this.highBoundX = MathUtil.ceil(Math.max(o.x - rx, o.x + rx));
      this.highBoundY = MathUtil.ceil(Math.max(o.y - ry, o.y + ry));
      this.highBoundZ = MathUtil.ceil(Math.max(o.z - rz, o.z + rz));
   }

   @Nonnull
   @Override
   public Vector3d getStart() {
      return this.o.clone();
   }

   @Nonnull
   @Override
   public Vector3d getEnd() {
      return new Vector3d(this.o.x, this.lowBoundY, this.o.z);
   }

   @Nonnull
   @Override
   public Vector3d getAnchor(@Nonnull Vector3d vector, double tx, double ty, double tz) {
      return CaveNodeShapeUtils.getSphereAnchor(vector, this.o, this.rx, this.ry, this.rz, tx, ty, tz);
   }

   @Nonnull
   @Override
   public IWorldBounds getBounds() {
      return this;
   }

   @Override
   public int getLowBoundX() {
      return this.lowBoundX;
   }

   @Override
   public int getLowBoundZ() {
      return this.lowBoundZ;
   }

   @Override
   public int getHighBoundX() {
      return this.highBoundX;
   }

   @Override
   public int getHighBoundZ() {
      return this.highBoundZ;
   }

   @Override
   public int getLowBoundY() {
      return this.lowBoundY;
   }

   @Override
   public int getHighBoundY() {
      return this.highBoundY;
   }

   @Override
   public boolean shouldReplace(int seed, double x, double z, int y) {
      double fy = y;
      double fx = x - this.o.x;
      fy -= this.o.y;
      double fz = z - this.o.z;
      fx /= this.rx;
      fy /= this.ry;
      fz /= this.rz;
      double t = this.caveType.getHeightRadiusFactor(seed, x, z, y);
      return fx * fx + fy * fy + fz * fz <= t * t;
   }

   @Override
   public double getFloorPosition(int seed, double x, double z) {
      for (int y = this.getLowBoundY(); y < this.o.y; y++) {
         if (this.shouldReplace(seed, x, z, y)) {
            return y - 1;
         }
      }

      return -1.0;
   }

   @Override
   public double getCeilingPosition(int seed, double x, double z) {
      for (int y = this.getHighBoundY(); y > this.o.y; y--) {
         if (this.shouldReplace(seed, x, z, y)) {
            return y + 1;
         }
      }

      return -1.0;
   }

   @Nonnull
   @Override
   public String toString() {
      return "EllipsoidCaveNodeShape{caveType="
         + this.caveType
         + ", o="
         + this.o
         + ", rx="
         + this.rx
         + ", ry="
         + this.ry
         + ", rz="
         + this.rz
         + ", lowBoundX="
         + this.lowBoundX
         + ", lowBoundY="
         + this.lowBoundY
         + ", lowBoundZ="
         + this.lowBoundZ
         + ", highBoundX="
         + this.highBoundX
         + ", highBoundY="
         + this.highBoundY
         + ", highBoundZ="
         + this.highBoundZ
         + "}";
   }

   public static class EllipsoidCaveNodeShapeGenerator implements CaveNodeShapeEnum.CaveNodeShapeGenerator {
      private final IDoubleRange radiusX;
      private final IDoubleRange radiusY;
      private final IDoubleRange radiusZ;

      public EllipsoidCaveNodeShapeGenerator(IDoubleRange radiusX, IDoubleRange radiusY, IDoubleRange radiusZ) {
         this.radiusX = radiusX;
         this.radiusY = radiusY;
         this.radiusZ = radiusZ;
      }

      @Nonnull
      @Override
      public CaveNodeShape generateCaveNodeShape(
         Random random,
         CaveType caveType,
         CaveNode parentNode,
         @Nonnull CaveNodeType.CaveNodeChildEntry childEntry,
         @Nonnull Vector3d origin,
         float yaw,
         float pitch
      ) {
         double rx = this.radiusX.getValue(random);
         double ry = this.radiusY.getValue(random);
         double rz = this.radiusZ.getValue(random);
         Vector3d offset = CaveNodeShapeUtils.getOffset(parentNode, childEntry);
         origin.add(offset);
         return new EllipsoidCaveNodeShape(caveType, origin, rx, ry, rz);
      }
   }
}
