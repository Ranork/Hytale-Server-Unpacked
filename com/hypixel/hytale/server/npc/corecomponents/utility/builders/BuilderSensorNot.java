package com.hypixel.hytale.server.npc.corecomponents.utility.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderObjectReferenceHelper;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.holder.StringHolder;
import com.hypixel.hytale.server.npc.asset.builder.validators.StringNullOrNotEmptyValidator;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderSensorBase;
import com.hypixel.hytale.server.npc.corecomponents.utility.SensorNot;
import com.hypixel.hytale.server.npc.instructions.Sensor;
import com.hypixel.hytale.server.npc.util.expression.ExecutionContext;
import com.hypixel.hytale.server.npc.util.expression.Scope;
import com.hypixel.hytale.server.npc.validators.NPCLoadTimeValidationHelper;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BuilderSensorNot extends BuilderSensorBase {
   protected final BuilderObjectReferenceHelper<Sensor> sensor = new BuilderObjectReferenceHelper<>(Sensor.class, this);
   protected final StringHolder targetSlot = new StringHolder();
   protected final StringHolder autoUnlockTargetSlot = new StringHolder();

   @Nullable
   public SensorNot build(@Nonnull BuilderSupport builderSupport) {
      Sensor sensor = this.getSensor(builderSupport);
      return sensor == null ? null : new SensorNot(this, builderSupport, sensor);
   }

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Invert sensor test";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return "Return true when the given sensor test fails.";
   }

   @Override
   public void registerTags(@Nonnull Set<String> tags) {
      super.registerTags(tags);
      tags.add("logic");
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.WorkInProgress;
   }

   @Nonnull
   public BuilderSensorNot readConfig(@Nonnull JsonElement data) {
      this.preventParameterOverride();
      this.requireObject(data, "Sensor", this.sensor, BuilderDescriptorState.Stable, "Sensor to test", null, this.validationHelper);
      this.getString(
         data,
         "UseTargetSlot",
         this.targetSlot,
         null,
         StringNullOrNotEmptyValidator.get(),
         BuilderDescriptorState.Stable,
         "A locked target slot to feed to action (if available)",
         null
      );
      this.getString(
         data,
         "AutoUnlockTargetSlot",
         this.autoUnlockTargetSlot,
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
         & this.sensor.validate(configName, validationHelper, this.builderManager, context, globalScope, errors);
   }

   @Nullable
   public Sensor getSensor(@Nonnull BuilderSupport support) {
      return this.sensor.build(support);
   }

   public int getUsedTargetSlot(@Nonnull BuilderSupport support) {
      String slot = this.targetSlot.get(support.getExecutionContext());
      return slot == null ? Integer.MIN_VALUE : support.getTargetSlot(slot);
   }

   public int getAutoUnlockTargetSlot(@Nonnull BuilderSupport support) {
      String slot = this.autoUnlockTargetSlot.get(support.getExecutionContext());
      return slot == null ? Integer.MIN_VALUE : support.getTargetSlot(slot);
   }
}
