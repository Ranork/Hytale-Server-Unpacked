package com.hypixel.hytale.server.core.modules.time;

import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.system.StoreSystem;
import com.hypixel.hytale.component.system.tick.TickingSystem;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.WorldConfig;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class WorldTimeSystems {
   public static class Init extends StoreSystem<EntityStore> {
      @Nonnull
      private final ResourceType<EntityStore, WorldTimeResource> worldTimeResourceType;

      public Init(@Nonnull ResourceType<EntityStore, WorldTimeResource> worldTimeResourceType) {
         this.worldTimeResourceType = worldTimeResourceType;
      }

      @Override
      public void onSystemAddedToStore(@Nonnull Store<EntityStore> store) {
         World world = store.getExternalData().getWorld();
         WorldTimeResource worldTimeResource = store.getResource(this.worldTimeResourceType);
         worldTimeResource.setGameTime0(world.getWorldConfig().getGameTime());
         world.execute(() -> worldTimeResource.updateMoonPhase(world, store));
      }

      @Override
      public void onSystemRemovedFromStore(@Nonnull Store<EntityStore> store) {
         World world = store.getExternalData().getWorld();
         WorldTimeResource worldTimeResource = store.getResource(this.worldTimeResourceType);
         WorldConfig worldConfig = world.getWorldConfig();
         worldConfig.setGameTime(worldTimeResource.getGameTime());
         worldConfig.markChanged();
      }
   }

   public static class Ticking extends TickingSystem<EntityStore> {
      @Nonnull
      private final ResourceType<EntityStore, WorldTimeResource> worldTimeResourceType;

      public Ticking(@Nonnull ResourceType<EntityStore, WorldTimeResource> worldTimeResourceType) {
         this.worldTimeResourceType = worldTimeResourceType;
      }

      @Override
      public void tick(float dt, int systemIndex, @Nonnull Store<EntityStore> store) {
         WorldTimeResource worldTimeResource = store.getResource(this.worldTimeResourceType);
         worldTimeResource.tick(dt, store);
      }
   }
}
