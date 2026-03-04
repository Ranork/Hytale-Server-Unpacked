package com.hypixel.hytale.server.worldgen.cave.shape;

import com.hypixel.hytale.math.util.TrigMathUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.procedurallib.supplier.IDoubleRange;
import com.hypixel.hytale.server.worldgen.cave.CaveNodeType;
import com.hypixel.hytale.server.worldgen.cave.CaveType;
import com.hypixel.hytale.server.worldgen.cave.element.CaveNode;
import com.hypixel.hytale.server.worldgen.util.bounds.IWorldBounds;
import java.util.Random;
import javax.annotation.Nonnull;

public class EmptyLineCaveNodeShape extends AbstractCaveNodeShape implements IWorldBounds {
   private final Vector3d o;
   private final Vector3d v;

   public EmptyLineCaveNodeShape(Vector3d o, Vector3d v) {
      this.o = o;
      this.v = v;
   }

   @Nonnull
   @Override
   public Vector3d getStart() {
      return this.o.clone();
   }

   @Nonnull
   @Override
   public Vector3d getEnd() {
      double x = this.o.x + this.v.x;
      double y = this.o.y + this.v.y;
      double z = this.o.z + this.v.z;
      return new Vector3d(x, y, z);
   }

   @Nonnull
   @Override
   public Vector3d getAnchor(@Nonnull Vector3d vector, double t, double tv, double th) {
      return CaveNodeShapeUtils.getLineAnchor(vector, this.o, this.v, t);
   }

   @Nonnull
   @Override
   public IWorldBounds getBounds() {
      return this;
   }

   @Override
   public int getLowBoundX() {
      return 0;
   }

   @Override
   public int getLowBoundZ() {
      return 0;
   }

   @Override
   public int getHighBoundX() {
      return 0;
   }

   @Override
   public int getHighBoundZ() {
      return 0;
   }

   @Override
   public int getLowBoundY() {
      return 0;
   }

   @Override
   public int getHighBoundY() {
      return 0;
   }

   @Override
   public boolean shouldReplace(int seed, double x, double z, int y) {
      return false;
   }

   @Override
   public double getFloorPosition(int seed, double x, double z) {
      return -1.0;
   }

   @Override
   public double getCeilingPosition(int seed, double x, double z) {
      return -1.0;
   }

   @Override
   public boolean hasGeometry() {
      return false;
   }

   @Nonnull
   @Override
   public String toString() {
      return "EmptyLineCaveNodeShape{o=" + this.o + ", v=" + this.v + "}";
   }

   public static class EmptyLineCaveNodeShapeGenerator implements CaveNodeShapeEnum.CaveNodeShapeGenerator {
      private final IDoubleRange length;

      public EmptyLineCaveNodeShapeGenerator(IDoubleRange length) {
         this.length = length;
      }

      @Nonnull
      @Override
      public CaveNodeShape generateCaveNodeShape(
         @Nonnull Random random, CaveType caveType, CaveNode parentNode, CaveNodeType.CaveNodeChildEntry childEntry, Vector3d origin, float yaw, float pitch
      ) {
         double l = this.length.getValue(random.nextDouble());
         Vector3d direction = new Vector3d(
               TrigMathUtil.sin(pitch) * TrigMathUtil.cos(yaw), TrigMathUtil.cos(pitch), TrigMathUtil.sin(pitch) * TrigMathUtil.sin(yaw)
            )
            .scale(l);
         return new EmptyLineCaveNodeShape(origin, direction);
      }
   }
}
