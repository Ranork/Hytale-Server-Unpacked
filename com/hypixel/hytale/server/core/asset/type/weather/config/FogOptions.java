package com.hypixel.hytale.server.core.asset.type.weather.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import javax.annotation.Nullable;

public class FogOptions {
   public static final BuilderCodec<FogOptions> CODEC = BuilderCodec.builder(FogOptions.class, FogOptions::new)
      .appendInherited(
         new KeyedCodec<>("IgnoreFogLimits", Codec.BOOLEAN),
         (opt, s) -> opt.ignoreFogLimits = s,
         opt -> opt.ignoreFogLimits,
         (opt, parent) -> opt.ignoreFogLimits = parent.ignoreFogLimits
      )
      .documentation("The client has a default minimum AND maximum for \"FogFar\". Toggling this on will allow your FogDistance[1] to bypass those limits.")
      .add()
      .<Float>appendInherited(
         new KeyedCodec<>("EffectiveViewDistanceMultiplier", Codec.FLOAT),
         (opt, s) -> opt.effectiveViewDistanceMultiplier = s,
         opt -> opt.effectiveViewDistanceMultiplier,
         (opt, parent) -> opt.effectiveViewDistanceMultiplier = parent.effectiveViewDistanceMultiplier
      )
      .documentation(
         "The client's default cap for FogDistance[1] (aka FogFar) is the effective view distance, meaning the farthest viewable chunk. This value (defaults 1.0) multiplies that cap. For example with high fog density, you can afford a fog multiplier of 1.3 as the cutoff of unloaded chunks may still be hidden."
      )
      .add()
      .<Float>appendInherited(
         new KeyedCodec<>("FogHeightCameraFixed", Codec.FLOAT),
         (opt, s) -> opt.fogHeightCameraFixed = s,
         opt -> opt.fogHeightCameraFixed,
         (opt, parent) -> opt.fogHeightCameraFixed = parent.fogHeightCameraFixed
      )
      .documentation(
         "By default, the client has e^(-FogHeightFalloff * ~Camera.Y) height-based fog. This adds significant fog near Camera.Y = 0. By setting this value (between 0.0 and 1.0), the Exp function is bypassed and there will be a fixed fog for height in the fog shader."
      )
      .add()
      .<Float>appendInherited(
         new KeyedCodec<>("FogHeightCameraOffset", Codec.FLOAT),
         (opt, s) -> opt.fogHeightCameraOffset = s,
         opt -> opt.fogHeightCameraOffset,
         (opt, parent) -> opt.fogHeightCameraOffset = parent.fogHeightCameraOffset
      )
      .documentation(
         "By default, the client has e^(-FogHeightFalloff * ~Camera.Y) height-based fog. This adds significant fog near Camera.Y = 0. The FogHeightCameraOffset is added to the Camera.Y."
      )
      .add()
      .build();
   private boolean ignoreFogLimits = false;
   private float effectiveViewDistanceMultiplier = 1.0F;
   private Float fogHeightCameraFixed = null;
   private float fogHeightCameraOffset = 0.0F;

   public boolean isIgnoreFogLimits() {
      return this.ignoreFogLimits;
   }

   public float getEffectiveViewDistanceMultiplier() {
      return this.effectiveViewDistanceMultiplier;
   }

   @Nullable
   public Float getFogHeightCameraFixed() {
      return this.fogHeightCameraFixed;
   }

   public float getFogHeightCameraOffset() {
      return this.fogHeightCameraOffset;
   }

   public com.hypixel.hytale.protocol.FogOptions toPacket() {
      com.hypixel.hytale.protocol.FogOptions proto = new com.hypixel.hytale.protocol.FogOptions();
      proto.ignoreFogLimits = this.ignoreFogLimits;
      proto.effectiveViewDistanceMultiplier = this.effectiveViewDistanceMultiplier;
      if (this.fogHeightCameraFixed == null) {
         proto.fogHeightCameraOverriden = false;
      } else {
         proto.fogHeightCameraOverriden = true;
         proto.fogHeightCameraFixed = this.fogHeightCameraFixed;
      }

      proto.fogHeightCameraOffset = this.fogHeightCameraOffset;
      return proto;
   }

   @Override
   public String toString() {
      return "FogOptions{ignoreFogLimits="
         + this.ignoreFogLimits
         + ", effectiveViewDistanceMultiplier="
         + this.effectiveViewDistanceMultiplier
         + ", fogHeightCameraFixed="
         + this.fogHeightCameraFixed
         + ", fogHeightCameraOffset="
         + this.fogHeightCameraOffset
         + "}";
   }
}
