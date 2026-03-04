package com.hypixel.hytale.server.core.universe.world.storage.component;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Resource;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.system.tick.RunWhenPausedSystem;
import com.hypixel.hytale.component.system.tick.TickingSystem;
import com.hypixel.hytale.math.shape.Box2D;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.server.core.modules.entity.player.ChunkTracker;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.ChunkFlag;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.events.ecs.ChunkUnloadEvent;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.logging.Level;
import javax.annotation.Nonnull;

public class ChunkUnloadingSystem extends TickingSystem<ChunkStore> implements RunWhenPausedSystem<ChunkStore> {
   public static final double DESPERATE_UNLOAD_RAM_USAGE_THRESHOLD = 0.85;
   public static final int DESPERATE_UNLOAD_MAX_POLL_COUNT = 3;
   public static final int TICKS_BEFORE_CHUNK_UNLOADING_REMINDER = 5000;
   public int ticksUntilUnloadingReminder = 5000;

   @Override
   public void tick(float dt, int systemIndex, @Nonnull Store<ChunkStore> store) {
      ChunkUnloadingSystem.Data dataResource = store.getResource(ChunkStore.UNLOAD_RESOURCE);
      World world = store.getExternalData().getWorld();
      if (!world.getWorldConfig().canUnloadChunks()) {
         this.ticksUntilUnloadingReminder--;
         if (this.ticksUntilUnloadingReminder <= 0) {
            world.getLogger().at(Level.INFO).log("This world has disabled chunk unloading");
            this.ticksUntilUnloadingReminder = 5000;
         }
      } else {
         int pollCount = 1;
         double percentOfRAMUsed = 1.0 - (double)Runtime.getRuntime().freeMemory() / Runtime.getRuntime().maxMemory();
         if (percentOfRAMUsed > 0.85) {
            double desperatePercent = (percentOfRAMUsed - 0.85) / 0.15000000000000002;
            pollCount = Math.max(MathUtil.ceil(desperatePercent * 3.0), 1);
         }

         dataResource.pollCount = pollCount;
         if (dataResource.tick(dt)) {
            dataResource.chunkTrackers.clear();
            world.getEntityStore().getStore().forEachChunk(ChunkTracker.getComponentType(), ChunkUnloadingSystem::collectTrackers);
            store.forEachEntityParallel(WorldChunk.getComponentType(), ChunkUnloadingSystem::tryUnload);
         }
      }
   }

   public static void tryUnload(int index, @Nonnull ArchetypeChunk<ChunkStore> archetypeChunk, @Nonnull CommandBuffer<ChunkStore> commandBuffer) {
      Store<ChunkStore> store = commandBuffer.getStore();
      World world = store.getExternalData().getWorld();
      WorldChunk worldChunkComponent = archetypeChunk.getComponent(index, WorldChunk.getComponentType());

      assert worldChunkComponent != null;

      ChunkUnloadingSystem.Data dataResource = commandBuffer.getResource(ChunkStore.UNLOAD_RESOURCE);
      ChunkTracker.ChunkVisibility chunkVisibility = getChunkVisibility(dataResource.chunkTrackers, worldChunkComponent.getIndex());
      if (chunkVisibility == ChunkTracker.ChunkVisibility.HOT) {
         worldChunkComponent.resetKeepAlive();
         worldChunkComponent.resetActiveTimer();
      } else {
         Box2D keepLoaded = world.getWorldConfig().getChunkConfig().getKeepLoadedRegion();
         boolean shouldKeepLoaded = worldChunkComponent.shouldKeepLoaded()
            || keepLoaded != null && isChunkInBox(keepLoaded, worldChunkComponent.getX(), worldChunkComponent.getZ());
         int pollCount = dataResource.pollCount;
         if (chunkVisibility == ChunkTracker.ChunkVisibility.COLD || worldChunkComponent.getNeedsSaving() || shouldKeepLoaded) {
            worldChunkComponent.resetKeepAlive();
            if (worldChunkComponent.is(ChunkFlag.TICKING) && worldChunkComponent.pollActiveTimer(pollCount) <= 0) {
               commandBuffer.run(s -> worldChunkComponent.setFlag(ChunkFlag.TICKING, false));
            }
         } else if (worldChunkComponent.pollKeepAlive(pollCount) <= 0) {
            Ref<ChunkStore> chunkRef = archetypeChunk.getReferenceTo(index);
            ChunkUnloadEvent event = new ChunkUnloadEvent(worldChunkComponent);
            commandBuffer.invoke(chunkRef, event);
            if (event.isCancelled()) {
               if (event.willResetKeepAlive()) {
                  worldChunkComponent.resetKeepAlive();
               }
            } else {
               commandBuffer.run(s -> s.getExternalData().remove(chunkRef, RemoveReason.UNLOAD));
            }
         }
      }
   }

   public static ChunkTracker.ChunkVisibility getChunkVisibility(@Nonnull List<ChunkTracker> playerChunkTrackers, long chunkIndex) {
      boolean isVisible = false;

      for (ChunkTracker chunkTracker : playerChunkTrackers) {
         switch (chunkTracker.getChunkVisibility(chunkIndex)) {
            case NONE:
            default:
               break;
            case HOT:
               return ChunkTracker.ChunkVisibility.HOT;
            case COLD:
               isVisible = true;
         }
      }

      return isVisible ? ChunkTracker.ChunkVisibility.COLD : ChunkTracker.ChunkVisibility.NONE;
   }

   private static boolean isChunkInBox(@Nonnull Box2D box, int x, int z) {
      int minX = ChunkUtil.minBlock(x);
      int minZ = ChunkUtil.minBlock(z);
      int maxX = ChunkUtil.maxBlock(x);
      int maxZ = ChunkUtil.maxBlock(z);
      return maxX >= box.min.x && minX <= box.max.x && maxZ >= box.min.y && minZ <= box.max.y;
   }

   private static void collectTrackers(@Nonnull ArchetypeChunk<EntityStore> archetypeChunk, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
      Store<ChunkStore> chunkStore = commandBuffer.getExternalData().getWorld().getChunkStore().getStore();
      ChunkUnloadingSystem.Data dataResource = chunkStore.getResource(ChunkStore.UNLOAD_RESOURCE);

      for (int index = 0; index < archetypeChunk.size(); index++) {
         ChunkTracker chunkTracker = archetypeChunk.getComponent(index, ChunkTracker.getComponentType());
         dataResource.chunkTrackers.add(chunkTracker);
      }
   }

   public static class Data implements Resource<ChunkStore> {
      public static final float UNLOAD_INTERVAL = 0.5F;
      private float time;
      private int pollCount = 1;
      @Nonnull
      private final List<ChunkTracker> chunkTrackers = new ObjectArrayList();

      public Data() {
         this.time = 0.5F;
      }

      public Data(float time) {
         this.time = time;
      }

      @Nonnull
      @Override
      public Resource<ChunkStore> clone() {
         return new ChunkUnloadingSystem.Data(this.time);
      }

      public boolean tick(float dt) {
         this.time -= dt;
         if (this.time <= 0.0F) {
            this.time += 0.5F;
            return true;
         } else {
            return false;
         }
      }
   }
}
