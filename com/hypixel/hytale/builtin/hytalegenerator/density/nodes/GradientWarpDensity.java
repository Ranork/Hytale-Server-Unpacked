package com.hypixel.hytale.builtin.hytalegenerator.density.nodes;

import com.hypixel.hytale.builtin.hytalegenerator.density.Density;
import com.hypixel.hytale.math.vector.Vector3d;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class GradientWarpDensity extends Density {
   private static final double HALF_PI = Math.PI / 2;
   @Nullable
   private Density input;
   @Nullable
   private Density warpInput;
   private final double slopeRange;
   private final double warpFactor;
   @Nonnull
   private final Density.Context rChildContext;
   @Nonnull
   private final Vector3d rGradient;
   @Nonnull
   public final Vector3d rPosition;

   public GradientWarpDensity(@Nonnull Density input, @Nonnull Density warpInput, double slopeRange, double warpFactor) {
      if (slopeRange <= 0.0) {
         throw new IllegalArgumentException();
      } else {
         this.slopeRange = slopeRange;
         this.warpFactor = warpFactor;
         this.input = input;
         this.warpInput = warpInput;
         this.rChildContext = new Density.Context();
         this.rGradient = new Vector3d();
         this.rPosition = new Vector3d();
      }
   }

   @Override
   public double process(@Nonnull Density.Context context) {
      if (this.input == null) {
         return 0.0;
      } else if (this.warpInput == null) {
         return this.input.process(context);
      } else {
         double valueAtOrigin = this.warpInput.process(context);
         double maxX = context.position.x + this.slopeRange;
         double maxY = context.position.y + this.slopeRange;
         double maxZ = context.position.z + this.slopeRange;
         this.rPosition.assign(context.position);
         this.rChildContext.assign(context);
         this.rChildContext.position = this.rPosition;
         this.rChildContext.position.assign(maxX, context.position.y, context.position.z);
         double deltaX = this.warpInput.process(this.rChildContext) - valueAtOrigin;
         this.rChildContext.position.assign(context.position.x, maxY, context.position.z);
         double deltaY = this.warpInput.process(this.rChildContext) - valueAtOrigin;
         this.rChildContext.position.assign(context.position.x, context.position.z, maxZ);
         double deltaZ = this.warpInput.process(this.rChildContext) - valueAtOrigin;
         this.rGradient.assign(deltaX, deltaY, deltaZ);
         this.rGradient.scale(1.0 / this.slopeRange);
         this.rGradient.scale(this.warpFactor);
         this.rGradient.add(context.position.x, context.position.y, context.position.z);
         this.rChildContext.position = this.rGradient;
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
