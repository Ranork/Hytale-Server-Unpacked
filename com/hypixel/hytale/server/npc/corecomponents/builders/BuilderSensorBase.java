package com.hypixel.hytale.server.npc.corecomponents.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderBase;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.holder.BooleanHolder;
import com.hypixel.hytale.server.npc.instructions.Sensor;
import com.hypixel.hytale.server.npc.util.expression.ExecutionContext;
import com.hypixel.hytale.server.npc.util.expression.Scope;
import com.hypixel.hytale.server.npc.validators.NPCLoadTimeValidationHelper;
import java.util.List;
import javax.annotation.Nonnull;

public abstract class BuilderSensorBase extends BuilderBase<Sensor> {
   protected boolean once;
   protected final BooleanHolder enabled = new BooleanHolder();

   @Nonnull
   @Override
   public Builder<Sensor> readCommonConfig(@Nonnull JsonElement data) {
      super.readCommonConfig(data);
      this.getBoolean(data, "Once", aBoolean -> this.once = aBoolean, false, BuilderDescriptorState.Stable, "Sensor only triggers once", null);
      this.getBoolean(data, "Enabled", this.enabled, true, BuilderDescriptorState.Stable, "Whether this sensor should be enabled on the NPC", null);
      return this;
   }

   @Nonnull
   @Override
   public Class<Sensor> category() {
      return Sensor.class;
   }

   public boolean getOnce() {
      return this.once;
   }

   public void setOnce(boolean once) {
      this.once = once;
   }

   @Override
   public boolean isEnabled(ExecutionContext context) {
      return this.enabled.get(context);
   }

   @Override
   public boolean validate(
      String configName, @Nonnull NPCLoadTimeValidationHelper validationHelper, ExecutionContext context, Scope globalScope, @Nonnull List<String> errors
   ) {
      validationHelper.updateParentSensorOnce(this.once);
      return super.validate(configName, validationHelper, context, globalScope, errors);
   }
}
