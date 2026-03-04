package com.hypixel.hytale.server.npc.corecomponents.world.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.holder.NumberArrayHolder;
import com.hypixel.hytale.server.npc.asset.builder.holder.StringHolder;
import com.hypixel.hytale.server.npc.asset.builder.validators.DoubleSequenceValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.StringNullOrNotEmptyValidator;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderSensorBase;
import com.hypixel.hytale.server.npc.corecomponents.world.SensorLight;
import com.hypixel.hytale.server.npc.instructions.Sensor;
import javax.annotation.Nonnull;

public class BuilderSensorLight extends BuilderSensorBase {
   public static final double[] FULL_LIGHT_RANGE = new double[]{0.0, 100.0};
   protected final StringHolder useTargetSlot = new StringHolder();
   protected final NumberArrayHolder lightRange = new NumberArrayHolder();
   protected final NumberArrayHolder skyLightRange = new NumberArrayHolder();
   protected final NumberArrayHolder sunlightRange = new NumberArrayHolder();
   protected final NumberArrayHolder redLightRange = new NumberArrayHolder();
   protected final NumberArrayHolder greenLightRange = new NumberArrayHolder();
   protected final NumberArrayHolder blueLightRange = new NumberArrayHolder();

   @Nonnull
   public SensorLight build(@Nonnull BuilderSupport builderSupport) {
      return new SensorLight(this, builderSupport);
   }

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Check the light levels of the block an entity is standing on";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return "Check the light levels of the block an entity is standing on. Can test light intensity, sky light or block channel levels.";
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nonnull
   @Override
   public Builder<Sensor> readConfig(@Nonnull JsonElement data) {
      this.getDoubleRange(
         data,
         "LightRange",
         this.lightRange,
         FULL_LIGHT_RANGE,
         DoubleSequenceValidator.betweenWeaklyMonotonic(0.0, 100.0),
         BuilderDescriptorState.Stable,
         "The light intensity percentage range",
         null
      );
      this.getDoubleRange(
         data,
         "SkyLightRange",
         this.skyLightRange,
         FULL_LIGHT_RANGE,
         DoubleSequenceValidator.betweenWeaklyMonotonic(0.0, 100.0),
         BuilderDescriptorState.Stable,
         "The sky light percentage range",
         null
      );
      this.getDoubleRange(
         data,
         "SunlightRange",
         this.sunlightRange,
         FULL_LIGHT_RANGE,
         DoubleSequenceValidator.betweenWeaklyMonotonic(0.0, 100.0),
         BuilderDescriptorState.Stable,
         "The sunlight percentage range",
         null
      );
      this.getDoubleRange(
         data,
         "RedLightRange",
         this.redLightRange,
         FULL_LIGHT_RANGE,
         DoubleSequenceValidator.betweenWeaklyMonotonic(0.0, 100.0),
         BuilderDescriptorState.Stable,
         "The red light percentage range",
         null
      );
      this.getDoubleRange(
         data,
         "GreenLightRange",
         this.greenLightRange,
         FULL_LIGHT_RANGE,
         DoubleSequenceValidator.betweenWeaklyMonotonic(0.0, 100.0),
         BuilderDescriptorState.Stable,
         "The green light percentage range",
         null
      );
      this.getDoubleRange(
         data,
         "BlueLightRange",
         this.blueLightRange,
         FULL_LIGHT_RANGE,
         DoubleSequenceValidator.betweenWeaklyMonotonic(0.0, 100.0),
         BuilderDescriptorState.Stable,
         "The blue light percentage range",
         null
      );
      this.getString(
         data,
         "UseTargetSlot",
         this.useTargetSlot,
         null,
         StringNullOrNotEmptyValidator.get(),
         BuilderDescriptorState.Stable,
         "A target slot to check. If omitted, will check self",
         null
      );
      return this;
   }

   public int getUsedTargetSlot(@Nonnull BuilderSupport support) {
      String slot = this.useTargetSlot.get(support.getExecutionContext());
      return slot == null ? Integer.MIN_VALUE : support.getTargetSlot(slot);
   }

   public double[] getLightRange(@Nonnull BuilderSupport builderSupport) {
      return this.lightRange.get(builderSupport.getExecutionContext());
   }

   public double[] getSkyLightRange(@Nonnull BuilderSupport builderSupport) {
      return this.skyLightRange.get(builderSupport.getExecutionContext());
   }

   public double[] getSunlightRange(@Nonnull BuilderSupport builderSupport) {
      return this.sunlightRange.get(builderSupport.getExecutionContext());
   }

   public double[] getRedLightRange(@Nonnull BuilderSupport builderSupport) {
      return this.redLightRange.get(builderSupport.getExecutionContext());
   }

   public double[] getGreenLightRange(@Nonnull BuilderSupport builderSupport) {
      return this.greenLightRange.get(builderSupport.getExecutionContext());
   }

   public double[] getBlueLightRange(@Nonnull BuilderSupport builderSupport) {
      return this.blueLightRange.get(builderSupport.getExecutionContext());
   }
}
