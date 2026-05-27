package com.hypixel.hytale.server.npc.corecomponents.combat.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.Feature;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderSensorBase;
import com.hypixel.hytale.server.npc.corecomponents.combat.BodyMotionCharge;
import com.hypixel.hytale.server.npc.corecomponents.combat.SensorChargeBlockCollisions;
import com.hypixel.hytale.server.npc.instructions.Sensor;
import javax.annotation.Nonnull;

public class BuilderSensorChargeBlockCollisions extends BuilderSensorBase {
   @Nonnull
   public Sensor build(@Nonnull BuilderSupport builderSupport) {
      return new SensorChargeBlockCollisions(this, builderSupport);
   }

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Match when the preceding BodyMotion Charge reports at least one block collision this tick.";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return "Match when the preceding BodyMotion Charge reports at least one block collision in the current rail-step tick. Provides a BlockCollisionProvider exposing the list of BlockHit entries to downstream actions.";
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nonnull
   @Override
   public Builder<Sensor> readConfig(@Nonnull JsonElement data) {
      this.requirePreceding(
         BodyMotionCharge.class, BuilderDescriptorState.Stable, "Requires a preceding BodyMotionCharge in the same instruction list scope", null
      );
      this.provideFeature(Feature.BlockHits);
      return this;
   }
}
