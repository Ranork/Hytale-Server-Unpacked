package com.hypixel.hytale.builtin.hytalegenerator.density.nodes.positions.returntypes;

import com.hypixel.hytale.builtin.hytalegenerator.density.Density;
import com.hypixel.hytale.math.vector.Vector3d;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CellValueReturnType extends ReturnType {
   @Nonnull
   private final Density sampleField;
   private final double defaultValue;
   @Nonnull
   private final Density.Context rChildContext;

   public CellValueReturnType(@Nonnull Density sampleField, double defaultValue) {
      this.sampleField = sampleField;
      this.defaultValue = defaultValue;
      this.rChildContext = new Density.Context();
   }

   @Override
   public double get(
      double distance0,
      double distance1,
      @Nonnull Vector3d samplePosition,
      @Nullable Vector3d closestPoint0,
      Vector3d closestPoint1,
      @Nonnull Density.Context context
   ) {
      if (closestPoint0 == null) {
         return this.defaultValue;
      } else {
         this.rChildContext.assign(context);
         this.rChildContext.position = closestPoint0;
         return this.sampleField.process(this.rChildContext);
      }
   }
}
