package com.hypixel.hytale.server.npc.corecomponents.utility.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.validators.StringNotEmptyValidator;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderSensorBase;
import com.hypixel.hytale.server.npc.corecomponents.utility.SensorEval;
import com.hypixel.hytale.server.npc.instructions.Sensor;
import javax.annotation.Nonnull;

public class BuilderSensorEval extends BuilderSensorBase {
   protected String expression;

   @Nonnull
   public SensorEval build(@Nonnull BuilderSupport builderSupport) {
      return new SensorEval(this, builderSupport);
   }

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Evaluate javascript expression and test if true";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return "Evaluate javascript expression and test truth value. Current values accessible are 'health' and 'blocked'.";
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
         data, "Expression", s -> this.expression = s, StringNotEmptyValidator.get(), BuilderDescriptorState.Experimental, "Javascript expression", null
      );
      return this;
   }

   public String getExpression() {
      return this.expression;
   }
}
