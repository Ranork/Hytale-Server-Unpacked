package com.hypixel.hytale.server.npc.corecomponents.timer.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.holder.StringHolder;
import com.hypixel.hytale.server.npc.asset.builder.holder.TemporalArrayHolder;
import com.hypixel.hytale.server.npc.asset.builder.validators.StringNotEmptyValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.TemporalSequenceValidator;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import com.hypixel.hytale.server.npc.corecomponents.timer.ActionSetAlarm;
import com.hypixel.hytale.server.npc.util.Alarm;
import java.time.Duration;
import java.time.Period;
import java.time.temporal.TemporalAmount;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BuilderActionSetAlarm extends BuilderActionBase {
   public static final TemporalAmount MIN_TIME = Duration.ZERO;
   public static final TemporalAmount MAX_TIME = Period.ofDays(Integer.MAX_VALUE);
   protected final StringHolder name = new StringHolder();
   protected final TemporalArrayHolder durationRange = new TemporalArrayHolder();

   @Nonnull
   public ActionSetAlarm build(@Nonnull BuilderSupport builderSupport) {
      return new ActionSetAlarm(this, builderSupport);
   }

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Set a named alarm on the NPC";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return this.getShortDescription();
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nonnull
   public BuilderActionSetAlarm readConfig(@Nonnull JsonElement data) {
      this.requireString(data, "Name", this.name, StringNotEmptyValidator.get(), BuilderDescriptorState.Stable, "The name of the alarm to set", null);
      this.requireTemporalRange(
         data,
         "DurationRange",
         this.durationRange,
         TemporalSequenceValidator.betweenWeaklyMonotonic(MIN_TIME, MAX_TIME),
         BuilderDescriptorState.Stable,
         "The duration range from which to pick a duration to set the alarm for",
         "The duration range from which to pick a duration to set the alarm for. [ \"P0D\", \"P0D\" ] will unset the alarm"
      );
      return this;
   }

   public Alarm getAlarm(@Nonnull BuilderSupport support) {
      return support.getAlarm(this.name.get(support.getExecutionContext()));
   }

   @Nullable
   public TemporalAmount[] getDurationRange(@Nonnull BuilderSupport support) {
      return this.durationRange.getTemporalArray(support.getExecutionContext());
   }
}
