package com.hypixel.hytale.server.core.asset.type.item.config;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.protocol.ItemPullbackConfiguration;
import com.hypixel.hytale.protocol.Vector3f;
import com.hypixel.hytale.server.core.io.NetworkSerializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ItemPullbackConfig implements NetworkSerializable<ItemPullbackConfiguration> {
   public static final BuilderCodec<ItemPullbackConfig> CODEC = BuilderCodec.builder(ItemPullbackConfig.class, ItemPullbackConfig::new)
      .append(
         new KeyedCodec<>("LeftOffsetOverride", Vector3d.AS_ARRAY_CODEC),
         (pullbackConfig, offOverride) -> pullbackConfig.leftOffsetOverride = offOverride == null
            ? null
            : new Vector3f((float)offOverride.getX(), (float)offOverride.getY(), (float)offOverride.getZ()),
         pullbackConfig -> pullbackConfig.leftOffsetOverride == null
            ? null
            : new Vector3d(pullbackConfig.leftOffsetOverride.x, pullbackConfig.leftOffsetOverride.y, pullbackConfig.leftOffsetOverride.z)
      )
      .add()
      .append(
         new KeyedCodec<>("LeftRotationOverride", Vector3d.AS_ARRAY_CODEC),
         (pullbackConfig, rotOverride) -> pullbackConfig.leftRotationOverride = rotOverride == null
            ? null
            : new Vector3f((float)rotOverride.getX(), (float)rotOverride.getY(), (float)rotOverride.getZ()),
         pullbackConfig -> pullbackConfig.leftRotationOverride == null
            ? null
            : new Vector3d(pullbackConfig.leftRotationOverride.x, pullbackConfig.leftRotationOverride.y, pullbackConfig.leftRotationOverride.z)
      )
      .add()
      .append(
         new KeyedCodec<>("RightOffsetOverride", Vector3d.AS_ARRAY_CODEC),
         (pullbackConfig, offOverride) -> pullbackConfig.rightOffsetOverride = offOverride == null
            ? null
            : new Vector3f((float)offOverride.getX(), (float)offOverride.getY(), (float)offOverride.getZ()),
         pullbackConfig -> pullbackConfig.rightOffsetOverride == null
            ? null
            : new Vector3d(pullbackConfig.rightOffsetOverride.x, pullbackConfig.rightOffsetOverride.y, pullbackConfig.rightOffsetOverride.z)
      )
      .add()
      .append(
         new KeyedCodec<>("RightRotationOverride", Vector3d.AS_ARRAY_CODEC),
         (pullbackConfig, rotOverride) -> pullbackConfig.rightRotationOverride = rotOverride == null
            ? null
            : new Vector3f((float)rotOverride.getX(), (float)rotOverride.getY(), (float)rotOverride.getZ()),
         pullbackConfig -> pullbackConfig.rightRotationOverride == null
            ? null
            : new Vector3d(pullbackConfig.rightRotationOverride.x, pullbackConfig.rightRotationOverride.y, pullbackConfig.rightRotationOverride.z)
      )
      .add()
      .build();
   @Nullable
   protected Vector3f leftOffsetOverride;
   @Nullable
   protected Vector3f leftRotationOverride;
   @Nullable
   protected Vector3f rightOffsetOverride;
   @Nullable
   protected Vector3f rightRotationOverride;

   ItemPullbackConfig() {
   }

   public ItemPullbackConfig(Vector3f leftOffsetOverride, Vector3f leftRotationOverride, Vector3f rightOffsetOverride, Vector3f rightRotationOverride) {
      this.leftOffsetOverride = leftOffsetOverride;
      this.leftRotationOverride = leftRotationOverride;
      this.rightOffsetOverride = rightOffsetOverride;
      this.rightRotationOverride = rightRotationOverride;
   }

   @Nonnull
   public ItemPullbackConfiguration toPacket() {
      ItemPullbackConfiguration packet = new ItemPullbackConfiguration();
      packet.leftOffsetOverride = this.leftOffsetOverride;
      packet.leftRotationOverride = this.leftRotationOverride;
      packet.rightOffsetOverride = this.rightOffsetOverride;
      packet.rightRotationOverride = this.rightRotationOverride;
      return packet;
   }
}
