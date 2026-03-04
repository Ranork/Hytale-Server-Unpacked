package com.hypixel.hytale.builtin.adventure.npcobjectives.npc.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.builtin.adventure.npcobjectives.npc.ActionCompleteTask;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.InstructionType;
import com.hypixel.hytale.server.npc.asset.builder.holder.BooleanHolder;
import com.hypixel.hytale.server.npc.corecomponents.audiovisual.builders.BuilderActionPlayAnimation;
import java.util.EnumSet;
import javax.annotation.Nonnull;

public class BuilderActionCompleteTask extends BuilderActionPlayAnimation {
   @Nonnull
   protected final BooleanHolder playAnimation = new BooleanHolder();

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Complete a task";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return "Complete a task. Tasks are picked based on those provided to SensorCanInteract.";
   }

   @Nonnull
   public ActionCompleteTask build(@Nonnull BuilderSupport builderSupport) {
      return new ActionCompleteTask(this, builderSupport);
   }

   @Nonnull
   public BuilderActionCompleteTask readConfig(@Nonnull JsonElement data) {
      super.readConfig(data);
      this.getBoolean(
         data,
         "PlayAnimation",
         this.playAnimation,
         true,
         BuilderDescriptorState.Stable,
         "Whether or not to play the animation associated with completing this task",
         null
      );
      this.requireInstructionType(EnumSet.of(InstructionType.Interaction));
      return this;
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   public boolean isPlayAnimation(@Nonnull BuilderSupport support) {
      return this.playAnimation.get(support.getExecutionContext());
   }
}
