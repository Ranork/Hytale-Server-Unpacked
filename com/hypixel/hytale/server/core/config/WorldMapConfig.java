package com.hypixel.hytale.server.core.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class WorldMapConfig {
   public static final int ABSOLUTE_MAX_VIEW_RADIUS = 512;
   @Nonnull
   public static final BuilderCodec<WorldMapConfig> ABSTRACT_CODEC = BuilderCodec.abstractBuilder(WorldMapConfig.class)
      .append(new KeyedCodec<>("ViewRadiusMin", Codec.INTEGER), (o, i) -> o.viewRadiusMin = i, o -> o.viewRadiusMin)
      .addValidator(Validators.greaterThanOrEqual(0))
      .add()
      .<Integer>append(new KeyedCodec<>("ViewRadiusMax", Codec.INTEGER), (o, i) -> o.viewRadiusMax = i, o -> o.viewRadiusMax)
      .addValidator(Validators.range(0, 512))
      .add()
      .afterDecode(config -> validate(config, 512))
      .build();
   @Nullable
   protected Integer viewRadiusMin;
   @Nullable
   protected Integer viewRadiusMax;

   public abstract int getDefaultViewRadiusMin();

   public abstract int getDefaultViewRadiusMax();

   public int getViewRadiusMin() {
      return this.viewRadiusMin != null ? this.viewRadiusMin : this.getDefaultViewRadiusMin();
   }

   public void setViewRadiusMin(int viewRadiusMin) {
      this.viewRadiusMin = viewRadiusMin;
   }

   public int getViewRadiusMax() {
      return this.viewRadiusMax != null ? this.viewRadiusMax : this.getDefaultViewRadiusMax();
   }

   public void setViewRadiusMax(int viewRadiusMax) {
      this.viewRadiusMax = viewRadiusMax;
   }

   protected static void validate(WorldMapConfig config, int ceiling) {
      int min = config.getViewRadiusMin();
      int max = config.getViewRadiusMax();
      if (min > max) {
         throw new IllegalArgumentException("ViewRadiusMin (" + min + ") must be less than or equal to ViewRadiusMax (" + max + ")");
      } else if (max > ceiling) {
         throw new IllegalArgumentException("ViewRadiusMax (" + max + ") must not exceed " + ceiling);
      }
   }
}
