package com.hypixel.hytale.server.npc.corecomponents.world;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.corecomponents.SensorBase;
import com.hypixel.hytale.server.npc.corecomponents.world.builders.BuilderSensorInWater;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import javax.annotation.Nonnull;

public class SensorInWater extends SensorBase {
   public SensorInWater(@Nonnull BuilderSensorInWater builderSensorBase) {
      super(builderSensorBase);
   }

   @Override
   public boolean matches(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, double dt, @Nonnull Store<EntityStore> store) {
      return super.matches(ref, role, dt, store) && role.getActiveMotionController().inWater();
   }

   @Override
   public InfoProvider getSensorInfo() {
      return null;
   }
}
