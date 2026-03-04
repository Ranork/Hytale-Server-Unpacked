package com.hypixel.hytale.builtin.hytalegenerator.framework.math;

import com.hypixel.hytale.math.vector.Vector3d;
import java.util.Objects;
import javax.annotation.Nonnull;

public class Calculator {
   public static int toIntFloored(double d) {
      d = Math.floor(d);
      return (int)d;
   }

   public static boolean perfectDiv(int x, int divisor) {
      return x % divisor == 0;
   }

   public static double max(@Nonnull double... n) {
      Objects.requireNonNull(n);
      if (n.length <= 0) {
         throw new IllegalArgumentException("array can't be empty");
      } else {
         double max = Double.NEGATIVE_INFINITY;

         for (double value : n) {
            if (max < value) {
               max = value;
            }
         }

         return max;
      }
   }

   public static double min(@Nonnull double... n) {
      Objects.requireNonNull(n);
      if (n.length <= 0) {
         throw new IllegalArgumentException("array can't be empty");
      } else {
         double min = Double.POSITIVE_INFINITY;

         for (double value : n) {
            if (min > value) {
               min = value;
            }
         }

         return min;
      }
   }

   public static int max(@Nonnull int... n) {
      Objects.requireNonNull(n);
      if (n.length <= 0) {
         throw new IllegalArgumentException("array can't be empty");
      } else {
         int max = Integer.MIN_VALUE;

         for (int value : n) {
            if (max < value) {
               max = value;
            }
         }

         return max;
      }
   }

   public static int min(@Nonnull int... n) {
      Objects.requireNonNull(n);
      if (n.length <= 0) {
         throw new IllegalArgumentException("array can't be empty");
      } else {
         int min = Integer.MAX_VALUE;

         for (int value : n) {
            if (min > value) {
               min = value;
            }
         }

         return min;
      }
   }

   public static int limit(int value, int floor, int ceil) {
      if (floor >= ceil) {
         throw new IllegalArgumentException("floor must be smaller than ceil");
      } else {
         return value < floor ? floor : Math.min(value, ceil);
      }
   }

   public static double limit(double value, double floor, double ceil) {
      if (floor >= ceil) {
         throw new IllegalArgumentException("floor must be smaller than ceil");
      } else if (value < floor) {
         return floor;
      } else {
         return value > ceil ? ceil : value;
      }
   }

   public static double distance(double x1, double y1, double z1, double x2, double y2, double z2) {
      return Math.sqrt(Math.pow(x2 - x1, 2.0) + Math.pow(y2 - y1, 2.0) + Math.pow(z2 - z1, 2.0));
   }

   public static double distance(@Nonnull Vector3d a, @Nonnull Vector3d b) {
      return Math.sqrt(Math.pow(b.x - a.x, 2.0) + Math.pow(b.y - a.y, 2.0) + Math.pow(b.z - a.z, 2.0));
   }

   public static double distance(double x1, double y1, double x2, double y2) {
      return Math.sqrt(Math.pow(x2 - x1, 2.0) + Math.pow(y2 - y1, 2.0));
   }

   public static boolean isDivisibleBy(int number, int divisor) {
      if (number == 0) {
         return false;
      } else {
         while (number != 1) {
            if (number % 4 != 0) {
               return false;
            }

            number >>= 2;
         }

         return true;
      }
   }

   public static double clamp(double wallA, double value, double wallB) {
      double floor;
      double ceil;
      if (wallA > wallB) {
         ceil = wallA;
         floor = wallB;
      } else {
         if (!(wallA < wallB)) {
            return wallA;
         }

         ceil = wallB;
         floor = wallA;
      }

      if (value < floor) {
         value = floor;
      } else if (value > ceil || Double.isInfinite(value)) {
         value = ceil;
      }

      return value;
   }

   public static int clamp(int wallA, int value, int wallB) {
      int floor;
      int ceil;
      if (wallA > wallB) {
         ceil = wallA;
         floor = wallB;
      } else {
         if (wallA >= wallB) {
            return wallA;
         }

         ceil = wallB;
         floor = wallA;
      }

      if (value < floor) {
         value = floor;
      } else if (value > ceil) {
         value = ceil;
      }

      return value;
   }

   public static int toNearestInt(double input) {
      return (int)Math.round(input);
   }

   public static double smoothMin(double range, double a, double b) {
      if (range < 0.0) {
         throw new IllegalArgumentException("negative range");
      } else if (range == 0.0) {
         return Math.min(a, b);
      } else if (Math.abs(a - b) >= range) {
         return Math.min(a, b);
      } else {
         double weight = clamp(0.0, 0.5 + 0.5 * (b - a) / range, 1.0);
         return Interpolation.linear(b, a, weight) - range * weight * (1.0 - weight);
      }
   }

   public static double smoothMax(double range, double a, double b) {
      if (range < 0.0) {
         throw new IllegalArgumentException("negative range");
      } else if (range == 0.0) {
         return Math.max(a, b);
      } else if (Math.abs(a - b) > range) {
         return Math.max(a, b);
      } else {
         double weight = clamp(0.0, 0.5 + 0.5 * (b - a) / range, 1.0);
         return Interpolation.linear(a, b, weight) + range * weight * (1.0 - weight);
      }
   }

   public static int wrap(int value, int max) {
      value %= max;
      return value < 0 ? value + max : value;
   }

   public static int floor(int value, int gridSize) {
      return value < 0 ? value / gridSize * gridSize - (value % gridSize != 0 ? gridSize : 0) : value / gridSize * gridSize;
   }

   public static int ceil(int value, int gridSize) {
      return value >= 0 ? (value + gridSize - 1) / gridSize * gridSize : value / gridSize * gridSize;
   }
}
