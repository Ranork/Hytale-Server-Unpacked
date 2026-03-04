package com.hypixel.hytale.server.flock.corecomponents;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.flock.Flock;
import com.hypixel.hytale.server.flock.FlockPlugin;
import com.hypixel.hytale.server.flock.corecomponents.builders.BuilderSensorFlockCombatDamage;
import com.hypixel.hytale.server.npc.corecomponents.SensorBase;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.EntityPositionProvider;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import com.hypixel.hytale.server.npc.util.DamageData;
import javax.annotation.Nonnull;

public class SensorFlockCombatDamage extends SensorBase {
   protected final boolean leaderOnly;
   protected final EntityPositionProvider positionProvider = new EntityPositionProvider();

   public SensorFlockCombatDamage(@Nonnull BuilderSensorFlockCombatDamage builder) {
      super(builder);
      this.leaderOnly = builder.isLeaderOnly();
   }

   @Override
   public boolean matches(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, double dt, @Nonnull Store<EntityStore> store) {
      if (!super.matches(ref, role, dt, store)) {
         this.positionProvider.clear();
         return false;
      } else {
         Flock flock = FlockPlugin.getFlock(store, ref);
         if (flock == null) {
            return false;
         } else {
            DamageData damageData = this.leaderOnly ? flock.getLeaderDamageData() : flock.getDamageData();
            Ref<EntityStore> entity = damageData.getMostDamagingAttacker();
            return this.positionProvider.setTarget(entity, store) != null;
         }
      }
   }

   @Override
   public InfoProvider getSensorInfo() {
      return this.positionProvider;
   }
}
