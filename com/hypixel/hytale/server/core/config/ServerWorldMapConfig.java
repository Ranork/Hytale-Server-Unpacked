package com.hypixel.hytale.server.core.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.HytaleServerConfig;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class ServerWorldMapConfig extends WorldMapConfig {
   @Nonnull
   public static final Codec<ServerWorldMapConfig> CODEC = BuilderCodec.builder(
         ServerWorldMapConfig.class, ServerWorldMapConfig::new, WorldMapConfig.ABSTRACT_CODEC
      )
      .build();
   @Nullable
   private transient HytaleServerConfig hytaleServerConfig;

   public ServerWorldMapConfig() {
   }

   public ServerWorldMapConfig(@Nonnull HytaleServerConfig hytaleServerConfig) {
      this.hytaleServerConfig = hytaleServerConfig;
   }

   public void setHytaleServerConfig(@Nonnull HytaleServerConfig hytaleServerConfig) {
      this.hytaleServerConfig = hytaleServerConfig;
   }

   @Override
   public int getDefaultViewRadiusMin() {
      return 1;
   }

   @Override
   public int getDefaultViewRadiusMax() {
      return 512;
   }

   @Override
   public void setViewRadiusMin(int viewRadiusMin) {
      super.setViewRadiusMin(viewRadiusMin);
      if (this.hytaleServerConfig != null) {
         this.hytaleServerConfig.markChanged();
      }
   }

   @Override
   public void setViewRadiusMax(int viewRadiusMax) {
      super.setViewRadiusMax(viewRadiusMax);
      if (this.hytaleServerConfig != null) {
         this.hytaleServerConfig.markChanged();
      }
   }
}
