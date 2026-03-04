package com.hypixel.hytale.math.vector;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.schema.metadata.ui.UIDisplayMode;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.math.util.HashUtil;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Vector2l {
   @Nonnull
   public static final BuilderCodec<Vector2l> CODEC = BuilderCodec.builder(Vector2l.class, Vector2l::new)
      .metadata(UIDisplayMode.COMPACT)
      .<Long>appendInherited(new KeyedCodec<>("X", Codec.LONG), (o, i) -> o.x = i, o -> o.x, (o, p) -> o.x = p.x)
      .addValidator(Validators.nonNull())
      .add()
      .<Long>appendInherited(new KeyedCodec<>("Y", Codec.LONG), (o, i) -> o.y = i, o -> o.y, (o, p) -> o.y = p.y)
      .addValidator(Validators.nonNull())
      .add()
      .build();
   public static final Vector2l ZERO = new Vector2l(0L, 0L);
   public static final Vector2l UP = new Vector2l(0L, 1L);
   public static final Vector2l POS_Y = UP;
   public static final Vector2l DOWN = new Vector2l(0L, -1L);
   public static final Vector2l NEG_Y = DOWN;
   public static final Vector2l RIGHT = new Vector2l(1L, 0L);
   public static final Vector2l POS_X = RIGHT;
   public static final Vector2l LEFT = new Vector2l(-1L, 0L);
   public static final Vector2l NEG_X = LEFT;
   public static final Vector2l ALL_ONES = new Vector2l(1L, 1L);
   public static final Vector2l[] DIRECTIONS = new Vector2l[]{UP, DOWN, LEFT, RIGHT};
   public long x;
   public long y;
   private transient int hash;

   public Vector2l() {
      this(0L, 0L);
   }

   public Vector2l(@Nonnull Vector2l v) {
      this(v.x, v.y);
   }

   public Vector2l(long x, long y) {
      this.x = x;
      this.y = y;
      this.hash = 0;
   }

   public long getX() {
      return this.x;
   }

   public void setX(long x) {
      this.x = x;
      this.hash = 0;
   }

   public long getY() {
      return this.y;
   }

   public void setY(long y) {
      this.y = y;
      this.hash = 0;
   }

   @Nonnull
   public Vector2l assign(@Nonnull Vector2l v) {
      this.x = v.x;
      this.y = v.y;
      this.hash = v.hash;
      return this;
   }

   @Nonnull
   public Vector2l assign(long v) {
      this.x = v;
      this.y = v;
      this.hash = 0;
      return this;
   }

   @Nonnull
   public Vector2l assign(@Nonnull long[] v) {
      this.x = v[0];
      this.y = v[1];
      this.hash = 0;
      return this;
   }

   @Nonnull
   public Vector2l assign(long x, long y) {
      this.x = x;
      this.y = y;
      this.hash = 0;
      return this;
   }

   @Nonnull
   public Vector2l add(@Nonnull Vector2l v) {
      this.x = this.x + v.x;
      this.y = this.y + v.y;
      this.hash = 0;
      return this;
   }

   @Nonnull
   public Vector2l add(long x, long y) {
      this.x += x;
      this.y += y;
      this.hash = 0;
      return this;
   }

   @Nonnull
   public Vector2l addScaled(@Nonnull Vector2l v, long s) {
      this.x = this.x + v.x * s;
      this.y = this.y + v.y * s;
      this.hash = 0;
      return this;
   }

   @Nonnull
   public Vector2l subtract(@Nonnull Vector2l v) {
      this.x = this.x - v.x;
      this.y = this.y - v.y;
      this.hash = 0;
      return this;
   }

   @Nonnull
   public Vector2l subtract(long x, long y) {
      this.x -= x;
      this.y -= y;
      this.hash = 0;
      return this;
   }

   @Nonnull
   public Vector2l negate() {
      this.x = -this.x;
      this.y = -this.y;
      this.hash = 0;
      return this;
   }

   @Nonnull
   public Vector2l scale(long s) {
      this.x *= s;
      this.y *= s;
      this.hash = 0;
      return this;
   }

   @Nonnull
   public Vector2l scale(double s) {
      this.x = (long)(this.x * s);
      this.y = (long)(this.y * s);
      this.hash = 0;
      return this;
   }

   @Nonnull
   public Vector2l scale(@Nonnull Vector2l p) {
      this.x = this.x * p.x;
      this.y = this.y * p.y;
      this.hash = 0;
      return this;
   }

   public long dot(@Nonnull Vector2l other) {
      return this.x * other.x + this.y * other.y;
   }

   public double distanceTo(@Nonnull Vector2l v) {
      return Math.sqrt(this.distanceSquaredTo(v));
   }

   public double distanceTo(long x, long y) {
      return Math.sqrt(this.distanceSquaredTo(x, y));
   }

   public long distanceSquaredTo(@Nonnull Vector2l v) {
      long x0 = v.x - this.x;
      long y0 = v.y - this.y;
      return x0 * x0 + y0 * y0;
   }

   public long distanceSquaredTo(long x, long y) {
      long dx = x - this.x;
      long dy = y - this.y;
      return dx * dx + dy * dy;
   }

   @Nonnull
   public Vector2l normalize() {
      return this.setLength(1L);
   }

   public double length() {
      return Math.sqrt(this.squaredLength());
   }

   public long squaredLength() {
      return this.x * this.x + this.y * this.y;
   }

   @Nonnull
   public Vector2l setLength(long newLen) {
      return this.scale(newLen / this.length());
   }

   @Nonnull
   public Vector2l clampLength(long maxLength) {
      double length = this.length();
      return maxLength > length ? this : this.scale(maxLength / length);
   }

   @Nonnull
   public Vector2l dropHash() {
      this.hash = 0;
      return this;
   }

   @Nonnull
   public Vector2l clone() {
      return new Vector2l(this.x, this.y);
   }

   @Override
   public boolean equals(@Nullable Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         Vector2l vector2l = (Vector2l)o;
         return vector2l.x == this.x && vector2l.y == this.y;
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      if (this.hash == 0) {
         this.hash = (int)HashUtil.hash(this.x, this.y);
      }

      return this.hash;
   }

   @Nonnull
   @Override
   public String toString() {
      return "Vector2l{x=" + this.x + ", y=" + this.y + "}";
   }

   @Nonnull
   public static Vector2l max(@Nonnull Vector2l a, @Nonnull Vector2l b) {
      return new Vector2l(Math.max(a.x, b.x), Math.max(a.y, b.y));
   }

   @Nonnull
   public static Vector2l min(@Nonnull Vector2l a, @Nonnull Vector2l b) {
      return new Vector2l(Math.min(a.x, b.x), Math.min(a.y, b.y));
   }
}
