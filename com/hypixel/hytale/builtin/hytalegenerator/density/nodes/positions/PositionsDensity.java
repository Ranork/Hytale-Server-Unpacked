package com.hypixel.hytale.builtin.hytalegenerator.density.nodes.positions;

import com.hypixel.hytale.builtin.hytalegenerator.density.Density;
import com.hypixel.hytale.builtin.hytalegenerator.density.nodes.positions.distancefunctions.DistanceFunction;
import com.hypixel.hytale.builtin.hytalegenerator.density.nodes.positions.returntypes.ReturnType;
import com.hypixel.hytale.builtin.hytalegenerator.positionproviders.PositionProvider;
import com.hypixel.hytale.math.vector.Vector3d;
import it.unimi.dsi.fastutil.doubles.Double2DoubleFunction;
import java.util.function.Consumer;
import javax.annotation.Nonnull;

public class PositionsDensity extends Density {
   @Nonnull
   private final PositionProvider positionProvider;
   private final double maxDistance;
   private final double maxDistanceRaw;
   @Nonnull
   private final ReturnType returnType;
   @Nonnull
   private final DistanceFunction distanceFunction;
   @Nonnull
   private final Vector3d rMin;
   @Nonnull
   private final Vector3d rMax;
   @Nonnull
   private final Vector3d rClosestPoint;
   @Nonnull
   private final Vector3d rPreviousClosestPoint;
   @Nonnull
   private final Vector3d rLocalPoint;
   @Nonnull
   private final double[] rDistance;
   @Nonnull
   private final boolean[] rHasClosestPoint;

   public PositionsDensity(
      @Nonnull PositionProvider positionsField, @Nonnull ReturnType returnType, @Nonnull DistanceFunction distanceFunction, double maxDistance
   ) {
      if (maxDistance < 0.0) {
         throw new IllegalArgumentException("negative distance");
      } else {
         this.positionProvider = positionsField;
         this.maxDistance = maxDistance;
         this.maxDistanceRaw = maxDistance * maxDistance;
         this.returnType = returnType;
         this.distanceFunction = distanceFunction;
         this.rMin = new Vector3d();
         this.rMax = new Vector3d();
         this.rClosestPoint = new Vector3d();
         this.rPreviousClosestPoint = new Vector3d();
         this.rLocalPoint = new Vector3d();
         this.rDistance = new double[2];
         this.rHasClosestPoint = new boolean[2];
      }
   }

   @Nonnull
   public static Double2DoubleFunction cellNoiseDistanceFunction(double maxDistance) {
      return d -> d / maxDistance - 1.0;
   }

   @Override
   public double process(@Nonnull Density.Context context) {
      this.rMin.assign(context.position).subtract(this.maxDistance);
      this.rMax.assign(context.position).add(this.maxDistance);
      this.rDistance[0] = Double.MAX_VALUE;
      this.rDistance[1] = Double.MAX_VALUE;
      this.rHasClosestPoint[0] = false;
      this.rHasClosestPoint[1] = false;
      this.rClosestPoint.assign(0.0, 0.0, 0.0);
      this.rPreviousClosestPoint.assign(0.0, 0.0, 0.0);
      this.rLocalPoint.assign(0.0, 0.0, 0.0);
      Consumer<Vector3d> positionsConsumer = providedPoint -> {
         this.rLocalPoint.x = providedPoint.x - context.position.x;
         this.rLocalPoint.y = providedPoint.y - context.position.y;
         this.rLocalPoint.z = providedPoint.z - context.position.z;
         double newDistance = this.distanceFunction.getDistance(this.rLocalPoint);
         if (!(this.maxDistanceRaw < newDistance)) {
            this.rDistance[1] = Math.max(Math.min(this.rDistance[1], newDistance), this.rDistance[0]);
            if (newDistance < this.rDistance[0]) {
               this.rDistance[0] = newDistance;
               this.rPreviousClosestPoint.assign(this.rClosestPoint);
               this.rClosestPoint.assign(providedPoint);
               this.rHasClosestPoint[1] = this.rHasClosestPoint[0];
               this.rHasClosestPoint[0] = true;
            }
         }
      };
      PositionProvider.Context positionsContext = new PositionProvider.Context();
      positionsContext.minInclusive = this.rMin;
      positionsContext.maxExclusive = this.rMax;
      positionsContext.consumer = positionsConsumer;
      this.positionProvider.positionsIn(positionsContext);
      this.rDistance[0] = Math.sqrt(this.rDistance[0]);
      this.rDistance[1] = Math.sqrt(this.rDistance[1]);
      return this.returnType
         .get(
            this.rDistance[0],
            this.rDistance[1],
            context.position.clone(),
            this.rHasClosestPoint[0] ? this.rClosestPoint : null,
            this.rHasClosestPoint[1] ? this.rPreviousClosestPoint : null,
            context
         );
   }
}
