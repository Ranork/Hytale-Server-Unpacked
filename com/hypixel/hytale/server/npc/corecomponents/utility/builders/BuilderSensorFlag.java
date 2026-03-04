package com.hypixel.hytale.server.npc.corecomponents.utility.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.holder.BooleanHolder;
import com.hypixel.hytale.server.npc.asset.builder.holder.StringHolder;
import com.hypixel.hytale.server.npc.asset.builder.validators.StringNotEmptyValidator;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderSensorBase;
import com.hypixel.hytale.server.npc.corecomponents.utility.SensorFlag;
import com.hypixel.hytale.server.npc.instructions.Sensor;
import javax.annotation.Nonnull;

public class BuilderSensorFlag extends BuilderSensorBase {
   protected final StringHolder name = new StringHolder();
   protected final BooleanHolder value = new BooleanHolder();

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Test if a named flag is set or not";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return this.getShortDescription();
   }

   @Nonnull
   public Sensor build(@Nonnull BuilderSupport builderSupport) {
      return new SensorFlag(this, builderSupport);
   }

   @Nonnull
   @Override
   public Builder<Sensor> readConfig(@Nonnull JsonElement data) {
      this.requireString(data, "Name", this.name, StringNotEmptyValidator.get(), BuilderDescriptorState.Stable, "The name of the flag", null);
      this.getBoolean(data, "Set", this.value, true, BuilderDescriptorState.Stable, "Whether the flag should be set or not", null);
      return this;
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   public int getFlagSlot(@Nonnull BuilderSupport support) {
      String flag = this.name.get(support.getExecutionContext());
      return support.getFlagSlot(flag);
   }

   public boolean getValue(@Nonnull BuilderSupport support) {
      return this.value.get(support.getExecutionContext());
   }
}
