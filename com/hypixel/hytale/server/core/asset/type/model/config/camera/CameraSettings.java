package com.hypixel.hytale.server.core.asset.type.model.config.camera;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.protocol.Vector3f;
import com.hypixel.hytale.server.core.codec.ProtocolCodecs;
import com.hypixel.hytale.server.core.io.NetworkSerializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CameraSettings implements NetworkSerializable<com.hypixel.hytale.protocol.CameraSettings> {
   public static final BuilderCodec<CameraSettings> CODEC = BuilderCodec.builder(CameraSettings.class, CameraSettings::new)
      .addField(
         new KeyedCodec<>("PositionOffset", ProtocolCodecs.VECTOR3F),
         (cameraSettings, s) -> cameraSettings.positionOffset = s,
         cameraSettings -> cameraSettings.positionOffset
      )
      .addField(new KeyedCodec<>("Yaw", CameraAxis.CODEC), (cameraSettings, s) -> cameraSettings.yaw = s, cameraSettings -> cameraSettings.yaw)
      .addField(new KeyedCodec<>("Pitch", CameraAxis.CODEC), (cameraSettings, s) -> cameraSettings.pitch = s, cameraSettings -> cameraSettings.pitch)
      .build();
   @Nullable
   protected Vector3f positionOffset;
   protected CameraAxis yaw;
   protected CameraAxis pitch;

   protected CameraSettings() {
   }

   public CameraSettings(Vector3f positionOffset, CameraAxis yaw, CameraAxis pitch) {
      this.positionOffset = positionOffset;
      this.yaw = yaw;
      this.pitch = pitch;
   }

   public CameraSettings(CameraSettings other) {
      this.positionOffset = other.positionOffset != null ? new Vector3f(other.positionOffset) : null;
      this.yaw = other.yaw;
      this.pitch = other.pitch;
   }

   @Nonnull
   public com.hypixel.hytale.protocol.CameraSettings toPacket() {
      com.hypixel.hytale.protocol.CameraSettings packet = new com.hypixel.hytale.protocol.CameraSettings();
      packet.positionOffset = this.positionOffset;
      if (this.yaw != null) {
         packet.yaw = this.yaw.toPacket();
      }

      if (this.pitch != null) {
         packet.pitch = this.pitch.toPacket();
      }

      return packet;
   }

   public Vector3f getPositionOffset() {
      return this.positionOffset;
   }

   public CameraAxis getYaw() {
      return this.yaw;
   }

   public CameraAxis getPitch() {
      return this.pitch;
   }

   public CameraSettings scale(float scale) {
      if (this.positionOffset != null) {
         this.positionOffset.x *= scale;
         this.positionOffset.y *= scale;
         this.positionOffset.z *= scale;
      }

      return this;
   }

   @Nonnull
   @Override
   public String toString() {
      return "CameraSettings{positionOffset=" + this.positionOffset + ", yaw=" + this.yaw + ", pitch=" + this.pitch + "}";
   }

   public CameraSettings clone() {
      return new CameraSettings(this);
   }
}
