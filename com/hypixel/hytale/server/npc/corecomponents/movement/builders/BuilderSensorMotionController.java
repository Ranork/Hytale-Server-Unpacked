package com.hypixel.hytale.server.npc.corecomponents.movement.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.validators.StringNotEmptyValidator;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderSensorBase;
import com.hypixel.hytale.server.npc.corecomponents.movement.SensorMotionController;
import com.hypixel.hytale.server.npc.instructions.Sensor;
import javax.annotation.Nonnull;

public class BuilderSensorMotionController extends BuilderSensorBase {
   protected String motionControllerName;

   @Nonnull
   public Sensor build(BuilderSupport builderSupport) {
      return new SensorMotionController(this);
   }

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Test if specific motion controller is active.";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return this.getShortDescription();
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Experimental;
   }

   @Nonnull
   @Override
   public Builder<Sensor> readConfig(@Nonnull JsonElement data) {
      this.requireString(
         data,
         "MotionController",
         s -> this.motionControllerName = s,
         StringNotEmptyValidator.get(),
         BuilderDescriptorState.Experimental,
         "Motion controller name to test for",
         null
      );
      return this;
   }

   public String getMotionControllerName() {
      return this.motionControllerName;
   }
}
