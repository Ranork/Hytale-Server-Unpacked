package com.hypixel.hytale.builtin.hytalegenerator.density.nodes;

import com.hypixel.hytale.builtin.hytalegenerator.VectorUtil;
import com.hypixel.hytale.builtin.hytalegenerator.density.Density;
import com.hypixel.hytale.math.vector.Vector3d;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class GradientDensity extends Density {
   private static final double HALF_PI = Math.PI / 2;
   @Nullable
   private Density input;
   private final double slopeRange;
   @Nonnull
   private final Vector3d axis;
   @Nonnull
   private final Density.Context rChildContext;
   @Nonnull
   private final Vector3d rSlopeDirection;
   @Nonnull
   private final Vector3d rPosition;

   public GradientDensity(@Nonnull Density input, double slopeRange, @Nonnull Vector3d axis) {
      if (slopeRange <= 0.0) {
         throw new IllegalArgumentException();
      } else {
         this.axis = axis.clone();
         this.slopeRange = slopeRange;
         this.input = input;
         this.rChildContext = new Density.Context();
         this.rSlopeDirection = new Vector3d();
         this.rPosition = new Vector3d();
      }
   }

   @Override
   public double process(@Nonnull Density.Context context) {
      if (this.input == null) {
         return 0.0;
      } else {
         this.rPosition.assign(context.position);
         this.rChildContext.assign(context);
         this.rChildContext.position = this.rPosition;
         double valueAtOrigin = this.input.process(this.rChildContext);
         double maxX = context.position.x + this.slopeRange;
         double maxY = context.position.y + this.slopeRange;
         double maxZ = context.position.z + this.slopeRange;
         this.rChildContext.position.assign(maxX, context.position.y, context.position.z);
         double deltaX = Math.abs(this.input.process(this.rChildContext) - valueAtOrigin);
         this.rChildContext.position.assign(context.position.x, maxY, context.position.z);
         double deltaY = Math.abs(this.input.process(this.rChildContext) - valueAtOrigin);
         this.rChildContext.position.assign(context.position.x, context.position.y, maxZ);
         double deltaZ = Math.abs(this.input.process(this.rChildContext) - valueAtOrigin);
         this.rSlopeDirection.assign(deltaX, deltaY, deltaZ);
         double slopeAngle = VectorUtil.angle(this.axis, this.rSlopeDirection);
         if (slopeAngle > Math.PI / 2) {
            slopeAngle = Math.PI - slopeAngle;
         }

         slopeAngle /= Math.PI / 2;
         return slopeAngle * 90.0;
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
