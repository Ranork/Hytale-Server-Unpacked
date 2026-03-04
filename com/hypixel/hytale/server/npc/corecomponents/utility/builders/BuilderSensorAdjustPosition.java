package com.hypixel.hytale.server.npc.corecomponents.utility.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderObjectReferenceHelper;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.Feature;
import com.hypixel.hytale.server.npc.asset.builder.holder.NumberArrayHolder;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderSensorBase;
import com.hypixel.hytale.server.npc.corecomponents.utility.SensorAdjustPosition;
import com.hypixel.hytale.server.npc.instructions.Sensor;
import com.hypixel.hytale.server.npc.util.expression.ExecutionContext;
import com.hypixel.hytale.server.npc.util.expression.Scope;
import com.hypixel.hytale.server.npc.validators.NPCLoadTimeValidationHelper;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BuilderSensorAdjustPosition extends BuilderSensorBase {
   protected final BuilderObjectReferenceHelper<Sensor> sensor = new BuilderObjectReferenceHelper<>(Sensor.class, this);
   protected final NumberArrayHolder offset = new NumberArrayHolder();

   @Nullable
   public SensorAdjustPosition build(@Nonnull BuilderSupport builderSupport) {
      Sensor sensor = this.getSensor(builderSupport);
      return sensor == null ? null : new SensorAdjustPosition(this, builderSupport, sensor);
   }

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Perform adjustments to the wrapped sensor's returned position";
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
   public BuilderSensorAdjustPosition readConfig(@Nonnull JsonElement data) {
      this.requireObject(data, "Sensor", this.sensor, BuilderDescriptorState.Stable, "Sensor to wrap", null, this.validationHelper);
      this.requireVector3d(
         data, "Offset", this.offset, null, BuilderDescriptorState.Stable, "The offset to apply to the returned position from the sensor", null
      );
      this.provideFeature(Feature.Position);
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

   @Nonnull
   public Vector3d getOffset(@Nonnull BuilderSupport support) {
      double[] offsetArray = this.offset.get(support.getExecutionContext());
      return new Vector3d(offsetArray[0], offsetArray[1], offsetArray[2]);
   }
}
