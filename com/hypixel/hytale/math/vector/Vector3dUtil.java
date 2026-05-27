package com.hypixel.hytale.math.vector;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.schema.metadata.ui.UIDisplayMode;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.math.codec.Vector3dArrayCodec;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.util.TrigMathUtil;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.joml.Vector3i;

public final class Vector3dUtil {
   @Nonnull
   public static final BuilderCodec<Vector3d> CODEC = BuilderCodec.builder(Vector3d.class, Vector3d::new)
      .metadata(UIDisplayMode.COMPACT)
      .<Double>appendInherited(new KeyedCodec<>("X", Codec.DOUBLE), (o, i) -> o.x = i, o -> o.x, (o, p) -> o.x = p.x)
      .addValidator(Validators.nonNull())
      .add()
      .<Double>appendInherited(new KeyedCodec<>("Y", Codec.DOUBLE), (o, i) -> o.y = i, o -> o.y, (o, p) -> o.y = p.y)
      .addValidator(Validators.nonNull())
      .add()
      .<Double>appendInherited(new KeyedCodec<>("Z", Codec.DOUBLE), (o, i) -> o.z = i, o -> o.z, (o, p) -> o.z = p.z)
      .addValidator(Validators.nonNull())
      .add()
      .build();
   @Deprecated
   public static final Vector3dArrayCodec AS_ARRAY_CODEC = new Vector3dArrayCodec();
   public static final Vector3dc ZERO = new Vector3d(0.0, 0.0, 0.0);
   public static final Vector3dc UP = new Vector3d(0.0, 1.0, 0.0);
   public static final Vector3dc POS_Y = UP;
   public static final Vector3dc DOWN = new Vector3d(0.0, -1.0, 0.0);
   public static final Vector3dc NEG_Y = DOWN;
   public static final Vector3dc FORWARD = new Vector3d(0.0, 0.0, -1.0);
   public static final Vector3dc NEG_Z = FORWARD;
   public static final Vector3dc NORTH = FORWARD;
   public static final Vector3dc BACKWARD = new Vector3d(0.0, 0.0, 1.0);
   public static final Vector3dc POS_Z = BACKWARD;
   public static final Vector3dc SOUTH = BACKWARD;
   public static final Vector3dc RIGHT = new Vector3d(1.0, 0.0, 0.0);
   public static final Vector3dc POS_X = RIGHT;
   public static final Vector3dc EAST = RIGHT;
   public static final Vector3dc LEFT = new Vector3d(-1.0, 0.0, 0.0);
   public static final Vector3dc NEG_X = LEFT;
   public static final Vector3dc WEST = LEFT;
   public static final Vector3dc ALL_ONES = new Vector3d(1.0, 1.0, 1.0);
   public static final Vector3dc MIN = new Vector3d(-Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE);
   public static final Vector3dc MAX = new Vector3d(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE);

   private Vector3dUtil() {
   }

   public static Vector3d setYawPitch(double yaw, double pitch, @Nonnull Vector3d dest) {
      float len = TrigMathUtil.cos(pitch);
      float x = len * -TrigMathUtil.sin(yaw);
      float y = TrigMathUtil.sin(pitch);
      float z = len * -TrigMathUtil.cos(yaw);
      return dest.set(x, y, z);
   }

   @Nonnull
   public static Vector3d directionTo(@Nonnull Vector3dc from, @Nonnull Vector3dc to) {
      return new Vector3d(to).sub(from).normalize();
   }

   @Nonnull
   public static String formatShortString(@Nullable Vector3dc v) {
      return v == null ? "" : v.x() + "/" + v.y() + "/" + v.z();
   }

   public static Vector3i toVector3i(@Nonnull Vector3d v) {
      return new Vector3i(MathUtil.floor(v.x), MathUtil.floor(v.y), MathUtil.floor(v.z));
   }

   public static Vector3d clipToZero(Vector3d vec, double epsilon) {
      vec.x = MathUtil.clipToZero(vec.x, epsilon);
      vec.y = MathUtil.clipToZero(vec.y, epsilon);
      vec.z = MathUtil.clipToZero(vec.z, epsilon);
      return vec;
   }

   public static boolean closeToZero(@Nonnull Vector3d vec, double epsilon) {
      return MathUtil.closeToZero(vec.x, epsilon) && MathUtil.closeToZero(vec.y, epsilon) && MathUtil.closeToZero(vec.z, epsilon);
   }
}
