package com.hypixel.hytale.server.spawning.util;

import com.hypixel.hytale.component.Resource;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.spawning.SpawningPlugin;
import javax.annotation.Nonnull;

public class FloodFillEntryPoolProviderSimple implements Resource<EntityStore> {
   @Nonnull
   private final FloodFillEntryPoolSimple pool = new FloodFillEntryPoolSimple();

   public static ResourceType<EntityStore, FloodFillEntryPoolProviderSimple> getResourceType() {
      return SpawningPlugin.get().getFloodFillEntryPoolProviderSimpleResourceType();
   }

   @Nonnull
   public FloodFillEntryPoolSimple getPool() {
      return this.pool;
   }

   @Nonnull
   @Override
   public Resource<EntityStore> clone() {
      return new FloodFillEntryPoolProviderSimple();
   }
}
