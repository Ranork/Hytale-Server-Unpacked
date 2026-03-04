package com.hypixel.hytale.server.flock.corecomponents.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.flock.corecomponents.SensorFlockCombatDamage;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.Feature;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderSensorBase;
import com.hypixel.hytale.server.npc.instructions.Sensor;
import javax.annotation.Nonnull;

public class BuilderSensorFlockCombatDamage extends BuilderSensorBase {
   protected boolean leaderOnly;

   @Nonnull
   public SensorFlockCombatDamage build(BuilderSupport builderSupport) {
      return new SensorFlockCombatDamage(this);
   }

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Test if flock with NPC received combat damage";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return "Return true if flock with NPC received combat damage. Target position is entity which did most damage.";
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nonnull
   @Override
   public Builder<Sensor> readConfig(@Nonnull JsonElement data) {
      this.getBoolean(data, "LeaderOnly", v -> this.leaderOnly = v, true, BuilderDescriptorState.Stable, "Only test for damage to flock leader", null);
      this.provideFeature(Feature.LiveEntity);
      return this;
   }

   public boolean isLeaderOnly() {
      return this.leaderOnly;
   }
}
