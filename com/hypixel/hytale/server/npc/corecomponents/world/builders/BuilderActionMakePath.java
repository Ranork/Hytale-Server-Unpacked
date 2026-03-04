package com.hypixel.hytale.server.npc.corecomponents.world.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.builtin.path.path.TransientPathDefinition;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderObjectReferenceHelper;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.BuilderValidationHelper;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import com.hypixel.hytale.server.npc.corecomponents.world.ActionMakePath;
import com.hypixel.hytale.server.npc.instructions.Action;
import com.hypixel.hytale.server.npc.util.expression.ExecutionContext;
import com.hypixel.hytale.server.npc.util.expression.Scope;
import com.hypixel.hytale.server.npc.validators.NPCLoadTimeValidationHelper;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BuilderActionMakePath extends BuilderActionBase {
   protected final BuilderObjectReferenceHelper<TransientPathDefinition> transientPath = new BuilderObjectReferenceHelper<>(TransientPathDefinition.class, this);

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Constructs a transient path for the NPC based on a series of rotations and distances";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return this.getShortDescription();
   }

   @Nonnull
   public Action build(@Nonnull BuilderSupport builderSupport) {
      return new ActionMakePath(this, builderSupport);
   }

   @Nonnull
   public BuilderActionMakePath readConfig(@Nonnull JsonElement data) {
      this.requireObject(
         data,
         "Path",
         this.transientPath,
         BuilderDescriptorState.WorkInProgress,
         "A transient path definition",
         null,
         new BuilderValidationHelper(this.fileName, null, this.internalReferenceResolver, null, null, this.extraInfo, null, this.readErrors)
      );
      return this;
   }

   @Override
   public void registerTags(@Nonnull Set<String> tags) {
      super.registerTags(tags);
      tags.add("path");
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.WorkInProgress;
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
         & this.transientPath.validate(configName, validationHelper, this.builderManager, context, globalScope, errors);
   }

   @Nullable
   public TransientPathDefinition getPath(@Nonnull BuilderSupport support) {
      return this.transientPath.build(support);
   }
}
