package com.hypixel.hytale.server.core.universe.world.lighting;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import javax.annotation.Nonnull;

public class ChunkLightingManager implements Runnable {
   @Nonnull
   private final HytaleLogger logger;
   @Nonnull
   private final Thread thread;
   @Nonnull
   private final World world;
   private final Semaphore semaphore = new Semaphore(1);
   private final Set<Vector3i> set = ConcurrentHashMap.newKeySet();
   private final ObjectArrayFIFOQueue<Vector3i> queue = new ObjectArrayFIFOQueue();
   private LightCalculation lightCalculation;

   public ChunkLightingManager(@Nonnull World world) {
      this.logger = HytaleLogger.get("World|" + world.getName() + "|L");
      this.thread = new Thread(this, "ChunkLighting - " + world.getName());
      this.thread.setDaemon(true);
      this.world = world;
      this.lightCalculation = new FloodLightCalculation(this);
   }

   @Nonnull
   protected HytaleLogger getLogger() {
      return this.logger;
   }

   @Nonnull
   public World getWorld() {
      return this.world;
   }

   public void setLightCalculation(LightCalculation lightCalculation) {
      this.lightCalculation = lightCalculation;
   }

   public LightCalculation getLightCalculation() {
      return this.lightCalculation;
   }

   public void start() {
      this.thread.start();
   }

   @Override
   public void run() {
      try {
         int lastSize = 0;
         int count = 0;

         while (!this.thread.isInterrupted()) {
            this.semaphore.drainPermits();
            Vector3i pos;
            synchronized (this.queue) {
               pos = this.queue.isEmpty() ? null : (Vector3i)this.queue.dequeue();
            }

            if (pos != null) {
               this.process(pos);
            }

            Thread.yield();
            int currentSize;
            synchronized (this.queue) {
               currentSize = this.queue.size();
            }

            if (currentSize != lastSize) {
               count = 0;
               lastSize = currentSize;
            } else if (count <= currentSize) {
               count++;
            } else {
               this.semaphore.acquire();
            }
         }
      } catch (InterruptedException var9) {
         Thread.currentThread().interrupt();
      }
   }

   private void process(Vector3i chunkPosition) {
      try {
         switch (this.lightCalculation.calculateLight(chunkPosition)) {
            case NOT_LOADED:
            case WAITING_FOR_NEIGHBOUR:
            case DONE:
               this.set.remove(chunkPosition);
               break;
            case INVALIDATED:
               synchronized (this.queue) {
                  this.queue.enqueue(chunkPosition);
               }
         }
      } catch (Exception var5) {
         ((HytaleLogger.Api)this.logger.at(Level.WARNING).withCause(var5)).log("Failed to calculate lighting for: %s", chunkPosition);
         this.set.remove(chunkPosition);
      }
   }

   public boolean interrupt() {
      if (this.thread.isAlive()) {
         this.thread.interrupt();
         return true;
      } else {
         return false;
      }
   }

   public void stop() {
      try {
         int i = 0;

         while (this.thread.isAlive()) {
            this.thread.interrupt();
            this.thread.join(this.world.getTickStepNanos() / 1000000);
            i += this.world.getTickStepNanos() / 1000000;
            if (i > 5000) {
               StringBuilder sb = new StringBuilder();

               for (StackTraceElement traceElement : this.thread.getStackTrace()) {
                  sb.append("\tat ").append(traceElement).append('\n');
               }

               HytaleLogger.getLogger().at(Level.SEVERE).log("Forcing ChunkLighting Thread %s to stop:\n%s", this.thread, sb.toString());
               this.thread.stop();
               break;
            }
         }
      } catch (InterruptedException var7) {
         Thread.currentThread().interrupt();
      }
   }

   public void init(WorldChunk worldChunk) {
      this.lightCalculation.init(worldChunk);
   }

   public void addToQueue(Vector3i chunkPosition) {
      if (this.set.add(chunkPosition)) {
         synchronized (this.queue) {
            this.queue.enqueue(chunkPosition);
         }

         this.semaphore.release(1);
      }
   }

   public boolean isQueued(int chunkX, int chunkZ) {
      Vector3i chunkPos = new Vector3i(chunkX, 0, chunkZ);

      for (int chunkY = 0; chunkY < 10; chunkY++) {
         chunkPos.setY(chunkY);
         if (this.isQueued(chunkPos)) {
            return true;
         }
      }

      return false;
   }

   public boolean isQueued(Vector3i chunkPosition) {
      return this.set.contains(chunkPosition);
   }

   public int getQueueSize() {
      synchronized (this.queue) {
         return this.queue.size();
      }
   }

   public boolean invalidateLightAtBlock(WorldChunk worldChunk, int blockX, int blockY, int blockZ, BlockType blockType, int oldHeight, int newHeight) {
      return this.lightCalculation.invalidateLightAtBlock(worldChunk, blockX, blockY, blockZ, blockType, oldHeight, newHeight);
   }

   public boolean invalidateLightInChunk(WorldChunk worldChunk) {
      return this.lightCalculation.invalidateLightInChunkSections(worldChunk, 0, 10);
   }

   public boolean invalidateLightInChunkSection(WorldChunk worldChunk, int sectionIndex) {
      return this.lightCalculation.invalidateLightInChunkSections(worldChunk, sectionIndex, sectionIndex + 1);
   }

   public boolean invalidateLightInChunkSections(WorldChunk worldChunk, int sectionIndexFrom, int sectionIndexTo) {
      return this.lightCalculation.invalidateLightInChunkSections(worldChunk, sectionIndexFrom, sectionIndexTo);
   }

   public void invalidateLoadedChunks() {
      this.world.getChunkStore().getStore().forEachEntityParallel(WorldChunk.getComponentType(), (index, archetypeChunk, storeCommandBuffer) -> {
         WorldChunk chunk = archetypeChunk.getComponent(index, WorldChunk.getComponentType());

         for (int y = 0; y < 10; y++) {
            BlockSection section = chunk.getBlockChunk().getSectionAtIndex(y);
            section.invalidateLocalLight();
            if (BlockChunk.SEND_LOCAL_LIGHTING_DATA || BlockChunk.SEND_GLOBAL_LIGHTING_DATA) {
               chunk.getBlockChunk().invalidateChunkSection(y);
            }
         }
      });
      this.world.getChunkStore().getChunkIndexes().forEach(index -> {
         int x = ChunkUtil.xOfChunkIndex(index);
         int z = ChunkUtil.zOfChunkIndex(index);

         for (int y = 0; y < 10; y++) {
            this.addToQueue(new Vector3i(x, y, z));
         }
      });
   }
}
