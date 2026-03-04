package com.hypixel.hytale.server.npc.corecomponents.entity.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.entity.SensorPlayer;
import com.hypixel.hytale.server.npc.instructions.Sensor;
import javax.annotation.Nonnull;

public class BuilderSensorPlayer extends BuilderSensorEntityBase {
   @Nonnull
   public SensorPlayer build(@Nonnull BuilderSupport builderSupport) {
      return new SensorPlayer(this, builderSupport);
   }

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Test if player matching specific attributes and filters is in range";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return "Return true if player matching specific attributes and filters is in range. Target is player.";
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nonnull
   @Override
   public Builder<Sensor> readConfig(@Nonnull JsonElement data) {
      super.readConfig(data);
      return this;
   }
}
