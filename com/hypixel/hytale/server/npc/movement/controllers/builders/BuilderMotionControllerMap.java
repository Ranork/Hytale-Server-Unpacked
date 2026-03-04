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
import com.hypixel.hytale.server.npc.movement.controllers.BuilderMotionControllerMapUtil;
import com.hypixel.hytale.server.npc.movement.controllers.MotionController;
import com.hypixel.hytale.server.npc.util.expression.ExecutionContext;
import com.hypixel.hytale.server.npc.util.expression.Scope;
import com.hypixel.hytale.server.npc.validators.NPCLoadTimeValidationHelper;
import com.hypixel.hytale.server.spawning.ISpawnable;
import com.hypixel.hytale.server.spawning.SpawnTestResult;
import com.hypixel.hytale.server.spawning.SpawningContext;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nonnull;

public class BuilderMotionControllerMap extends BuilderBase<Map<String, MotionController>> implements ISpawnable {
   private final BuilderObjectMapHelper<String, MotionController> motionControllers = new BuilderObjectMapHelper<>(
      MotionController.class, MotionController::getType, this
   );

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
      return this.motionControllers.testEach((motionControllerBuilder, _context) -> {
         if (!(motionControllerBuilder instanceof ISpawnable)) {
            throw new IllegalStateException("MotionController must implement ISpawnable");
         } else {
            return ((ISpawnable)motionControllerBuilder).canSpawn(_context);
         }
      }, this.builderManager, context.getExecutionContext(), context, SpawnTestResult.TEST_OK, SpawnTestResult.FAIL_NO_MOTION_CONTROLLERS, this.getParent());
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
