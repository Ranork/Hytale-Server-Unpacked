package com.hypixel.hytale.math.iterator;

import com.hypixel.hytale.math.util.TrigMathUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import java.util.Iterator;
import javax.annotation.Nonnull;

public class CircleIterator implements Iterator<Vector3d> {
   private final Vector3d origin;
   private final int pointTotal;
   private final double radius;
   private final float angleOffset;
   private int pointIndex;

   public CircleIterator(Vector3d origin, double radius, int pointTotal) {
      this(origin, radius, pointTotal, 0.0F);
   }

   public CircleIterator(Vector3d origin, double radius, int pointTotal, float angleOffset) {
      this.origin = origin;
      this.pointTotal = pointTotal;
      this.angleOffset = angleOffset;
      this.pointIndex = 0;
      this.radius = radius;
   }

   @Override
   public boolean hasNext() {
      return this.pointIndex < this.pointTotal;
   }

   @Nonnull
   public Vector3d next() {
      this.pointIndex++;
      float angle = (float)this.pointIndex / this.pointTotal * (float) (Math.PI * 2) + this.angleOffset;
      return new Vector3d(
         TrigMathUtil.cos(angle) * this.radius + this.origin.getX(), this.origin.getY(), TrigMathUtil.sin(angle) * this.radius + this.origin.getZ()
      );
   }
}
