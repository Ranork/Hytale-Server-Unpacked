package com.hypixel.hytale.math.shape;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.math.vector.Vector2dUtil;
import javax.annotation.Nonnull;
import org.joml.Vector2d;

public class Box2D implements Shape2D {
   public static final BuilderCodec<Box2D> CODEC = BuilderCodec.builder(Box2D.class, Box2D::new)
      .append(new KeyedCodec<>("Min", Vector2dUtil.CODEC), (shape, min) -> shape.min.set(min), shape -> shape.min)
      .add()
      .append(new KeyedCodec<>("Max", Vector2dUtil.CODEC), (shape, max) -> shape.max.set(max), shape -> shape.max)
      .add()
      .build();
   @Nonnull
   public final Vector2d min = new Vector2d();
   @Nonnull
   public final Vector2d max = new Vector2d();

   public Box2D() {
   }

   public Box2D(@Nonnull Box2D box) {
      this();
      this.min.set(box.min);
      this.max.set(box.max);
   }

   public Box2D(@Nonnull Vector2d min, @Nonnull Vector2d max) {
      this();
      this.min.set(min);
      this.max.set(max);
   }

   public Box2D(double xMin, double yMin, double xMax, double yMax) {
      this();
      this.min.set(xMin, yMin);
      this.max.set(xMax, yMax);
   }

   @Nonnull
   public Box2D setMinMax(@Nonnull Vector2d min, @Nonnull Vector2d max) {
      this.min.set(min);
      this.max.set(max);
      return this;
   }

   @Nonnull
   public Box2D setMinMax(@Nonnull double[] min, @Nonnull double[] max) {
      this.min.set(min);
      this.max.set(max);
      return this;
   }

   @Nonnull
   public Box2D setMinMax(@Nonnull float[] min, @Nonnull float[] max) {
      this.min.set(min);
      this.max.set(max);
      return this;
   }

   @Nonnull
   public Box2D setEmpty() {
      this.setMinMax(Double.MAX_VALUE, -Double.MAX_VALUE);
      return this;
   }

   @Nonnull
   public Box2D setMinMax(double min, double max) {
      this.min.set(min);
      this.max.set(max);
      return this;
   }

   @Nonnull
   public Box2D union(@Nonnull Box2D bb) {
      if (this.min.x > bb.min.x) {
         this.min.x = bb.min.x;
      }

      if (this.min.y > bb.min.y) {
         this.min.y = bb.min.y;
      }

      if (this.max.x < bb.max.x) {
         this.max.x = bb.max.x;
      }

      if (this.max.y < bb.max.y) {
         this.max.y = bb.max.y;
      }

      return this;
   }

   @Nonnull
   public Box2D assign(@Nonnull Box2D other) {
      this.min.set(other.min);
      this.max.set(other.max);
      return this;
   }

   @Nonnull
   public Box2D minkowskiSum(@Nonnull Box2D bb) {
      this.min.sub(bb.max);
      this.max.sub(bb.min);
      return this;
   }

   @Nonnull
   public Box2D normalize() {
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

      return this;
   }

   @Nonnull
   public Box2D offset(@Nonnull Vector2d pos) {
      this.min.add(pos);
      this.max.add(pos);
      return this;
   }

   @Nonnull
   public Box2D sweep(@Nonnull Vector2d v) {
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

      return this;
   }

   @Nonnull
   public Box2D extendToInt() {
      this.min.floor();
      this.max.ceil();
      return this;
   }

   @Nonnull
   public Box2D extend(double extentX, double extentY) {
      this.min.sub(extentX, extentY);
      this.max.add(extentX, extentY);
      return this;
   }

   public double width() {
      return this.max.x - this.min.x;
   }

   public double height() {
      return this.max.y - this.min.y;
   }

   public boolean isIntersecting(@Nonnull Box2D other) {
      return !(this.min.x > other.max.x) && !(other.min.x > this.max.x) && !(this.min.y > other.max.y) && !(other.min.y > this.max.y);
   }

   @Nonnull
   @Override
   public Box2D getBox(double x, double y) {
      return new Box2D(this.min.x() + x, this.min.y() + y, this.max.x() + x, this.max.y() + y);
   }

   @Override
   public boolean containsPosition(@Nonnull Vector2d origin, @Nonnull Vector2d position) {
      double x = position.x() - origin.x();
      double y = position.y() - origin.y();
      return x >= this.min.x() && x <= this.max.x() && y >= this.min.y() && y <= this.max.y();
   }

   @Override
   public boolean containsPosition(@Nonnull Vector2d origin, double xx, double yy) {
      double x = xx - origin.x();
      double y = yy - origin.y();
      return x >= this.min.x() && x <= this.max.x() && y >= this.min.y() && y <= this.max.y();
   }

   @Nonnull
   @Override
   public String toString() {
      return "Box2D{min=" + this.min + ", max=" + this.max + "}";
   }
}
