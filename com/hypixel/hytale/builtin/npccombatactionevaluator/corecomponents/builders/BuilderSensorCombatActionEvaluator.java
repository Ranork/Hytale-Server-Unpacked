package com.hypixel.hytale.builtin.npccombatactionevaluator.corecomponents.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.builtin.npccombatactionevaluator.corecomponents.SensorCombatActionEvaluator;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.Feature;
import com.hypixel.hytale.server.npc.asset.builder.holder.BooleanHolder;
import com.hypixel.hytale.server.npc.asset.builder.holder.DoubleHolder;
import com.hypixel.hytale.server.npc.asset.builder.validators.DoubleSingleValidator;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderSensorBase;
import com.hypixel.hytale.server.npc.instructions.Sensor;
import com.hypixel.hytale.server.npc.util.expression.ExecutionContext;
import com.hypixel.hytale.server.npc.util.expression.Scope;
import com.hypixel.hytale.server.npc.validators.NPCLoadTimeValidationHelper;
import com.hypixel.hytale.server.npc.valuestore.ValueStoreValidator;
import java.util.List;
import java.util.function.ToIntFunction;
import javax.annotation.Nonnull;

public class BuilderSensorCombatActionEvaluator extends BuilderSensorBase {
   @Nonnull
   protected final BooleanHolder targetInRange = new BooleanHolder();
   @Nonnull
   protected final DoubleHolder allowableDeviation = new DoubleHolder();
   protected ToIntFunction<BuilderSupport> minRangeStoreSlot;
   protected ToIntFunction<BuilderSupport> maxRangeStoreSlot;
   protected ToIntFunction<BuilderSupport> positioningAngleStoreSlot;

   @Nonnull
   public Sensor build(@Nonnull BuilderSupport builderSupport) {
      return new SensorCombatActionEvaluator(this, builderSupport);
   }

   @Nonnull
   @Override
   public String getShortDescription() {
      return "A sensor which handles funnelling information to actions and motions from the combat action evaluator.";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return "A sensor which handles funnelling information to actions and motions from the combat action evaluator. Delivers the current attack target and desired range for supported direct child motions.";
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Experimental;
   }

   @Nonnull
   public BuilderSensorCombatActionEvaluator readConfig(@Nonnull JsonElement data) {
      this.requireBoolean(
         data, "TargetInRange", this.targetInRange, BuilderDescriptorState.Stable, "Whether to match on target being in or out of range.", null
      );
      this.getDouble(
         data,
         "AllowableDeviation",
         this.allowableDeviation,
         0.5,
         DoubleSingleValidator.greater0(),
         BuilderDescriptorState.Stable,
         "The allowable deviation from the desired attack range",
         null
      );
      this.minRangeStoreSlot = this.requireDoubleValueStoreParameter("CAEMinRange", ValueStoreValidator.UseType.READ);
      this.maxRangeStoreSlot = this.requireDoubleValueStoreParameter("CAEMaxRange", ValueStoreValidator.UseType.READ);
      this.positioningAngleStoreSlot = this.requireDoubleValueStoreParameter("CAEPositioningAngle", ValueStoreValidator.UseType.READ);
      this.provideFeature(Feature.LiveEntity);
      return this;
   }

   @Override
   public boolean validate(
      String configName, @Nonnull NPCLoadTimeValidationHelper validationHelper, ExecutionContext context, Scope globalScope, @Nonnull List<String> errors
   ) {
      boolean valid = true;
      String state = validationHelper.getCurrentStateName();
      if (state == null) {
         errors.add(String.format("%s: CombatActionEvaluator sensors must belong to a state! At %s", configName, this.getBreadCrumbs()));
         valid = false;
      }

      return super.validate(configName, validationHelper, context, globalScope, errors) & valid;
   }

   public boolean isTargetInRange(@Nonnull BuilderSupport support) {
      return this.targetInRange.get(support.getExecutionContext());
   }

   public int getMinRangeStoreSlot(BuilderSupport support) {
      return this.minRangeStoreSlot.applyAsInt(support);
   }

   public int getMaxRangeStoreSlot(BuilderSupport support) {
      return this.maxRangeStoreSlot.applyAsInt(support);
   }

   public int getPositioningAngleStoreSlot(BuilderSupport support) {
      return this.positioningAngleStoreSlot.applyAsInt(support);
   }

   public double getAllowableDeviation(@Nonnull BuilderSupport support) {
      return this.allowableDeviation.get(support.getExecutionContext());
   }

   public int getTargetSlot(@Nonnull BuilderSupport support) {
      return support.getTargetSlot("CAETargetSlot");
   }
}
