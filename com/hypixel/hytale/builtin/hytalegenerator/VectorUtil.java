package com.hypixel.hytale.builtin.hytalegenerator;

import com.hypixel.hytale.builtin.hytalegenerator.framework.math.Calculator;
import com.hypixel.hytale.math.vector.Vector2d;
import com.hypixel.hytale.math.vector.Vector2i;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.doubles.DoubleObjectPair;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javax.annotation.Nonnull;

public class VectorUtil {
   public static boolean areasOverlap(@Nonnull Vector3d minA, @Nonnull Vector3d maxA, @Nonnull Vector3d minB, @Nonnull Vector3d maxB) {
      return isAnyGreater(maxA, minB) && isAnySmaller(minA, maxB);
   }

   public static double distanceToSegment3d(@Nonnull Vector3d point, @Nonnull Vector3d p0, @Nonnull Vector3d p1) {
      Vector3d lineVec = p1.clone().addScaled(p0, -1.0);
      Vector3d pointVec = point.clone().addScaled(p0, -1.0);
      double lineLength = lineVec.length();
      Vector3d lineUnitVec = lineVec.clone().setLength(1.0);
      Vector3d pointVecScaled = pointVec.clone().scale(1.0 / lineLength);
      double t = lineUnitVec.dot(pointVecScaled);
      t = Calculator.clamp(0.0, t, 1.0);
      Vector3d nearestPoint = lineVec.clone().scale(t);
      return nearestPoint.distanceTo(pointVec);
   }

   public static double distanceToLine3d(
      @Nonnull Vector3d point,
      @Nonnull Vector3d p0,
      @Nonnull Vector3d p1,
      @Nonnull Vector3d rLineVec,
      @Nonnull Vector3d rPointVec,
      @Nonnull Vector3d rLineUnitVec,
      @Nonnull Vector3d rPointVecScaled,
      @Nonnull Vector3d rNearestPoint
   ) {
      rLineVec.assign(p1).subtract(p0);
      rPointVec.assign(point).subtract(p0);
      double lineLength = rLineVec.length();
      rLineUnitVec.assign(rLineVec).setLength(1.0);
      rPointVecScaled.assign(rPointVec).scale(1.0 / lineLength);
      double t = rLineUnitVec.dot(rPointVecScaled);
      rNearestPoint.assign(rLineVec).scale(t);
      return rNearestPoint.distanceTo(rPointVec);
   }

   @Nonnull
   public static Vector3d nearestPointOnSegment3d(@Nonnull Vector3d point, @Nonnull Vector3d p0, @Nonnull Vector3d p1) {
      Vector3d lineVec = p1.clone().addScaled(p0, -1.0);
      Vector3d pointVec = point.clone().addScaled(p0, -1.0);
      double lineLength = lineVec.length();
      Vector3d lineUnitVec = lineVec.clone().setLength(1.0);
      Vector3d pointVecScaled = pointVec.clone().scale(1.0 / lineLength);
      double t = lineUnitVec.dot(pointVecScaled);
      t = Calculator.clamp(0.0, t, 1.0);
      Vector3d nearestPoint = lineVec.clone().scale(t);
      return nearestPoint.add(p0);
   }

   @Nonnull
   public static void nearestPointOnLine3d(
      @Nonnull Vector3d point,
      @Nonnull Vector3d p0,
      @Nonnull Vector3d p1,
      @Nonnull Vector3d vector_out,
      @Nonnull Vector3d rLineVec,
      @Nonnull Vector3d rPointVec,
      @Nonnull Vector3d rLineUnitVec,
      @Nonnull Vector3d rPointVecScaled
   ) {
      rLineVec.assign(p1).subtract(p0);
      rPointVec.assign(point).subtract(p0);
      double lineLength = rLineVec.length();
      rLineUnitVec.assign(rLineVec).setLength(1.0);
      rPointVecScaled.assign(rPointVec).scale(1.0 / lineLength);
      double t = rLineUnitVec.dot(rPointVecScaled);
      vector_out.assign(rLineVec).scale(t);
      vector_out.add(p0);
   }

   public static boolean[] shortestSegmentBetweenTwoSegments(
      @Nonnull Vector3d a0, @Nonnull Vector3d a1, @Nonnull Vector3d b0, @Nonnull Vector3d b1, boolean clamp, @Nonnull Vector3d p0Out, @Nonnull Vector3d p1Out
   ) {
      boolean[] flags = new boolean[2];
      Vector3d A = a1.clone().addScaled(a0, -1.0);
      Vector3d B = b1.clone().addScaled(b0, -1.0);
      double magA = A.length();
      double magB = B.length();
      Vector3d _A = A.clone().scale(1.0 / magA);
      Vector3d _B = B.clone().scale(1.0 / magB);
      Vector3d cross = _A.cross(_B);
      double denom = Math.pow(cross.length(), 2.0);
      if (denom == 0.0) {
         flags[0] = true;
         double d0 = _A.dot(b0.clone().addScaled(a0, -1.0));
         if (clamp) {
            double d1 = _A.dot(b1.clone().addScaled(a0, -1.0));
            if (d0 <= 0.0 && d1 <= 0.0) {
               if (Math.abs(d0) < Math.abs(d1)) {
                  p0Out.assign(a0);
                  p1Out.assign(b0);
                  flags[1] = true;
                  return flags;
               }

               p0Out.assign(a0);
               p1Out.assign(b1);
               flags[1] = true;
               return flags;
            }

            if (d0 >= magA && d1 >= magA) {
               if (Math.abs(d0) < Math.abs(d1)) {
                  p0Out.assign(a1);
                  p1Out.assign(b0);
                  flags[1] = true;
                  return flags;
               }

               p0Out.assign(a1);
               p1Out.assign(b1);
               flags[1] = true;
               return flags;
            }
         }

         return flags;
      } else {
         Vector3d t = b0.clone().addScaled(a0, -1.0);
         double detA = determinant(t, _B, cross);
         double detB = determinant(t, _A, cross);
         double t0 = detA / denom;
         double t1 = detB / denom;
         Vector3d pA = _A.clone().scale(t0).add(a0);
         Vector3d pB = _B.clone().scale(t1).add(b0);
         if (clamp) {
            if (t0 < 0.0) {
               pA = a0.clone();
            } else if (t0 > magA) {
               pA = a1.clone();
            }

            if (t1 < 0.0) {
               pB = b0.clone();
            } else if (t1 > magB) {
               pB = b1.clone();
            }

            if (t0 < 0.0 || t0 > magA) {
               double dot = _B.dot(pA.clone().addScaled(b0, -1.0));
               if (dot < 0.0) {
                  dot = 0.0;
               } else if (dot > magB) {
                  dot = magB;
               }

               pB = b0.clone().add(_B.clone().scale(dot));
            }

            if (t1 < 0.0 || t1 > magA) {
               double dot = _A.dot(pB.clone().addScaled(a0, -1.0));
               if (dot < 0.0) {
                  dot = 0.0;
               } else if (dot > magA) {
                  dot = magA;
               }

               pA = a0.clone().add(_A.clone().scale(dot));
            }
         }

         p0Out.assign(pA);
         p1Out.assign(pB);
         flags[1] = true;
         return flags;
      }
   }

   public static double determinant(@Nonnull Vector3d v1, @Nonnull Vector3d v2) {
      Vector3d crossProduct = v1.cross(v2);
      return crossProduct.length();
   }

   public static double determinant(@Nonnull Vector3d a, @Nonnull Vector3d b, @Nonnull Vector3d c) {
      double det = a.x * b.y * c.z + b.x * c.y * a.z + c.x * a.y * b.z;
      return det - (a.z * b.y * c.x + b.z * c.y * a.x + c.z * a.y * b.x);
   }

   @Nonnull
   public static DoubleObjectPair<Vector3d> distanceAndNearestPointOnSegment3d(@Nonnull Vector3d point, @Nonnull Vector3d p0, @Nonnull Vector3d p1) {
      Vector3d lineVec = p1.clone().addScaled(p0, -1.0);
      Vector3d pointVec = point.clone().addScaled(p0, -1.0);
      double lineLength = lineVec.length();
      Vector3d lineUnitVec = lineVec.clone().setLength(1.0);
      Vector3d pointVecScaled = pointVec.clone().scale(1.0 / lineLength);
      double t = lineUnitVec.dot(pointVecScaled);
      t = Calculator.clamp(0.0, t, 1.0);
      Vector3d nearestPoint = lineVec.clone().scale(t);
      return DoubleObjectPair.of(nearestPoint.distanceTo(pointVec), nearestPoint.add(p0));
   }

   public static double angle(@Nonnull Vector3d a, @Nonnull Vector3d b) {
      double top = a.x * b.x + a.y * b.y + a.z * b.z;
      double bottomLeft = Math.sqrt(a.x * a.x + a.y * a.y + a.z * a.z);
      double bottomRight = Math.sqrt(b.x * b.x + b.y * b.y + b.z * b.z);
      return Math.acos(top / (bottomLeft * bottomRight));
   }

   public static void rotateAroundAxis(@Nonnull Vector3d vec, @Nonnull Vector3d axis, double theta) {
      Vector3d unitAxis = new Vector3d(axis);
      unitAxis.normalize();
      double xPrime = unitAxis.x * (unitAxis.x * vec.x + unitAxis.y * vec.y + unitAxis.z * vec.z) * (1.0 - Math.cos(theta))
         + vec.x * Math.cos(theta)
         + (-unitAxis.z * vec.y + unitAxis.y * vec.z) * Math.sin(theta);
      double yPrime = unitAxis.y * (unitAxis.x * vec.x + unitAxis.y * vec.y + unitAxis.z * vec.z) * (1.0 - Math.cos(theta))
         + vec.y * Math.cos(theta)
         + (unitAxis.z * vec.x - unitAxis.x * vec.z) * Math.sin(theta);
      double zPrime = unitAxis.z * (unitAxis.x * vec.x + unitAxis.y * vec.y + unitAxis.z * vec.z) * (1.0 - Math.cos(theta))
         + vec.z * Math.cos(theta)
         + (-unitAxis.y * vec.x + unitAxis.x * vec.y) * Math.sin(theta);
      vec.x = xPrime;
      vec.y = yPrime;
      vec.z = zPrime;
   }

   public static void rotateVectorByAxisAngle(@Nonnull Vector3d vec, @Nonnull Vector3d axis, double angle) {
      Vector3d crossProd = axis.cross(vec);
      double cosAngle = Math.cos(angle);
      double sinAngle = Math.sin(angle);
      double x = vec.x * cosAngle + crossProd.x * sinAngle + axis.x * axis.dot(vec) * (1.0 - cosAngle);
      double y = vec.y * cosAngle + crossProd.y * sinAngle + axis.y * axis.dot(vec) * (1.0 - cosAngle);
      double z = vec.z * cosAngle + crossProd.z * sinAngle + axis.z * axis.dot(vec) * (1.0 - cosAngle);
      vec.x = x;
      vec.y = y;
      vec.z = z;
   }

   public static boolean isInside(@Nonnull Vector3i point, @Nonnull Vector3i min, @Nonnull Vector3i max) {
      return point.x >= min.x && point.x < max.x && point.y >= min.y && point.y < max.y && point.z >= min.z && point.z < max.z;
   }

   public static boolean isInside(@Nonnull Vector3d point, @Nonnull Vector3d min, @Nonnull Vector3d max) {
      return !isAnySmaller(point, min) && isSmaller(point, max);
   }

   public static boolean isAnySmaller(@Nonnull Vector3d point, @Nonnull Vector3d limit) {
      return point.x < limit.x || point.y < limit.y || point.z < limit.z;
   }

   public static boolean isSmaller(@Nonnull Vector3d point, @Nonnull Vector3d limit) {
      return point.x < limit.x && point.y < limit.y && point.z < limit.z;
   }

   public static boolean isAnyGreater(@Nonnull Vector3d point, @Nonnull Vector3d limit) {
      return point.x > limit.x || point.y > limit.y || point.z > limit.z;
   }

   public static boolean isAnySmaller(@Nonnull Vector3i point, @Nonnull Vector3i limit) {
      return point.x < limit.x || point.y < limit.y || point.z < limit.z;
   }

   public static boolean isAnyGreater(@Nonnull Vector3i point, @Nonnull Vector3i limit) {
      return point.x > limit.x || point.y > limit.y || point.z > limit.z;
   }

   public static boolean isInside(@Nonnull Vector2d point, @Nonnull Vector2d min, @Nonnull Vector2d max) {
      return !isAnySmaller(point, min) && isSmaller(point, max);
   }

   public static boolean isAnySmaller(@Nonnull Vector2d point, @Nonnull Vector2d limit) {
      return point.x < limit.x || point.y < limit.y;
   }

   public static boolean isSmaller(@Nonnull Vector2d point, @Nonnull Vector2d limit) {
      return point.x < limit.x && point.y < limit.y;
   }

   public static boolean isAnyGreater(@Nonnull Vector2d point, @Nonnull Vector2d limit) {
      return point.x > limit.x || point.y > limit.y;
   }

   public static boolean isAnySmaller(@Nonnull Vector2i point, @Nonnull Vector2i limit) {
      return point.x < limit.x || point.y < limit.y;
   }

   public static boolean isSmaller(@Nonnull Vector2i point, @Nonnull Vector2i limit) {
      return point.x < limit.x && point.y < limit.y;
   }

   public static boolean isAnyGreater(@Nonnull Vector2i point, @Nonnull Vector2i limit) {
      return point.x > limit.x || point.y > limit.y;
   }

   @Nonnull
   public static Vector3i fromOperation(@Nonnull Vector3i v1, @Nonnull Vector3i v2, @Nonnull VectorUtil.BiOperation3i operation) {
      return new Vector3i(
         operation.run(v1.x, v2.x, VectorUtil.Retriever.ofIndex(0)),
         operation.run(v1.y, v2.y, VectorUtil.Retriever.ofIndex(1)),
         operation.run(v1.z, v2.z, VectorUtil.Retriever.ofIndex(2))
      );
   }

   @Nonnull
   public static Vector3i fromOperation(@Nonnull VectorUtil.NakedOperation3i operation) {
      return new Vector3i(
         operation.run(VectorUtil.Retriever.ofIndex(0)), operation.run(VectorUtil.Retriever.ofIndex(1)), operation.run(VectorUtil.Retriever.ofIndex(2))
      );
   }

   public static void bitShiftRight(int shift, @Nonnull Vector3i vector) {
      if (shift < 0) {
         throw new IllegalArgumentException("negative shift");
      } else {
         vector.x >>= shift;
         vector.y >>= shift;
         vector.z >>= shift;
         vector.dropHash();
      }
   }

   public static void bitShiftLeft(int shift, @Nonnull Vector3i vector) {
      if (shift < 0) {
         throw new IllegalArgumentException("negative shift");
      } else {
         vector.x <<= shift;
         vector.y <<= shift;
         vector.z <<= shift;
         vector.dropHash();
      }
   }

   @Nonnull
   public static List<Vector2i> orderByDistanceFrom(@Nonnull Vector2i origin, @Nonnull List<Vector2i> vectors) {
      ArrayList<Pair<Double, Vector2i>> distances = new ArrayList<>(vectors.size());

      for (int i = 0; i < vectors.size(); i++) {
         Vector2i vec = vectors.get(i);
         double distance = Calculator.distance(vec.x, vec.y, origin.x, origin.y);
         distances.add(Pair.of(distance, vec));
      }

      distances.sort(Comparator.comparingDouble(Pair::first));
      ArrayList<Vector2i> sorted = new ArrayList<>(distances.size());

      for (Pair<Double, Vector2i> pair : distances) {
         sorted.add((Vector2i)pair.second());
      }

      return sorted;
   }

   @FunctionalInterface
   public interface BiOperation3i {
      int run(int var1, int var2, @Nonnull VectorUtil.Retriever var3);
   }

   @FunctionalInterface
   public interface NakedOperation3i {
      int run(@Nonnull VectorUtil.Retriever var1);
   }

   @FunctionalInterface
   public interface Operation3i {
      int run(int var1, @Nonnull VectorUtil.Retriever var2);
   }

   public static class Retriever {
      private int index;

      public Retriever(int index) {
         this.index = index;
      }

      public int getIndex() {
         return this.index;
      }

      public int from(@Nonnull Vector3i vec) {
         return switch (this.index) {
            case 0 -> vec.x;
            case 1 -> vec.y;
            case 2 -> vec.z;
            default -> throw new IllegalArgumentException();
         };
      }

      public int from(@Nonnull Vector2i vec) {
         return switch (this.index) {
            case 0 -> vec.x;
            case 1 -> vec.y;
            default -> throw new IllegalArgumentException();
         };
      }

      public double from(@Nonnull Vector3d vec) {
         return switch (this.index) {
            case 0 -> vec.x;
            case 1 -> vec.y;
            case 2 -> vec.z;
            default -> throw new IllegalArgumentException();
         };
      }

      public double from(@Nonnull Vector2d vec) {
         return switch (this.index) {
            case 0 -> vec.x;
            case 1 -> vec.y;
            default -> throw new IllegalArgumentException();
         };
      }

      @Nonnull
      public static VectorUtil.Retriever ofIndex(int index) {
         return new VectorUtil.Retriever(index);
      }
   }
}
