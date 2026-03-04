package com.hypixel.hytale.server.npc.statetransition.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderBase;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderObjectReferenceHelper;
import com.hypixel.hytale.server.npc.asset.builder.BuilderObjectStaticListHelper;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.holder.BooleanHolder;
import com.hypixel.hytale.server.npc.instructions.ActionList;
import com.hypixel.hytale.server.npc.statetransition.StateTransitionController;
import com.hypixel.hytale.server.npc.util.expression.ExecutionContext;
import com.hypixel.hytale.server.npc.util.expression.Scope;
import com.hypixel.hytale.server.npc.validators.NPCLoadTimeValidationHelper;
import java.util.BitSet;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BuilderStateTransition extends BuilderBase<BuilderStateTransition.StateTransition> {
   protected final BuilderObjectStaticListHelper<BuilderStateTransitionEdges.StateTransitionEdges> stateTransitionEdges = new BuilderObjectStaticListHelper<>(
      BuilderStateTransitionEdges.StateTransitionEdges.class, this
   );
   protected final BuilderObjectReferenceHelper<ActionList> actions = new BuilderObjectReferenceHelper<>(ActionList.class, this);
   protected final BooleanHolder enabled = new BooleanHolder();

   @Nonnull
   @Override
   public String getShortDescription() {
      return "An entry containing a list of actions to execute when moving from one state to another";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return this.getShortDescription();
   }

   @Nonnull
   public BuilderStateTransition.StateTransition build(@Nonnull BuilderSupport builderSupport) {
      return new BuilderStateTransition.StateTransition(this, builderSupport);
   }

   @Nonnull
   @Override
   public Class<BuilderStateTransition.StateTransition> category() {
      return BuilderStateTransition.StateTransition.class;
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Override
   public boolean isEnabled(ExecutionContext context) {
      return this.enabled.get(context);
   }

   @Nonnull
   @Override
   public Builder<BuilderStateTransition.StateTransition> readConfig(@Nonnull JsonElement data) {
      this.requireArray(
         data, "States", this.stateTransitionEdges, null, BuilderDescriptorState.Stable, "List of state transitions", null, this.validationHelper
      );
      this.requireObject(data, "Actions", this.actions, BuilderDescriptorState.Stable, "List of actions", null, this.validationHelper);
      this.getBoolean(data, "Enabled", this.enabled, true, BuilderDescriptorState.Stable, "Whether this sensor should be enabled on the NPC", null);
      return this;
   }

   @Override
   public boolean validate(
      String configName,
      @Nonnull NPCLoadTimeValidationHelper validationHelper,
      @Nonnull ExecutionContext context,
      Scope globalScope,
      @Nonnull List<String> errors
   ) {
      boolean valid = super.validate(configName, validationHelper, context, globalScope, errors)
         & this.stateTransitionEdges.validate(configName, validationHelper, this.builderManager, context, globalScope, errors)
         & this.actions.validate(configName, validationHelper, this.builderManager, context, globalScope, errors);
      List<BuilderStateTransitionEdges.StateTransitionEdges> edges = this.stateTransitionEdges.staticBuild(this.builderManager);
      BitSet seenEdges = new BitSet();
      int[] allMainStates = this.stateHelper.getAllMainStates();

      for (BuilderStateTransitionEdges.StateTransitionEdges edge : edges) {
         for (int from : edge.getFromStateIndices() != null ? edge.getFromStateIndices() : allMainStates) {
            for (int to : edge.getToStateIndices() != null ? edge.getToStateIndices() : allMainStates) {
               int combinedValue = StateTransitionController.indexStateTransitionEdge(from, to);
               if (seenEdges.get(combinedValue)) {
                  errors.add(
                     String.format(
                        "Cannot define the same edge twice in state transitions: %s -> %s!",
                        this.stateHelper.getStateName(from),
                        this.stateHelper.getStateName(to)
                     )
                  );
                  valid = false;
               } else {
                  seenEdges.set(combinedValue);
               }
            }
         }
      }

      return valid;
   }

   @Nullable
   public List<BuilderStateTransitionEdges.StateTransitionEdges> getStateTransitionEdges(@Nonnull BuilderSupport support) {
      return this.stateTransitionEdges.build(support);
   }

   @Nonnull
   public ActionList getActionList(@Nonnull BuilderSupport builderSupport) {
      ActionList actions = this.actions.build(builderSupport);
      if (actions != ActionList.EMPTY_ACTION_LIST) {
         actions.setAtomic(false);
         actions.setBlocking(true);
      }

      return actions;
   }

   public static class StateTransition {
      @Nullable
      private final List<BuilderStateTransitionEdges.StateTransitionEdges> stateTransitionEdges;
      @Nonnull
      private final ActionList actions;

      private StateTransition(@Nonnull BuilderStateTransition builder, @Nonnull BuilderSupport support) {
         this.stateTransitionEdges = builder.getStateTransitionEdges(support);
         this.actions = builder.getActionList(support);
      }

      @Nullable
      public List<BuilderStateTransitionEdges.StateTransitionEdges> getStateTransitionEdges() {
         return this.stateTransitionEdges;
      }

      @Nonnull
      public ActionList getActions() {
         return this.actions;
      }
   }
}
