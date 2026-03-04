package com.hypixel.hytale.server.npc.corecomponents.utility.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderObjectReferenceHelper;
import com.hypixel.hytale.server.npc.asset.builder.BuilderObjectStaticListHelper;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.holder.BooleanHolder;
import com.hypixel.hytale.server.npc.asset.builder.validators.ArrayNotEmptyValidator;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderSensorBase;
import com.hypixel.hytale.server.npc.corecomponents.utility.SensorValueProviderWrapper;
import com.hypixel.hytale.server.npc.instructions.Sensor;
import com.hypixel.hytale.server.npc.util.expression.ExecutionContext;
import com.hypixel.hytale.server.npc.util.expression.Scope;
import com.hypixel.hytale.server.npc.validators.NPCLoadTimeValidationHelper;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BuilderSensorValueProviderWrapper extends BuilderSensorBase {
   protected final BooleanHolder passValues = new BooleanHolder();
   protected final BuilderObjectReferenceHelper<Sensor> sensor = new BuilderObjectReferenceHelper<>(Sensor.class, this);
   protected final BuilderObjectStaticListHelper<BuilderValueToParameterMapping.ValueToParameterMapping> parameterMappings = new BuilderObjectStaticListHelper<>(
      BuilderValueToParameterMapping.ValueToParameterMapping.class, this
   );

   @Nullable
   public SensorValueProviderWrapper build(@Nonnull BuilderSupport builderSupport) {
      Sensor sensor = this.getSensor(builderSupport);
      return sensor == null ? null : new SensorValueProviderWrapper(this, builderSupport, sensor);
   }

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Wraps a sensor and passes down some additional parameter overrides pulled from the value store";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return this.getShortDescription();
   }

   @Override
   public void registerTags(@Nonnull Set<String> tags) {
      super.registerTags(tags);
      tags.add("logic");
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nonnull
   public BuilderSensorValueProviderWrapper readConfig(@Nonnull JsonElement data) {
      this.getBoolean(data, "PassValues", this.passValues, true, BuilderDescriptorState.Stable, "Used to enable/disable passing of values in components", null);
      this.requireObject(data, "Sensor", this.sensor, BuilderDescriptorState.Stable, "Sensor to wrap", null, this.validationHelper);
      this.requireArray(
         data,
         "ValueToParameterMappings",
         this.parameterMappings,
         ArrayNotEmptyValidator.get(),
         BuilderDescriptorState.Stable,
         "The mappings of values to override parameters",
         null,
         this.validationHelper
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
      boolean valid = super.validate(configName, validationHelper, context, globalScope, errors)
         & this.sensor.validate(configName, validationHelper, this.builderManager, context, globalScope, errors)
         & this.parameterMappings.validate(configName, validationHelper, this.builderManager, context, globalScope, errors);
      HashSet<String> parameterSlotNames = new HashSet<>();

      for (BuilderValueToParameterMapping.ValueToParameterMapping mapping : this.parameterMappings.staticBuild(this.builderManager)) {
         String name = mapping.getToParameterSlotName();
         if (!parameterSlotNames.add(name)) {
            errors.add(String.format("%s: Cannot write values to the same parameter override from more than one source: %s", configName, name));
            valid = false;
         }
      }

      return valid;
   }

   public boolean isPassValues(@Nonnull BuilderSupport support) {
      return this.passValues.get(support.getExecutionContext());
   }

   @Nullable
   public Sensor getSensor(@Nonnull BuilderSupport support) {
      return this.sensor.build(support);
   }

   @Nullable
   public List<BuilderValueToParameterMapping.ValueToParameterMapping> getParameterMappings(@Nonnull BuilderSupport support) {
      return !this.passValues.get(support.getExecutionContext()) ? null : this.parameterMappings.build(support);
   }
}
