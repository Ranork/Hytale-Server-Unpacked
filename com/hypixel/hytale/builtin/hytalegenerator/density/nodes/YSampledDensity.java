package com.hypixel.hytale.builtin.hytalegenerator.density.nodes;

import com.hypixel.hytale.builtin.hytalegenerator.density.Density;
import com.hypixel.hytale.builtin.hytalegenerator.framework.math.Interpolation;
import com.hypixel.hytale.math.vector.Vector3d;
import javax.annotation.Nonnull;

public class YSampledDensity extends Density {
   @Nonnull
   private Density input;
   private final double sampleDistance;
   private final double sampleDistanceInverse;
   private final double sampleOffset;
   private double value0;
   private double value1;
   private double y0;
   private double y1;
   private double x;
   private double z;
   private boolean isEmpty;
   private final Vector3d rChildPosition;
   private final Density.Context rChildContext;

   public YSampledDensity(@Nonnull Density input, double sampleDistance, double sampleOffset) {
      assert sampleDistance > 0.0;

      this.input = input;
      this.sampleDistance = sampleDistance;
      this.sampleDistanceInverse = 1.0 / sampleDistance;
      this.sampleOffset = sampleOffset;
      this.isEmpty = true;
      this.rChildPosition = new Vector3d();
      this.rChildContext = new Density.Context();
   }

   @Override
   public double process(@Nonnull Density.Context context) {
      if (context.position.x != this.x || context.position.z != this.z || this.isEmpty) {
         double newY0 = this.toY0(context.position.y);
         double newY1 = newY0 + this.sampleDistance;
         this.rChildContext.assign(context);
         this.rChildContext.position = this.rChildPosition;
         this.y0 = newY0;
         this.y1 = newY1;
         this.rChildPosition.assign(context.position.x, this.y0, context.position.z);
         this.value0 = this.input.process(this.rChildContext);
         this.rChildPosition.assign(context.position.x, this.y1, context.position.z);
         this.value1 = this.input.process(this.rChildContext);
         this.isEmpty = false;
         this.x = context.position.x;
         this.z = context.position.z;
      } else if (context.position.y < this.y0 || context.position.y > this.y1) {
         double newY0 = this.toY0(context.position.y);
         double newY1 = newY0 + this.sampleDistance;
         this.rChildContext.assign(context);
         this.rChildContext.position = this.rChildPosition;
         if (newY0 == this.y1) {
            this.y0 = this.y1;
            this.value0 = this.value1;
         } else {
            this.y0 = newY0;
            this.rChildPosition.assign(context.position.x, this.y0, context.position.z);
            this.value0 = this.input.process(this.rChildContext);
         }

         if (newY1 == this.y0) {
            this.y1 = this.y0;
            this.value1 = this.value0;
         } else {
            this.y1 = newY1;
            this.rChildPosition.assign(context.position.x, this.y1, context.position.z);
            this.value1 = this.input.process(this.rChildContext);
         }
      }

      double ratio = (context.position.y - this.y0) * this.sampleDistanceInverse;
      return Interpolation.linear(this.value0, this.value1, ratio);
   }

   private double toY0(double position) {
      return this.toCellGrid(position) * this.sampleDistance + this.sampleOffset;
   }

   private double toCellGrid(double position) {
      return Math.floor((position - this.sampleOffset) * this.sampleDistanceInverse);
   }

   @Override
   public void setInputs(@Nonnull Density[] inputs) {
      assert inputs.length != 0;

      assert inputs[0] != null;

      this.input = inputs[0];
   }
}
