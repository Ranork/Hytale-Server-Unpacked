package com.hypixel.hytale.server.core.asset.type.gameplay;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.asset.type.gameplay.sleep.SleepConfig;
import javax.annotation.Nonnull;

public class WorldConfig {
   @Nonnull
   public static final BuilderCodec<WorldConfig> CODEC = BuilderCodec.builder(WorldConfig.class, WorldConfig::new)
      .append(
         new KeyedCodec<>("AllowBlockBreaking", Codec.BOOLEAN),
         (worldConfig, o) -> worldConfig.allowBlockBreaking = o,
         worldConfig -> worldConfig.allowBlockBreaking
      )
      .add()
      .append(
         new KeyedCodec<>("AllowBlockGathering", Codec.BOOLEAN),
         (worldConfig, o) -> worldConfig.allowBlockGathering = o,
         worldConfig -> worldConfig.allowBlockGathering
      )
      .add()
      .append(
         new KeyedCodec<>("AllowBlockPlacement", Codec.BOOLEAN),
         (worldConfig, o) -> worldConfig.allowBlockPlacement = o,
         worldConfig -> worldConfig.allowBlockPlacement
      )
      .add()
      .<Double>append(
         new KeyedCodec<>("BlockPlacementFragilityTimer", Codec.DOUBLE),
         (worldConfig, d) -> worldConfig.blockPlacementFragilityTimer = d.floatValue(),
         worldConfig -> (double)worldConfig.blockPlacementFragilityTimer
      )
      .documentation("The timer, in seconds, that blocks have after placement during which they are fragile and can be broken instantly")
      .add()
      .<Integer>append(
         new KeyedCodec<>("DaytimeDurationSeconds", Codec.INTEGER),
         (worldConfig, i) -> worldConfig.daytimeDurationSeconds = i,
         worldConfig -> worldConfig.daytimeDurationSeconds
      )
      .documentation("The number of real-world seconds it takes for the day to pass (from sunrise to sunset)")
      .add()
      .<Integer>append(
         new KeyedCodec<>("NighttimeDurationSeconds", Codec.INTEGER),
         (worldConfig, i) -> worldConfig.nighttimeDurationSeconds = i,
         worldConfig -> worldConfig.nighttimeDurationSeconds
      )
      .documentation("The number of real-world seconds it takes for the night to pass (from sunset to sunrise)")
      .add()
      .append(new KeyedCodec<>("TotalMoonPhases", Codec.INTEGER), (worldConfig, i) -> worldConfig.totalMoonPhases = i, o -> o.totalMoonPhases)
      .add()
      .<SleepConfig>append(
         new KeyedCodec<>("Sleep", SleepConfig.CODEC), (worldConfig, sleepConfig) -> worldConfig.sleepConfig = sleepConfig, o -> o.sleepConfig
      )
      .documentation("Configurations related to sleeping in this world (in beds)")
      .add()
      .build();
   public static final int DEFAULT_TOTAL_DAY_DURATION_SECONDS = 2880;
   public static final int DEFAULT_DAYTIME_DURATION_SECONDS = 1728;
   public static final int DEFAULT_NIGHTTIME_DURATION_SECONDS = 1728;
   protected boolean allowBlockBreaking = true;
   protected boolean allowBlockGathering = true;
   protected boolean allowBlockPlacement = true;
   protected int daytimeDurationSeconds = 1728;
   protected int nighttimeDurationSeconds = 1728;
   private int totalMoonPhases = 5;
   protected float blockPlacementFragilityTimer;
   private SleepConfig sleepConfig = SleepConfig.DEFAULT;

   public boolean isBlockBreakingAllowed() {
      return this.allowBlockBreaking;
   }

   public boolean isBlockGatheringAllowed() {
      return this.allowBlockGathering;
   }

   public boolean isBlockPlacementAllowed() {
      return this.allowBlockPlacement;
   }

   public int getDaytimeDurationSeconds() {
      return this.daytimeDurationSeconds;
   }

   public int getNighttimeDurationSeconds() {
      return this.nighttimeDurationSeconds;
   }

   public int getTotalMoonPhases() {
      return this.totalMoonPhases;
   }

   public float getBlockPlacementFragilityTimer() {
      return this.blockPlacementFragilityTimer;
   }

   public SleepConfig getSleepConfig() {
      return this.sleepConfig;
   }
}
