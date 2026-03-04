package com.hypixel.hytale.server.core.modules.time;

import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.system.DelayedSystem;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class TimePacketSystem extends DelayedSystem<EntityStore> {
   private static final float BROADCAST_INTERVAL = 1.0F;
   @Nonnull
   private final ResourceType<EntityStore, WorldTimeResource> worldTimeResourceType;

   public TimePacketSystem(@Nonnull ResourceType<EntityStore, WorldTimeResource> worldTimeResourceType) {
      super(1.0F);
      this.worldTimeResourceType = worldTimeResourceType;
   }

   @Override
   public void delayedTick(float dt, int systemIndex, @Nonnull Store<EntityStore> store) {
      World world = store.getExternalData().getWorld();
      if (!world.getWorldConfig().isGameTimePaused()) {
         WorldTimeResource worldTimeResource = store.getResource(this.worldTimeResourceType);
         worldTimeResource.broadcastTimePacket(store);
      }
   }
}
