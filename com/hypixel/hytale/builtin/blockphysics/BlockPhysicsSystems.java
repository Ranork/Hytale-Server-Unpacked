package com.hypixel.hytale.builtin.blockphysics;

import com.hypixel.hytale.builtin.blocktick.system.ChunkBlockTickSystem;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.DisableProcessingAssert;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.Order;
import com.hypixel.hytale.component.dependency.SystemDependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.server.core.asset.type.blocktick.BlockTickStrategy;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.blocktype.component.BlockPhysics;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.AbstractCachedAccessor;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.chunk.section.ChunkSection;
import com.hypixel.hytale.server.core.universe.world.chunk.section.FluidSection;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Set;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockPhysicsSystems {
   @Nonnull
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   public static final int MAX_SUPPORT_RADIUS = 14;

   public static class CachedAccessor extends AbstractCachedAccessor {
      @Nonnull
      private static final ThreadLocal<BlockPhysicsSystems.CachedAccessor> THREAD_LOCAL = ThreadLocal.withInitial(BlockPhysicsSystems.CachedAccessor::new);
      private static final int PHYSICS_COMPONENT = 0;
      private static final int FLUID_COMPONENT = 1;
      private static final int BLOCK_COMPONENT = 2;
      protected BlockSection selfBlockSection;
      protected BlockPhysics selfPhysics;
      protected FluidSection selfFluidSection;

      protected CachedAccessor() {
         super(3);
      }

      @Nonnull
      public static BlockPhysicsSystems.CachedAccessor of(
         ComponentAccessor<ChunkStore> commandBuffer,
         BlockSection blockSection,
         BlockPhysics section,
         FluidSection fluidSection,
         int cx,
         int cy,
         int cz,
         int radius
      ) {
         BlockPhysicsSystems.CachedAccessor accessor = THREAD_LOCAL.get();
         accessor.init(commandBuffer, cx, cy, cz, radius);
         accessor.insertSectionComponent(0, section, cx, cy, cz);
         accessor.insertSectionComponent(1, fluidSection, cx, cy, cz);
         accessor.insertSectionComponent(2, blockSection, cx, cy, cz);
         accessor.selfBlockSection = blockSection;
         accessor.selfPhysics = section;
         accessor.selfFluidSection = fluidSection;
         return accessor;
      }

      @Nullable
      public BlockPhysics getBlockPhysics(int cx, int cy, int cz) {
         return this.getComponentSection(cx, cy, cz, 0, BlockPhysics.getComponentType());
      }

      @Nullable
      public FluidSection getFluidSection(int cx, int cy, int cz) {
         return this.getComponentSection(cx, cy, cz, 1, FluidSection.getComponentType());
      }

      @Nullable
      public BlockSection getBlockSection(int cx, int cy, int cz) {
         return this.getComponentSection(cx, cy, cz, 2, BlockSection.getComponentType());
      }

      public void performBlockUpdate(int x, int y, int z, int maxSupportDistance) {
         for (int ix = -1; ix < 2; ix++) {
            int wx = x + ix;

            for (int iz = -1; iz < 2; iz++) {
               int wz = z + iz;

               for (int iy = -1; iy < 2; iy++) {
                  int wy = y + iy;
                  BlockPhysics physics = this.getBlockPhysics(ChunkUtil.chunkCoordinate(wx), ChunkUtil.chunkCoordinate(wy), ChunkUtil.chunkCoordinate(wz));
                  int support = physics != null ? physics.get(wx, wy, wz) : 0;
                  if (support <= maxSupportDistance) {
                     BlockSection blockChunk = this.getBlockSection(ChunkUtil.chunkCoordinate(wx), ChunkUtil.chunkCoordinate(wy), ChunkUtil.chunkCoordinate(wz));
                     if (blockChunk != null) {
                        blockChunk.setTicking(wx, wy, wz, true);
                     }
                  }
               }
            }
         }
      }

      public void performBlockUpdate(int x, int y, int z) {
         for (int ix = -1; ix < 2; ix++) {
            int wx = x + ix;

            for (int iz = -1; iz < 2; iz++) {
               int wz = z + iz;

               for (int iy = -1; iy < 2; iy++) {
                  int wy = y + iy;
                  BlockSection blockChunk = this.getBlockSection(ChunkUtil.chunkCoordinate(wx), ChunkUtil.chunkCoordinate(wy), ChunkUtil.chunkCoordinate(wz));
                  if (blockChunk != null) {
                     blockChunk.setTicking(wx, wy, wz, true);
                  }
               }
            }
         }
      }
   }

   public static class Ticking extends EntityTickingSystem<ChunkStore> implements DisableProcessingAssert {
      @Nonnull
      private static final Query<ChunkStore> QUERY = Query.and(
         ChunkSection.getComponentType(), BlockSection.getComponentType(), BlockPhysics.getComponentType(), FluidSection.getComponentType()
      );
      @Nonnull
      private static final Set<Dependency<ChunkStore>> DEPENDENCIES = Set.of(
         new SystemDependency<>(Order.AFTER, ChunkBlockTickSystem.PreTick.class), new SystemDependency<>(Order.BEFORE, ChunkBlockTickSystem.Ticking.class)
      );

      @Nonnull
      @Override
      public Query<ChunkStore> getQuery() {
         return QUERY;
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
         ChunkSection section = archetypeChunk.getComponent(index, ChunkSection.getComponentType());

         assert section != null;

         try {
            BlockSection blockSectionComponent = archetypeChunk.getComponent(index, BlockSection.getComponentType());
            if (blockSectionComponent == null) {
               return;
            }

            if (blockSectionComponent.getTickingBlocksCountCopy() <= 0) {
               return;
            }

            BlockPhysics blockPhysicsComponent = archetypeChunk.getComponent(index, BlockPhysics.getComponentType());
            if (blockPhysicsComponent == null) {
               return;
            }

            FluidSection fluidSectionComponent = archetypeChunk.getComponent(index, FluidSection.getComponentType());
            if (fluidSectionComponent == null) {
               return;
            }

            Ref<ChunkStore> columnRef = section.getChunkColumnReference();
            if (columnRef == null || !columnRef.isValid()) {
               return;
            }

            WorldChunk worldChunkComponent = commandBuffer.getComponent(columnRef, WorldChunk.getComponentType());
            if (worldChunkComponent == null) {
               return;
            }

            BlockPhysicsSystems.CachedAccessor accessor = BlockPhysicsSystems.CachedAccessor.of(
               commandBuffer, blockSectionComponent, blockPhysicsComponent, fluidSectionComponent, section.getX(), section.getY(), section.getZ(), 14
            );
            blockSectionComponent.forEachTicking(
               worldChunkComponent,
               accessor,
               section.getY(),
               (wc, accessor1, localX, localY, localZ, blockId) -> {
                  BlockPhysics phys = accessor1.selfPhysics;
                  boolean isDeco = phys.isDeco(localX, localY, localZ);
                  BlockType blockType = BlockType.getAssetMap().getAsset(blockId);
                  if (blockType == null || blockId == 0) {
                     return BlockTickStrategy.IGNORED;
                  } else if (blockType.canBePlacedAsDeco() && isDeco) {
                     return BlockTickStrategy.IGNORED;
                  } else {
                     World world = wc.getWorld();
                     Store<EntityStore> entityStore = world.getEntityStore().getStore();
                     int blockX = wc.getX() << 5 | localX;
                     int blockZ = wc.getZ() << 5 | localZ;
                     int filler = accessor1.selfBlockSection.getFiller(localX, localY, localZ);
                     int rotation = accessor1.selfBlockSection.getRotationIndex(localX, localY, localZ);

                     return switch (BlockPhysicsUtil.applyBlockPhysics(
                        entityStore,
                        wc.getReference(),
                        accessor,
                        accessor1.selfBlockSection,
                        accessor1.selfPhysics,
                        accessor1.selfFluidSection,
                        blockX,
                        localY,
                        blockZ,
                        blockType,
                        rotation,
                        filler
                     )) {
                        case WAITING_CHUNK -> BlockTickStrategy.WAIT_FOR_ADJACENT_CHUNK_LOAD;
                        case VALID -> BlockTickStrategy.IGNORED;
                        case INVALID -> BlockTickStrategy.SLEEP;
                     };
                  }
               }
            );
         } catch (Exception var13) {
            ((HytaleLogger.Api)BlockPhysicsSystems.LOGGER.at(Level.SEVERE).withCause(var13)).log("Failed to tick chunk: %s", section);
         }
      }
   }
}
