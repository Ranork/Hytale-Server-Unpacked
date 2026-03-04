package com.hypixel.hytale.server.core.prefab.event;

import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.system.EcsEvent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class PrefabPlaceEntityEvent extends EcsEvent {
   private final int prefabId;
   @Nonnull
   private final Holder<EntityStore> holder;

   public PrefabPlaceEntityEvent(int prefabId, @Nonnull Holder<EntityStore> holder) {
      this.prefabId = prefabId;
      this.holder = holder;
   }

   public int getPrefabId() {
      return this.prefabId;
   }

   @Nonnull
   public Holder<EntityStore> getHolder() {
      return this.holder;
   }
}
