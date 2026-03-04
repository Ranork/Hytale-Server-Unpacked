package com.hypixel.hytale.builtin.adventure.npcobjectives.npc.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.builtin.adventure.npcobjectives.npc.ActionStartObjective;
import com.hypixel.hytale.builtin.adventure.npcobjectives.npc.validators.ObjectiveExistsValidator;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.InstructionType;
import com.hypixel.hytale.server.npc.asset.builder.holder.AssetHolder;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import java.util.EnumSet;
import javax.annotation.Nonnull;

public class BuilderActionStartObjective extends BuilderActionBase {
   @Nonnull
   protected final AssetHolder objectiveId = new AssetHolder();

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Start the given objective for the currently iterated player in the interaction instruction";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return this.getShortDescription();
   }

   @Nonnull
   public ActionStartObjective build(@Nonnull BuilderSupport builderSupport) {
      return new ActionStartObjective(this, builderSupport);
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nonnull
   public BuilderActionStartObjective readConfig(@Nonnull JsonElement data) {
      this.requireAsset(data, "Objective", this.objectiveId, ObjectiveExistsValidator.required(), BuilderDescriptorState.Stable, "The task to start", null);
      this.requireInstructionType(EnumSet.of(InstructionType.Interaction));
      return this;
   }

   public String getObjectiveId(@Nonnull BuilderSupport support) {
      return this.objectiveId.get(support.getExecutionContext());
   }
}
