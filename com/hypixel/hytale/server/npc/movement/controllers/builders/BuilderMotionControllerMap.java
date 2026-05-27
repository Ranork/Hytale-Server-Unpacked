package com.hypixel.hytale.server.npc.movement.controllers.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderBase;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderInfo;
import com.hypixel.hytale.server.npc.asset.builder.BuilderObjectMapHelper;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.BuilderValidationHelper;
import com.hypixel.hytale.server.npc.asset.builder.validators.ArrayNotEmptyValidator;
import com.hypixel.hytale.server.npc.movement.MovementMode;
import com.hypixel.hytale.server.npc.movement.controllers.BuilderMotionControllerMapUtil;
import com.hypixel.hytale.server.npc.movement.controllers.MotionController;
import com.hypixel.hytale.server.npc.util.expression.ExecutionContext;
import com.hypixel.hytale.server.npc.util.expression.Scope;
import com.hypixel.hytale.server.npc.validators.NPCLoadTimeValidationHelper;
import com.hypixel.hytale.server.spawning.ISpawnable;
import com.hypixel.hytale.server.spawning.SpawnTestResult;
import com.hypixel.hytale.server.spawning.SpawningContext;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BuilderMotionControllerMap extends BuilderBase<Map<String, MotionController>> implements ISpawnable {
   private final BuilderObjectMapHelper<String, MotionController> motionControllers = new BuilderObjectMapHelper<>(
      MotionController.class, MotionController::getType, this
   );
   @Nullable
   private EnumMap<MovementMode, BuilderMotionControllerBase> modeToController;

   @Nonnull
   public Map<String, MotionController> build(@Nonnull BuilderSupport builderSupport) {
      return new HashMap<>(this.motionControllers.build(builderSupport));
   }

   @Nonnull
   @Override
   public String getShortDescription() {
      return "List of motion controllers";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return "Non-empty list of motion controllers.";
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nonnull
   @Override
   public Builder<Map<String, MotionController>> readConfig(@Nonnull JsonElement data) {
      this.requireArray(
         data,
         this.motionControllers,
         ArrayNotEmptyValidator.get(),
         BuilderDescriptorState.Stable,
         "List of motion controllers",
         null,
         new BuilderValidationHelper(this.fileName, null, this.internalReferenceResolver, null, null, this.extraInfo, null, this.readErrors)
      );
      return this;
   }

   @Override
   public Class<Map<String, MotionController>> category() {
      return BuilderMotionControllerMapUtil.CLASS_REFERENCE;
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

   @Nonnull
   @Override
   public SpawnTestResult canSpawn(@Nonnull SpawningContext context) {
      if (this.motionControllers.hasNoElements()) {
         return SpawnTestResult.FAIL_NO_MOTION_CONTROLLERS;
      } else {
         MovementMode targetMode = context.movementMode;
         if (targetMode != null) {
            BuilderMotionControllerBase mc = this.getModeToController(context.getExecutionContext()).get(targetMode);
            if (mc == null) {
               return SpawnTestResult.FAIL_NO_MOTION_CONTROLLER_MATCH;
            } else {
               SpawnTestResult result = mc.canSpawn(context);
               if (result == SpawnTestResult.TEST_OK) {
                  context.activeMotionControllerType = mc.getType();
               }

               return result;
            }
         } else {
            return Objects.requireNonNullElse(this.motionControllers.findFirst((builder, ctx) -> {
               if (builder instanceof BuilderMotionControllerBase mcx) {
                  SpawnTestResult r = mcx.canSpawn(ctx);
                  if (r == SpawnTestResult.TEST_OK) {
                     ctx.activeMotionControllerType = mcx.getType();
                     return r;
                  } else {
                     return null;
                  }
               } else {
                  throw new IllegalStateException("MotionController builder must extend BuilderMotionControllerBase");
               }
            }, this.builderManager, context.getExecutionContext(), context, null, this.getParent()), SpawnTestResult.FAIL_NO_MOTION_CONTROLLER_MATCH);
         }
      }
   }

   @Nonnull
   private EnumMap<MovementMode, BuilderMotionControllerBase> getModeToController(@Nonnull ExecutionContext executionContext) {
      if (this.modeToController != null) {
         return this.modeToController;
      } else {
         this.modeToController = new EnumMap<>(MovementMode.class);
         this.motionControllers.forEach((builder, map) -> {
            if (!(builder instanceof BuilderMotionControllerBase mc)) {
               throw new IllegalStateException("MotionController builder must extend BuilderMotionControllerBase");
            } else {
               for (MovementMode mode : mc.getSupportedMovementModes()) {
                  map.put(mode, mc);
               }
            }
         }, this.modeToController, this.builderManager, executionContext, this.getParent());
         return this.modeToController;
      }
   }

   @Override
   public void getMovementModes(
      @Nonnull SpawningContext context,
      @Nonnull Set<MovementMode> outSupportedMovementModes,
      @Nonnull Set<MovementMode> outDefaultMovementModes,
      @Nonnull Set<MovementMode> outSafeMovementModes
   ) {
      this.motionControllers.forEach((motionControllerBuilder, modes) -> {
         if (!(motionControllerBuilder instanceof ISpawnable)) {
            throw new IllegalStateException("MotionController must implement ISpawnable");
         } else {
            ((ISpawnable)motionControllerBuilder).getMovementModes(context, modes, outDefaultMovementModes, outSafeMovementModes);
         }
      }, outSupportedMovementModes, this.builderManager, context.getExecutionContext(), this.getParent());
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
         & this.motionControllers.validate(configName, validationHelper, this.builderManager, context, globalScope, errors);
   }
}
