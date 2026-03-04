package com.hypixel.hytale.server.npc.corecomponents.entity;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.SensorBase;
import com.hypixel.hytale.server.npc.corecomponents.entity.builders.BuilderSensorKill;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import com.hypixel.hytale.server.npc.sensorinfo.PositionProvider;
import com.hypixel.hytale.server.npc.util.DamageData;
import javax.annotation.Nonnull;

public class SensorKill extends SensorBase {
   protected final int targetSlot;
   protected final PositionProvider positionProvider = new PositionProvider();

   public SensorKill(@Nonnull BuilderSensorKill builder, @Nonnull BuilderSupport support) {
      super(builder);
      this.targetSlot = builder.getTargetSlot(support);
   }

   @Override
   public boolean matches(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, double dt, @Nonnull Store<EntityStore> store) {
      NPCEntity npcComponent = store.getComponent(ref, NPCEntity.getComponentType());

      assert npcComponent != null;

      DamageData damageData = npcComponent.getDamageData();
      if (super.matches(ref, role, dt, store) && damageData.haveKill()) {
         Ref<EntityStore> targetRef;
         if (this.targetSlot >= 0) {
            targetRef = role.getMarkedEntitySupport().getMarkedEntityRef(this.targetSlot);
            if (targetRef == null || !damageData.haveKilled(targetRef)) {
               this.positionProvider.clear();
               return false;
            }
         } else {
            targetRef = damageData.getAnyKilled();
         }

         if (targetRef == null) {
            this.positionProvider.clear();
            return false;
         } else {
            Vector3d killPosition = damageData.getKillPosition(targetRef);
            if (killPosition == null) {
               this.positionProvider.clear();
               return false;
            } else {
               this.positionProvider.setTarget(killPosition);
               return true;
            }
         }
      } else {
         this.positionProvider.clear();
         return false;
      }
   }

   @Override
   public InfoProvider getSensorInfo() {
      return this.positionProvider;
   }
}
