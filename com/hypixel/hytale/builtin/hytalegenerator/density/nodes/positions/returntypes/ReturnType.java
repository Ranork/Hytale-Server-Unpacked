package com.hypixel.hytale.builtin.hytalegenerator.density.nodes.positions.returntypes;

import com.hypixel.hytale.builtin.hytalegenerator.density.Density;
import com.hypixel.hytale.math.vector.Vector3d;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class ReturnType {
   protected double maxDistance = Double.MAX_VALUE;

   public abstract double get(
      double var1, double var3, @Nonnull Vector3d var5, @Nullable Vector3d var6, @Nullable Vector3d var7, @Nullable Density.Context var8
   );

   public void setMaxDistance(double maxDistance) {
      if (maxDistance < 0.0) {
         throw new IllegalArgumentException("negative distance");
      } else {
         this.maxDistance = maxDistance;
      }
   }
}
