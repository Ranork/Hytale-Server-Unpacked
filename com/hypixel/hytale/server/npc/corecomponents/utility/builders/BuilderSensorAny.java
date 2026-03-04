package com.hypixel.hytale.server.npc.corecomponents.utility.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderSensorBase;
import com.hypixel.hytale.server.npc.corecomponents.utility.SensorAny;
import com.hypixel.hytale.server.npc.instructions.Sensor;
import java.util.Set;
import javax.annotation.Nonnull;

public class BuilderSensorAny extends BuilderSensorBase {
   @Nonnull
   public Sensor build(BuilderSupport builderSupport) {
      return (Sensor)(!this.once ? Sensor.NULL : new SensorAny(this));
   }

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Return always true";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return "Sensor always signals true but doesn't return a target.";
   }

   @Override
   public void registerTags(@Nonnull Set<String> tags) {
      super.registerTags(tags);
      tags.add("logic");
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nonnull
   @Override
   public Builder<Sensor> readConfig(JsonElement data) {
      return this;
   }
}
