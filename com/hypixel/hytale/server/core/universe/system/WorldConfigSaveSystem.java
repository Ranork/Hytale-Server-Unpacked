package com.hypixel.hytale.server.core.universe.system;

import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.system.DelayedSystem;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.WorldConfig;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nonnull;

public class WorldConfigSaveSystem extends DelayedSystem<EntityStore> {
   public WorldConfigSaveSystem() {
      super(10.0F);
   }

   @Override
   public void delayedTick(float dt, int systemIndex, @Nonnull Store<EntityStore> store) {
      World world = store.getExternalData().getWorld();
      saveWorldConfigAndResources(world).join();
   }

   @Nonnull
   public static CompletableFuture<Void> saveWorldConfigAndResources(@Nonnull World world) {
      WorldConfig worldConfig = world.getWorldConfig();
      return worldConfig.isSavingConfig() && worldConfig.consumeHasChanged()
         ? CompletableFuture.allOf(
            world.getChunkStore().getStore().saveAllResources(),
            world.getEntityStore().getStore().saveAllResources(),
            Universe.get().getWorldConfigProvider().save(world.getSavePath(), world.getWorldConfig(), world)
         )
         : CompletableFuture.allOf(world.getChunkStore().getStore().saveAllResources(), world.getEntityStore().getStore().saveAllResources());
   }
}
