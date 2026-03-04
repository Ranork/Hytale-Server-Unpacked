package com.hypixel.hytale.builtin.adventure.camera.asset;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.server.core.io.NetworkSerializable;
import java.util.Arrays;
import javax.annotation.Nonnull;

public class CameraShakeConfig implements NetworkSerializable<com.hypixel.hytale.protocol.CameraShakeConfig> {
   @Nonnull
   public static final BuilderCodec<CameraShakeConfig> CODEC = BuilderCodec.builder(CameraShakeConfig.class, CameraShakeConfig::new)
      .appendInherited(new KeyedCodec<>("Duration", Codec.FLOAT), (o, v) -> o.duration = v, o -> o.duration, (o, p) -> o.duration = p.duration)
      .documentation("The time period that the camera will shake at full intensity for")
      .addValidator(Validators.min(0.0F))
      .add()
      .<Float>appendInherited(new KeyedCodec<>("StartTime", Codec.FLOAT), (o, v) -> o.startTime = v, o -> o.startTime, (o, p) -> o.startTime = p.startTime)
      .documentation(
         "The initial time value that the Offset and Rotation noises are sampled from when the camera-shake starts. If absent, the camera-shake uses a continuously incremented time value."
      )
      .add()
      .<EasingConfig>appendInherited(new KeyedCodec<>("EaseIn", EasingConfig.CODEC), (o, v) -> o.easeIn = v, o -> o.easeIn, (o, p) -> o.easeIn = p.easeIn)
      .documentation("The fade-in time and intensity curve for the camera shake")
      .addValidator(Validators.nonNull())
      .add()
      .<EasingConfig>appendInherited(new KeyedCodec<>("EaseOut", EasingConfig.CODEC), (o, v) -> o.easeOut = v, o -> o.easeOut, (o, p) -> o.easeOut = p.easeOut)
      .documentation("The fade-out time and intensity curve for the camera shake")
      .addValidator(Validators.nonNull())
      .add()
      .<CameraShakeConfig.OffsetNoise>appendInherited(
         new KeyedCodec<>("Offset", CameraShakeConfig.OffsetNoise.CODEC), (o, v) -> o.offset = v, o -> o.offset, (o, p) -> o.offset = p.offset
      )
      .documentation("The translational offset motion")
      .add()
      .<CameraShakeConfig.RotationNoise>appendInherited(
         new KeyedCodec<>("Rotation", CameraShakeConfig.RotationNoise.CODEC), (o, v) -> o.rotation = v, o -> o.rotation, (o, p) -> o.rotation = p.rotation
      )
      .documentation("The rotational motion")
      .add()
      .build();
   protected float duration;
   protected Float startTime;
   protected EasingConfig easeIn = EasingConfig.NONE;
   protected EasingConfig easeOut = EasingConfig.NONE;
   protected CameraShakeConfig.OffsetNoise offset = CameraShakeConfig.OffsetNoise.NONE;
   protected CameraShakeConfig.RotationNoise rotation = CameraShakeConfig.RotationNoise.NONE;

   @Nonnull
   public com.hypixel.hytale.protocol.CameraShakeConfig toPacket() {
      boolean continuous = this.startTime == null;
      float startTime = continuous ? 0.0F : this.startTime;
      return new com.hypixel.hytale.protocol.CameraShakeConfig(
         this.duration, startTime, continuous, this.easeIn.toPacket(), this.easeOut.toPacket(), this.offset.toPacket(), this.rotation.toPacket()
      );
   }

   @Nonnull
   @Override
   public String toString() {
      return "CameraShakeConfig{duration="
         + this.duration
         + ", startTime="
         + this.startTime
         + ", easeIn="
         + this.easeIn
         + ", easeOut="
         + this.easeOut
         + ", offset="
         + this.offset
         + ", rotation="
         + this.rotation
         + "}";
   }

   public static class OffsetNoise implements NetworkSerializable<com.hypixel.hytale.protocol.OffsetNoise> {
      @Nonnull
      public static final BuilderCodec<CameraShakeConfig.OffsetNoise> CODEC = BuilderCodec.builder(
            CameraShakeConfig.OffsetNoise.class, CameraShakeConfig.OffsetNoise::new
         )
         .documentation(
            "The translational offset noise sources. Each component's list of noise configurations are summed together to calculate the output value for that component"
         )
         .<NoiseConfig[]>appendInherited(new KeyedCodec<>("X", NoiseConfig.ARRAY_CODEC), (o, v) -> o.x = v, o -> o.x, (o, p) -> o.x = p.x)
         .documentation("The noise used to vary the camera x-offset")
         .add()
         .<NoiseConfig[]>appendInherited(new KeyedCodec<>("Y", NoiseConfig.ARRAY_CODEC), (o, v) -> o.y = v, o -> o.y, (o, p) -> o.y = p.y)
         .documentation("The noise used to vary the camera y-offset")
         .add()
         .<NoiseConfig[]>appendInherited(new KeyedCodec<>("Z", NoiseConfig.ARRAY_CODEC), (o, v) -> o.z = v, o -> o.z, (o, p) -> o.z = p.z)
         .documentation("The noise used to vary the camera z-offset")
         .add()
         .build();
      @Nonnull
      public static final CameraShakeConfig.OffsetNoise NONE = new CameraShakeConfig.OffsetNoise();
      protected NoiseConfig[] x;
      protected NoiseConfig[] y;
      protected NoiseConfig[] z;

      @Nonnull
      public com.hypixel.hytale.protocol.OffsetNoise toPacket() {
         return new com.hypixel.hytale.protocol.OffsetNoise(NoiseConfig.toPacket(this.x), NoiseConfig.toPacket(this.y), NoiseConfig.toPacket(this.z));
      }

      @Nonnull
      @Override
      public String toString() {
         return "OffsetNoise{x="
            + Arrays.toString((Object[])this.x)
            + ", y="
            + Arrays.toString((Object[])this.y)
            + ", z="
            + Arrays.toString((Object[])this.z)
            + "}";
      }
   }

   public static class RotationNoise implements NetworkSerializable<com.hypixel.hytale.protocol.RotationNoise> {
      @Nonnull
      public static final BuilderCodec<CameraShakeConfig.RotationNoise> CODEC = BuilderCodec.builder(
            CameraShakeConfig.RotationNoise.class, CameraShakeConfig.RotationNoise::new
         )
         .documentation(
            "The rotational noise sources. Each component's list of noise configurations are summed together to calculate the output value for that component"
         )
         .<NoiseConfig[]>appendInherited(new KeyedCodec<>("Pitch", NoiseConfig.ARRAY_CODEC), (o, v) -> o.pitch = v, o -> o.pitch, (o, p) -> o.pitch = p.pitch)
         .documentation("The noise used to vary the camera pitch")
         .add()
         .<NoiseConfig[]>appendInherited(new KeyedCodec<>("Yaw", NoiseConfig.ARRAY_CODEC), (o, v) -> o.yaw = v, o -> o.yaw, (o, p) -> o.yaw = p.yaw)
         .documentation("The noise used to vary the camera yaw")
         .add()
         .<NoiseConfig[]>appendInherited(new KeyedCodec<>("Roll", NoiseConfig.ARRAY_CODEC), (o, v) -> o.roll = v, o -> o.roll, (o, p) -> o.roll = p.roll)
         .documentation("The noise used to vary the camera roll")
         .add()
         .build();
      @Nonnull
      public static final CameraShakeConfig.RotationNoise NONE = new CameraShakeConfig.RotationNoise();
      protected NoiseConfig[] pitch;
      protected NoiseConfig[] yaw;
      protected NoiseConfig[] roll;

      @Nonnull
      public com.hypixel.hytale.protocol.RotationNoise toPacket() {
         return new com.hypixel.hytale.protocol.RotationNoise(NoiseConfig.toPacket(this.pitch), NoiseConfig.toPacket(this.yaw), NoiseConfig.toPacket(this.roll));
      }

      @Nonnull
      @Override
      public String toString() {
         return "RotationNoise{pitch="
            + Arrays.toString((Object[])this.pitch)
            + ", yaw="
            + Arrays.toString((Object[])this.yaw)
            + ", roll="
            + Arrays.toString((Object[])this.roll)
            + "}";
      }
   }
}
