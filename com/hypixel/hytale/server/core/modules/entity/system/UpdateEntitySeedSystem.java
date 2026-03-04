package com.hypixel.hytale.server.core.modules.entity.system;

import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.system.DelayedSystem;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class UpdateEntitySeedSystem extends DelayedSystem<EntityStore> {
   public UpdateEntitySeedSystem() {
      super(1.0F);
   }

   @Override
   public void delayedTick(float dt, int systemIndex, @Nonnull Store<EntityStore> store) {
      store.getExternalData().getWorld().updateEntitySeed(store);
   }
}
