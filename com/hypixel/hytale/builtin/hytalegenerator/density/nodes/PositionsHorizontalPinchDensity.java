package com.hypixel.hytale.builtin.hytalegenerator.density.nodes;

import com.hypixel.hytale.builtin.hytalegenerator.ReusableList;
import com.hypixel.hytale.builtin.hytalegenerator.density.Density;
import com.hypixel.hytale.builtin.hytalegenerator.math.Calculator;
import com.hypixel.hytale.builtin.hytalegenerator.pipe.Control;
import com.hypixel.hytale.builtin.hytalegenerator.positionproviders.PositionProvider;
import it.unimi.dsi.fastutil.doubles.Double2DoubleFunction;
import javax.annotation.Nonnull;
import org.joml.Vector3d;
import org.joml.Vector3dc;

public class PositionsHorizontalPinchDensity extends Density {
   @Nonnull
   private Density input;
   @Nonnull
   private final PositionProvider positions;
   @Nonnull
   private final Double2DoubleFunction pinchCurve;
   @Nonnull
   private final PositionsHorizontalPinchDensity.Cache cache;
   private final double maxDistance;
   private final boolean distanceNormalized;
   private final double positionsMinY;
   private final double positionsMaxY;
   @Nonnull
   private final Vector3d rWarpVector;
   @Nonnull
   private final Vector3d rSamplePoint;
   @Nonnull
   private final Vector3d rMin;
   @Nonnull
   private final Vector3d rMax;
   @Nonnull
   private final Vector3d rPosition;
   @Nonnull
   private final Vector3d rConsumerResult;
   @Nonnull
   private final ReusableList<Vector3d> rWarpVectors;
   @Nonnull
   private final ReusableList<Double> rNormalizedDistances;
   @Nonnull
   private final ReusableList<Double> rWeights;
   @Nonnull
   private final PositionProvider.Context rPositionsContext;
   @Nonnull
   private final Density.Context rChildContext;

   public PositionsHorizontalPinchDensity(
      @Nonnull Density input,
      @Nonnull PositionProvider positions,
      @Nonnull Double2DoubleFunction pinchCurve,
      double maxDistance,
      boolean distanceNormalized,
      double positionsMinY,
      double positionsMaxY
   ) {
      assert maxDistance >= 0.0;

      if (positionsMinY > positionsMaxY) {
         positionsMinY = positionsMaxY;
      }

      this.input = input;
      this.positions = positions;
      this.pinchCurve = pinchCurve;
      this.maxDistance = maxDistance;
      this.distanceNormalized = distanceNormalized;
      this.positionsMinY = positionsMinY;
      this.positionsMaxY = positionsMaxY;
      this.cache = new PositionsHorizontalPinchDensity.Cache();
      this.rWarpVector = new Vector3d();
      this.rSamplePoint = new Vector3d();
      this.rMin = new Vector3d();
      this.rMax = new Vector3d();
      this.rPosition = new Vector3d();
      this.rConsumerResult = new Vector3d();
      this.rWarpVectors = new ReusableList<>();
      this.rNormalizedDistances = new ReusableList<>();
      this.rWeights = new ReusableList<>();
      this.rPositionsContext = new PositionProvider.Context();
      this.rChildContext = new Density.Context();
   }

   @Override
   public double process(@Nonnull Density.Context context) {
      if (this.input == null) {
         return 0.0;
      } else if (this.positions == null) {
         return this.input.process(context);
      } else {
         if (this.cache.x == context.position.x && this.cache.z == context.position.z && !this.cache.hasValue) {
            this.rWarpVector.set(this.cache.warpVector);
         } else {
            this.calculateWarpVector(context, this.rWarpVector);
            this.cache.warpVector = this.rWarpVector;
         }

         this.rPosition.set(this.rWarpVector.x + context.position.x, this.rWarpVector.y + context.position.y, this.rWarpVector.z + context.position.z);
         this.rChildContext.assign(context);
         this.rChildContext.position = this.rPosition;
         return this.input.process(this.rChildContext);
      }
   }

   @Override
   public void setInputs(@Nonnull Density[] inputs) {
      if (inputs.length == 0) {
         this.input = new ConstantValueDensity(0.0);
      }

      this.input = inputs[0];
   }

   private void consumer(@Nonnull Vector3d iteratedPosition, @Nonnull Control control) {
      double distance = Calculator.distance(iteratedPosition.x, iteratedPosition.z, this.rSamplePoint.x, this.rSamplePoint.z);
      if (!(distance > this.maxDistance)) {
         double normalizedDistance = distance / this.maxDistance;
         this.rConsumerResult.set(iteratedPosition).sub(this.rSamplePoint);
         this.rConsumerResult.y = 0.0;
         double radialDistance;
         if (this.distanceNormalized) {
            radialDistance = this.pinchCurve.applyAsDouble(normalizedDistance);
            radialDistance *= this.maxDistance;
         } else {
            radialDistance = this.pinchCurve.applyAsDouble(distance);
         }

         if (!(Math.abs(this.rConsumerResult.length()) < 1.0E-9)) {
            this.rConsumerResult.normalize(radialDistance);
         }

         if (this.rWarpVectors.isAtHardCapacity()) {
            this.rWarpVectors.expandAndSet(new Vector3d(this.rConsumerResult));
         } else {
            this.rWarpVectors.expandAndGet().set(this.rConsumerResult);
         }

         this.rNormalizedDistances.expandAndSet(normalizedDistance);
      }
   }

   public void calculateWarpVector(@Nonnull Density.Context context, @Nonnull Vector3d vector_out) {
      this.rMin.set(context.position.x - this.maxDistance, this.positionsMinY, context.position.z - this.maxDistance);
      this.rMax.set(context.position.x + this.maxDistance, this.positionsMaxY, context.position.z + this.maxDistance);
      this.rSamplePoint.set(context.position);
      this.rWarpVectors.clear();
      this.rNormalizedDistances.clear();
      this.rPositionsContext.bounds.min.set(this.rMin);
      this.rPositionsContext.bounds.max.set(this.rMax);
      this.rPositionsContext.pipe = this::consumer;
      this.positions.generate(this.rPositionsContext);
      vector_out.set(0.0, 0.0, 0.0);
      if (this.rWarpVectors.getSoftSize() != 0) {
         if (this.rWarpVectors.getSoftSize() == 1) {
            vector_out.set((Vector3dc)this.rWarpVectors.get(0));
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
               vector_out.add(warpVector);
            }
         }
      }
   }

   private static class Cache {
      double x;
      double z;
      Vector3d warpVector;
      boolean hasValue;
   }
}
