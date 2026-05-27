package com.hypixel.hytale.builtin.triggervolumes.shape;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.lookup.CodecMapCodec;
import javax.annotation.Nonnull;
import org.joml.Vector3d;

public abstract class TriggerVolumeShape {
   @Nonnull
   public static final CodecMapCodec<TriggerVolumeShape> CODEC = new CodecMapCodec<>("Type");
   @Nonnull
   public static final BuilderCodec<TriggerVolumeShape> BASE_CODEC = BuilderCodec.abstractBuilder(TriggerVolumeShape.class).build();

   public abstract boolean contains(@Nonnull Vector3d var1, @Nonnull Vector3d var2);

   public abstract double getBoundingRadius();

   public abstract double getMaxDistanceFromOrigin();

   public abstract void getWorldAABB(@Nonnull Vector3d var1, @Nonnull Vector3d var2, @Nonnull Vector3d var3);

   public abstract void rotateInPlace(float var1);

   @Nonnull
   public abstract TriggerVolumeShape copy();

   static {
      CODEC.register("Box", BoxShape.class, BoxShape.CODEC);
      CODEC.register("Sphere", SphereShape.class, SphereShape.CODEC);
      CODEC.register("Cylinder", CylinderShape.class, CylinderShape.CODEC);
   }
}
