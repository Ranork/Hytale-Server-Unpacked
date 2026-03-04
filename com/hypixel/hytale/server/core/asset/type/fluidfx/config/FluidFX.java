package com.hypixel.hytale.server.core.asset.type.fluidfx.config;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetKeyValidator;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.validation.ValidatorCache;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.protocol.Color;
import com.hypixel.hytale.protocol.FluidFXMovementSettings;
import com.hypixel.hytale.protocol.FluidFog;
import com.hypixel.hytale.protocol.NearFar;
import com.hypixel.hytale.server.core.codec.ProtocolCodecs;
import com.hypixel.hytale.server.core.io.NetworkSerializable;
import java.lang.ref.SoftReference;
import java.util.Arrays;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class FluidFX implements JsonAssetWithMap<String, IndexedLookupTableAssetMap<String, FluidFX>>, NetworkSerializable<com.hypixel.hytale.protocol.FluidFX> {
   public static final AssetBuilderCodec<String, FluidFX> CODEC = AssetBuilderCodec.builder(
         FluidFX.class,
         FluidFX::new,
         Codec.STRING,
         (fluidFX, s) -> fluidFX.id = s,
         fluidFX -> fluidFX.id,
         (asset, data) -> asset.data = data,
         asset -> asset.data
      )
      .appendInherited(
         new KeyedCodec<>("Fog", new EnumCodec<>(FluidFog.class)),
         (fluidFX, fluidFog) -> fluidFX.fog = fluidFog,
         fluidFX -> fluidFX.fog,
         (fluidFX, parent) -> fluidFX.fog = parent.fog
      )
      .addValidator(Validators.nonNull())
      .add()
      .appendInherited(
         new KeyedCodec<>("FogColor", ProtocolCodecs.COLOR),
         (fluidFX, o) -> fluidFX.fogColor = o,
         fluidFX -> fluidFX.fogColor,
         (fluidFX, parent) -> fluidFX.fogColor = parent.fogColor
      )
      .add()
      .<double[]>appendInherited(new KeyedCodec<>("FogDistance", Codec.DOUBLE_ARRAY), (weather, o) -> {
         weather.fogDistance = new float[2];
         weather.fogDistance[0] = (float)o[0];
         weather.fogDistance[1] = (float)o[1];
      }, weather -> new double[]{weather.fogDistance[0], weather.fogDistance[1]}, (weather, parent) -> weather.fogDistance = parent.fogDistance)
      .addValidator(Validators.doubleArraySize(2))
      .add()
      .appendInherited(
         new KeyedCodec<>("FogDepthStart", Codec.DOUBLE),
         (fluidFX, s) -> fluidFX.fogDepthStart = s.floatValue(),
         fluidFX -> (double)fluidFX.fogDepthStart,
         (fluidFX, parent) -> fluidFX.fogDepthStart = parent.fogDepthStart
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("FogDepthFalloff", Codec.DOUBLE),
         (fluidFX, s) -> fluidFX.fogDepthFalloff = s.floatValue(),
         fluidFX -> (double)fluidFX.fogDepthFalloff,
         (fluidFX, parent) -> fluidFX.fogDepthFalloff = parent.fogDepthFalloff
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("ColorsSaturation", Codec.DOUBLE),
         (fluidFX, s) -> fluidFX.colorsSaturation = s.floatValue(),
         fluidFX -> (double)fluidFX.colorsSaturation,
         (fluidFX, parent) -> fluidFX.colorsSaturation = parent.colorsSaturation
      )
      .add()
      .<double[]>appendInherited(
         new KeyedCodec<>("ColorsFilter", Codec.DOUBLE_ARRAY),
         (weather, o) -> {
            weather.colorsFilter = new float[3];
            weather.colorsFilter[0] = (float)o[0];
            weather.colorsFilter[1] = (float)o[1];
            weather.colorsFilter[2] = (float)o[2];
         },
         weather -> new double[]{weather.colorsFilter[0], weather.colorsFilter[1], weather.colorsFilter[2]},
         (weather, parent) -> weather.colorsFilter = parent.colorsFilter
      )
      .addValidator(Validators.doubleArraySize(3))
      .add()
      .appendInherited(
         new KeyedCodec<>("DistortionAmplitude", Codec.DOUBLE),
         (fluidFX, s) -> fluidFX.distortionAmplitude = s.floatValue(),
         fluidFX -> (double)fluidFX.distortionAmplitude,
         (fluidFX, parent) -> fluidFX.distortionAmplitude = parent.distortionAmplitude
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("DistortionFrequency", Codec.DOUBLE),
         (fluidFX, s) -> fluidFX.distortionFrequency = s.floatValue(),
         fluidFX -> (double)fluidFX.distortionFrequency,
         (fluidFX, parent) -> fluidFX.distortionFrequency = parent.distortionFrequency
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("Particle", FluidParticle.CODEC),
         (fluidFX, s) -> fluidFX.particle = s,
         fluidFX -> fluidFX.particle,
         (fluidFX, parent) -> fluidFX.particle = parent.particle
      )
      .add()
      .appendInherited(
         new KeyedCodec<>(
            "MovementSettings",
            BuilderCodec.builder(FluidFXMovementSettings.class, FluidFXMovementSettings::new)
               .append(
                  new KeyedCodec<>("SwimUpSpeed", Codec.DOUBLE),
                  (movementSettings, val) -> movementSettings.swimUpSpeed = val.floatValue(),
                  movementSettings -> (double)movementSettings.swimUpSpeed
               )
               .add()
               .append(
                  new KeyedCodec<>("SwimDownSpeed", Codec.DOUBLE),
                  (movementSettings, val) -> movementSettings.swimDownSpeed = val.floatValue(),
                  movementSettings -> (double)movementSettings.swimDownSpeed
               )
               .add()
               .append(
                  new KeyedCodec<>("SinkSpeed", Codec.DOUBLE),
                  (movementSettings, val) -> movementSettings.sinkSpeed = val.floatValue(),
                  movementSettings -> (double)movementSettings.sinkSpeed
               )
               .add()
               .append(
                  new KeyedCodec<>("HorizontalSpeedMultiplier", Codec.DOUBLE),
                  (movementSettings, val) -> movementSettings.horizontalSpeedMultiplier = val.floatValue(),
                  movementSettings -> (double)movementSettings.horizontalSpeedMultiplier
               )
               .add()
               .append(
                  new KeyedCodec<>("FieldOfViewMultiplier", Codec.DOUBLE),
                  (movementSettings, val) -> movementSettings.fieldOfViewMultiplier = val.floatValue(),
                  movementSettings -> (double)movementSettings.fieldOfViewMultiplier
               )
               .add()
               .append(
                  new KeyedCodec<>("EntryVelocityMultiplier", Codec.DOUBLE),
                  (movementSettings, val) -> movementSettings.entryVelocityMultiplier = val.floatValue(),
                  movementSettings -> (double)movementSettings.entryVelocityMultiplier
               )
               .add()
               .build()
         ),
         (fluidFX, movementSettings) -> fluidFX.movementSettings = movementSettings,
         fluidFX -> fluidFX.movementSettings,
         (fluidFX, parent) -> fluidFX.movementSettings = parent.movementSettings
      )
      .add()
      .build();
   public static final Color DEFAULT_FOG_COLOR = new Color((byte)-1, (byte)-1, (byte)-1);
   public static final float[] DEFAULT_FOG_DISTANCE = new float[]{0.0F, 32.0F};
   public static final float[] DEFAULT_COLORS_FILTER = new float[]{1.0F, 1.0F, 1.0F};
   public static final int EMPTY_ID = 0;
   public static final String EMPTY = "Empty";
   public static final FluidFX EMPTY_FLUID_FX = getUnknownFor("Empty");
   public static final ValidatorCache<String> VALIDATOR_CACHE = new ValidatorCache<>(new AssetKeyValidator<>(FluidFX::getAssetStore));
   private static AssetStore<String, FluidFX, IndexedLookupTableAssetMap<String, FluidFX>> ASSET_STORE;
   protected AssetExtraInfo.Data data;
   protected String id;
   @Nonnull
   protected FluidFog fog = FluidFog.Color;
   protected Color fogColor = DEFAULT_FOG_COLOR;
   protected float[] fogDistance = DEFAULT_FOG_DISTANCE;
   protected float fogDepthStart = 40.0F;
   protected float fogDepthFalloff = 10.0F;
   protected float colorsSaturation = 1.0F;
   protected float[] colorsFilter = DEFAULT_COLORS_FILTER;
   protected float distortionAmplitude = 8.0F;
   protected float distortionFrequency = 4.0F;
   protected FluidParticle particle;
   protected FluidFXMovementSettings movementSettings;
   private SoftReference<com.hypixel.hytale.protocol.FluidFX> cachedPacket;

   public static AssetStore<String, FluidFX, IndexedLookupTableAssetMap<String, FluidFX>> getAssetStore() {
      if (ASSET_STORE == null) {
         ASSET_STORE = AssetRegistry.getAssetStore(FluidFX.class);
      }

      return ASSET_STORE;
   }

   public static IndexedLookupTableAssetMap<String, FluidFX> getAssetMap() {
      return (IndexedLookupTableAssetMap<String, FluidFX>)getAssetStore().getAssetMap();
   }

   public FluidFX(
      String id,
      FluidFog fog,
      Color fogColor,
      float[] fogDistance,
      float fogDepthStart,
      float fogDepthFalloff,
      float colorsSaturation,
      float[] colorsFilter,
      float distortionAmplitude,
      float distortionFrequency,
      FluidParticle particle,
      FluidFXMovementSettings movementSettings
   ) {
      this.id = id;
      this.fog = fog;
      this.fogColor = fogColor;
      this.fogDistance = fogDistance;
      this.fogDepthStart = fogDepthStart;
      this.fogDepthFalloff = fogDepthFalloff;
      this.colorsSaturation = colorsSaturation;
      this.colorsFilter = colorsFilter;
      this.distortionAmplitude = distortionAmplitude;
      this.distortionFrequency = distortionFrequency;
      this.particle = particle;
      this.movementSettings = movementSettings;
   }

   public FluidFX(String id) {
      this.id = id;
   }

   protected FluidFX() {
   }

   @Nonnull
   public com.hypixel.hytale.protocol.FluidFX toPacket() {
      com.hypixel.hytale.protocol.FluidFX cached = this.cachedPacket == null ? null : this.cachedPacket.get();
      if (cached != null) {
         return cached;
      } else {
         com.hypixel.hytale.protocol.FluidFX packet = new com.hypixel.hytale.protocol.FluidFX();
         packet.id = this.id;
         packet.fogMode = this.fog;
         packet.fogColor = this.fogColor;
         packet.fogDistance = new NearFar(this.fogDistance[0], this.fogDistance[1]);
         packet.fogDepthStart = this.fogDepthStart;
         packet.fogDepthFalloff = this.fogDepthFalloff;
         packet.colorFilter = new Color((byte)(this.colorsFilter[0] * 255.0F), (byte)(this.colorsFilter[1] * 255.0F), (byte)(this.colorsFilter[2] * 255.0F));
         packet.colorSaturation = this.colorsSaturation;
         packet.distortionAmplitude = this.distortionAmplitude;
         packet.distortionFrequency = this.distortionFrequency;
         if (this.particle != null) {
            packet.particle = this.particle.toPacket();
         }

         if (this.movementSettings != null) {
            packet.movementSettings = this.movementSettings;
         }

         this.cachedPacket = new SoftReference<>(packet);
         return packet;
      }
   }

   public String getId() {
      return this.id;
   }

   public FluidFog getFog() {
      return this.fog;
   }

   public Color getFogColor() {
      return this.fogColor;
   }

   public float[] getFogDistance() {
      return this.fogDistance;
   }

   public float getColorsSaturation() {
      return this.colorsSaturation;
   }

   public float[] getColorsFilter() {
      return this.colorsFilter;
   }

   public float getDistortionAmplitude() {
      return this.distortionAmplitude;
   }

   public float getDistortionFrequency() {
      return this.distortionFrequency;
   }

   public float getFogDepthStart() {
      return this.fogDepthStart;
   }

   public float getFogDepthFalloff() {
      return this.fogDepthFalloff;
   }

   public FluidParticle getParticle() {
      return this.particle;
   }

   public FluidFXMovementSettings getMovementSettings() {
      return this.movementSettings;
   }

   @Override
   public boolean equals(@Nullable Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         FluidFX fluidFX = (FluidFX)o;
         return this.id != null ? this.id.equals(fluidFX.id) : fluidFX.id == null;
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return this.id != null ? this.id.hashCode() : 0;
   }

   @Nonnull
   @Override
   public String toString() {
      return "FluidFX{id='"
         + this.id
         + "', fog="
         + this.fog
         + ", fogColor='"
         + this.fogColor
         + "', fogDistance="
         + Arrays.toString(this.fogDistance)
         + ", fogDepthStart="
         + this.fogDepthStart
         + ", fogDepthFalloff="
         + this.fogDepthFalloff
         + ", colorsSaturation="
         + this.colorsSaturation
         + ", colorsFilter="
         + Arrays.toString(this.colorsFilter)
         + ", distortionAmplitude="
         + this.distortionAmplitude
         + ", distortionFrequency="
         + this.distortionFrequency
         + ", particle="
         + this.particle
         + ", movementSettings="
         + this.movementSettings
         + "}";
   }

   @Nonnull
   public static FluidFX getUnknownFor(String unknownId) {
      return new FluidFX(unknownId);
   }
}
