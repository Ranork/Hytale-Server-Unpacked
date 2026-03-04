package com.hypixel.hytale.server.npc.corecomponents.utility.builders;

import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.utility.SensorOr;
import com.hypixel.hytale.server.npc.instructions.Sensor;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BuilderSensorOr extends BuilderSensorMany {
   @Nonnull
   @Override
   public String getShortDescription() {
      return "Logical OR of list of sensors";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return "Evaluate sensors and execute action when at least one sensor signals true. Target is provided by first sensor signalling true.";
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nullable
   public SensorOr build(@Nonnull BuilderSupport builderSupport) {
      List<Sensor> sensors = this.objectListHelper.build(builderSupport);
      return sensors.isEmpty() ? null : new SensorOr(this, builderSupport, sensors);
   }
}
