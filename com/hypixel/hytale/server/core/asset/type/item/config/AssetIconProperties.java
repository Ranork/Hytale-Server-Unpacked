package com.hypixel.hytale.server.core.asset.type.item.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.math.vector.Vector2d;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.protocol.Vector2f;
import com.hypixel.hytale.protocol.Vector3f;
import com.hypixel.hytale.server.core.io.NetworkSerializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AssetIconProperties implements NetworkSerializable<com.hypixel.hytale.protocol.AssetIconProperties> {
   public static final BuilderCodec<AssetIconProperties> CODEC = BuilderCodec.builder(AssetIconProperties.class, AssetIconProperties::new)
      .addField(new KeyedCodec<>("Scale", Codec.DOUBLE), (props, scale) -> props.scale = scale.floatValue(), props -> (double)props.scale)
      .addField(
         new KeyedCodec<>("Translation", Vector2d.AS_ARRAY_CODEC),
         (props, translation) -> props.translation = translation == null ? null : new Vector2f((float)translation.getX(), (float)translation.getY()),
         props -> props.translation == null ? null : new Vector2d(props.translation.x, props.translation.y)
      )
      .addField(
         new KeyedCodec<>("Rotation", Vector3d.AS_ARRAY_CODEC),
         (props, rot) -> props.rotation = rot == null ? null : new Vector3f((float)rot.getX(), (float)rot.getY(), (float)rot.getZ()),
         props -> props.rotation == null ? null : new Vector3d(props.rotation.x, props.rotation.y, props.rotation.z)
      )
      .build();
   private float scale;
   @Nullable
   private Vector2f translation;
   @Nullable
   private Vector3f rotation;

   AssetIconProperties() {
   }

   public AssetIconProperties(float scale, Vector2f translation, Vector3f rotation) {
      this.scale = scale;
      this.translation = translation;
      this.rotation = rotation;
   }

   public float getScale() {
      return this.scale;
   }

   @Nullable
   public Vector2f getTranslation() {
      return this.translation;
   }

   @Nullable
   public Vector3f getRotation() {
      return this.rotation;
   }

   @Nonnull
   public com.hypixel.hytale.protocol.AssetIconProperties toPacket() {
      com.hypixel.hytale.protocol.AssetIconProperties packet = new com.hypixel.hytale.protocol.AssetIconProperties();
      packet.scale = this.scale;
      packet.translation = this.translation;
      packet.rotation = this.rotation;
      return packet;
   }

   @Nonnull
   @Override
   public String toString() {
      return "AssetIconProperties{scale=" + this.scale + ", translation=" + this.translation + ", rotation=" + this.rotation + "}";
   }
}
