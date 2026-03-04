package com.hypixel.hytale.server.npc.corecomponents.statemachine.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderSensorBase;
import com.hypixel.hytale.server.npc.corecomponents.statemachine.SensorState;
import com.hypixel.hytale.server.npc.instructions.Sensor;
import javax.annotation.Nonnull;

public class BuilderSensorState extends BuilderSensorBase {
   protected String state;
   protected String subState;
   protected int stateIndex;
   protected int subStateIndex;
   protected boolean defaultSubState;
   protected boolean ignoreMissingSetState;
   protected boolean componentLocal;

   @Nonnull
   public SensorState build(@Nonnull BuilderSupport builderSupport) {
      return new SensorState(this, builderSupport);
   }

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Test for a specific state";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return "Signal if NPC is set to specific state.";
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nonnull
   @Override
   public Builder<Sensor> readConfig(@Nonnull JsonElement data) {
      this.requireStateString(data, "State", true, (state, subState, isDefault) -> {
         this.state = state;
         this.subState = subState;
         this.defaultSubState = isDefault;
      }, BuilderDescriptorState.Stable, "State to compare to", null);
      this.getBoolean(
         data,
         "IgnoreMissingSetState",
         v -> this.ignoreMissingSetState = v,
         false,
         BuilderDescriptorState.Stable,
         "Override and ignore checks for matching setter action that sets this state",
         "Override and ignore checks for matching setter action that sets this state. Intended for use in cases such as the FlockState action which sets the state via another NPC"
      );
      this.registerStateSensor(this.state, this.subState, this::setIndexes);
      if (this.ignoreMissingSetState) {
         this.registerStateSetter(this.state, this.subState, (m, v) -> {});
      }

      this.componentLocal = this.isComponent();
      return this;
   }

   public int getState() {
      return this.stateIndex;
   }

   public void setIndexes(int main, int sub) {
      this.stateIndex = main;
      this.subStateIndex = sub;
   }

   public boolean isDefaultSubState() {
      return this.defaultSubState;
   }

   public int getSubStateIndex() {
      return this.subStateIndex;
   }

   public boolean isComponentLocal() {
      return this.componentLocal;
   }
}
