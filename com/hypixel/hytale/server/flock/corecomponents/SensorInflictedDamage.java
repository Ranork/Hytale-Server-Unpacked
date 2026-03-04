package com.hypixel.hytale.server.flock.corecomponents;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.group.EntityGroup;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.flock.Flock;
import com.hypixel.hytale.server.flock.FlockPlugin;
import com.hypixel.hytale.server.flock.corecomponents.builders.BuilderSensorInflictedDamage;
import com.hypixel.hytale.server.npc.corecomponents.SensorBase;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.EntityPositionProvider;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import com.hypixel.hytale.server.npc.util.DamageData;
import java.util.function.Supplier;
import javax.annotation.Nonnull;

public class SensorInflictedDamage extends SensorBase {
   protected final SensorInflictedDamage.Target target;
   protected final boolean friendlyFire;
   protected final EntityPositionProvider positionProvider = new EntityPositionProvider();

   public SensorInflictedDamage(@Nonnull BuilderSensorInflictedDamage builder) {
      super(builder);
      this.target = builder.getTarget();
      this.friendlyFire = builder.isFriendlyFire();
   }

   @Override
   public boolean matches(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, double dt, @Nonnull Store<EntityStore> store) {
      if (!super.matches(ref, role, dt, store)) {
         this.positionProvider.clear();
         return false;
      } else {
         NPCEntity npcComponent = store.getComponent(ref, NPCEntity.getComponentType());
         DamageData damageData;
         if (this.target == SensorInflictedDamage.Target.Self) {
            damageData = npcComponent.getDamageData();
         } else {
            Flock npcFlock = FlockPlugin.getFlock(store, ref);
            if (npcFlock == null) {
               this.positionProvider.clear();
               return false;
            }

            damageData = this.target == SensorInflictedDamage.Target.FlockLeader ? npcFlock.getLeaderDamageData() : npcFlock.getDamageData();
         }

         Ref<EntityStore> victimReference = damageData.getMostDamagedVictim();
         if (victimReference == null) {
            this.positionProvider.clear();
            return false;
         } else if (!this.friendlyFire && inSameFlock(ref, victimReference, store)) {
            this.positionProvider.clear();
            return false;
         } else {
            return this.positionProvider.setTarget(victimReference, store) != null;
         }
      }
   }

   @Override
   public InfoProvider getSensorInfo() {
      return this.positionProvider;
   }

   protected static boolean inSameFlock(@Nonnull Ref<EntityStore> selfReference, @Nonnull Ref<EntityStore> target, @Nonnull Store<EntityStore> store) {
      Ref<EntityStore> flockReference = FlockPlugin.getFlockReference(selfReference, store);
      if (flockReference == null) {
         return false;
      } else {
         EntityGroup entityGroupComponent = store.getComponent(flockReference, EntityGroup.getComponentType());

         assert entityGroupComponent != null;

         return entityGroupComponent.isMember(target);
      }
   }

   public static enum Target implements Supplier<String> {
      Flock("Check flock"),
      FlockLeader("Check flock leader only"),
      Self("Check self");

      private final String description;

      private Target(String description) {
         this.description = description;
      }

      public String get() {
         return this.description;
      }
   }
}
