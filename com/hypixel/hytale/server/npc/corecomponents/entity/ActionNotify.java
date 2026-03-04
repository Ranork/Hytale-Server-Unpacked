package com.hypixel.hytale.server.npc.corecomponents.entity;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.components.messaging.BeaconSupport;
import com.hypixel.hytale.server.npc.corecomponents.ActionBase;
import com.hypixel.hytale.server.npc.corecomponents.entity.builders.BuilderActionNotify;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import javax.annotation.Nonnull;

public class ActionNotify extends ActionBase {
   protected final String message;
   protected final double expirationTime;
   protected final int usedTargetSlot;

   public ActionNotify(@Nonnull BuilderActionNotify builderActionBase, @Nonnull BuilderSupport support) {
      super(builderActionBase);
      this.message = builderActionBase.getMessage(support);
      this.expirationTime = builderActionBase.getExpirationTime();
      this.usedTargetSlot = builderActionBase.getUsedTargetSlot(support);
   }

   @Override
   public boolean execute(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, @Nonnull InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
      super.execute(ref, role, sensorInfo, dt, store);
      Ref<EntityStore> targetRef;
      if (this.usedTargetSlot >= 0) {
         targetRef = role.getMarkedEntitySupport().getMarkedEntityRef(this.usedTargetSlot);
      } else {
         targetRef = sensorInfo.getPositionProvider().getTarget();
      }

      if (targetRef != null) {
         BeaconSupport beaconSupport = store.getComponent(targetRef, BeaconSupport.getComponentType());
         if (beaconSupport != null) {
            beaconSupport.postMessage(this.message, ref, this.expirationTime);
         }
      }

      return true;
   }
}
