package com.hypixel.hytale.server.npc.corecomponents.utility.builders;

import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.utility.SensorAnd;
import com.hypixel.hytale.server.npc.instructions.Sensor;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BuilderSensorAnd extends BuilderSensorMany {
   @Nonnull
   @Override
   public String getShortDescription() {
      return "Logical AND of list of sensors";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return "Evaluate all sensors and execute action only when all sensor signal true. Target is provided by first sensor.";
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nullable
   public SensorAnd build(@Nonnull BuilderSupport builderSupport) {
      List<Sensor> sensors = this.objectListHelper.build(builderSupport);
      return sensors.isEmpty() ? null : new SensorAnd(this, builderSupport, sensors);
   }
}
