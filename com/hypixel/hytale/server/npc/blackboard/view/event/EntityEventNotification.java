package com.hypixel.hytale.server.npc.blackboard.view.event;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nullable;

public class EntityEventNotification extends EventNotification {
   @Nullable
   private Ref<EntityStore> flockReference;

   @Nullable
   public Ref<EntityStore> getFlockReference() {
      return this.flockReference;
   }

   public void setFlockReference(@Nullable Ref<EntityStore> flockReference) {
      this.flockReference = flockReference;
   }
}
