package com.hypixel.hytale.server.npc.corecomponents.combat.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.Feature;
import com.hypixel.hytale.server.npc.asset.builder.holder.BooleanHolder;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderSensorBase;
import com.hypixel.hytale.server.npc.corecomponents.combat.BodyMotionCharge;
import com.hypixel.hytale.server.npc.corecomponents.combat.SensorChargeEntityCollisions;
import com.hypixel.hytale.server.npc.instructions.Sensor;
import javax.annotation.Nonnull;

public class BuilderSensorChargeEntityCollisions extends BuilderSensorBase {
   protected final BooleanHolder getPlayers = new BooleanHolder();
   protected final BooleanHolder getNPCs = new BooleanHolder();
   protected final BooleanHolder excludeOwnType = new BooleanHolder();

   @Nonnull
   public Sensor build(@Nonnull BuilderSupport builderSupport) {
      return new SensorChargeEntityCollisions(this, builderSupport);
   }

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Match when the preceding BodyMotion Charge reports at least one entity collision this tick.";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return "Match when the preceding BodyMotion Charge reports at least one entity collision in the current rail-step tick. Provides an EntityCollisionProvider exposing the filtered list of EntityHit entries to downstream actions.";
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nonnull
   @Override
   public Builder<Sensor> readConfig(@Nonnull JsonElement data) {
      this.getBoolean(data, "GetPlayers", this.getPlayers, true, BuilderDescriptorState.Stable, "Include player hits from the charge motion", null);
      this.getBoolean(data, "GetNPCs", this.getNPCs, true, BuilderDescriptorState.Stable, "Include NPC hits from the charge motion", null);
      this.getBoolean(
         data,
         "ExcludeOwnType",
         this.excludeOwnType,
         false,
         BuilderDescriptorState.Stable,
         "When GetNPCs is true, exclude NPCs with the same role type as self",
         null
      );
      this.validateAny(this.getPlayers, this.getNPCs);
      this.requirePreceding(
         BodyMotionCharge.class, BuilderDescriptorState.Stable, "Requires a preceding BodyMotionCharge in the same instruction list scope", null
      );
      this.provideFeature(Feature.EntityHits);
      return this;
   }

   public boolean isGetPlayers(@Nonnull BuilderSupport support) {
      return this.getPlayers.get(support.getExecutionContext());
   }

   public boolean isGetNPCs(@Nonnull BuilderSupport support) {
      return this.getNPCs.get(support.getExecutionContext());
   }

   public boolean isExcludeOwnType(@Nonnull BuilderSupport support) {
      return this.excludeOwnType.get(support.getExecutionContext());
   }
}
