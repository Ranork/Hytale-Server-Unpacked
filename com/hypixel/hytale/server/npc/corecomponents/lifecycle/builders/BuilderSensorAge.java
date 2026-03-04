package com.hypixel.hytale.server.npc.corecomponents.lifecycle.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.core.modules.time.WorldTimeResource;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.holder.TemporalArrayHolder;
import com.hypixel.hytale.server.npc.asset.builder.validators.TemporalSequenceValidator;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderSensorBase;
import com.hypixel.hytale.server.npc.corecomponents.lifecycle.SensorAge;
import com.hypixel.hytale.server.npc.instructions.Sensor;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.temporal.TemporalAmount;
import javax.annotation.Nonnull;

public class BuilderSensorAge extends BuilderSensorBase {
   public static final TemporalAmount MIN_TIME = Duration.ZERO;
   public static final TemporalAmount MAX_TIME = Period.ofDays(Integer.MAX_VALUE);
   protected final TemporalArrayHolder ageRange = new TemporalArrayHolder();

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Triggers when the age of the NPC falls between a certain range";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return "Triggers when the age of the NPC falls between a certain range. Range is defined in terms of period (e.g. 1Y2M3W4D - 1 year, 2 months, 3 weeks, 4 days) or duration (e.g. 2DT3H4M - 2 days, 3 hours, 4 minutes)";
   }

   @Nonnull
   public Sensor build(@Nonnull BuilderSupport builderSupport) {
      return new SensorAge(this, builderSupport);
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nonnull
   @Override
   public Builder<Sensor> readConfig(@Nonnull JsonElement data) {
      this.requireTemporalRange(
         data,
         "AgeRange",
         this.ageRange,
         TemporalSequenceValidator.betweenMonotonic(MIN_TIME, MAX_TIME),
         BuilderDescriptorState.Stable,
         "The age range within which to trigger",
         null
      );
      return this;
   }

   @Nonnull
   public Instant[] getAgeRange(@Nonnull BuilderSupport support) {
      Instant spawnInstant = support.getEntity().getSpawnInstant();
      LocalDateTime spawnTime = LocalDateTime.ofInstant(spawnInstant, WorldTimeResource.ZONE_OFFSET);
      TemporalAmount[] range = this.ageRange.getTemporalArray(support.getExecutionContext());
      Instant[] ageInstants = new Instant[range.length];
      ageInstants[0] = spawnTime.plus(range[0]).toInstant(WorldTimeResource.ZONE_OFFSET);
      ageInstants[1] = spawnTime.plus(range[1]).toInstant(WorldTimeResource.ZONE_OFFSET);
      return ageInstants;
   }
}
