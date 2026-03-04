package com.hypixel.hytale.server.npc.corecomponents.movement.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.holder.DoubleHolder;
import com.hypixel.hytale.server.npc.asset.builder.validators.DoubleSingleValidator;
import com.hypixel.hytale.server.npc.corecomponents.movement.BodyMotionLand;
import com.hypixel.hytale.server.npc.movement.controllers.MotionControllerFly;
import com.hypixel.hytale.server.npc.movement.controllers.MotionControllerWalk;
import com.hypixel.hytale.server.npc.util.expression.ExecutionContext;
import com.hypixel.hytale.server.npc.util.expression.Scope;
import com.hypixel.hytale.server.npc.validators.NPCLoadTimeValidationHelper;
import java.util.List;
import javax.annotation.Nonnull;

public class BuilderBodyMotionLand extends BuilderBodyMotionFind {
   protected final DoubleHolder goalLenience = new DoubleHolder();

   @Nonnull
   public BodyMotionLand build(@Nonnull BuilderSupport builderSupport) {
      return new BodyMotionLand(this, builderSupport);
   }

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Try to land at the given position";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return "Try to land at the given position using a seek like motion";
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Experimental;
   }

   @Nonnull
   public BuilderBodyMotionLand readConfig(@Nonnull JsonElement data) {
      super.readConfig(data);
      this.getDouble(
         data,
         "GoalLenience",
         this.goalLenience,
         2.0,
         DoubleSingleValidator.greater0(),
         BuilderDescriptorState.Experimental,
         "The distance from the target landing point that is acceptable to land at",
         null
      );
      return this;
   }

   @Override
   public boolean validate(
      String configName, @Nonnull NPCLoadTimeValidationHelper validationHelper, ExecutionContext context, Scope globalScope, @Nonnull List<String> errors
   ) {
      boolean result = super.validate(configName, validationHelper, context, globalScope, errors);
      validationHelper.requireMotionControllerType(MotionControllerFly.class);
      validationHelper.requireMotionControllerType(MotionControllerWalk.class);
      return result;
   }

   public double getGoalLenience(@Nonnull BuilderSupport support) {
      return this.goalLenience.get(support.getExecutionContext());
   }

   @Override
   public double getDesiredAltitudeWeight(BuilderSupport support) {
      return 0.0;
   }
}
