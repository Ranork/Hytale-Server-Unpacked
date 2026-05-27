package com.hypixel.hytale.server.npc.corecomponents.statemachine.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.InstructionType;
import com.hypixel.hytale.server.npc.asset.builder.StatePair;
import com.hypixel.hytale.server.npc.asset.builder.validators.StateStringValidator;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import com.hypixel.hytale.server.npc.corecomponents.statemachine.ActionParentState;
import javax.annotation.Nonnull;

public class BuilderActionParentState extends BuilderActionBase {
   protected String state;
   protected boolean clearHeadMotion;
   protected boolean clearBodyMotion;

   @Nonnull
   public ActionParentState build(@Nonnull BuilderSupport builderSupport) {
      return new ActionParentState(this, builderSupport);
   }

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Set the main state of NPC from within a component";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return "Set the main state of NPC from within a component";
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nonnull
   public BuilderActionParentState readConfig(@Nonnull JsonElement data) {
      this.validateIsComponent();
      StateStringValidator validator = StateStringValidator.mainStateOnly();
      this.requireString(
         data,
         "State",
         v -> this.state = validator.getMainState(),
         validator,
         BuilderDescriptorState.Stable,
         "The alias of the external state to set, as defined by _ImportStates in parameters",
         null
      );
      this.getBoolean(
         data,
         "ClearHeadMotion",
         v -> this.clearHeadMotion = v,
         true,
         BuilderDescriptorState.Stable,
         "Whether the head motion will be cleared during the state transition actions",
         null
      );
      this.getBoolean(
         data,
         "ClearBodyMotion",
         v -> this.clearBodyMotion = v,
         true,
         BuilderDescriptorState.Stable,
         "Whether the body motion will be cleared during the state transition actions",
         null
      );
      this.registerStateSetter(this.state, null, (m, s) -> {});
      this.requireInstructionType(InstructionType.StateChangeAllowedInstructions);
      return this;
   }

   public boolean isClearHeadMotion() {
      return this.clearHeadMotion;
   }

   public boolean isClearBodyMotion() {
      return this.clearBodyMotion;
   }

   @Nonnull
   public StatePair getStatePair(@Nonnull BuilderSupport support) {
      return support.getMappedStatePair(this.stateHelper.getComponentImportStateIndex(this.state));
   }
}
