package com.hypixel.hytale.server.npc.corecomponents.utility.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderObjectListHelper;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.holder.StringHolder;
import com.hypixel.hytale.server.npc.asset.builder.validators.ArrayNotEmptyValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.StringNullOrNotEmptyValidator;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderSensorBase;
import com.hypixel.hytale.server.npc.instructions.Sensor;
import com.hypixel.hytale.server.npc.util.expression.ExecutionContext;
import com.hypixel.hytale.server.npc.util.expression.Scope;
import com.hypixel.hytale.server.npc.validators.NPCLoadTimeValidationHelper;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;

public abstract class BuilderSensorMany extends BuilderSensorBase {
   @Nonnull
   protected BuilderObjectListHelper<Sensor> objectListHelper = new BuilderObjectListHelper<>(Sensor.class, this);
   protected final StringHolder unlockTargetSlot = new StringHolder();

   @Override
   public void registerTags(@Nonnull Set<String> tags) {
      super.registerTags(tags);
      tags.add("logic");
   }

   @Nonnull
   @Override
   public Builder<Sensor> readConfig(@Nonnull JsonElement data) {
      this.preventParameterOverride();
      this.requireArray(
         data, "Sensors", this.objectListHelper, ArrayNotEmptyValidator.get(), BuilderDescriptorState.Stable, "List of sensors", null, this.validationHelper
      );
      this.getString(
         data,
         "AutoUnlockTargetSlot",
         this.unlockTargetSlot,
         null,
         StringNullOrNotEmptyValidator.get(),
         BuilderDescriptorState.Stable,
         "A target slot to unlock when sensor doesn't match anymore",
         null
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
         & this.objectListHelper.validate(configName, validationHelper, this.builderManager, context, globalScope, errors);
   }

   public int getAutoUnlockedTargetSlot(@Nonnull BuilderSupport support) {
      String slot = this.unlockTargetSlot.get(support.getExecutionContext());
      return slot == null ? Integer.MIN_VALUE : support.getTargetSlot(slot);
   }
}
