package com.hypixel.hytale.builtin.hytalegenerator.density.nodes;

import com.hypixel.hytale.builtin.hytalegenerator.density.Density;
import com.hypixel.hytale.math.vector.Vector3d;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SliderDensity extends Density {
   private final double slideX;
   private final double slideY;
   private final double slideZ;
   @Nullable
   private Density input;
   @Nonnull
   private final Vector3d rChildPosition;
   @Nonnull
   private final Density.Context rChildContext;

   public SliderDensity(double slideX, double slideY, double slideZ, Density input) {
      this.slideX = slideX;
      this.slideY = slideY;
      this.slideZ = slideZ;
      this.input = input;
      this.rChildPosition = new Vector3d();
      this.rChildContext = new Density.Context();
   }

   @Override
   public double process(@Nonnull Density.Context context) {
      if (this.input == null) {
         return 0.0;
      } else {
         this.rChildPosition.assign(context.position.x - this.slideX, context.position.y - this.slideY, context.position.z - this.slideZ);
         this.rChildContext.assign(context);
         this.rChildContext.position = this.rChildPosition;
         return this.input.process(this.rChildContext);
      }
   }

   @Override
   public void setInputs(@Nonnull Density[] inputs) {
      if (inputs.length == 0) {
         this.input = null;
      }

      this.input = inputs[0];
   }
}
