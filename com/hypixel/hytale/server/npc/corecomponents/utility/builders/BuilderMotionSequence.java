package com.hypixel.hytale.server.npc.corecomponents.utility.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderObjectListHelper;
import com.hypixel.hytale.server.npc.asset.builder.validators.ArrayNotEmptyValidator;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderMotionBase;
import com.hypixel.hytale.server.npc.instructions.Motion;
import com.hypixel.hytale.server.npc.util.expression.ExecutionContext;
import com.hypixel.hytale.server.npc.util.expression.Scope;
import com.hypixel.hytale.server.npc.validators.NPCLoadTimeValidationHelper;
import java.util.List;
import javax.annotation.Nonnull;

public abstract class BuilderMotionSequence<T extends Motion> extends BuilderMotionBase<T> {
   protected BuilderObjectListHelper<T> steps;
   protected boolean looped;
   protected boolean restartOnActivate;

   @Nonnull
   @Override
   public String getShortDescription() {
      return "(Looped)Sequence of motions";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return "Sequence of motions. Can be used in conjunction with 'Timer' to model more complex motions.";
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nonnull
   public BuilderMotionSequence<T> readConfig(@Nonnull JsonElement data) {
      this.getBoolean(data, "Looped", b -> this.looped = b, true, BuilderDescriptorState.Stable, "When true restart after last motion is finished", null);
      this.getBoolean(
         data,
         "RestartOnActivate",
         b -> this.restartOnActivate = b,
         false,
         BuilderDescriptorState.Experimental,
         "Restart from first motion when NPC is activated.",
         null
      );
      this.requireArray(
         data, "Motions", this.steps, ArrayNotEmptyValidator.get(), BuilderDescriptorState.Stable, "Array of motions", null, this.validationHelper
      );
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
      return super.validate(configName, validationHelper, context, globalScope, errors)
         & this.steps.validate(configName, validationHelper, this.builderManager, context, globalScope, errors);
   }

   public boolean isLooped() {
      return this.looped;
   }

   public boolean isRestartOnActivate() {
      return this.restartOnActivate;
   }
}
