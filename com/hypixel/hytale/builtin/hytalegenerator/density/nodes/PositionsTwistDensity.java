package com.hypixel.hytale.builtin.hytalegenerator.density.nodes;

import com.hypixel.hytale.builtin.hytalegenerator.ReusableList;
import com.hypixel.hytale.builtin.hytalegenerator.VectorUtil;
import com.hypixel.hytale.builtin.hytalegenerator.density.Density;
import com.hypixel.hytale.builtin.hytalegenerator.pipe.Control;
import com.hypixel.hytale.builtin.hytalegenerator.positionproviders.PositionProvider;
import it.unimi.dsi.fastutil.doubles.Double2DoubleFunction;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.joml.Vector3d;

public class PositionsTwistDensity extends Density {
   @Nullable
   private Density input;
   @Nullable
   private final PositionProvider positions;
   @Nonnull
   private final Double2DoubleFunction twistCurve;
   @Nonnull
   private final Vector3d twistAxis;
   private final double maxDistance;
   private final boolean distanceNormalized;
   private final boolean zeroPositionsY;
   @Nonnull
   private final Vector3d rMin;
   @Nonnull
   private final Vector3d rMax;
   @Nonnull
   private final Vector3d rSamplePoint;
   @Nonnull
   private final Vector3d rQueryPosition;
   @Nonnull
   private final Vector3d rWarpVector;
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

   public PositionsTwistDensity(
      @Nullable Density input,
      @Nullable PositionProvider positions,
      @Nonnull Double2DoubleFunction twistCurve,
      @Nonnull Vector3d twistAxis,
      double maxDistance,
      boolean distanceNormalized,
      boolean zeroPositionsY
   ) {
      assert maxDistance >= 0.0;

      if (twistAxis.length() < 1.0E-9) {
         twistAxis = new Vector3d(0.0, 1.0, 0.0);
      }

      this.input = input;
      this.positions = positions;
      this.twistCurve = twistCurve;
      this.twistAxis = twistAxis;
      this.maxDistance = maxDistance;
      this.distanceNormalized = distanceNormalized;
      this.zeroPositionsY = zeroPositionsY;
      this.rMin = new Vector3d();
      this.rMax = new Vector3d();
      this.rSamplePoint = new Vector3d();
      this.rQueryPosition = new Vector3d();
      this.rWarpVector = new Vector3d();
      this.rWarpVectors = new ReusableList<>();
      this.rWarpDistances = new ReusableList<>();
      this.rWeights = new ReusableList<>();
      this.rPositionsContext = new PositionProvider.Context();
      this.rChildContext = new Density.Context();
   }

   public void consumer(@Nonnull Vector3d p, @Nonnull Control control) {
      double distance = p.distance(this.rQueryPosition);
      if (!(distance > this.maxDistance)) {
         double normalizedDistance = distance / this.maxDistance;
         this.rWarpVector.set(this.rSamplePoint);
         double twistAngle;
         if (this.distanceNormalized) {
            twistAngle = this.twistCurve.applyAsDouble(normalizedDistance);
         } else {
            twistAngle = this.twistCurve.applyAsDouble(distance);
         }

         twistAngle /= 180.0;
         twistAngle *= Math.PI;
         this.rWarpVector.sub(p);
         VectorUtil.rotateAroundAxis(this.rWarpVector, this.twistAxis, twistAngle);
         this.rWarpVector.add(p);
         this.rWarpVector.sub(this.rSamplePoint);
         if (this.rWarpVectors.isAtHardCapacity()) {
            this.rWarpVectors.expandAndSet(new Vector3d(this.rWarpVector));
         } else {
            this.rWarpVectors.expandAndGet().set(this.rWarpVector);
         }

         this.rWarpDistances.expandAndSet(normalizedDistance);
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
         this.rQueryPosition.set(context.position);
         if (this.zeroPositionsY) {
            this.rQueryPosition.y = 0.0;
            this.rMin.y = -1.0;
            this.rMax.y = 1.0;
         }

         this.rWarpVectors.clear();
         this.rWarpDistances.clear();
         this.rPositionsContext.bounds.min.set(this.rMin);
         this.rPositionsContext.bounds.max.set(this.rMax);
         this.rPositionsContext.pipe = this::consumer;
         this.positions.generate(this.rPositionsContext);
         if (this.rWarpVectors.getSoftSize() == 0) {
            return this.input.process(context);
         } else if (this.rWarpVectors.getSoftSize() == 1) {
            Vector3d warpVector = this.rWarpVectors.get(0);
            this.rSamplePoint.add(warpVector);
            Density.Context childContext = new Density.Context(context);
            childContext.position = this.rSamplePoint;
            return this.input.process(childContext);
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
               warpVector.mul(weight);
               this.rSamplePoint.add(warpVector);
            }

            this.rChildContext.assign(context);
            this.rChildContext.position = this.rSamplePoint;
            return this.input.process(this.rChildContext);
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
