package com.hypixel.hytale.server.npc.corecomponents.combat.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.holder.EnumSetHolder;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderSensorBase;
import com.hypixel.hytale.server.npc.corecomponents.combat.BodyMotionCharge;
import com.hypixel.hytale.server.npc.corecomponents.combat.SensorChargeState;
import com.hypixel.hytale.server.npc.instructions.Sensor;
import java.util.EnumSet;
import javax.annotation.Nonnull;

public class BuilderSensorChargeState extends BuilderSensorBase {
   protected final EnumSetHolder<BodyMotionCharge.ChargeState> states = new EnumSetHolder<>();

   @Nonnull
   public Sensor build(@Nonnull BuilderSupport builderSupport) {
      return new SensorChargeState(this, builderSupport);
   }

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Test if a BodyMotion Charge is in one of the given states.";
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
   @Override
   public Builder<Sensor> readConfig(@Nonnull JsonElement data) {
      this.requireEnumSet(
         data, "States", this.states, BodyMotionCharge.ChargeState.class, BuilderDescriptorState.Stable, "The charge states that trigger this sensor", null
      );
      this.requirePreceding(
         BodyMotionCharge.class, BuilderDescriptorState.Stable, "Requires a preceding BodyMotionCharge in the same instruction list scope", null
      );
      return this;
   }

   @Nonnull
   public EnumSet<BodyMotionCharge.ChargeState> getStates(@Nonnull BuilderSupport support) {
      return this.states.get(support.getExecutionContext());
   }
}
