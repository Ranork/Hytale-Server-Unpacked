package com.hypixel.hytale.builtin.npccombatactionevaluator.corecomponents;

import com.hypixel.hytale.builtin.npccombatactionevaluator.corecomponents.builders.BuilderSensorHasHostileTargetMemory;
import com.hypixel.hytale.builtin.npccombatactionevaluator.memory.TargetMemory;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.corecomponents.SensorBase;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import javax.annotation.Nonnull;

public class SensorHasHostileTargetMemory extends SensorBase {
   private static final ComponentType<EntityStore, TargetMemory> TARGET_MEMORY = TargetMemory.getComponentType();

   public SensorHasHostileTargetMemory(@Nonnull BuilderSensorHasHostileTargetMemory builder) {
      super(builder);
   }

   @Override
   public boolean matches(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, double dt, @Nonnull Store<EntityStore> store) {
      if (!super.matches(ref, role, dt, store)) {
         return false;
      } else {
         TargetMemory targetMemory = ref.getStore().getComponent(ref, TARGET_MEMORY);
         return targetMemory != null && !targetMemory.getKnownHostiles().isEmpty();
      }
   }

   @Override
   public InfoProvider getSensorInfo() {
      return null;
   }
}
