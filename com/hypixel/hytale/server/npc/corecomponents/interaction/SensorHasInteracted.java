package com.hypixel.hytale.server.npc.corecomponents.interaction;

import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.corecomponents.SensorBase;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderSensorBase;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import javax.annotation.Nonnull;

public class SensorHasInteracted extends SensorBase {
   public SensorHasInteracted(@Nonnull BuilderSensorBase builderSensorBase) {
      super(builderSensorBase);
   }

   @Override
   public boolean matches(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, double dt, @Nonnull Store<EntityStore> store) {
      if (!super.matches(ref, role, dt, store)) {
         return false;
      } else {
         Ref<EntityStore> target = role.getStateSupport().getInteractionIterationTarget();
         if (target == null) {
            return false;
         } else {
            Archetype<EntityStore> targetArchetype = store.getArchetype(target);
            return targetArchetype.contains(DeathComponent.getComponentType()) ? false : role.getStateSupport().consumeInteraction(target);
         }
      }
   }

   @Override
   public InfoProvider getSensorInfo() {
      return null;
   }
}
