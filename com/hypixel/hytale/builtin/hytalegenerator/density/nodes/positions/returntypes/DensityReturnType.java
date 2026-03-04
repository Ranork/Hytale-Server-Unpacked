package com.hypixel.hytale.builtin.hytalegenerator.density.nodes.positions.returntypes;

import com.hypixel.hytale.builtin.hytalegenerator.density.Density;
import com.hypixel.hytale.math.Range;
import com.hypixel.hytale.math.vector.Vector3d;
import it.unimi.dsi.fastutil.objects.Object2DoubleAVLTreeMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DensityReturnType extends ReturnType {
   @Nonnull
   private final Density choiceDensity;
   private final double defaultValue;
   @Nonnull
   private final double[][] delimiters;
   @Nonnull
   private final Density[] sampleDensities;
   private final boolean calculateDistanceFromWall;
   @Nonnull
   private final Vector3d rScaledSamplePointClone;
   @Nonnull
   private final Density.Context rChildContext;

   public DensityReturnType(
      @Nonnull Density choiceDensity, @Nonnull Map<Range, Density> densityDelimiters, boolean calculateDistanceFromWall, double defaultValue
   ) {
      this.choiceDensity = choiceDensity;
      this.defaultValue = defaultValue;
      this.calculateDistanceFromWall = calculateDistanceFromWall;
      this.delimiters = new double[densityDelimiters.size()][2];
      this.sampleDensities = new Density[densityDelimiters.size()];
      int i = 0;

      for (Map.Entry<Range, Density> entry : densityDelimiters.entrySet()) {
         this.delimiters[i][0] = entry.getKey().getMin();
         this.delimiters[i][1] = entry.getKey().getMax();
         this.sampleDensities[i] = entry.getValue();
         i++;
      }

      this.rScaledSamplePointClone = new Vector3d();
      this.rChildContext = new Density.Context();
   }

   @Override
   public double get(
      double distance0,
      double distance1,
      @Nonnull Vector3d samplePoint,
      @Nullable Vector3d closestPoint0,
      @Nullable Vector3d closestPoint1,
      @Nullable Density.Context context
   ) {
      double distanceFromWall = Double.MAX_VALUE;
      if (closestPoint0 != null && this.calculateDistanceFromWall) {
         distance0 = this.rScaledSamplePointClone.assign(samplePoint).subtract(closestPoint0).length();
         double fromMaxDistance = Math.abs(super.maxDistance - distance0);
         if (closestPoint1 == null) {
            distanceFromWall = fromMaxDistance;
         } else {
            distance1 = this.rScaledSamplePointClone.assign(samplePoint).subtract(closestPoint1).length();
            double l = distance1 / this.maxDistance;
            double fromOtherCell = Math.abs(distance1 - distance0) / 2.0;
            distanceFromWall = fromOtherCell;
         }
      }

      double choiceValue = this.defaultValue;
      if (closestPoint0 == null) {
         return this.defaultValue;
      } else {
         choiceValue = this.choiceDensity.process(context);
         int i = 0;

         for (double[] delimiter : this.delimiters) {
            if (choiceValue >= delimiter[0] && choiceValue < delimiter[1]) {
               this.rChildContext.assign(context);
               this.rChildContext.densityAnchor = closestPoint0;
               this.rChildContext.distanceFromCellWall = distanceFromWall;
               return this.sampleDensities[i].process(this.rChildContext);
            }

            i++;
         }

         return this.defaultValue;
      }
   }

   private static class Entry {
      @Nonnull
      private final Object2DoubleMap<Vector3d> map;
      @Nonnull
      private final LinkedList<Vector3d> keyHistory;
      private final int size;

      public Entry(int size) {
         if (size < 0) {
            throw new IllegalArgumentException("negative size");
         } else {
            this.map = new Object2DoubleAVLTreeMap(new DensityReturnType.Vector3dComparator());
            this.keyHistory = new LinkedList<>();
            this.size = size;
         }
      }

      public boolean containsKey(Vector3d k) {
         return this.map.containsKey(k);
      }

      public double get(Vector3d k) {
         return this.map.getOrDefault(k, 0.0);
      }

      public void put(Vector3d k, double v) {
         if (this.keyHistory.size() == this.size) {
            Vector3d oldKey = this.keyHistory.removeLast();
            this.map.removeDouble(oldKey);
         }

         this.map.put(k, v);
         this.keyHistory.addFirst(k);
      }
   }

   private static class Vector3dComparator implements Comparator<Vector3d> {
      public int compare(@Nonnull Vector3d o1, @Nonnull Vector3d o2) {
         if (o1.y < o2.y || o1.x < o2.x || o1.z < o2.z) {
            return -1;
         } else {
            return !(o1.y > o2.y) && !(o1.x > o2.x) && !(o1.z > o2.z) ? 0 : 1;
         }
      }
   }
}
