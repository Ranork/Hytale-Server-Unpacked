package com.hypixel.hytale.server.core.asset.type.weather.config;

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
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.schema.metadata.ui.UIDefaultCollapsedState;
import com.hypixel.hytale.codec.schema.metadata.ui.UIEditor;
import com.hypixel.hytale.codec.schema.metadata.ui.UIEditorFeatures;
import com.hypixel.hytale.codec.schema.metadata.ui.UIEditorSectionStart;
import com.hypixel.hytale.codec.validation.ValidatorCache;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.common.util.ArrayUtil;
import com.hypixel.hytale.protocol.Color;
import com.hypixel.hytale.protocol.ColorAlpha;
import com.hypixel.hytale.protocol.NearFar;
import com.hypixel.hytale.protocol.WeatherParticle;
import com.hypixel.hytale.server.core.asset.common.CommonAssetValidator;
import com.hypixel.hytale.server.core.asset.type.particle.config.ParticleSystem;
import com.hypixel.hytale.server.core.codec.ProtocolCodecs;
import com.hypixel.hytale.server.core.io.NetworkSerializable;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.lang.ref.SoftReference;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

public class Weather implements JsonAssetWithMap<String, IndexedLookupTableAssetMap<String, Weather>>, NetworkSerializable<com.hypixel.hytale.protocol.Weather> {
   public static final BuilderCodec<WeatherParticle> PARTICLE_CODEC = BuilderCodec.builder(WeatherParticle.class, WeatherParticle::new)
      .documentation("Particle System that can be spawned in relation to a weather.")
      .<String>append(new KeyedCodec<>("SystemId", Codec.STRING), (particle, s) -> particle.systemId = s, particle -> particle.systemId)
      .addValidator(Validators.nonNull())
      .addValidator(ParticleSystem.VALIDATOR_CACHE.getValidator())
      .add()
      .<Color>append(new KeyedCodec<>("Color", ProtocolCodecs.COLOR), (particle, o) -> particle.color = o, particle -> particle.color)
      .documentation("The colour used if none was specified in the particle settings.")
      .add()
      .<Float>append(new KeyedCodec<>("Scale", Codec.FLOAT), (particle, f) -> particle.scale = f, particle -> particle.scale)
      .documentation("The scale of the particle system.")
      .add()
      .<Boolean>append(new KeyedCodec<>("OvergroundOnly", Codec.BOOLEAN), (particle, s) -> particle.isOvergroundOnly = s, particle -> particle.isOvergroundOnly)
      .documentation("Sets if the particles can only spawn above the columns highest blocks.")
      .add()
      .<Float>append(
         new KeyedCodec<>("PositionOffsetMultiplier", Codec.FLOAT),
         (particle, f) -> particle.positionOffsetMultiplier = f,
         particle -> particle.positionOffsetMultiplier
      )
      .documentation("The amount the system will move ahead of the camera.")
      .add()
      .build();
   public static final AssetBuilderCodec<String, Weather> CODEC = AssetBuilderCodec.builder(
         Weather.class,
         Weather::new,
         Codec.STRING,
         (weather, s) -> weather.id = s,
         weather -> weather.id,
         (asset, data) -> asset.data = data,
         asset -> asset.data
      )
      .metadata(new UIEditorFeatures(UIEditorFeatures.EditorFeature.WEATHER_DAYTIME_BAR, UIEditorFeatures.EditorFeature.WEATHER_PREVIEW_LOCAL))
      .<String>appendInherited(
         new KeyedCodec<>("Stars", Codec.STRING),
         (weather, o) -> weather.stars = o,
         weather -> weather.stars,
         (weather, parent) -> weather.stars = parent.stars
      )
      .addValidator(CommonAssetValidator.TEXTURE_SKY)
      .metadata(UIDefaultCollapsedState.UNCOLLAPSED)
      .add()
      .<String>appendInherited(
         new KeyedCodec<>("ScreenEffect", Codec.STRING),
         (weather, o) -> weather.screenEffect = o,
         weather -> weather.screenEffect,
         (weather, parent) -> weather.screenEffect = parent.screenEffect
      )
      .addValidator(CommonAssetValidator.UI_SCREEN_EFFECT)
      .add()
      .<double[]>appendInherited(new KeyedCodec<>("FogDistance", Codec.DOUBLE_ARRAY), (weather, o) -> {
         weather.fogDistance = new float[2];
         weather.fogDistance[0] = (float)o[0];
         weather.fogDistance[1] = (float)o[1];
      }, weather -> new double[]{weather.fogDistance[0], weather.fogDistance[1]}, (weather, parent) -> weather.fogDistance = parent.fogDistance)
      .addValidator(Validators.nonNull())
      .addValidator(Validators.doubleArraySize(2))
      .addValidator(Validators.monotonicSequentialDoubleArrayValidator())
      .documentation(
         "Array of strictly two values. First is FogNear, which is expected to be negative. Second is FogFar. FogNear determines how foggy it is at the player's position, while FogFar determines the range at which FogDensities starts ramping up."
      )
      .add()
      .<FogOptions>appendInherited(
         new KeyedCodec<>("FogOptions", FogOptions.CODEC),
         (weather, o) -> weather.fogOptions = o,
         weather -> weather.fogOptions,
         (weather, parent) -> weather.fogOptions = parent.fogOptions
      )
      .documentation("Optional extra information about the fog for this Weather")
      .add()
      .appendInherited(
         new KeyedCodec<>("Particle", PARTICLE_CODEC),
         (weather, o) -> weather.particle = o,
         weather -> weather.particle,
         (weather, parent) -> weather.particle = parent.particle
      )
      .add()
      .<TimeColorAlpha[]>appendInherited(
         new KeyedCodec<>("ScreenEffectColors", TimeColorAlpha.ARRAY_CODEC),
         (weather, o) -> weather.screenEffectColors = o,
         weather -> weather.screenEffectColors,
         (weather, parent) -> weather.screenEffectColors = parent.screenEffectColors
      )
      .metadata(new UIEditor(UIEditor.TIMELINE))
      .metadata(UIDefaultCollapsedState.UNCOLLAPSED)
      .add()
      .<TimeFloat[]>appendInherited(
         new KeyedCodec<>("SunlightDampingMultipliers", new ArrayCodec<>(TimeFloat.CODEC, TimeFloat[]::new)),
         (weather, o) -> weather.sunlightDampingMultiplier = o,
         weather -> weather.sunlightDampingMultiplier,
         (weather, parent) -> weather.sunlightDampingMultiplier = parent.sunlightDampingMultiplier
      )
      .metadata(new UIEditorSectionStart("Colors"))
      .metadata(new UIEditor(UIEditor.TIMELINE))
      .metadata(UIDefaultCollapsedState.UNCOLLAPSED)
      .add()
      .<TimeColor[]>appendInherited(
         new KeyedCodec<>("SunlightColors", TimeColor.ARRAY_CODEC),
         (weather, o) -> weather.sunlightColors = o,
         weather -> weather.sunlightColors,
         (weather, parent) -> weather.sunlightColors = parent.sunlightColors
      )
      .metadata(new UIEditor(UIEditor.TIMELINE))
      .metadata(UIDefaultCollapsedState.UNCOLLAPSED)
      .add()
      .<TimeColor[]>appendInherited(
         new KeyedCodec<>("SunColors", TimeColor.ARRAY_CODEC),
         (weather, o) -> weather.sunColors = o,
         weather -> weather.sunColors,
         (weather, parent) -> weather.sunColors = parent.sunColors
      )
      .metadata(new UIEditor(UIEditor.TIMELINE))
      .metadata(UIDefaultCollapsedState.UNCOLLAPSED)
      .add()
      .<TimeColorAlpha[]>appendInherited(
         new KeyedCodec<>("MoonColors", TimeColorAlpha.ARRAY_CODEC),
         (weather, o) -> weather.moonColors = o,
         weather -> weather.moonColors,
         (weather, parent) -> weather.moonColors = parent.moonColors
      )
      .metadata(new UIEditor(UIEditor.TIMELINE))
      .metadata(UIDefaultCollapsedState.UNCOLLAPSED)
      .add()
      .<TimeColorAlpha[]>appendInherited(
         new KeyedCodec<>("SunGlowColors", TimeColorAlpha.ARRAY_CODEC),
         (weather, o) -> weather.sunGlowColors = o,
         weather -> weather.sunGlowColors,
         (weather, parent) -> weather.sunGlowColors = parent.sunGlowColors
      )
      .metadata(new UIEditor(UIEditor.TIMELINE))
      .metadata(UIDefaultCollapsedState.UNCOLLAPSED)
      .add()
      .<TimeColorAlpha[]>appendInherited(
         new KeyedCodec<>("MoonGlowColors", TimeColorAlpha.ARRAY_CODEC),
         (weather, o) -> weather.moonGlowColors = o,
         weather -> weather.moonGlowColors,
         (weather, parent) -> weather.moonGlowColors = parent.moonGlowColors
      )
      .metadata(new UIEditor(UIEditor.TIMELINE))
      .metadata(UIDefaultCollapsedState.UNCOLLAPSED)
      .add()
      .<TimeFloat[]>appendInherited(
         new KeyedCodec<>("SunScales", new ArrayCodec<>(TimeFloat.CODEC, TimeFloat[]::new)),
         (weather, o) -> weather.sunScales = o,
         weather -> weather.sunScales,
         (weather, parent) -> weather.sunScales = parent.sunScales
      )
      .metadata(new UIEditor(UIEditor.TIMELINE))
      .metadata(UIDefaultCollapsedState.UNCOLLAPSED)
      .add()
      .<TimeFloat[]>appendInherited(
         new KeyedCodec<>("MoonScales", new ArrayCodec<>(TimeFloat.CODEC, TimeFloat[]::new)),
         (weather, o) -> weather.moonScales = o,
         weather -> weather.moonScales,
         (weather, parent) -> weather.moonScales = parent.moonScales
      )
      .metadata(new UIEditor(UIEditor.TIMELINE))
      .metadata(UIDefaultCollapsedState.UNCOLLAPSED)
      .add()
      .<TimeColorAlpha[]>appendInherited(
         new KeyedCodec<>("SkyTopColors", TimeColorAlpha.ARRAY_CODEC),
         (weather, o) -> weather.skyTopColors = o,
         weather -> weather.skyTopColors,
         (weather, parent) -> weather.skyTopColors = parent.skyTopColors
      )
      .metadata(new UIEditor(UIEditor.TIMELINE))
      .metadata(UIDefaultCollapsedState.UNCOLLAPSED)
      .add()
      .<TimeColorAlpha[]>appendInherited(
         new KeyedCodec<>("SkyBottomColors", TimeColorAlpha.ARRAY_CODEC),
         (weather, o) -> weather.skyBottomColors = o,
         weather -> weather.skyBottomColors,
         (weather, parent) -> weather.skyBottomColors = parent.skyBottomColors
      )
      .metadata(new UIEditor(UIEditor.TIMELINE))
      .metadata(UIDefaultCollapsedState.UNCOLLAPSED)
      .add()
      .<TimeColorAlpha[]>appendInherited(
         new KeyedCodec<>("SkySunsetColors", TimeColorAlpha.ARRAY_CODEC),
         (weather, o) -> weather.skySunsetColors = o,
         weather -> weather.skySunsetColors,
         (weather, parent) -> weather.skySunsetColors = parent.skySunsetColors
      )
      .metadata(new UIEditor(UIEditor.TIMELINE))
      .metadata(UIDefaultCollapsedState.UNCOLLAPSED)
      .add()
      .<TimeColor[]>appendInherited(
         new KeyedCodec<>("FogColors", TimeColor.ARRAY_CODEC),
         (weather, o) -> weather.fogColors = o,
         weather -> weather.fogColors,
         (weather, parent) -> weather.fogColors = parent.fogColors
      )
      .metadata(new UIEditor(UIEditor.TIMELINE))
      .metadata(UIDefaultCollapsedState.UNCOLLAPSED)
      .add()
      .<TimeFloat[]>appendInherited(
         new KeyedCodec<>("FogHeightFalloffs", new ArrayCodec<>(TimeFloat.CODEC, TimeFloat[]::new)),
         (weather, o) -> weather.fogHeightFalloffs = o,
         weather -> weather.fogHeightFalloffs,
         (weather, parent) -> weather.fogHeightFalloffs = parent.fogHeightFalloffs
      )
      .metadata(new UIEditor(UIEditor.TIMELINE))
      .metadata(UIDefaultCollapsedState.UNCOLLAPSED)
      .add()
      .<TimeFloat[]>appendInherited(
         new KeyedCodec<>("FogDensities", new ArrayCodec<>(TimeFloat.CODEC, TimeFloat[]::new)),
         (weather, o) -> weather.fogDensities = o,
         weather -> weather.fogDensities,
         (weather, parent) -> weather.fogDensities = parent.fogDensities
      )
      .metadata(new UIEditor(UIEditor.TIMELINE))
      .metadata(UIDefaultCollapsedState.UNCOLLAPSED)
      .add()
      .<TimeColor[]>appendInherited(
         new KeyedCodec<>("WaterTints", TimeColor.ARRAY_CODEC),
         (weather, o) -> weather.waterTints = o,
         weather -> weather.waterTints,
         (weather, parent) -> weather.waterTints = parent.waterTints
      )
      .metadata(new UIEditor(UIEditor.TIMELINE))
      .metadata(UIDefaultCollapsedState.UNCOLLAPSED)
      .add()
      .<TimeColor[]>appendInherited(
         new KeyedCodec<>("ColorFilters", TimeColor.ARRAY_CODEC),
         (weather, o) -> weather.colorFilters = o,
         weather -> weather.colorFilters,
         (weather, parent) -> weather.colorFilters = parent.colorFilters
      )
      .metadata(new UIEditor(UIEditor.TIMELINE))
      .metadata(UIDefaultCollapsedState.UNCOLLAPSED)
      .add()
      .<DayTexture[]>appendInherited(
         new KeyedCodec<>("Moons", new ArrayCodec<>(DayTexture.CODEC, DayTexture[]::new)),
         (weather, o) -> weather.moons = o,
         weather -> weather.moons,
         (weather, parent) -> weather.moons = parent.moons
      )
      .metadata(new UIEditorSectionStart("Moons"))
      .metadata(UIDefaultCollapsedState.UNCOLLAPSED)
      .add()
      .<Cloud[]>appendInherited(
         new KeyedCodec<>("Clouds", new ArrayCodec<>(Cloud.CODEC, Cloud[]::new)),
         (weather, o) -> weather.clouds = o,
         weather -> weather.clouds,
         (weather, parent) -> weather.clouds = parent.clouds
      )
      .metadata(new UIEditorSectionStart("Clouds"))
      .metadata(UIDefaultCollapsedState.UNCOLLAPSED)
      .add()
      .build();
   public static final ValidatorCache<String> VALIDATOR_CACHE = new ValidatorCache<>(new AssetKeyValidator<>(Weather::getAssetStore));
   private static AssetStore<String, Weather, IndexedLookupTableAssetMap<String, Weather>> ASSET_STORE;
   public static final float[] DEFAULT_FOG_DISTANCE = new float[]{-96.0F, 1024.0F};
   public static final int UNKNOWN_ID = 0;
   public static final Weather UNKNOWN = new Weather("Unknown");
   protected AssetExtraInfo.Data data;
   protected String id;
   protected DayTexture[] moons;
   protected Cloud[] clouds;
   protected TimeFloat[] sunlightDampingMultiplier;
   protected TimeColor[] sunlightColors;
   protected TimeColor[] sunColors;
   protected TimeColorAlpha[] moonColors;
   protected TimeColorAlpha[] sunGlowColors;
   protected TimeColorAlpha[] moonGlowColors;
   protected TimeFloat[] sunScales;
   protected TimeFloat[] moonScales;
   protected TimeColorAlpha[] skyTopColors;
   protected TimeColorAlpha[] skyBottomColors;
   protected TimeColorAlpha[] skySunsetColors;
   protected TimeColor[] fogColors;
   protected TimeFloat[] fogHeightFalloffs;
   protected TimeFloat[] fogDensities;
   protected TimeColor[] waterTints;
   protected float[] fogDistance = DEFAULT_FOG_DISTANCE;
   protected FogOptions fogOptions;
   protected String screenEffect;
   protected TimeColorAlpha[] screenEffectColors;
   protected TimeColor[] colorFilters;
   protected String stars;
   protected WeatherParticle particle;
   private SoftReference<com.hypixel.hytale.protocol.Weather> cachedPacket;

   public static AssetStore<String, Weather, IndexedLookupTableAssetMap<String, Weather>> getAssetStore() {
      if (ASSET_STORE == null) {
         ASSET_STORE = AssetRegistry.getAssetStore(Weather.class);
      }

      return ASSET_STORE;
   }

   public static IndexedLookupTableAssetMap<String, Weather> getAssetMap() {
      return (IndexedLookupTableAssetMap<String, Weather>)getAssetStore().getAssetMap();
   }

   public Weather(
      String id,
      DayTexture[] moons,
      Cloud[] clouds,
      TimeFloat[] sunlightDampingMultiplier,
      TimeColor[] sunlightColors,
      TimeColor[] sunColors,
      TimeColorAlpha[] moonColors,
      TimeColorAlpha[] sunGlowColors,
      TimeColorAlpha[] moonGlowColors,
      TimeFloat[] sunScales,
      TimeFloat[] moonScales,
      TimeColorAlpha[] skyTopColors,
      TimeColorAlpha[] skyBottomColors,
      TimeColorAlpha[] skySunsetColors,
      TimeColor[] fogColors,
      TimeFloat[] fogHeightFalloffs,
      TimeFloat[] fogDensities,
      TimeColor[] waterTints,
      float[] fogDistance,
      FogOptions fogOptions,
      String screenEffect,
      TimeColorAlpha[] screenEffectColors,
      TimeColor[] colorFilters,
      String stars,
      WeatherParticle particle
   ) {
      this.id = id;
      this.moons = moons;
      this.clouds = clouds;
      this.sunlightDampingMultiplier = sunlightDampingMultiplier;
      this.sunlightColors = sunlightColors;
      this.sunColors = sunColors;
      this.moonColors = moonColors;
      this.sunGlowColors = sunGlowColors;
      this.moonGlowColors = moonGlowColors;
      this.sunScales = sunScales;
      this.moonScales = moonScales;
      this.skyTopColors = skyTopColors;
      this.skyBottomColors = skyBottomColors;
      this.skySunsetColors = skySunsetColors;
      this.fogColors = fogColors;
      this.fogHeightFalloffs = fogHeightFalloffs;
      this.fogDensities = fogDensities;
      this.waterTints = waterTints;
      this.fogDistance = fogDistance;
      this.fogOptions = fogOptions;
      this.screenEffect = screenEffect;
      this.screenEffectColors = screenEffectColors;
      this.colorFilters = colorFilters;
      this.stars = stars;
      this.particle = particle;
   }

   public Weather(String id) {
      this.id = id;
   }

   protected Weather() {
   }

   @Nonnull
   public com.hypixel.hytale.protocol.Weather toPacket() {
      com.hypixel.hytale.protocol.Weather cached = this.cachedPacket == null ? null : this.cachedPacket.get();
      if (cached != null) {
         return cached;
      } else {
         com.hypixel.hytale.protocol.Weather packet = new com.hypixel.hytale.protocol.Weather();
         packet.id = this.id;
         if (this.moons != null && this.moons.length > 0) {
            packet.moons = toStringMap(this.moons);
         }

         if (this.clouds != null && this.clouds.length > 0) {
            packet.clouds = ArrayUtil.copyAndMutate(this.clouds, Cloud::toPacket, com.hypixel.hytale.protocol.Cloud[]::new);
         }

         if (this.sunlightDampingMultiplier != null && this.sunlightDampingMultiplier.length > 0) {
            packet.sunlightDampingMultiplier = toFloatMap(this.sunlightDampingMultiplier);
         }

         if (this.sunlightColors != null && this.sunlightColors.length > 0) {
            packet.sunlightColors = toColorMap(this.sunlightColors);
         }

         if (this.sunColors != null && this.sunColors.length > 0) {
            packet.sunColors = toColorMap(this.sunColors);
         }

         if (this.sunGlowColors != null && this.sunGlowColors.length > 0) {
            packet.sunGlowColors = toColorAlphaMap(this.sunGlowColors);
         }

         if (this.sunScales != null && this.sunScales.length > 0) {
            packet.sunScales = toFloatMap(this.sunScales);
         }

         if (this.moonColors != null && this.moonColors.length > 0) {
            packet.moonColors = toColorAlphaMap(this.moonColors);
         }

         if (this.moonGlowColors != null && this.moonGlowColors.length > 0) {
            packet.moonGlowColors = toColorAlphaMap(this.moonGlowColors);
         }

         if (this.moonScales != null && this.moonScales.length > 0) {
            packet.moonScales = toFloatMap(this.moonScales);
         }

         if (this.skyTopColors != null && this.skyTopColors.length > 0) {
            packet.skyTopColors = toColorAlphaMap(this.skyTopColors);
         }

         if (this.skyBottomColors != null && this.skyBottomColors.length > 0) {
            packet.skyBottomColors = toColorAlphaMap(this.skyBottomColors);
         }

         if (this.skySunsetColors != null && this.skySunsetColors.length > 0) {
            packet.skySunsetColors = toColorAlphaMap(this.skySunsetColors);
         }

         if (this.fogColors != null && this.fogColors.length > 0) {
            packet.fogColors = toColorMap(this.fogColors);
         }

         if (this.fogHeightFalloffs != null && this.fogHeightFalloffs.length > 0) {
            packet.fogHeightFalloffs = toFloatMap(this.fogHeightFalloffs);
         }

         if (this.fogDensities != null && this.fogDensities.length > 0) {
            packet.fogDensities = toFloatMap(this.fogDensities);
         }

         packet.screenEffect = this.screenEffect;
         if (this.screenEffectColors != null && this.screenEffectColors.length > 0) {
            packet.screenEffectColors = toColorAlphaMap(this.screenEffectColors);
         }

         if (this.colorFilters != null && this.colorFilters.length > 0) {
            packet.colorFilters = toColorMap(this.colorFilters);
         }

         if (this.waterTints != null && this.waterTints.length > 0) {
            packet.waterTints = toColorMap(this.waterTints);
         }

         if (this.fogOptions != null) {
            packet.fogOptions = this.fogOptions.toPacket();
         }

         packet.fog = new NearFar(this.fogDistance[0], this.fogDistance[1]);
         packet.stars = this.stars;
         if (this.particle != null) {
            packet.particle = this.particle;
         }

         if (this.data != null) {
            IntSet tags = this.data.getExpandedTagIndexes();
            packet.tagIndexes = tags.toIntArray();
         }

         this.cachedPacket = new SoftReference<>(packet);
         return packet;
      }
   }

   public String getId() {
      return this.id;
   }

   public DayTexture[] getMoons() {
      return this.moons;
   }

   public Cloud[] getClouds() {
      return this.clouds;
   }

   public TimeFloat[] getSunlightDampingMultiplier() {
      return this.sunlightDampingMultiplier;
   }

   public TimeColor[] getSunlightColors() {
      return this.sunlightColors;
   }

   public TimeColor[] getSunColors() {
      return this.sunColors;
   }

   public TimeColorAlpha[] getMoonColors() {
      return this.moonColors;
   }

   public TimeColorAlpha[] getSunGlowColors() {
      return this.sunGlowColors;
   }

   public TimeColorAlpha[] getMoonGlowColors() {
      return this.moonGlowColors;
   }

   public TimeFloat[] getSunScales() {
      return this.sunScales;
   }

   public TimeFloat[] getMoonScales() {
      return this.moonScales;
   }

   public TimeColorAlpha[] getSkyTopColors() {
      return this.skyTopColors;
   }

   public TimeColorAlpha[] getSkyBottomColors() {
      return this.skyBottomColors;
   }

   public TimeColorAlpha[] getSkySunsetColors() {
      return this.skySunsetColors;
   }

   public TimeColor[] getFogColors() {
      return this.fogColors;
   }

   public TimeFloat[] getFogHeightFalloffs() {
      return this.fogHeightFalloffs;
   }

   public TimeFloat[] getFogDensities() {
      return this.fogDensities;
   }

   public TimeColor[] getWaterTints() {
      return this.waterTints;
   }

   public float[] getFogDistance() {
      return this.fogDistance;
   }

   public FogOptions getFogOptions() {
      return this.fogOptions;
   }

   public String getScreenEffect() {
      return this.screenEffect;
   }

   public TimeColorAlpha[] getScreenEffectColors() {
      return this.screenEffectColors;
   }

   public TimeColor[] getColorFilters() {
      return this.colorFilters;
   }

   public String getStars() {
      return this.stars;
   }

   public WeatherParticle getParticle() {
      return this.particle;
   }

   @Nonnull
   @Override
   public String toString() {
      return "Weather{id='"
         + this.id
         + "', moons="
         + Arrays.toString((Object[])this.moons)
         + ", clouds="
         + Arrays.toString((Object[])this.clouds)
         + ", sunlightDampingMultiplier="
         + Arrays.toString((Object[])this.sunlightDampingMultiplier)
         + ", sunlightColors="
         + Arrays.toString((Object[])this.sunlightColors)
         + ", sunColors="
         + Arrays.toString((Object[])this.sunColors)
         + ", sunGlowColors="
         + Arrays.toString((Object[])this.sunGlowColors)
         + ", sunScales="
         + Arrays.toString((Object[])this.sunScales)
         + ", moonColors="
         + Arrays.toString((Object[])this.moonColors)
         + ", moonGlowColors="
         + Arrays.toString((Object[])this.moonGlowColors)
         + ", moonScales="
         + Arrays.toString((Object[])this.moonScales)
         + ", skyTopColors="
         + Arrays.toString((Object[])this.skyTopColors)
         + ", skyBottomColors="
         + Arrays.toString((Object[])this.skyBottomColors)
         + ", skySunsetColors="
         + Arrays.toString((Object[])this.skySunsetColors)
         + ", fogColors="
         + Arrays.toString((Object[])this.fogColors)
         + ", fogHeightFalloffs="
         + Arrays.toString((Object[])this.fogHeightFalloffs)
         + ", fogDensities="
         + Arrays.toString((Object[])this.fogDensities)
         + ", fogDistance="
         + Arrays.toString(this.fogDistance)
         + ", fogOptions="
         + this.fogOptions
         + ", screenEffect="
         + this.screenEffect
         + ", screenEffectColors="
         + Arrays.toString((Object[])this.screenEffectColors)
         + ", colorFilters="
         + Arrays.toString((Object[])this.colorFilters)
         + ", waterTints="
         + Arrays.toString((Object[])this.waterTints)
         + ", stars="
         + this.stars
         + ", particle="
         + this.particle
         + "}";
   }

   @Nonnull
   public static Map<Integer, String> toStringMap(@Nonnull DayTexture[] dayTexture) {
      return Arrays.stream(dayTexture).collect(Collectors.toMap(DayTexture::getDay, DayTexture::getTexture));
   }

   @Nonnull
   public static Map<Float, Float> toFloatMap(@Nonnull TimeFloat[] timeFloat) {
      return Arrays.stream(timeFloat).collect(Collectors.toMap(TimeFloat::getHour, TimeFloat::getValue));
   }

   @Nonnull
   public static Map<Float, Color> toColorMap(@Nonnull TimeColor[] timeColor) {
      return Arrays.stream(timeColor).collect(Collectors.toMap(TimeColor::getHour, TimeColor::getColor));
   }

   @Nonnull
   public static Map<Float, ColorAlpha> toColorAlphaMap(@Nonnull TimeColorAlpha[] timeColorAlpha) {
      return Arrays.stream(timeColorAlpha).collect(Collectors.toMap(TimeColorAlpha::getHour, TimeColorAlpha::getColor));
   }
}
