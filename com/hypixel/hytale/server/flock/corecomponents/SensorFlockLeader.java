package com.hypixel.hytale.server.flock.corecomponents;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.group.EntityGroup;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.flock.FlockMembership;
import com.hypixel.hytale.server.flock.corecomponents.builders.BuilderSensorFlockLeader;
import com.hypixel.hytale.server.npc.corecomponents.SensorBase;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.EntityPositionProvider;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import javax.annotation.Nonnull;

public class SensorFlockLeader extends SensorBase {
   protected final EntityPositionProvider positionProvider = new EntityPositionProvider();

   public SensorFlockLeader(@Nonnull BuilderSensorFlockLeader builder) {
      super(builder);
   }

   @Override
   public boolean matches(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, double dt, @Nonnull Store<EntityStore> store) {
      if (!super.matches(ref, role, dt, store)) {
         this.positionProvider.clear();
         return false;
      } else {
         FlockMembership membership = store.getComponent(ref, FlockMembership.getComponentType());
         if (membership == null) {
            this.positionProvider.clear();
            return false;
         } else {
            EntityGroup group = null;
            Ref<EntityStore> flockReference = membership.getFlockRef();
            if (flockReference != null && flockReference.isValid()) {
               group = store.getComponent(flockReference, EntityGroup.getComponentType());
            }

            if (group == null) {
               this.positionProvider.clear();
               return false;
            } else {
               return this.positionProvider.setTarget(group.getLeaderRef(), store) != null;
            }
         }
      }
   }

   @Override
   public InfoProvider getSensorInfo() {
      return this.positionProvider;
   }
}
