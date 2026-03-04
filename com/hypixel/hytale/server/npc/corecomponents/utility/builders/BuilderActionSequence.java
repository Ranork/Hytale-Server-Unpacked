package com.hypixel.hytale.server.npc.corecomponents.utility.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderObjectReferenceHelper;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import com.hypixel.hytale.server.npc.corecomponents.utility.ActionSequence;
import com.hypixel.hytale.server.npc.instructions.ActionList;
import com.hypixel.hytale.server.npc.util.expression.ExecutionContext;
import com.hypixel.hytale.server.npc.util.expression.Scope;
import com.hypixel.hytale.server.npc.validators.NPCLoadTimeValidationHelper;
import java.util.List;
import javax.annotation.Nonnull;

public class BuilderActionSequence extends BuilderActionBase {
   protected final BuilderObjectReferenceHelper<ActionList> actions = new BuilderObjectReferenceHelper<>(ActionList.class, this);
   protected boolean blocking;
   protected boolean atomic;

   @Nonnull
   public ActionSequence build(@Nonnull BuilderSupport builderSupport) {
      return new ActionSequence(this, builderSupport);
   }

   @Nonnull
   public BuilderActionSequence readConfig(@Nonnull JsonElement data) {
      this.getBoolean(
         data,
         "Blocking",
         b -> this.blocking = b,
         false,
         BuilderDescriptorState.Stable,
         "Do not execute an action unless the previous action could execute",
         null
      );
      this.getBoolean(data, "Atomic", b -> this.atomic = b, false, BuilderDescriptorState.Stable, "Only execute actions if all actions can be executed", null);
      this.requireObject(data, "Actions", this.actions, BuilderDescriptorState.Stable, "List of actions", null, this.validationHelper);
      return this;
   }

   @Nonnull
   @Override
   public String getShortDescription() {
      return "List of actions.";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return "Execute list of actions.";
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
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
         & this.actions.validate(configName, validationHelper, this.builderManager, context, globalScope, errors);
   }

   @Nonnull
   public ActionList getActionList(@Nonnull BuilderSupport builderSupport) {
      ActionList actions = this.actions.build(builderSupport);
      if (actions != ActionList.EMPTY_ACTION_LIST) {
         actions.setAtomic(this.atomic);
         actions.setBlocking(this.blocking);
      }

      return actions;
   }
}
