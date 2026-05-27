package com.hypixel.hytale.math.iterator;

import com.hypixel.hytale.math.util.TrigMathUtil;
import java.util.Iterator;
import javax.annotation.Nonnull;
import org.joml.Vector3d;
import org.joml.Vector3dc;

public class CircleIterator implements Iterator<Vector3d> {
   private final Vector3dc origin;
   private final int pointTotal;
   private final double radius;
   private final float angleOffset;
   private int pointIndex;

   public CircleIterator(Vector3dc origin, double radius, int pointTotal) {
      this(origin, radius, pointTotal, 0.0F);
   }

   public CircleIterator(Vector3dc origin, double radius, int pointTotal, float angleOffset) {
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
      return new Vector3d(TrigMathUtil.cos(angle) * this.radius + this.origin.x(), this.origin.y(), TrigMathUtil.sin(angle) * this.radius + this.origin.z());
   }
}
