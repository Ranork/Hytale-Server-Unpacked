package com.hypixel.hytale.server.core.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import javax.annotation.Nonnull;

public final class WorldWorldMapConfig extends WorldMapConfig {
   public static final int DEFAULT_VIEW_RADIUS_MIN = 3;
   public static final int DEFAULT_VIEW_RADIUS_MAX = 32;
   public static final float DEFAULT_IMAGE_SCALE = 3.0F;
   public static final float DEFAULT_VIEW_RADIUS_MULTIPLIER = 2.0F;
   public static final float DEFAULT_SCALE = 128.0F;
   public static final float DEFAULT_MIN_SCALE = 32.0F;
   public static final float DEFAULT_MAX_SCALE = 175.0F;
   @Nonnull
   public static final Codec<WorldWorldMapConfig> CODEC = BuilderCodec.builder(
         WorldWorldMapConfig.class, WorldWorldMapConfig::new, WorldMapConfig.ABSTRACT_CODEC
      )
      .build();

   @Override
   public int getDefaultViewRadiusMin() {
      return 3;
   }

   @Override
   public int getDefaultViewRadiusMax() {
      return 32;
   }
}
