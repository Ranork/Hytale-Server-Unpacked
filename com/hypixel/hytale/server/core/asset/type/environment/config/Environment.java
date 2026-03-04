package com.hypixel.hytale.server.core.asset.type.environment.config;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetKeyValidator;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.codecs.map.Int2ObjectMapCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;
import com.hypixel.hytale.codec.schema.metadata.ui.UIDefaultCollapsedState;
import com.hypixel.hytale.codec.schema.metadata.ui.UIEditor;
import com.hypixel.hytale.codec.schema.metadata.ui.UIEditorFeatures;
import com.hypixel.hytale.codec.schema.metadata.ui.UIEditorSectionStart;
import com.hypixel.hytale.codec.validation.ValidatorCache;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.codec.validation.validator.MapKeyValidator;
import com.hypixel.hytale.common.map.IWeightedMap;
import com.hypixel.hytale.common.map.WeightedMap;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.Color;
import com.hypixel.hytale.protocol.WorldEnvironment;
import com.hypixel.hytale.server.core.asset.type.fluidfx.config.FluidFX;
import com.hypixel.hytale.server.core.asset.type.fluidfx.config.FluidParticle;
import com.hypixel.hytale.server.core.asset.type.weather.config.Weather;
import com.hypixel.hytale.server.core.codec.ProtocolCodecs;
import com.hypixel.hytale.server.core.codec.WeightedMapCodec;
import com.hypixel.hytale.server.core.io.NetworkSerializable;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.lang.ref.SoftReference;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Environment implements JsonAssetWithMap<String, IndexedLookupTableAssetMap<String, Environment>>, NetworkSerializable<WorldEnvironment> {
   public static final int HOURS_PER_DAY = (int)ChronoUnit.DAYS.getDuration().toHours();
   public static final int MAX_KEY_HOUR = HOURS_PER_DAY - 1;
   public static final Integer[] HOURS = new Integer[HOURS_PER_DAY];
   @Nonnull
   private static final IWeightedMap<WeatherForecast> DEFAULT_WEATHER_FORECAST;
   public static final AssetBuilderCodec<String, Environment> CODEC;
   public static final ValidatorCache<String> VALIDATOR_CACHE;
   private static AssetStore<String, Environment, IndexedLookupTableAssetMap<String, Environment>> ASSET_STORE;
   public static final int UNKNOWN_ID = 0;
   public static final Environment UNKNOWN;
   protected AssetExtraInfo.Data data;
   protected String id;
   protected Color waterTint;
   protected Map<String, FluidParticle> fluidParticles = Collections.emptyMap();
   protected Int2ObjectMap<IWeightedMap<WeatherForecast>> weatherForecasts;
   protected double spawnDensity;
   protected boolean blockModificationAllowed = true;
   private SoftReference<WorldEnvironment> cachedPacket;

   public static AssetStore<String, Environment, IndexedLookupTableAssetMap<String, Environment>> getAssetStore() {
      if (ASSET_STORE == null) {
         ASSET_STORE = AssetRegistry.getAssetStore(Environment.class);
      }

      return ASSET_STORE;
   }

   public static IndexedLookupTableAssetMap<String, Environment> getAssetMap() {
      return (IndexedLookupTableAssetMap<String, Environment>)getAssetStore().getAssetMap();
   }

   public Environment(
      String id, Color waterTint, Map<String, FluidParticle> fluidParticles, Int2ObjectMap<IWeightedMap<WeatherForecast>> weatherForecasts, double spawnDensity
   ) {
      this.id = id;
      this.waterTint = waterTint;
      this.fluidParticles = fluidParticles;
      this.weatherForecasts = weatherForecasts;
      this.spawnDensity = spawnDensity;
   }

   protected Environment() {
   }

   public String getId() {
      return this.id;
   }

   public Color getWaterTint() {
      return this.waterTint;
   }

   public Map<String, FluidParticle> getFluidParticles() {
      return this.fluidParticles;
   }

   public Int2ObjectMap<IWeightedMap<WeatherForecast>> getWeatherForecasts() {
      return this.weatherForecasts;
   }

   public IWeightedMap<WeatherForecast> getWeatherForecast(int hour) {
      if (hour < 0 || hour > MAX_KEY_HOUR) {
         throw new IllegalArgumentException("hour must be in range of 0 to " + MAX_KEY_HOUR);
      } else {
         return this.weatherForecasts == null ? DEFAULT_WEATHER_FORECAST : (IWeightedMap)this.weatherForecasts.getOrDefault(hour, DEFAULT_WEATHER_FORECAST);
      }
   }

   public double getSpawnDensity() {
      return this.spawnDensity;
   }

   public boolean isBlockModificationAllowed() {
      return this.blockModificationAllowed;
   }

   @Nonnull
   public WorldEnvironment toPacket() {
      WorldEnvironment cached = this.cachedPacket == null ? null : this.cachedPacket.get();
      if (cached != null) {
         return cached;
      } else {
         WorldEnvironment packet = new WorldEnvironment();
         packet.id = this.id;
         if (this.waterTint != null) {
            packet.waterTint = this.waterTint;
         }

         if (!this.fluidParticles.isEmpty()) {
            Map<Integer, com.hypixel.hytale.protocol.FluidParticle> map = new Int2ObjectOpenHashMap(this.fluidParticles.size());

            for (Entry<String, FluidParticle> entry : this.fluidParticles.entrySet()) {
               int index = FluidFX.getAssetMap().getIndex(entry.getKey());
               if (index != Integer.MIN_VALUE) {
                  map.put(index, entry.getValue().toPacket());
               }
            }

            packet.fluidParticles = map;
         }

         if (this.data != null) {
            IntSet tags = this.data.getExpandedTagIndexes();
            packet.tagIndexes = tags.toIntArray();
         }

         this.cachedPacket = new SoftReference<>(packet);
         return packet;
      }
   }

   @Override
   public boolean equals(@Nullable Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         Environment that = (Environment)o;
         return this.id.equals(that.id);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return this.id.hashCode();
   }

   @Nonnull
   @Override
   public String toString() {
      return "Environment{id='"
         + this.id
         + "', waterTint='"
         + this.waterTint
         + "', fluidParticles='"
         + this.fluidParticles
         + "', weatherForecasts="
         + this.weatherForecasts
         + ", spawnDensity="
         + this.spawnDensity
         + ", packet="
         + this.cachedPacket
         + "}";
   }

   @Nonnull
   public static Environment getUnknownFor(final String unknownId) {
      return new Environment() {
         {
            this.id = unknownId;
            this.waterTint = new Color((byte)10, (byte)51, (byte)85);
            this.spawnDensity = 0.175;
         }
      };
   }

   public static int getIndexOrUnknown(String id, String message, Object... params) {
      int environmentIndex = getAssetMap().getIndex(id);
      if (environmentIndex == Integer.MIN_VALUE) {
         HytaleLogger.getLogger().at(Level.WARNING).logVarargs(message, params);
         getAssetStore().loadAssets("Hytale:Hytale", Collections.singletonList(getUnknownFor(id)));
         int index = getAssetMap().getIndex(id);
         if (index == Integer.MIN_VALUE) {
            throw new IllegalArgumentException("Unknown key! " + id);
         }

         environmentIndex = index;
      }

      return environmentIndex;
   }

   static {
      WeightedMap.Builder<WeatherForecast> mapBuilder = WeightedMap.builder(WeatherForecast.EMPTY_ARRAY);
      mapBuilder.put(new WeatherForecast(Weather.UNKNOWN.getId(), 1.0), 1.0);
      DEFAULT_WEATHER_FORECAST = mapBuilder.build();

      for (int i = 0; i < HOURS_PER_DAY; i++) {
         HOURS[i] = i;
      }

      CODEC = AssetBuilderCodec.builder(
            Environment.class,
            Environment::new,
            Codec.STRING,
            (environment, s) -> environment.id = s,
            environment -> environment.id,
            (asset, data) -> asset.data = data,
            asset -> asset.data
         )
         .metadata(new UIEditorFeatures(UIEditorFeatures.EditorFeature.WEATHER_DAYTIME_BAR))
         .appendInherited(
            new KeyedCodec<>("WaterTint", ProtocolCodecs.COLOR),
            (environment, s) -> environment.waterTint = s,
            environment -> environment.waterTint,
            (environment, parent) -> environment.waterTint = parent.waterTint
         )
         .add()
         .<Map>appendInherited(
            new KeyedCodec<>("FluidParticles", new MapCodec<>(FluidParticle.CODEC, HashMap::new)),
            (environment, s) -> environment.fluidParticles = s,
            environment -> environment.fluidParticles,
            (environment, parent) -> environment.fluidParticles = parent.fluidParticles
         )
         .addValidator(FluidFX.VALIDATOR_CACHE.getMapKeyValidator())
         .add()
         .appendInherited(
            new KeyedCodec<>("SpawnDensity", Codec.DOUBLE),
            (environment, d) -> environment.spawnDensity = d,
            environment -> environment.spawnDensity,
            (environment, parent) -> environment.spawnDensity = parent.spawnDensity
         )
         .add()
         .appendInherited(
            new KeyedCodec<>("BlockModificationAllowed", Codec.BOOLEAN),
            (environment, b) -> environment.blockModificationAllowed = b,
            environment -> environment.blockModificationAllowed,
            (environment, parent) -> environment.blockModificationAllowed = parent.blockModificationAllowed
         )
         .add()
         .<Int2ObjectMap<IWeightedMap<WeatherForecast>>>appendInherited(
            new KeyedCodec<>(
               "WeatherForecasts",
               new Int2ObjectMapCodec<>(new WeightedMapCodec<>(WeatherForecast.CODEC, WeatherForecast.EMPTY_ARRAY), Int2ObjectOpenHashMap::new),
               true
            ),
            (environment, l) -> environment.weatherForecasts = l,
            environment -> environment.weatherForecasts,
            (environment, parent) -> environment.weatherForecasts = parent.weatherForecasts
         )
         .addValidator(Validators.nonNull())
         .addValidator(new MapKeyValidator<>(Validators.range(0, MAX_KEY_HOUR)))
         .addValidator(Validators.requiredMapKeysValidator(HOURS))
         .metadata(new UIEditor(UIEditor.WEIGHTED_TIMELINE))
         .metadata(new UIEditorSectionStart("Weather"))
         .metadata(UIDefaultCollapsedState.UNCOLLAPSED)
         .add()
         .build();
      VALIDATOR_CACHE = new ValidatorCache<>(new AssetKeyValidator<>(Environment::getAssetStore));
      UNKNOWN = getUnknownFor("Unknown");
   }
}
