package com.hypixel.hytale.server.npc.blackboard.view.event;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class EntityEventNotification extends EventNotification {
   private Ref<EntityStore> flockReference;

   public Ref<EntityStore> getFlockReference() {
      return this.flockReference;
   }

   public void setFlockReference(Ref<EntityStore> flockReference) {
      this.flockReference = flockReference;
   }
}
