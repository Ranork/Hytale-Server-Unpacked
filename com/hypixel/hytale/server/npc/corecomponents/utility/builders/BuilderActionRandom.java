package com.hypixel.hytale.server.npc.corecomponents.utility.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderObjectListHelper;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.WeightedAction;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import com.hypixel.hytale.server.npc.corecomponents.utility.ActionRandom;
import com.hypixel.hytale.server.npc.util.expression.ExecutionContext;
import com.hypixel.hytale.server.npc.util.expression.Scope;
import com.hypixel.hytale.server.npc.validators.NPCLoadTimeValidationHelper;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BuilderActionRandom extends BuilderActionBase {
   protected final BuilderObjectListHelper<WeightedAction> actions = new BuilderObjectListHelper<>(WeightedAction.class, this);

   @Nonnull
   public ActionRandom build(@Nonnull BuilderSupport builderSupport) {
      return new ActionRandom(this, builderSupport);
   }

   @Nonnull
   public BuilderActionRandom readConfig(@Nonnull JsonElement data) {
      this.requireArray(data, "Actions", this.actions, null, BuilderDescriptorState.Stable, "List of possible actions", null, this.validationHelper);
      return this;
   }

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Execute a single random action from a list of weighted actions.";
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

   @Nullable
   public List<WeightedAction> getActions(@Nonnull BuilderSupport builderSupport) {
      return this.actions.build(builderSupport);
   }
}
