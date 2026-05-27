package com.hypixel.hytale.server.core.modules.entity.component;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.math.Axis;
import com.hypixel.hytale.math.util.TrigMathUtil;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.math.vector.Rotation3fc;
import com.hypixel.hytale.math.vector.Vector3dUtil;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import org.joml.Vector3d;
import org.joml.Vector3i;

public class HeadRotation implements Component<EntityStore> {
   public static final BuilderCodec<HeadRotation> CODEC = BuilderCodec.builder(HeadRotation.class, HeadRotation::new)
      .append(new KeyedCodec<>("Rotation", Rotation3f.CODEC), (o, i) -> o.rotation.set(i), o -> o.rotation)
      .add()
      .build();
   private final Rotation3f rotation = new Rotation3f();

   public static ComponentType<EntityStore, HeadRotation> getComponentType() {
      return EntityModule.get().getHeadRotationComponentType();
   }

   public HeadRotation() {
   }

   public HeadRotation(@Nonnull Rotation3fc rotation) {
      this.rotation.set(rotation);
   }

   @Nonnull
   public Rotation3f getRotation() {
      return this.rotation;
   }

   public void setRotation(@Nonnull Rotation3fc rotation) {
      this.rotation.set(rotation);
   }

   public Vector3d getDirection() {
      return getDirection(this.rotation.pitch(), this.rotation.yaw(), new Vector3d());
   }

   @Nonnull
   public Vector3i getAxisDirection() {
      return getAxisDirection(this.rotation.pitch(), this.rotation.yaw(), new Vector3i());
   }

   @Nonnull
   public Vector3i getAxisDirection(@Nonnull Vector3i result) {
      return getAxisDirection(this.rotation.pitch(), this.rotation.yaw(), result);
   }

   @Nonnull
   public Vector3i getHorizontalAxisDirection() {
      return getAxisDirection(0.0F, this.rotation.yaw(), new Vector3i());
   }

   @Nonnull
   public Axis getAxis() {
      Vector3i axisDirection = this.getAxisDirection();
      if (axisDirection.x() != 0) {
         return Axis.X;
      } else {
         return axisDirection.y() != 0 ? Axis.Y : Axis.Z;
      }
   }

   @Nonnull
   public static Vector3i getAxisDirection(float pitch, float yaw, @Nonnull Vector3i result) {
      if (Float.isNaN(pitch)) {
         throw new IllegalStateException("Pitch can't be NaN");
      } else if (Float.isNaN(yaw)) {
         throw new IllegalStateException("Yaw can't be NaN");
      } else {
         double len = TrigMathUtil.cos(pitch);
         double x = len * -TrigMathUtil.sin(yaw);
         double y = TrigMathUtil.sin(pitch);
         double z = len * -TrigMathUtil.cos(yaw);
         return result.set((int)Math.round(x), (int)Math.round(y), (int)Math.round(z));
      }
   }

   @Nonnull
   private static Vector3d getDirection(float pitch, float yaw, @Nonnull Vector3d result) {
      if (Float.isNaN(pitch)) {
         throw new IllegalStateException("Pitch can't be NaN");
      } else if (Float.isNaN(yaw)) {
         throw new IllegalStateException("Yaw can't be NaN");
      } else {
         return Vector3dUtil.setYawPitch(yaw, pitch, result);
      }
   }

   public void teleportRotation(@Nonnull Rotation3f rotation) {
      float yaw = rotation.yaw();
      if (!Float.isNaN(yaw)) {
         this.rotation.setYaw(yaw);
      }

      float pitch = rotation.pitch();
      if (!Float.isNaN(pitch)) {
         this.rotation.setPitch(pitch);
      }

      float roll = rotation.roll();
      if (!Float.isNaN(roll)) {
         this.rotation.setRoll(roll);
      }
   }

   @Nonnull
   public HeadRotation clone() {
      return new HeadRotation(this.rotation);
   }
}
