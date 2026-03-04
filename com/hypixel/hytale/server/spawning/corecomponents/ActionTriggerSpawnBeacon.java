package com.hypixel.hytale.server.spawning.corecomponents;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.ActionBase;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import com.hypixel.hytale.server.spawning.beacons.SpawnBeacon;
import com.hypixel.hytale.server.spawning.corecomponents.builders.BuilderActionTriggerSpawnBeacon;
import com.hypixel.hytale.server.spawning.util.FloodFillPositionSelector;
import com.hypixel.hytale.server.spawning.wrappers.BeaconSpawnWrapper;
import javax.annotation.Nonnull;

public class ActionTriggerSpawnBeacon extends ActionBase {
   protected final int beaconId;
   protected final int range;
   protected final int targetSlot;

   public ActionTriggerSpawnBeacon(@Nonnull BuilderActionTriggerSpawnBeacon builder, @Nonnull BuilderSupport support) {
      super(builder);
      this.beaconId = builder.getBeaconId(support);
      this.range = builder.getRange(support);
      this.targetSlot = builder.getTargetSlot(support);
   }

   @Override
   public boolean canExecute(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
      return super.canExecute(ref, role, sensorInfo, dt, store)
         && (this.targetSlot == Integer.MIN_VALUE || role.getMarkedEntitySupport().hasMarkedEntityInSlot(this.targetSlot));
   }

   @Override
   public void registerWithSupport(@Nonnull Role role) {
      role.getPositionCache().requireSpawnBeaconDistance(this.range);
   }

   @Override
   public boolean execute(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
      super.execute(ref, role, sensorInfo, dt, store);

      for (Ref<EntityStore> spawnBeaconRef : role.getPositionCache().getSpawnBeaconList()) {
         SpawnBeacon spawnBeaconComponent = store.getComponent(spawnBeaconRef, SpawnBeacon.getComponentType());

         assert spawnBeaconComponent != null;

         BeaconSpawnWrapper spawnWrapper = spawnBeaconComponent.getSpawnWrapper();
         if (spawnWrapper.getSpawnIndex() == this.beaconId) {
            Ref<EntityStore> targetRef;
            if (this.targetSlot != Integer.MIN_VALUE) {
               targetRef = role.getMarkedEntitySupport().getMarkedEntityRef(this.targetSlot);
            } else {
               targetRef = ref;
            }

            FloodFillPositionSelector floodFillPositionSelectorComponent = store.getComponent(spawnBeaconRef, FloodFillPositionSelector.getComponentType());

            assert floodFillPositionSelectorComponent != null;

            spawnBeaconComponent.manualTrigger(spawnBeaconRef, floodFillPositionSelectorComponent, targetRef, store);
            return true;
         }
      }

      return true;
   }
}
