package com.hypixel.hytale.server.core.modules.entity.tracker;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public final class NetworkId implements Component<EntityStore> {
   private final int id;

   @Nonnull
   public static ComponentType<EntityStore, NetworkId> getComponentType() {
      return EntityModule.get().getNetworkIdComponentType();
   }

   public NetworkId(int id) {
      this.id = id;
   }

   public int getId() {
      return this.id;
   }

   @Nonnull
   @Override
   public Component<EntityStore> clone() {
      return this;
   }
}
