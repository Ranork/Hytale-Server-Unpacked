package com.hypixel.hytale.math.vector;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.schema.metadata.ui.UIDisplayMode;
import com.hypixel.hytale.math.Axis;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.util.TrigMathUtil;
import javax.annotation.Nonnull;
import org.joml.Quaterniond;
import org.joml.Quaterniondc;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.joml.Vector3i;

public class Rotation3f implements Rotation3fc {
   @Nonnull
   public static final BuilderCodec<Rotation3f> CODEC = BuilderCodec.builder(Rotation3f.class, () -> new Rotation3f(Float.NaN, Float.NaN, Float.NaN))
      .metadata(UIDisplayMode.COMPACT)
      .appendInherited(new KeyedCodec<>("Pitch", Codec.FLOAT), (o, i) -> o.x = i, o -> Float.isNaN(o.x) ? null : o.x, (o, p) -> o.x = p.x)
      .add()
      .appendInherited(new KeyedCodec<>("Yaw", Codec.FLOAT), (o, i) -> o.y = i, o -> Float.isNaN(o.y) ? null : o.y, (o, p) -> o.y = p.y)
      .add()
      .appendInherited(new KeyedCodec<>("Roll", Codec.FLOAT), (o, i) -> o.z = i, o -> Float.isNaN(o.z) ? null : o.z, (o, p) -> o.z = p.z)
      .add()
      .build();
   public static final Rotation3fc ZERO = new Rotation3f(0.0F, 0.0F, 0.0F);
   public static final Rotation3fc IDENTITY = ZERO;
   public static final Rotation3fc NaN = new Rotation3f(Float.NaN, Float.NaN, Float.NaN);
   public float x;
   public float y;
   public float z;

   public Rotation3f() {
      this(0.0F, 0.0F, 0.0F);
   }

   public Rotation3f(@Nonnull Rotation3fc v) {
      this(v.x(), v.y(), v.z());
   }

   public Rotation3f(float pitch, float yaw, float roll) {
      this.x = pitch;
      this.y = yaw;
      this.z = roll;
   }

   @Override
   public float x() {
      return this.x;
   }

   @Override
   public float pitch() {
      return this.x;
   }

   public void setX(float x) {
      this.x = x;
   }

   public void setPitch(float pitch) {
      this.x = pitch;
   }

   @Override
   public float y() {
      return this.y;
   }

   @Override
   public float yaw() {
      return this.y;
   }

   public void setY(float y) {
      this.y = y;
   }

   public void setYaw(float yaw) {
      this.y = yaw;
   }

   @Override
   public float z() {
      return this.z;
   }

   @Override
   public float roll() {
      return this.z;
   }

   public void setZ(float z) {
      this.z = z;
   }

   public void setRoll(float roll) {
      this.z = roll;
   }

   @Nonnull
   public Rotation3f set(@Nonnull Rotation3fc v) {
      this.x = v.x();
      this.y = v.y();
      this.z = v.z();
      return this;
   }

   @Nonnull
   public Rotation3f set(@Nonnull float[] v) {
      this.x = v[0];
      this.y = v[1];
      this.z = v[2];
      return this;
   }

   @Nonnull
   public Rotation3f set(float x, float y, float z) {
      this.x = x;
      this.y = y;
      this.z = z;
      return this;
   }

   @Nonnull
   public Rotation3f add(@Nonnull Rotation3f v) {
      this.x = this.x + v.x;
      this.y = this.y + v.y;
      this.z = this.z + v.z;
      return this;
   }

   @Nonnull
   public Rotation3f add(float x, float y, float z) {
      this.x += x;
      this.y += y;
      this.z += z;
      return this;
   }

   public void addPitch(float pitch) {
      this.x += pitch;
   }

   public void addYaw(float yaw) {
      this.y += yaw;
   }

   public void addRoll(float roll) {
      this.z += roll;
   }

   @Nonnull
   public Rotation3f sub(@Nonnull Rotation3f v) {
      this.x = this.x - v.x;
      this.y = this.y - v.y;
      this.z = this.z - v.z;
      return this;
   }

   @Nonnull
   public Rotation3f sub(@Nonnull Vector3i v) {
      this.x = this.x - v.x;
      this.y = this.y - v.y;
      this.z = this.z - v.z;
      return this;
   }

   @Nonnull
   public Rotation3f sub(float x, float y, float z) {
      this.x -= x;
      this.y -= y;
      this.z -= z;
      return this;
   }

   public void addRotationOnAxis(@Nonnull Axis axis, int degrees) {
      float rad = (float) (Math.PI / 180.0) * degrees;
      switch (axis) {
         case X:
            this.setPitch(this.pitch() + rad);
            break;
         case Y:
            this.setYaw(this.yaw() + rad);
            break;
         case Z:
            this.setRoll(this.roll() + rad);
      }
   }

   public void flipRotationOnAxis(@Nonnull Axis axis) {
      this.addRotationOnAxis(axis, 180);
   }

   @Nonnull
   public Rotation3f negate() {
      this.x = -this.x;
      this.y = -this.y;
      this.z = -this.z;
      return this;
   }

   @Nonnull
   public Rotation3f mul(float s) {
      this.x *= s;
      this.y *= s;
      this.z *= s;
      return this;
   }

   @Nonnull
   public Rotation3f mul(@Nonnull Rotation3f p) {
      this.x = this.x * p.x;
      this.y = this.y * p.y;
      this.z = this.z * p.z;
      return this;
   }

   public float distanceSquaredTo(float x, float y, float z) {
      float dx = x - this.x;
      float dy = y - this.y;
      float dz = z - this.z;
      return dx * dx + dy * dy + dz * dz;
   }

   public boolean isFinite() {
      return Float.isFinite(this.x) && Float.isFinite(this.y) && Float.isFinite(this.z);
   }

   @Override
   public Quaterniond getQuaternion(Quaterniond dest) {
      return dest.rotationYXZ(this.y, this.x, this.z);
   }

   @Override
   public Rotation3f premul(Quaterniondc q, Rotation3f dest) {
      Quaterniond resultQuat = this.getQuaternion(new Quaterniond());
      Vector3d eulerAngles = resultQuat.premul(q).getEulerAnglesYXZ(new Vector3d());
      return dest.set((float)eulerAngles.x, (float)eulerAngles.y, (float)eulerAngles.z);
   }

   @Override
   public Rotation3f mul(Quaterniondc q, Rotation3f dest) {
      Quaterniond tempQuat = this.getQuaternion(new Quaterniond());
      Quaterniond resultQuat = tempQuat.mul(q);
      Vector3d eulerAngles = resultQuat.getEulerAnglesYXZ(new Vector3d());
      return dest.set((float)eulerAngles.x, (float)eulerAngles.y, (float)eulerAngles.z);
   }

   public Rotation3f premul(Quaterniondc q) {
      return this.premul(q, this);
   }

   public Rotation3f mul(Quaterniondc q) {
      return this.mul(q, this);
   }

   @Override
   public Vector3d transform(Vector3dc vec, Vector3d dest) {
      return vec.rotateZ(this.z, dest).rotateX(this.x).rotateY(this.y);
   }

   @Override
   public Vector3d transform(Vector3d vec) {
      return this.transform(vec, vec);
   }

   @Nonnull
   public Rotation3f clone() {
      return new Rotation3f(this.x, this.y, this.z);
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (obj == null) {
         return false;
      } else if (this.getClass() != obj.getClass()) {
         return false;
      } else {
         Rotation3f other = (Rotation3f)obj;
         if (Float.floatToIntBits(this.x) != Float.floatToIntBits(other.x)) {
            return false;
         } else {
            return Float.floatToIntBits(this.y) != Float.floatToIntBits(other.y) ? false : Float.floatToIntBits(this.z) == Float.floatToIntBits(other.z);
         }
      }
   }

   @Override
   public int hashCode() {
      int prime = 31;
      int result = 1;
      result = 31 * result + Float.floatToIntBits(this.x);
      result = 31 * result + Float.floatToIntBits(this.y);
      return 31 * result + Float.floatToIntBits(this.z);
   }

   @Nonnull
   @Override
   public String toString() {
      return "Rotation3f{x=" + this.x + ", y=" + this.y + ", z=" + this.z + "}";
   }

   @Nonnull
   public static Rotation3f lerp(@Nonnull Rotation3f a, @Nonnull Rotation3f b, float t) {
      return lerpUnclamped(a, b, MathUtil.clamp(t, 0.0F, 1.0F));
   }

   @Nonnull
   public static Rotation3f lerpUnclamped(@Nonnull Rotation3f a, @Nonnull Rotation3f b, float t) {
      return new Rotation3f(a.x + t * (b.x - a.x), a.y + t * (b.y - a.y), a.z + t * (b.z - a.z));
   }

   @Nonnull
   public static Rotation3f lerpAngle(@Nonnull Rotation3fc a, @Nonnull Rotation3fc b, float t) {
      return lerpAngle(a, b, t, new Rotation3f());
   }

   @Nonnull
   public static Rotation3f lerpAngle(@Nonnull Rotation3fc a, @Nonnull Rotation3fc b, float t, @Nonnull Rotation3f target) {
      target.set(MathUtil.lerpAngle(a.x(), b.x(), t), MathUtil.lerpAngle(a.y(), b.y(), t), MathUtil.lerpAngle(a.z(), b.z(), t));
      return target;
   }

   @Nonnull
   public static Rotation3f lookAt(@Nonnull Vector3d relative) {
      return lookAt(relative, new Rotation3f());
   }

   @Nonnull
   public static Rotation3f lookAt(@Nonnull Vector3d relative, @Nonnull Rotation3f result) {
      if (!MathUtil.closeToZero(relative.x) || !MathUtil.closeToZero(relative.z)) {
         float yaw = TrigMathUtil.atan2((float)(-relative.x), (float)(-relative.z));
         result.y = MathUtil.wrapAngle(yaw);
      }

      double length = relative.lengthSquared();
      if (length > 0.0) {
         float pitch = (float) (Math.PI / 2) - (float)Math.acos(relative.y / Math.sqrt(length));
         result.x = MathUtil.clamp(pitch, (float) (-Math.PI / 2) + MathUtil.PITCH_EDGE_PADDING, (float) (Math.PI / 2) - MathUtil.PITCH_EDGE_PADDING);
      }

      return result;
   }
}
