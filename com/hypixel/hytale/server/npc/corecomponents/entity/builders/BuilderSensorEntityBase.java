package com.hypixel.hytale.server.npc.corecomponents.entity.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderObjectReferenceHelper;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.BuilderValidationHelper;
import com.hypixel.hytale.server.npc.asset.builder.ComponentContext;
import com.hypixel.hytale.server.npc.asset.builder.Feature;
import com.hypixel.hytale.server.npc.asset.builder.holder.BooleanHolder;
import com.hypixel.hytale.server.npc.asset.builder.holder.DoubleHolder;
import com.hypixel.hytale.server.npc.asset.builder.holder.StringHolder;
import com.hypixel.hytale.server.npc.asset.builder.validators.DoubleSingleValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.RelationalOperator;
import com.hypixel.hytale.server.npc.asset.builder.validators.StringNullOrNotEmptyValidator;
import com.hypixel.hytale.server.npc.corecomponents.ISensorEntityCollector;
import com.hypixel.hytale.server.npc.corecomponents.ISensorEntityPrioritiser;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderSensorWithEntityFilters;
import com.hypixel.hytale.server.npc.corecomponents.entity.prioritisers.SensorEntityPrioritiserDefault;
import com.hypixel.hytale.server.npc.instructions.Sensor;
import com.hypixel.hytale.server.npc.util.expression.ExecutionContext;
import com.hypixel.hytale.server.npc.util.expression.Scope;
import com.hypixel.hytale.server.npc.validators.NPCLoadTimeValidationHelper;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class BuilderSensorEntityBase extends BuilderSensorWithEntityFilters {
   protected final DoubleHolder range = new DoubleHolder();
   protected final DoubleHolder minRange = new DoubleHolder();
   protected final BooleanHolder lockOnTarget = new BooleanHolder();
   protected final BooleanHolder autoUnlockTarget = new BooleanHolder();
   protected final BooleanHolder onlyLockedTarget = new BooleanHolder();
   protected final StringHolder lockedTargetSlot = new StringHolder();
   protected final StringHolder ignoredTargetSlot = new StringHolder();
   protected final BooleanHolder useProjectedDistance = new BooleanHolder();
   protected final BuilderObjectReferenceHelper<ISensorEntityPrioritiser> prioritiser = new BuilderObjectReferenceHelper<>(ISensorEntityPrioritiser.class, this);
   protected final BuilderObjectReferenceHelper<ISensorEntityCollector> collector = new BuilderObjectReferenceHelper<>(ISensorEntityCollector.class, this);

   @Nonnull
   @Override
   public Builder<Sensor> readConfig(@Nonnull JsonElement data) {
      this.getDouble(
         data, "MinRange", this.minRange, 0.0, DoubleSingleValidator.greaterEqual0(), BuilderDescriptorState.Stable, "Minimum range to test entities in", null
      );
      this.requireDouble(data, "Range", this.range, DoubleSingleValidator.greater0(), BuilderDescriptorState.Stable, "Maximum range to test entities in", null);
      this.getBoolean(data, "LockOnTarget", this.lockOnTarget, false, BuilderDescriptorState.Stable, "Matched target becomes locked target", null);
      this.getString(
         data,
         "LockedTargetSlot",
         this.lockedTargetSlot,
         "LockedTarget",
         StringNullOrNotEmptyValidator.get(),
         BuilderDescriptorState.Stable,
         "The target slot to use for locking on or unlocking",
         null
      );
      this.getBoolean(
         data,
         "AutoUnlockTarget",
         this.autoUnlockTarget,
         false,
         BuilderDescriptorState.Stable,
         "Unlock locked target when sensor not matching it anymore",
         null
      );
      this.getBoolean(data, "OnlyLockedTarget", this.onlyLockedTarget, false, BuilderDescriptorState.Stable, "Test only locked target", null);
      this.getString(
         data,
         "IgnoredTargetSlot",
         this.ignoredTargetSlot,
         null,
         StringNullOrNotEmptyValidator.get(),
         BuilderDescriptorState.Stable,
         "The target slot to use for ignoring",
         null
      );
      this.getBoolean(
         data,
         "UseProjectedDistance",
         this.useProjectedDistance,
         false,
         BuilderDescriptorState.Stable,
         "Use the projected movement direction vector for distance, rather than the Euclidean distance",
         null
      );
      this.getObject(
         data,
         "Prioritiser",
         this.prioritiser,
         BuilderDescriptorState.Stable,
         "A prioritiser for selecting results based on additional parameters",
         null,
         this.validationHelper
      );
      this.getObject(
         data,
         "Collector",
         this.collector,
         BuilderDescriptorState.Stable,
         "A collector which can process all checked entities and act on them based on whether they match or not",
         null,
         this.validationHelper
      );
      BuilderValidationHelper builderHelper = this.createFilterValidationHelper(ComponentContext.SensorEntity);
      this.getArray(data, "Filters", this.filters, null, BuilderDescriptorState.Stable, "A series of entity filter sensors to test", null, builderHelper);
      this.validateDoubleRelation(this.range, RelationalOperator.GreaterEqual, this.minRange);
      this.provideFeature(Feature.LiveEntity);
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
      boolean result = this.prioritiser.validate(configName, validationHelper, this.builderManager, context, globalScope, errors)
         & super.validate(configName, validationHelper, context, globalScope, errors)
         & this.collector.validate(configName, validationHelper, this.builderManager, context, globalScope, errors);
      validationHelper.clearPrioritiserProvidedFilterTypes();
      return result;
   }

   public double getRange(@Nonnull BuilderSupport builderSupport) {
      return this.range.get(builderSupport.getExecutionContext());
   }

   public double getMinRange(@Nonnull BuilderSupport builderSupport) {
      return this.minRange.get(builderSupport.getExecutionContext());
   }

   public boolean isLockOnTarget(@Nonnull BuilderSupport builderSupport) {
      return this.lockOnTarget.get(builderSupport.getExecutionContext());
   }

   public boolean isOnlyLockedTarget(@Nonnull BuilderSupport builderSupport) {
      return this.onlyLockedTarget.get(builderSupport.getExecutionContext());
   }

   public int getLockedTargetSlot(@Nonnull BuilderSupport support) {
      return !this.lockOnTarget.get(support.getExecutionContext()) && !this.onlyLockedTarget.get(support.getExecutionContext())
         ? Integer.MIN_VALUE
         : support.getTargetSlot(this.lockedTargetSlot.get(support.getExecutionContext()));
   }

   public int getIgnoredTargetSlot(@Nonnull BuilderSupport support) {
      String slot = this.ignoredTargetSlot.get(support.getExecutionContext());
      return slot == null ? Integer.MIN_VALUE : support.getTargetSlot(slot);
   }

   public boolean isAutoUnlockTarget(@Nonnull BuilderSupport builderSupport) {
      return this.autoUnlockTarget.get(builderSupport.getExecutionContext());
   }

   public boolean isUseProjectedDistance(@Nonnull BuilderSupport support) {
      return this.useProjectedDistance.get(support.getExecutionContext());
   }

   @Nullable
   public ISensorEntityPrioritiser getPrioritiser(@Nonnull BuilderSupport support) {
      return (ISensorEntityPrioritiser)(!this.prioritiser.isPresent() ? new SensorEntityPrioritiserDefault() : this.prioritiser.build(support));
   }

   @Nullable
   public ISensorEntityCollector getCollector(@Nonnull BuilderSupport support) {
      return this.collector.isPresent() ? this.collector.build(support) : ISensorEntityCollector.DEFAULT;
   }
}
