package com.hypixel.hytale.builtin.hytalegenerator.density.nodes;

import com.hypixel.hytale.builtin.hytalegenerator.ReusableList;
import com.hypixel.hytale.builtin.hytalegenerator.density.Density;
import com.hypixel.hytale.builtin.hytalegenerator.pipe.Control;
import com.hypixel.hytale.builtin.hytalegenerator.positionproviders.PositionProvider;
import it.unimi.dsi.fastutil.doubles.Double2DoubleFunction;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.joml.Vector3d;

public class PositionsPinchDensity extends Density {
   @Nullable
   private Density input;
   @Nullable
   private final PositionProvider positions;
   @Nonnull
   private final Double2DoubleFunction pinchCurve;
   private final double maxDistance;
   private final boolean distanceNormalized;
   @Nonnull
   private final Vector3d rMin;
   @Nonnull
   private final Vector3d rMax;
   @Nonnull
   private final Vector3d rSamplePoint;
   @Nonnull
   private final Vector3d rWarpVector;
   @Nonnull
   private final ReusableList<Vector3d> rWarpVectors;
   @Nonnull
   private final ReusableList<Double> rNormalizedDistances;
   @Nonnull
   private final ReusableList<Double> rWeights;
   @Nonnull
   private final Density.Context rChildContext;
   @Nonnull
   private final PositionProvider.Context rPositionsContext;

   public PositionsPinchDensity(
      @Nullable Density input, @Nullable PositionProvider positions, @Nonnull Double2DoubleFunction pinchCurve, double maxDistance, boolean distanceNormalized
   ) {
      assert maxDistance >= 0.0;

      this.input = input;
      this.positions = positions;
      this.pinchCurve = pinchCurve;
      this.maxDistance = maxDistance;
      this.distanceNormalized = distanceNormalized;
      this.rMin = new Vector3d();
      this.rMax = new Vector3d();
      this.rSamplePoint = new Vector3d();
      this.rWarpVector = new Vector3d();
      this.rWarpVectors = new ReusableList<>();
      this.rNormalizedDistances = new ReusableList<>();
      this.rWeights = new ReusableList<>();
      this.rChildContext = new Density.Context();
      this.rPositionsContext = new PositionProvider.Context();
   }

   private void pipe(@Nonnull Vector3d p, @Nonnull Control control) {
      double distance = p.distance(this.rSamplePoint);
      if (!(distance > this.maxDistance)) {
         double normalizedDistance = distance / this.maxDistance;
         this.rWarpVector.set(p).sub(this.rSamplePoint);
         double radialDistance;
         if (this.distanceNormalized) {
            radialDistance = this.pinchCurve.applyAsDouble(normalizedDistance);
            radialDistance *= this.maxDistance;
         } else {
            radialDistance = this.pinchCurve.applyAsDouble(distance);
         }

         if (!(Math.abs(this.rWarpVector.length()) < 1.0E-9)) {
            this.rWarpVector.normalize(radialDistance);
            if (this.rWarpVectors.isAtHardCapacity()) {
               this.rWarpVectors.expandAndSet(new Vector3d(this.rWarpVector));
            } else {
               this.rWarpVectors.expandAndGet().set(this.rWarpVector);
            }

            this.rNormalizedDistances.expandAndSet(normalizedDistance);
         }
      }
   }

   @Override
   public double process(@Nonnull Density.Context context) {
      if (this.input == null) {
         return 0.0;
      } else if (this.positions == null) {
         return this.input.process(context);
      } else {
         this.rMin.set(context.position.x - this.maxDistance, context.position.y - this.maxDistance, context.position.z - this.maxDistance);
         this.rMax.set(context.position.x + this.maxDistance, context.position.y + this.maxDistance, context.position.z + this.maxDistance);
         this.rSamplePoint.set(context.position);
         this.rWarpVectors.clear();
         this.rNormalizedDistances.clear();
         this.rPositionsContext.bounds.min.set(this.rMin);
         this.rPositionsContext.bounds.max.set(this.rMax);
         this.rPositionsContext.pipe = this::pipe;
         this.positions.generate(this.rPositionsContext);
         if (this.rWarpVectors.getSoftSize() == 0) {
            return this.input.process(context);
         } else {
            this.rChildContext.assign(context);
            this.rChildContext.position = this.rSamplePoint;
            if (this.rWarpVectors.getSoftSize() == 1) {
               Vector3d warpVector = this.rWarpVectors.get(0);
               this.rSamplePoint.add(warpVector);
               return this.input.process(this.rChildContext);
            } else {
               int possiblePointsSize = this.rWarpVectors.getSoftSize();
               this.rWeights.clear();
               double totalWeight = 0.0;

               for (int i = 0; i < possiblePointsSize; i++) {
                  double normalizedDistance = this.rNormalizedDistances.get(i);
                  double weight = 1.0 - normalizedDistance;
                  this.rWeights.expandAndSet(weight);
                  totalWeight += weight;
               }

               for (int i = 0; i < possiblePointsSize; i++) {
                  double weight = this.rWeights.get(i) / totalWeight;
                  Vector3d warpVector = this.rWarpVectors.get(i);
                  warpVector.mul(weight);
                  this.rSamplePoint.add(warpVector);
               }

               return this.input.process(this.rChildContext);
            }
         }
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
