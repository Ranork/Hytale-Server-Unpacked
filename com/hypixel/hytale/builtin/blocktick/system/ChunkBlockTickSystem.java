package com.hypixel.hytale.builtin.blocktick.system;

import com.hypixel.hytale.builtin.blocktick.BlockTickPlugin;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.Order;
import com.hypixel.hytale.component.dependency.SystemDependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.asset.type.blocktick.BlockTickManager;
import com.hypixel.hytale.server.core.asset.type.blocktick.BlockTickStrategy;
import com.hypixel.hytale.server.core.asset.type.blocktick.config.TickProcedure;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.modules.time.WorldTimeResource;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.time.Instant;
import java.util.Set;
import java.util.logging.Level;
import javax.annotation.Nonnull;

public class ChunkBlockTickSystem {
   @Nonnull
   protected static final HytaleLogger LOGGER = BlockTickPlugin.get().getLogger();

   public static class PreTick extends EntityTickingSystem<ChunkStore> {
      @Nonnull
      private static final ComponentType<ChunkStore, BlockChunk> COMPONENT_TYPE_WORLD_CHUNK = BlockChunk.getComponentType();

      @Override
      public Query<ChunkStore> getQuery() {
         return COMPONENT_TYPE_WORLD_CHUNK;
      }

      @Override
      public boolean isParallel(int archetypeChunkSize, int taskCount) {
         return EntityTickingSystem.maybeUseParallel(archetypeChunkSize, taskCount);
      }

      @Override
      public void tick(
         float dt,
         int index,
         @Nonnull ArchetypeChunk<ChunkStore> archetypeChunk,
         @Nonnull Store<ChunkStore> store,
         @Nonnull CommandBuffer<ChunkStore> commandBuffer
      ) {
         World world = commandBuffer.getExternalData().getWorld();
         Store<EntityStore> entityStore = world.getEntityStore().getStore();
         Instant timeResource = entityStore.getResource(WorldTimeResource.getResourceType()).getGameTime();
         BlockChunk blockChunkComponent = archetypeChunk.getComponent(index, COMPONENT_TYPE_WORLD_CHUNK);

         assert blockChunkComponent != null;

         try {
            blockChunkComponent.preTick(timeResource);
         } catch (Throwable var11) {
            ((HytaleLogger.Api)ChunkBlockTickSystem.LOGGER.at(Level.SEVERE).withCause(var11)).log("Failed to pre-tick chunk: %s", blockChunkComponent);
         }
      }
   }

   public static class Ticking extends EntityTickingSystem<ChunkStore> {
      @Nonnull
      private static final ComponentType<ChunkStore, WorldChunk> COMPONENT_TYPE_WORLD_CHUNK = WorldChunk.getComponentType();
      @Nonnull
      private static final Set<Dependency<ChunkStore>> DEPENDENCIES = Set.of(new SystemDependency<>(Order.AFTER, ChunkBlockTickSystem.PreTick.class));

      @Override
      public Query<ChunkStore> getQuery() {
         return COMPONENT_TYPE_WORLD_CHUNK;
      }

      @Nonnull
      @Override
      public Set<Dependency<ChunkStore>> getDependencies() {
         return DEPENDENCIES;
      }

      @Override
      public void tick(
         float dt,
         int index,
         @Nonnull ArchetypeChunk<ChunkStore> archetypeChunk,
         @Nonnull Store<ChunkStore> store,
         @Nonnull CommandBuffer<ChunkStore> commandBuffer
      ) {
         Ref<ChunkStore> reference = archetypeChunk.getReferenceTo(index);
         WorldChunk worldChunkComponent = archetypeChunk.getComponent(index, COMPONENT_TYPE_WORLD_CHUNK);

         assert worldChunkComponent != null;

         try {
            tick(reference, worldChunkComponent);
         } catch (Throwable var9) {
            ((HytaleLogger.Api)ChunkBlockTickSystem.LOGGER.at(Level.SEVERE).withCause(var9)).log("Failed to tick chunk: %s", worldChunkComponent);
         }
      }

      protected static void tick(@Nonnull Ref<ChunkStore> ref, @Nonnull WorldChunk worldChunk) {
         BlockChunk blockChunkComponent = worldChunk.getBlockChunk();
         if (blockChunkComponent == null) {
            ChunkBlockTickSystem.LOGGER
               .at(Level.WARNING)
               .log("World chunk (%d, %d) has no BlockChunk component, skipping block ticking.", worldChunk.getX(), worldChunk.getZ());
         } else {
            int ticked = blockChunkComponent.forEachTicking(ref, worldChunk, (r, c, localX, localY, localZ, blockId) -> {
               World world = c.getWorld();
               int blockX = c.getX() << 5 | localX;
               int blockZ = c.getZ() << 5 | localZ;
               return tickProcedure(world, c, blockX, localY, blockZ, blockId);
            });
            if (ticked > 0) {
               ChunkBlockTickSystem.LOGGER.at(Level.FINER).log("Ticked %d blocks in chunk (%d, %d)", ticked, worldChunk.getX(), worldChunk.getZ());
            }
         }
      }

      protected static BlockTickStrategy tickProcedure(@Nonnull World world, @Nonnull WorldChunk chunk, int blockX, int blockY, int blockZ, int blockId) {
         if (world.getWorldConfig().isBlockTicking() && BlockTickManager.hasBlockTickProvider()) {
            TickProcedure procedure = BlockTickPlugin.get().getTickProcedure(blockId);
            if (procedure == null) {
               return BlockTickStrategy.IGNORED;
            } else {
               try {
                  return procedure.onTick(world, chunk, blockX, blockY, blockZ, blockId);
               } catch (Throwable var9) {
                  BlockType blockType = BlockType.getAssetMap().getAsset(blockId);
                  ((HytaleLogger.Api)ChunkBlockTickSystem.LOGGER.at(Level.WARNING).withCause(var9))
                     .log("Failed to tick block at (%d, %d, %d) ID %s in world %s:", blockX, blockY, blockZ, blockType.getId(), world.getName());
                  return BlockTickStrategy.SLEEP;
               }
            }
         } else {
            return BlockTickStrategy.IGNORED;
         }
      }
   }
}
