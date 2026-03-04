package com.hypixel.hytale.server.npc.corecomponents.utility;

import com.hypixel.hytale.server.npc.corecomponents.SensorBase;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderSensorBase;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import javax.annotation.Nonnull;

public class SensorAny extends SensorBase {
   public SensorAny(@Nonnull BuilderSensorBase builderSensorBase) {
      super(builderSensorBase);
   }

   @Override
   public InfoProvider getSensorInfo() {
      return null;
   }
}
