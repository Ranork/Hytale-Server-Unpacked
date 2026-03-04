package com.hypixel.hytale.server.npc.corecomponents.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderBase;
import com.hypixel.hytale.server.npc.asset.builder.InstructionType;
import com.hypixel.hytale.server.npc.instructions.Motion;
import com.hypixel.hytale.server.npc.util.expression.ExecutionContext;
import com.hypixel.hytale.server.npc.util.expression.Scope;
import com.hypixel.hytale.server.npc.validators.NPCLoadTimeValidationHelper;
import java.util.List;
import javax.annotation.Nonnull;

public abstract class BuilderMotionBase<T extends Motion> extends BuilderBase<T> {
   @Override
   public boolean canRequireFeature() {
      return true;
   }

   @Override
   public Builder<T> readCommonConfig(JsonElement data) {
      this.requireInstructionType(InstructionType.MotionAllowedInstructions);
      return super.readCommonConfig(data);
   }

   @Override
   public final boolean isEnabled(ExecutionContext context) {
      return true;
   }

   @Override
   public boolean validate(
      String configName, @Nonnull NPCLoadTimeValidationHelper validationHelper, ExecutionContext context, Scope globalScope, @Nonnull List<String> errors
   ) {
      boolean result = super.validate(configName, validationHelper, context, globalScope, errors);
      if (validationHelper.isParentSensorOnce()) {
         errors.add(String.format("%s: Once is set on a sensor controlling a step with a motion at: %s", configName, this.getBreadCrumbs()));
         return false;
      } else {
         return result;
      }
   }
}
