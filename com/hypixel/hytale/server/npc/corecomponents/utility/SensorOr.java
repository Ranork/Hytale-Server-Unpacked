package com.hypixel.hytale.server.npc.corecomponents.utility;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.utility.builders.BuilderSensorOr;
import com.hypixel.hytale.server.npc.instructions.Sensor;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.role.support.DebugSupport;
import com.hypixel.hytale.server.npc.sensorinfo.WrappedInfoProvider;
import java.util.List;
import javax.annotation.Nonnull;

public class SensorOr extends SensorMany {
   public SensorOr(@Nonnull BuilderSensorOr builder, @Nonnull BuilderSupport support, @Nonnull List<Sensor> sensors) {
      super(builder, support, sensors);
   }

   @Override
   public boolean matches(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, double dt, @Nonnull Store<EntityStore> store) {
      this.infoProvider.clearMatches();
      this.infoProvider.clearPositionMatch();
      DebugSupport debugSupport = role.getDebugSupport();
      if (!super.matches(ref, role, dt, store)) {
         if (this.autoUnlockTargetSlot >= 0) {
            role.getMarkedEntitySupport().clearMarkedEntity(this.autoUnlockTargetSlot);
         }

         if (debugSupport.isTraceSensorFails()) {
            debugSupport.setLastFailingSensor(this);
         }

         return false;
      } else {
         Sensor match = null;
         if (super.matches(ref, role, dt, store)) {
            for (Sensor s : this.sensors) {
               if (s.matches(ref, role, dt, store)) {
                  if (match == null) {
                     match = s;
                  }

                  this.infoProvider.addMatch(s);
                  if (!this.infoProvider.hasPosition() && s.getSensorInfo() != null && s.getSensorInfo().hasPosition()) {
                     this.infoProvider.setPositionMatch(s.getSensorInfo().getPositionProvider());
                  }
               }
            }
         }

         if (match == null) {
            if (this.autoUnlockTargetSlot >= 0) {
               role.getMarkedEntitySupport().clearMarkedEntity(this.autoUnlockTargetSlot);
            }

            if (debugSupport.isTraceSensorFails()) {
               debugSupport.setLastFailingSensor(this);
            }

            return false;
         } else {
            return true;
         }
      }
   }

   @Nonnull
   @Override
   protected WrappedInfoProvider createInfoProvider() {
      return new WrappedInfoProvider();
   }
}
