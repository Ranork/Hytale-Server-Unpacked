package com.hypixel.hytale.builtin.hytalegenerator.density.nodes;

import com.hypixel.hytale.builtin.hytalegenerator.ReusableList;
import com.hypixel.hytale.builtin.hytalegenerator.density.Density;
import com.hypixel.hytale.builtin.hytalegenerator.framework.math.Calculator;
import com.hypixel.hytale.builtin.hytalegenerator.positionproviders.PositionProvider;
import com.hypixel.hytale.math.vector.Vector3d;
import it.unimi.dsi.fastutil.doubles.Double2DoubleFunction;
import javax.annotation.Nonnull;

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
   private final ReusableList<Double> rWarpDistances;
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
      if (maxDistance < 0.0) {
         throw new IllegalArgumentException();
      } else {
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
         this.rWarpDistances = new ReusableList<>();
         this.rWeights = new ReusableList<>();
         this.rPositionsContext = new PositionProvider.Context();
         this.rChildContext = new Density.Context();
      }
   }

   @Override
   public double process(@Nonnull Density.Context context) {
      if (this.input == null) {
         return 0.0;
      } else if (this.positions == null) {
         return this.input.process(context);
      } else {
         if (this.cache.x == context.position.x && this.cache.z == context.position.z && !this.cache.hasValue) {
            this.rWarpVector.assign(this.cache.warpVector);
         } else {
            this.calculateWarpVector(context, this.rWarpVector);
            this.cache.warpVector = this.rWarpVector;
         }

         this.rPosition.assign(this.rWarpVector.x + context.position.x, this.rWarpVector.y + context.position.y, this.rWarpVector.z + context.position.z);
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

   private void consumer(@Nonnull Vector3d iteratedPosition) {
      double distance = Calculator.distance(iteratedPosition.x, iteratedPosition.z, this.rSamplePoint.x, this.rSamplePoint.z);
      if (!(distance > this.maxDistance)) {
         double normalizedDistance = distance / this.maxDistance;
         this.rConsumerResult.assign(iteratedPosition).subtract(this.rSamplePoint);
         this.rConsumerResult.setY(0.0);
         double radialDistance;
         if (this.distanceNormalized) {
            radialDistance = this.pinchCurve.applyAsDouble(normalizedDistance);
            radialDistance *= this.maxDistance;
         } else {
            radialDistance = this.pinchCurve.applyAsDouble(distance);
         }

         if (!(Math.abs(this.rConsumerResult.length()) < 1.0E-9)) {
            this.rConsumerResult.setLength(radialDistance);
         }

         if (this.rWarpVectors.isAtHardCapacity()) {
            this.rWarpVectors.expandAndSet(this.rConsumerResult.clone());
         } else {
            this.rWarpVectors.expandAndGet().assign(this.rConsumerResult);
         }

         this.rWarpDistances.expandAndSet(normalizedDistance);
      }
   }

   public void calculateWarpVector(@Nonnull Density.Context context, @Nonnull Vector3d vector_out) {
      this.rMin.assign(context.position.x - this.maxDistance, this.positionsMinY, context.position.z - this.maxDistance);
      this.rMax.assign(context.position.x + this.maxDistance, this.positionsMaxY, context.position.z + this.maxDistance);
      this.rSamplePoint.assign(context.position);
      this.rWarpVectors.clear();
      this.rWarpDistances.clear();
      this.rPositionsContext.minInclusive = this.rMin;
      this.rPositionsContext.maxExclusive = this.rMax;
      this.rPositionsContext.consumer = this::consumer;
      this.positions.positionsIn(this.rPositionsContext);
      if (this.rWarpVectors.getSoftSize() == 0) {
         vector_out.assign(0.0, 0.0, 0.0);
      } else if (this.rWarpVectors.getSoftSize() == 1) {
         vector_out.assign(this.rWarpVectors.get(0));
      } else {
         int possiblePointsSize = this.rWarpVectors.getSoftSize();
         this.rWeights.clear();
         double totalWeight = 0.0;

         for (int i = 0; i < possiblePointsSize; i++) {
            double distance = this.rWarpDistances.get(i);
            double weight = 1.0 - distance;
            this.rWeights.expandAndSet(weight);
            totalWeight += weight;
         }

         for (int i = 0; i < possiblePointsSize; i++) {
            double weight = this.rWeights.get(i) / totalWeight;
            Vector3d warpVector = this.rWarpVectors.get(i);
            warpVector.scale(weight);
            vector_out.add(warpVector);
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
