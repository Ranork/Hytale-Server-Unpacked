package com.hypixel.hytale.server.npc.movement.controllers.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderBaseWithType;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderInfo;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.holder.DoubleHolder;
import com.hypixel.hytale.server.npc.asset.builder.holder.FloatHolder;
import com.hypixel.hytale.server.npc.asset.builder.validators.DoubleRangeValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.DoubleSingleValidator;
import com.hypixel.hytale.server.npc.movement.controllers.MotionController;
import com.hypixel.hytale.server.npc.util.expression.ExecutionContext;
import com.hypixel.hytale.server.npc.util.expression.Scope;
import com.hypixel.hytale.server.npc.validators.NPCLoadTimeValidationHelper;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;

public abstract class BuilderMotionControllerBase extends BuilderBaseWithType<MotionController> {
   protected float epsilonAngle;
   protected double epsilonSpeed;
   protected double forceVelocityDamping;
   protected final DoubleHolder maxHorizontalSpeed = new DoubleHolder();
   protected final DoubleHolder fastHorizontalThreshold = new DoubleHolder();
   protected double fastHorizontalThresholdRange;
   protected final FloatHolder maxHeadRotationSpeed = new FloatHolder();

   @Nonnull
   @Override
   public Builder<MotionController> readCommonConfig(@Nonnull JsonElement data) {
      super.readCommonConfig(data);
      this.readTypeKey(data);
      this.getDouble(
         data,
         "EpsilonSpeed",
         v -> this.epsilonSpeed = v,
         1.0E-5,
         DoubleSingleValidator.greater0(),
         BuilderDescriptorState.Experimental,
         "Minimum speed considered non 0",
         null
      );
      this.getFloat(
         data,
         "EpsilonAngle",
         v -> this.epsilonAngle = v,
         3.0F,
         DoubleSingleValidator.greater0(),
         BuilderDescriptorState.Experimental,
         "Minimum angle difference considered non 0 in degrees",
         null
      );
      this.getFloat(
         data,
         "MaxHeadRotationSpeed",
         this.maxHeadRotationSpeed,
         360.0,
         DoubleRangeValidator.between(0.0, 360.0),
         BuilderDescriptorState.Stable,
         "Maximum rotation speed of the head in degrees",
         null
      );
      this.getDouble(
         data,
         "ForceVelocityDamping",
         v -> this.forceVelocityDamping = v,
         0.5,
         DoubleSingleValidator.greater0(),
         BuilderDescriptorState.Experimental,
         "Damping of external force/velocity over time",
         null
      );
      this.getDouble(
         data,
         "RunThreshold",
         this.fastHorizontalThreshold,
         0.7,
         DoubleRangeValidator.between01(),
         BuilderDescriptorState.WorkInProgress,
         "Relative threshold when running animation should be used",
         null
      );
      this.getDouble(
         data,
         "RunThresholdRange",
         v -> this.fastHorizontalThresholdRange = v,
         0.15,
         DoubleRangeValidator.between01(),
         BuilderDescriptorState.WorkInProgress,
         "Relative threshold range for switching between running/walking",
         null
      );
      return this;
   }

   @Override
   public final boolean isEnabled(ExecutionContext context) {
      return true;
   }

   @Nonnull
   @Override
   public String getIdentifier() {
      BuilderInfo builderInfo = NPCPlugin.get().getBuilderInfo(this);
      Objects.requireNonNull(builderInfo, "Have builder but can't get builderInfo for it");
      return builderInfo.getKeyName();
   }

   @Override
   public boolean validate(
      String configName, @Nonnull NPCLoadTimeValidationHelper validationHelper, ExecutionContext context, Scope globalScope, @Nonnull List<String> errors
   ) {
      boolean result = super.validate(configName, validationHelper, context, globalScope, errors);
      validationHelper.registerMotionControllerType(this.getClassType());
      return result;
   }

   public float getEpsilonAngle() {
      return (float) (Math.PI / 180.0) * this.epsilonAngle;
   }

   public double getEpsilonSpeed() {
      return this.epsilonSpeed;
   }

   public double getForceVelocityDamping() {
      return this.forceVelocityDamping;
   }

   public double getMaxHorizontalSpeed(@Nonnull BuilderSupport builderSupport) {
      return this.maxHorizontalSpeed.get(builderSupport.getExecutionContext());
   }

   public float getMaxHeadRotationSpeed(@Nonnull BuilderSupport support) {
      return this.maxHeadRotationSpeed.get(support.getExecutionContext()) * (float) (Math.PI / 180.0);
   }

   public double getFastHorizontalThreshold(@Nonnull BuilderSupport builderSupport) {
      return this.fastHorizontalThreshold.get(builderSupport.getExecutionContext());
   }

   public double getFastHorizontalThresholdRange() {
      return this.fastHorizontalThresholdRange;
   }

   public abstract Class<? extends MotionController> getClassType();
}
