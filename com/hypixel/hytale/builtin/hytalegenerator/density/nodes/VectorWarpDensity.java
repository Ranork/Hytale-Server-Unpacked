package com.hypixel.hytale.builtin.hytalegenerator.density.nodes;

import com.hypixel.hytale.builtin.hytalegenerator.density.Density;
import com.hypixel.hytale.math.vector.Vector3d;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class VectorWarpDensity extends Density {
   @Nullable
   private Density input;
   @Nullable
   private Density warpInput;
   private final double warpFactor;
   @Nonnull
   private final Vector3d warpVector;
   @Nonnull
   private final Vector3d rSamplePoint;
   @Nonnull
   private final Density.Context rChildContext;

   public VectorWarpDensity(@Nonnull Density input, @Nonnull Density warpInput, double warpFactor, @Nonnull Vector3d warpVector) {
      this.input = input;
      this.warpInput = warpInput;
      this.warpFactor = warpFactor;
      this.warpVector = warpVector;
      this.rSamplePoint = new Vector3d();
      this.rChildContext = new Density.Context();
   }

   @Override
   public double process(@Nonnull Density.Context context) {
      if (this.input == null) {
         return 0.0;
      } else if (this.warpInput == null) {
         return this.input.process(context);
      } else {
         double warp = this.warpInput.process(context);
         warp *= this.warpFactor;
         this.rSamplePoint.assign(this.warpVector);
         this.rSamplePoint.setLength(1.0);
         this.rSamplePoint.scale(warp);
         this.rSamplePoint.add(context.position);
         this.rChildContext.assign(context);
         this.rChildContext.position = this.rSamplePoint;
         return this.input.process(this.rChildContext);
      }
   }

   @Override
   public void setInputs(@Nonnull Density[] inputs) {
      if (inputs.length == 0) {
         this.input = null;
      }

      this.input = inputs[0];
      if (inputs.length < 2) {
         this.warpInput = null;
      }

      this.warpInput = inputs[1];
   }
}
