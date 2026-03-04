package com.hypixel.hytale.server.core.asset.type.ambiencefx.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.common.util.ArrayUtil;
import com.hypixel.hytale.protocol.Range;
import com.hypixel.hytale.protocol.Rangeb;
import com.hypixel.hytale.protocol.Rangef;
import com.hypixel.hytale.server.core.asset.type.environment.config.Environment;
import com.hypixel.hytale.server.core.asset.type.fluidfx.config.FluidFX;
import com.hypixel.hytale.server.core.asset.type.tagpattern.config.TagPattern;
import com.hypixel.hytale.server.core.asset.type.weather.config.Weather;
import com.hypixel.hytale.server.core.codec.ProtocolCodecs;
import com.hypixel.hytale.server.core.io.NetworkSerializable;
import java.util.Arrays;
import javax.annotation.Nonnull;

public class AmbienceFXConditions implements NetworkSerializable<com.hypixel.hytale.protocol.AmbienceFXConditions> {
   public static final BuilderCodec<AmbienceFXConditions> CODEC = BuilderCodec.builder(AmbienceFXConditions.class, AmbienceFXConditions::new)
      .appendInherited(
         new KeyedCodec<>("Never", Codec.BOOLEAN),
         (ambienceFXConditions, l) -> ambienceFXConditions.never = l,
         ambienceFXConditions -> ambienceFXConditions.never,
         (ambienceFXConditions, parent) -> ambienceFXConditions.never = parent.never
      )
      .documentation("If true, this Ambience will never conditionally trigger (but can be set server-side, for example).")
      .add()
      .<String[]>appendInherited(
         new KeyedCodec<>("EnvironmentIds", Codec.STRING_ARRAY),
         (ambienceFXConditions, l) -> ambienceFXConditions.environmentIds = l,
         ambienceFXConditions -> ambienceFXConditions.environmentIds,
         (ambienceFXConditions, parent) -> ambienceFXConditions.environmentIds = parent.environmentIds
      )
      .addValidator(Environment.VALIDATOR_CACHE.getArrayValidator())
      .add()
      .<String>appendInherited(
         new KeyedCodec<>("EnvironmentTagPattern", TagPattern.CHILD_ASSET_CODEC),
         (ambienceFxConditions, t) -> ambienceFxConditions.environmentTagPattern = t,
         ambienceFXConditions -> ambienceFXConditions.environmentTagPattern,
         (ambienceFXConditions, parent) -> ambienceFXConditions.environmentTagPattern = parent.environmentTagPattern
      )
      .addValidator(TagPattern.VALIDATOR_CACHE.getValidator())
      .documentation("A tag pattern to use for matching environments.")
      .add()
      .<String>appendInherited(
         new KeyedCodec<>("WeatherTagPattern", TagPattern.CHILD_ASSET_CODEC),
         (ambienceFxConditions, t) -> ambienceFxConditions.weatherTagPattern = t,
         ambienceFXConditions -> ambienceFXConditions.weatherTagPattern,
         (ambienceFXConditions, parent) -> ambienceFXConditions.weatherTagPattern = parent.weatherTagPattern
      )
      .addValidator(TagPattern.VALIDATOR_CACHE.getValidator())
      .documentation("A tag pattern to use for matching weathers.")
      .add()
      .<String[]>appendInherited(
         new KeyedCodec<>("WeatherIds", Codec.STRING_ARRAY),
         (ambienceFXConditions, l) -> ambienceFXConditions.weatherIds = l,
         ambienceFXConditions -> ambienceFXConditions.weatherIds,
         (ambienceFXConditions, parent) -> ambienceFXConditions.weatherIds = parent.weatherIds
      )
      .addValidator(Weather.VALIDATOR_CACHE.getArrayValidator())
      .add()
      .<String[]>appendInherited(
         new KeyedCodec<>("FluidFXIds", Codec.STRING_ARRAY),
         (ambienceFXConditions, l) -> ambienceFXConditions.fluidFXIds = l,
         ambienceFXConditions -> ambienceFXConditions.fluidFXIds,
         (ambienceFXConditions, parent) -> ambienceFXConditions.fluidFXIds = parent.fluidFXIds
      )
      .addValidator(FluidFX.VALIDATOR_CACHE.getArrayValidator())
      .add()
      .appendInherited(
         new KeyedCodec<>("SurroundingBlockSoundSets", new ArrayCodec<>(AmbienceFXBlockSoundSet.CODEC, AmbienceFXBlockSoundSet[]::new)),
         (ambienceFXConditions, l) -> ambienceFXConditions.surroundingBlockSoundSets = l,
         ambienceFXConditions -> ambienceFXConditions.surroundingBlockSoundSets,
         (ambienceFXConditions, parent) -> ambienceFXConditions.surroundingBlockSoundSets = parent.surroundingBlockSoundSets
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("Altitude", ProtocolCodecs.RANGE),
         (ambienceFXBlockEnvironment, o) -> ambienceFXBlockEnvironment.altitude = o,
         ambienceFXBlockEnvironment -> ambienceFXBlockEnvironment.altitude,
         (ambienceFXConditions, parent) -> ambienceFXConditions.altitude = parent.altitude
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("Walls", ProtocolCodecs.RANGEB),
         (ambienceFXBlockEnvironment, o) -> ambienceFXBlockEnvironment.walls = o,
         ambienceFXBlockEnvironment -> ambienceFXBlockEnvironment.walls,
         (ambienceFXConditions, parent) -> ambienceFXConditions.walls = parent.walls
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("Roof", Codec.BOOLEAN),
         (ambienceFXConditions, aBoolean) -> ambienceFXConditions.roof = aBoolean,
         ambienceFXConditions -> ambienceFXConditions.roof,
         (ambienceFXConditions, parent) -> ambienceFXConditions.roof = parent.roof
      )
      .add()
      .<String>appendInherited(
         new KeyedCodec<>("RoofMaterialTagPattern", TagPattern.CHILD_ASSET_CODEC),
         (ambienceFxConditions, t) -> ambienceFxConditions.roofMaterialTagPattern = t,
         ambienceFXConditions -> ambienceFXConditions.roofMaterialTagPattern,
         (ambienceFXConditions, parent) -> ambienceFXConditions.roofMaterialTagPattern = parent.roofMaterialTagPattern
      )
      .addValidator(TagPattern.VALIDATOR_CACHE.getValidator())
      .documentation("A tag pattern to use for matching roof material. If Roof is not required, will only be matched if a roof is present.")
      .add()
      .appendInherited(
         new KeyedCodec<>("Floor", Codec.BOOLEAN),
         (ambienceFXConditions, aBoolean) -> ambienceFXConditions.floor = aBoolean,
         ambienceFXConditions -> ambienceFXConditions.floor,
         (ambienceFXConditions, parent) -> ambienceFXConditions.floor = parent.floor
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("SunLightLevel", ProtocolCodecs.RANGEB),
         (ambienceFXBlockEnvironment, o) -> ambienceFXBlockEnvironment.sunLightLevel = o,
         ambienceFXBlockEnvironment -> ambienceFXBlockEnvironment.sunLightLevel,
         (ambienceFXConditions, parent) -> ambienceFXConditions.sunLightLevel = parent.sunLightLevel
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("TorchLightLevel", ProtocolCodecs.RANGEB),
         (ambienceFXBlockEnvironment, o) -> ambienceFXBlockEnvironment.torchLightLevel = o,
         ambienceFXBlockEnvironment -> ambienceFXBlockEnvironment.torchLightLevel,
         (ambienceFXConditions, parent) -> ambienceFXConditions.torchLightLevel = parent.torchLightLevel
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("GlobalLightLevel", ProtocolCodecs.RANGEB),
         (ambienceFXBlockEnvironment, o) -> ambienceFXBlockEnvironment.globalLightLevel = o,
         ambienceFXBlockEnvironment -> ambienceFXBlockEnvironment.globalLightLevel,
         (ambienceFXConditions, parent) -> ambienceFXConditions.globalLightLevel = parent.globalLightLevel
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("DayTime", ProtocolCodecs.RANGEF),
         (ambienceFXBlockEnvironment, o) -> ambienceFXBlockEnvironment.dayTime = o,
         ambienceFXBlockEnvironment -> ambienceFXBlockEnvironment.dayTime,
         (ambienceFXConditions, parent) -> ambienceFXConditions.dayTime = parent.dayTime
      )
      .add()
      .afterDecode(AmbienceFXConditions::processConfig)
      .build();
   public static final Range DEFAULT_ALTITUDE = new Range(0, 512);
   public static final Rangeb DEFAULT_WALLS = new Rangeb((byte)0, (byte)4);
   public static final Rangeb DEFAULT_LIGHT_LEVEL = new Rangeb((byte)0, (byte)15);
   public static final Rangef DEFAULT_DAY_TIME = new Rangef(0.0F, 24.0F);
   protected boolean never;
   protected String[] environmentIds;
   protected transient int[] environmentIndices;
   protected String[] weatherIds;
   protected transient int[] weatherIndices;
   protected String environmentTagPattern;
   protected String weatherTagPattern;
   protected String[] fluidFXIds;
   protected transient int[] fluidFXIndices;
   protected AmbienceFXBlockSoundSet[] surroundingBlockSoundSets;
   protected Range altitude = DEFAULT_ALTITUDE;
   protected Rangeb walls = DEFAULT_WALLS;
   protected boolean roof;
   protected String roofMaterialTagPattern;
   protected boolean floor;
   protected Rangeb sunLightLevel = DEFAULT_LIGHT_LEVEL;
   protected Rangeb torchLightLevel = DEFAULT_LIGHT_LEVEL;
   protected Rangeb globalLightLevel = DEFAULT_LIGHT_LEVEL;
   protected Rangef dayTime = DEFAULT_DAY_TIME;

   protected AmbienceFXConditions() {
   }

   @Nonnull
   public com.hypixel.hytale.protocol.AmbienceFXConditions toPacket() {
      com.hypixel.hytale.protocol.AmbienceFXConditions packet = new com.hypixel.hytale.protocol.AmbienceFXConditions();
      packet.never = this.never;
      if (this.environmentIndices != null && this.environmentIndices.length > 0) {
         packet.environmentIndices = this.environmentIndices;
      }

      if (this.environmentTagPattern != null) {
         packet.environmentTagPatternIndex = TagPattern.getAssetMap().getIndex(this.environmentTagPattern);
      } else {
         packet.environmentTagPatternIndex = -1;
      }

      if (this.weatherIndices != null && this.weatherIndices.length > 0) {
         packet.weatherIndices = this.weatherIndices;
      }

      if (this.weatherTagPattern != null) {
         packet.weatherTagPatternIndex = TagPattern.getAssetMap().getIndex(this.weatherTagPattern);
      } else {
         packet.weatherTagPatternIndex = -1;
      }

      if (this.fluidFXIndices != null) {
         packet.fluidFXIndices = this.fluidFXIndices;
      }

      if (this.surroundingBlockSoundSets != null && this.surroundingBlockSoundSets.length > 0) {
         packet.surroundingBlockSoundSets = ArrayUtil.copyAndMutate(
            this.surroundingBlockSoundSets, AmbienceFXBlockSoundSet::toPacket, com.hypixel.hytale.protocol.AmbienceFXBlockSoundSet[]::new
         );
      }

      packet.altitude = this.altitude;
      packet.walls = this.walls;
      packet.roof = this.roof;
      if (this.roofMaterialTagPattern != null) {
         packet.roofMaterialTagPatternIndex = TagPattern.getAssetMap().getIndex(this.roofMaterialTagPattern);
      } else {
         packet.roofMaterialTagPatternIndex = -1;
      }

      packet.floor = this.floor;
      packet.sunLightLevel = this.sunLightLevel;
      packet.torchLightLevel = this.torchLightLevel;
      packet.globalLightLevel = this.globalLightLevel;
      packet.dayTime = this.dayTime;
      return packet;
   }

   public boolean isNever() {
      return this.never;
   }

   public String[] getEnvironmentIds() {
      return this.environmentIds;
   }

   public int[] getEnvironmentIndices() {
      return this.environmentIndices;
   }

   public String[] getWeatherIds() {
      return this.weatherIds;
   }

   public int[] getWeatherIndices() {
      return this.weatherIndices;
   }

   public String[] getFluidFXIds() {
      return this.fluidFXIds;
   }

   public int[] getFluidFXIndices() {
      return this.fluidFXIndices;
   }

   public AmbienceFXBlockSoundSet[] getSurroundingBlockSoundSets() {
      return this.surroundingBlockSoundSets;
   }

   public Range getAltitude() {
      return this.altitude;
   }

   public Rangeb getWalls() {
      return this.walls;
   }

   public boolean getRoof() {
      return this.roof;
   }

   public boolean getFloor() {
      return this.floor;
   }

   public Rangeb getSunLightLevel() {
      return this.sunLightLevel;
   }

   public Rangeb getTorchLightLevel() {
      return this.torchLightLevel;
   }

   public Rangeb getGlobalLightLevel() {
      return this.globalLightLevel;
   }

   public Rangef getDayTime() {
      return this.dayTime;
   }

   public boolean isRoof() {
      return this.roof;
   }

   public boolean isFloor() {
      return this.floor;
   }

   protected void processConfig() {
      if (this.environmentIds != null) {
         this.environmentIndices = new int[this.environmentIds.length];

         for (int i = 0; i < this.environmentIds.length; i++) {
            this.environmentIndices[i] = Environment.getAssetMap().getIndex(this.environmentIds[i]);
         }
      }

      if (this.weatherIds != null) {
         this.weatherIndices = new int[this.weatherIds.length];

         for (int i = 0; i < this.weatherIds.length; i++) {
            this.weatherIndices[i] = Weather.getAssetMap().getIndex(this.weatherIds[i]);
         }
      }

      if (this.fluidFXIds != null) {
         this.fluidFXIndices = new int[this.fluidFXIds.length];

         for (int i = 0; i < this.fluidFXIds.length; i++) {
            this.fluidFXIndices[i] = FluidFX.getAssetMap().getIndex(this.fluidFXIds[i]);
         }
      }
   }

   @Nonnull
   @Override
   public String toString() {
      return "AmbienceFXConditions{,never="
         + this.never
         + ",environmentIds="
         + Arrays.toString((Object[])this.environmentIds)
         + ", environmentIndices="
         + Arrays.toString(this.environmentIndices)
         + ", environmentTagPattern="
         + this.environmentTagPattern
         + ", weatherIds="
         + Arrays.toString((Object[])this.weatherIds)
         + ", weatherIndices="
         + Arrays.toString(this.weatherIndices)
         + ", fluidFXIds="
         + Arrays.toString((Object[])this.fluidFXIds)
         + ", fluidFXIndices="
         + Arrays.toString(this.fluidFXIndices)
         + ", surroundingBlockSoundSets="
         + Arrays.toString((Object[])this.surroundingBlockSoundSets)
         + ", altitude="
         + this.altitude
         + ", walls="
         + this.walls
         + ", roof="
         + this.roof
         + ", roofMaterialTagPattern="
         + this.roofMaterialTagPattern
         + ", floor="
         + this.floor
         + ", sunLightLevel="
         + this.sunLightLevel
         + ", torchLightLevel="
         + this.torchLightLevel
         + ", globalLightLevel="
         + this.globalLightLevel
         + ", dayTime="
         + this.dayTime
         + "}";
   }
}
