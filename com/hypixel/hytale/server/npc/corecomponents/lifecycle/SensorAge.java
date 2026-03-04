package com.hypixel.hytale.server.npc.corecomponents.lifecycle;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.time.WorldTimeResource;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.SensorBase;
import com.hypixel.hytale.server.npc.corecomponents.lifecycle.builders.BuilderSensorAge;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import java.time.Instant;
import javax.annotation.Nonnull;

public class SensorAge extends SensorBase {
   protected final Instant minAgeInstant;
   protected final Instant maxAgeInstant;

   public SensorAge(@Nonnull BuilderSensorAge builderSensorAge, @Nonnull BuilderSupport builderSupport) {
      super(builderSensorAge);
      Instant[] ageRange = builderSensorAge.getAgeRange(builderSupport);
      this.minAgeInstant = ageRange[0];
      this.maxAgeInstant = ageRange[1];
   }

   @Override
   public boolean matches(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, double dt, @Nonnull Store<EntityStore> store) {
      if (!super.matches(ref, role, dt, store)) {
         return false;
      } else {
         WorldTimeResource worldTimeResource = store.getResource(WorldTimeResource.getResourceType());
         Instant currentInstant = worldTimeResource.getGameTime();
         return !currentInstant.isBefore(this.minAgeInstant) && !currentInstant.isAfter(this.maxAgeInstant);
      }
   }

   @Override
   public InfoProvider getSensorInfo() {
      return null;
   }
}
