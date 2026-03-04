package com.hypixel.hytale.math.shape;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.function.predicate.TriIntObjPredicate;
import com.hypixel.hytale.function.predicate.TriIntPredicate;
import com.hypixel.hytale.math.Axis;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import javax.annotation.Nonnull;

public class Box implements Shape {
   public static final Codec<Box> CODEC = BuilderCodec.builder(Box.class, Box::new)
      .append(new KeyedCodec<>("Min", Vector3d.CODEC), (box, v) -> box.min.assign(v), box -> box.min)
      .add()
      .append(new KeyedCodec<>("Max", Vector3d.CODEC), (box, v) -> box.max.assign(v), box -> box.max)
      .add()
      .validator((box, results) -> {
         if (box.width() <= 0.0) {
            results.fail("Width is <= 0! Given: " + box.width());
         }

         if (box.height() <= 0.0) {
            results.fail("Height is <= 0! Given: " + box.height());
         }

         if (box.depth() <= 0.0) {
            results.fail("Depth is <= 0! Given: " + box.depth());
         }
      })
      .build();
   public static final Box UNIT = new Box(Vector3d.ZERO, Vector3d.ALL_ONES);
   public static final Box ZERO = new Box(Vector3d.ZERO, Vector3d.ZERO);
   @Nonnull
   public final Vector3d min = new Vector3d();
   @Nonnull
   public final Vector3d max = new Vector3d();

   @Nonnull
   public static Box horizontallyCentered(double width, double height, double depth) {
      return new Box(-width / 2.0, 0.0, -depth / 2.0, width / 2.0, height, depth / 2.0);
   }

   public Box() {
   }

   public Box(@Nonnull Box box) {
      this();
      this.min.assign(box.min);
      this.max.assign(box.max);
   }

   public Box(@Nonnull Vector3d min, @Nonnull Vector3d max) {
      this();
      this.min.assign(min);
      this.max.assign(max);
   }

   public Box(double xMin, double yMin, double zMin, double xMax, double yMax, double zMax) {
      this();
      this.min.assign(xMin, yMin, zMin);
      this.max.assign(xMax, yMax, zMax);
   }

   public static Box cube(@Nonnull Vector3d min, double side) {
      return new Box(min.x, min.y, min.z, min.x + side, min.y + side, min.z + side);
   }

   public static Box centeredCube(@Nonnull Vector3d center, double inradius) {
      return new Box(center.x - inradius, center.y - inradius, center.z - inradius, center.x + inradius, center.y + inradius, center.z + inradius);
   }

   @Nonnull
   public Box setMinMax(@Nonnull Vector3d min, @Nonnull Vector3d max) {
      this.min.assign(min);
      this.max.assign(max);
      return this;
   }

   @Nonnull
   public Box setMinMax(@Nonnull double[] min, @Nonnull double[] max) {
      this.min.assign(min);
      this.max.assign(max);
      return this;
   }

   @Nonnull
   public Box setMinMax(@Nonnull float[] min, @Nonnull float[] max) {
      this.min.assign(min);
      this.max.assign(max);
      return this;
   }

   @Nonnull
   public Box setEmpty() {
      this.setMinMax(Double.MAX_VALUE, -Double.MAX_VALUE);
      return this;
   }

   @Nonnull
   public Box setMinMax(double min, double max) {
      this.min.assign(min);
      this.max.assign(max);
      return this;
   }

   @Nonnull
   public Box union(@Nonnull Box bb) {
      if (this.min.x > bb.min.x) {
         this.min.x = bb.min.x;
      }

      if (this.min.y > bb.min.y) {
         this.min.y = bb.min.y;
      }

      if (this.min.z > bb.min.z) {
         this.min.z = bb.min.z;
      }

      if (this.max.x < bb.max.x) {
         this.max.x = bb.max.x;
      }

      if (this.max.y < bb.max.y) {
         this.max.y = bb.max.y;
      }

      if (this.max.z < bb.max.z) {
         this.max.z = bb.max.z;
      }

      return this;
   }

   @Nonnull
   public Box assign(@Nonnull Box other) {
      this.min.assign(other.min);
      this.max.assign(other.max);
      return this;
   }

   @Nonnull
   public Box assign(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
      this.min.assign(minX, minY, minZ);
      this.max.assign(maxX, maxY, maxZ);
      return this;
   }

   @Nonnull
   public Box minkowskiSum(@Nonnull Box bb) {
      this.min.subtract(bb.max);
      this.max.subtract(bb.min);
      return this;
   }

   @Nonnull
   public Box scale(float scale) {
      this.min.scale(scale);
      this.max.scale(scale);
      return this;
   }

   @Nonnull
   public Box normalize() {
      if (this.min.x > this.max.x) {
         double t = this.min.x;
         this.min.x = this.max.x;
         this.max.x = t;
      }

      if (this.min.y > this.max.y) {
         double t = this.min.y;
         this.min.y = this.max.y;
         this.max.y = t;
      }

      if (this.min.z > this.max.z) {
         double t = this.min.z;
         this.min.z = this.max.z;
         this.max.z = t;
      }

      return this;
   }

   @Nonnull
   public Box rotateX(float angleInRadians) {
      this.min.rotateX(angleInRadians);
      this.max.rotateX(angleInRadians);
      return this;
   }

   @Nonnull
   public Box rotateY(float angleInRadians) {
      this.min.rotateY(angleInRadians);
      this.max.rotateY(angleInRadians);
      return this;
   }

   @Nonnull
   public Box rotateZ(float angleInRadians) {
      this.min.rotateZ(angleInRadians);
      this.max.rotateZ(angleInRadians);
      return this;
   }

   @Nonnull
   public Box offset(double x, double y, double z) {
      this.min.add(x, y, z);
      this.max.add(x, y, z);
      return this;
   }

   @Nonnull
   public Box offset(@Nonnull Vector3d pos) {
      this.min.add(pos);
      this.max.add(pos);
      return this;
   }

   @Nonnull
   public Box sweep(@Nonnull Vector3d v) {
      if (v.x < 0.0) {
         this.min.x = this.min.x + v.x;
      } else if (v.x > 0.0) {
         this.max.x = this.max.x + v.x;
      }

      if (v.y < 0.0) {
         this.min.y = this.min.y + v.y;
      } else if (v.y > 0.0) {
         this.max.y = this.max.y + v.y;
      }

      if (v.z < 0.0) {
         this.min.z = this.min.z + v.z;
      } else if (v.z > 0.0) {
         this.max.z = this.max.z + v.z;
      }

      return this;
   }

   @Nonnull
   public Box extend(double extentX, double extentY, double extentZ) {
      this.min.subtract(extentX, extentY, extentZ);
      this.max.add(extentX, extentY, extentZ);
      return this;
   }

   public double width() {
      return this.max.x - this.min.x;
   }

   public double height() {
      return this.max.y - this.min.y;
   }

   public double depth() {
      return this.max.z - this.min.z;
   }

   public double dimension(@Nonnull Axis axis) {
      return switch (axis) {
         case X -> this.width();
         case Y -> this.height();
         case Z -> this.depth();
      };
   }

   public double getThickness() {
      return MathUtil.minValue(this.width(), this.height(), this.depth());
   }

   public double getMaximumThickness() {
      return MathUtil.maxValue(this.width(), this.height(), this.depth());
   }

   public double getVolume() {
      double w = this.width();
      if (w <= 0.0) {
         return 0.0;
      } else {
         double h = this.height();
         if (h <= 0.0) {
            return 0.0;
         } else {
            double d = this.depth();
            return d <= 0.0 ? 0.0 : w * h * d;
         }
      }
   }

   public boolean hasVolume() {
      return this.min.x <= this.max.x && this.min.y <= this.max.y && this.min.z <= this.max.z;
   }

   public boolean isIntersecting(@Nonnull Box other) {
      return !(this.min.x > other.max.x)
         && !(other.min.x > this.max.x)
         && !(this.min.y > other.max.y)
         && !(other.min.y > this.max.y)
         && !(this.min.z > other.max.z)
         && !(other.min.z > this.max.z);
   }

   public boolean isUnitBox() {
      return this.min.equals(Vector3d.ZERO) && this.max.equals(Vector3d.ALL_ONES);
   }

   public double middleX() {
      return (this.min.x + this.max.x) / 2.0;
   }

   public double middleY() {
      return (this.min.y + this.max.y) / 2.0;
   }

   public double middleZ() {
      return (this.min.z + this.max.z) / 2.0;
   }

   @Nonnull
   public Box clone() {
      Box box = new Box();
      box.assign(this);
      return box;
   }

   @Nonnull
   public Vector3d getMin() {
      return this.min;
   }

   @Nonnull
   public Vector3d getMax() {
      return this.max;
   }

   @Nonnull
   @Override
   public Box getBox(double x, double y, double z) {
      return new Box(this.min.getX() + x, this.min.getY() + y, this.min.getZ() + z, this.max.getX() + x, this.max.getY() + y, this.max.getZ() + z);
   }

   @Override
   public boolean containsPosition(double x, double y, double z) {
      return x >= this.min.getX() && x <= this.max.getX() && y >= this.min.getY() && y <= this.max.getY() && z >= this.min.getZ() && z <= this.max.getZ();
   }

   @Override
   public void expand(double radius) {
      this.extend(radius, radius, radius);
   }

   public boolean containsBlock(int x, int y, int z) {
      int minX = MathUtil.floor(this.min.getX());
      int minY = MathUtil.floor(this.min.getY());
      int minZ = MathUtil.floor(this.min.getZ());
      int maxX = MathUtil.ceil(this.max.getX());
      int maxY = MathUtil.ceil(this.max.getY());
      int maxZ = MathUtil.ceil(this.max.getZ());
      return x >= minX && x < maxX && y >= minY && y < maxY && z >= minZ && z < maxZ;
   }

   public boolean containsBlock(@Nonnull Vector3i origin, int x, int y, int z) {
      return this.containsBlock(x - origin.getX(), y - origin.getY(), z - origin.getZ());
   }

   @Override
   public boolean forEachBlock(double x, double y, double z, double epsilon, @Nonnull TriIntPredicate consumer) {
      int minX = MathUtil.floor(x + this.min.getX() - epsilon);
      int minY = MathUtil.floor(y + this.min.getY() - epsilon);
      int minZ = MathUtil.floor(z + this.min.getZ() - epsilon);
      int maxX = MathUtil.floor(x + this.max.getX() + epsilon);
      int maxY = MathUtil.floor(y + this.max.getY() + epsilon);
      int maxZ = MathUtil.floor(z + this.max.getZ() + epsilon);

      for (int _x = minX; _x <= maxX; _x++) {
         for (int _y = minY; _y <= maxY; _y++) {
            for (int _z = minZ; _z <= maxZ; _z++) {
               if (!consumer.test(_x, _y, _z)) {
                  return false;
               }
            }
         }
      }

      return true;
   }

   @Override
   public <T> boolean forEachBlock(double x, double y, double z, double epsilon, T t, @Nonnull TriIntObjPredicate<T> consumer) {
      int minX = MathUtil.floor(x + this.min.getX() - epsilon);
      int minY = MathUtil.floor(y + this.min.getY() - epsilon);
      int minZ = MathUtil.floor(z + this.min.getZ() - epsilon);
      int maxX = MathUtil.floor(x + this.max.getX() + epsilon);
      int maxY = MathUtil.floor(y + this.max.getY() + epsilon);
      int maxZ = MathUtil.floor(z + this.max.getZ() + epsilon);

      for (int _x = minX; _x <= maxX; _x++) {
         for (int _y = minY; _y <= maxY; _y++) {
            for (int _z = minZ; _z <= maxZ; _z++) {
               if (!consumer.test(_x, _y, _z, t)) {
                  return false;
               }
            }
         }
      }

      return true;
   }

   public double getMaximumExtent() {
      double maximumExtent = 0.0;
      if (-this.min.x > maximumExtent) {
         maximumExtent = -this.min.x;
      }

      if (-this.min.y > maximumExtent) {
         maximumExtent = -this.min.y;
      }

      if (-this.min.z > maximumExtent) {
         maximumExtent = -this.min.z;
      }

      if (this.max.x - 1.0 > maximumExtent) {
         maximumExtent = this.max.x - 1.0;
      }

      if (this.max.y - 1.0 > maximumExtent) {
         maximumExtent = this.max.y - 1.0;
      }

      if (this.max.z - 1.0 > maximumExtent) {
         maximumExtent = this.max.z - 1.0;
      }

      return maximumExtent;
   }

   public boolean intersectsLine(@Nonnull Vector3d start, @Nonnull Vector3d end) {
      Vector3d direction = end.clone().subtract(start);
      double tmin = 0.0;
      double tmax = 1.0;
      if (Math.abs(direction.x) < 1.0E-10) {
         if (start.x < this.min.x || start.x > this.max.x) {
            return false;
         }
      } else {
         double t1 = (this.min.x - start.x) / direction.x;
         double t2 = (this.max.x - start.x) / direction.x;
         if (t1 > t2) {
            double temp = t1;
            t1 = t2;
            t2 = temp;
         }

         tmin = Math.max(tmin, t1);
         tmax = Math.min(tmax, t2);
         if (tmin > tmax) {
            return false;
         }
      }

      if (Math.abs(direction.y) < 1.0E-10) {
         if (start.y < this.min.y || start.y > this.max.y) {
            return false;
         }
      } else {
         double t1x = (this.min.y - start.y) / direction.y;
         double t2x = (this.max.y - start.y) / direction.y;
         if (t1x > t2x) {
            double temp = t1x;
            t1x = t2x;
            t2x = temp;
         }

         tmin = Math.max(tmin, t1x);
         tmax = Math.min(tmax, t2x);
         if (tmin > tmax) {
            return false;
         }
      }

      if (!(Math.abs(direction.z) < 1.0E-10)) {
         double t1xx = (this.min.z - start.z) / direction.z;
         double t2xx = (this.max.z - start.z) / direction.z;
         if (t1xx > t2xx) {
            double temp = t1xx;
            t1xx = t2xx;
            t2xx = temp;
         }

         tmin = Math.max(tmin, t1xx);
         tmax = Math.min(tmax, t2xx);
         return !(tmin > tmax);
      } else {
         return !(start.z < this.min.z) && !(start.z > this.max.z);
      }
   }

   @Nonnull
   @Override
   public String toString() {
      return "Box{min=" + this.min + ", max=" + this.max + "}";
   }
}
