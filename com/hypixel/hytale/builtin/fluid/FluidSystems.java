package com.hypixel.hytale.builtin.fluid;

import com.hypixel.hytale.builtin.blocktick.system.ChunkBlockTickSystem;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.Order;
import com.hypixel.hytale.component.dependency.RootDependency;
import com.hypixel.hytale.component.dependency.SystemDependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.HolderSystem;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.component.system.tick.RunWhenPausedSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.packets.world.ServerSetFluid;
import com.hypixel.hytale.protocol.packets.world.ServerSetFluids;
import com.hypixel.hytale.protocol.packets.world.SetFluidCmd;
import com.hypixel.hytale.server.core.asset.type.blocktick.BlockTickStrategy;
import com.hypixel.hytale.server.core.asset.type.fluid.Fluid;
import com.hypixel.hytale.server.core.asset.type.fluid.FluidTicker;
import com.hypixel.hytale.server.core.modules.LegacyModule;
import com.hypixel.hytale.server.core.modules.entity.player.ChunkTracker;
import com.hypixel.hytale.server.core.modules.migrations.ChunkColumnMigrationSystem;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.ChunkColumn;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.chunk.section.ChunkSection;
import com.hypixel.hytale.server.core.universe.world.chunk.section.FluidSection;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.logging.Level;
import javax.annotation.Nonnull;

public class FluidSystems {
   @Nonnull
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   private static final int MAX_CHANGES_PER_PACKET = 1024;

   public static class EnsureFluidSection extends HolderSystem<ChunkStore> {
      @Nonnull
      private final ComponentType<ChunkStore, FluidSection> fluidSectionComponentType;
      @Nonnull
      private final Query<ChunkStore> query;

      public EnsureFluidSection(
         @Nonnull ComponentType<ChunkStore, ChunkSection> chunkSectionComponentType, @Nonnull ComponentType<ChunkStore, FluidSection> fluidSectionComponentType
      ) {
         this.fluidSectionComponentType = fluidSectionComponentType;
         this.query = Query.and(chunkSectionComponentType, Query.not(fluidSectionComponentType));
      }

      @Override
      public void onEntityAdd(@Nonnull Holder<ChunkStore> holder, @Nonnull AddReason reason, @Nonnull Store<ChunkStore> store) {
         holder.addComponent(this.fluidSectionComponentType, new FluidSection());
      }

      @Override
      public void onEntityRemoved(@Nonnull Holder<ChunkStore> holder, @Nonnull RemoveReason reason, @Nonnull Store<ChunkStore> store) {
      }

      @Nonnull
      @Override
      public Query<ChunkStore> getQuery() {
         return this.query;
      }

      @Nonnull
      @Override
      public Set<Dependency<ChunkStore>> getDependencies() {
         return RootDependency.firstSet();
      }
   }

   public static class LoadPacketGenerator extends ChunkStore.LoadFuturePacketDataQuerySystem {
      @Nonnull
      private final ComponentType<ChunkStore, ChunkColumn> chunkColumnComponentType;
      @Nonnull
      private final ComponentType<ChunkStore, FluidSection> fluidSectionComponentType;

      public LoadPacketGenerator(
         @Nonnull ComponentType<ChunkStore, ChunkColumn> chunkColumnComponentType, @Nonnull ComponentType<ChunkStore, FluidSection> fluidSectionComponentType
      ) {
         this.chunkColumnComponentType = chunkColumnComponentType;
         this.fluidSectionComponentType = fluidSectionComponentType;
      }

      public void fetch(
         int index,
         @Nonnull ArchetypeChunk<ChunkStore> archetypeChunk,
         Store<ChunkStore> store,
         @Nonnull CommandBuffer<ChunkStore> commandBuffer,
         PlayerRef query,
         @Nonnull List<CompletableFuture<ToClientPacket>> results
      ) {
         ChunkColumn chunkColumnComponent = archetypeChunk.getComponent(index, this.chunkColumnComponentType);

         assert chunkColumnComponent != null;

         for (Ref<ChunkStore> sectionRef : chunkColumnComponent.getSections()) {
            FluidSection fluidSectionComponent = commandBuffer.getComponent(sectionRef, this.fluidSectionComponentType);
            if (fluidSectionComponent != null) {
               results.add(fluidSectionComponent.getCachedPacket().exceptionally(throwable -> {
                  if (throwable != null) {
                     ((HytaleLogger.Api)FluidSystems.LOGGER.at(Level.SEVERE).withCause(throwable)).log("Exception when compressing chunk fluids:");
                  }

                  return null;
               }).thenApply(Function.identity()));
            }
         }
      }

      @Nonnull
      @Override
      public Query<ChunkStore> getQuery() {
         return this.chunkColumnComponentType;
      }
   }

   public static class MigrateFromColumn extends ChunkColumnMigrationSystem {
      @Nonnull
      private final ComponentType<ChunkStore, ChunkColumn> chunkColumnComponentType;
      @Nonnull
      private final ComponentType<ChunkStore, BlockChunk> blockChunkComponentType;
      @Nonnull
      private final ComponentType<ChunkStore, FluidSection> fluidSectionComponentType;
      @Nonnull
      private final Query<ChunkStore> query;
      @Nonnull
      private final Set<Dependency<ChunkStore>> dependencies = Set.of(new SystemDependency<>(Order.BEFORE, LegacyModule.MigrateLegacySections.class));

      public MigrateFromColumn(
         @Nonnull ComponentType<ChunkStore, ChunkColumn> chunkColumnComponentType,
         @Nonnull ComponentType<ChunkStore, BlockChunk> blockChunkComponentType,
         @Nonnull ComponentType<ChunkStore, FluidSection> fluidSectionComponentType
      ) {
         this.chunkColumnComponentType = chunkColumnComponentType;
         this.blockChunkComponentType = blockChunkComponentType;
         this.fluidSectionComponentType = fluidSectionComponentType;
         this.query = Query.and(chunkColumnComponentType, blockChunkComponentType);
      }

      @Override
      public void onEntityAdd(@Nonnull Holder<ChunkStore> holder, @Nonnull AddReason reason, @Nonnull Store<ChunkStore> store) {
         ChunkColumn chunkColumnComponent = holder.getComponent(this.chunkColumnComponentType);

         assert chunkColumnComponent != null;

         BlockChunk blockChunkComponent = holder.getComponent(this.blockChunkComponentType);

         assert blockChunkComponent != null;

         Holder<ChunkStore>[] sections = chunkColumnComponent.getSectionHolders();
         if (sections != null) {
            BlockSection[] legacySections = blockChunkComponent.getMigratedSections();
            if (legacySections != null) {
               for (int i = 0; i < sections.length; i++) {
                  Holder<ChunkStore> section = sections[i];
                  BlockSection paletteSection = legacySections[i];
                  if (section != null && paletteSection != null) {
                     FluidSection fluid = paletteSection.takeMigratedFluid();
                     if (fluid != null) {
                        section.putComponent(this.fluidSectionComponentType, fluid);
                        blockChunkComponent.markNeedsSaving();
                     }
                  }
               }
            }
         }
      }

      @Override
      public void onEntityRemoved(@Nonnull Holder<ChunkStore> holder, @Nonnull RemoveReason reason, @Nonnull Store<ChunkStore> store) {
      }

      @Nonnull
      @Override
      public Query<ChunkStore> getQuery() {
         return this.query;
      }

      @Nonnull
      @Override
      public Set<Dependency<ChunkStore>> getDependencies() {
         return this.dependencies;
      }
   }

   public static class ReplicateChanges extends EntityTickingSystem<ChunkStore> implements RunWhenPausedSystem<ChunkStore> {
      @Nonnull
      private final ComponentType<ChunkStore, ChunkSection> chunkSectionComponentType;
      @Nonnull
      private final ComponentType<ChunkStore, FluidSection> fluidSectionComponentType;
      @Nonnull
      private final ComponentType<ChunkStore, WorldChunk> worldChunkComponentType;
      @Nonnull
      private final Query<ChunkStore> query;

      public ReplicateChanges(
         @Nonnull ComponentType<ChunkStore, ChunkSection> chunkSectionComponentType,
         @Nonnull ComponentType<ChunkStore, FluidSection> fluidSectionComponentType,
         @Nonnull ComponentType<ChunkStore, WorldChunk> worldChunkComponentType
      ) {
         this.chunkSectionComponentType = chunkSectionComponentType;
         this.fluidSectionComponentType = fluidSectionComponentType;
         this.worldChunkComponentType = worldChunkComponentType;
         this.query = Query.and(chunkSectionComponentType, fluidSectionComponentType);
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
         FluidSection fluidSectionComponent = archetypeChunk.getComponent(index, this.fluidSectionComponentType);

         assert fluidSectionComponent != null;

         IntOpenHashSet changes = fluidSectionComponent.getAndClearChangedPositions();
         if (!changes.isEmpty()) {
            ChunkSection chunkSectionComponent = archetypeChunk.getComponent(index, this.chunkSectionComponentType);

            assert chunkSectionComponent != null;

            World world = commandBuffer.getExternalData().getWorld();
            WorldChunk worldChunkComponent = commandBuffer.getComponent(chunkSectionComponent.getChunkColumnReference(), this.worldChunkComponentType);
            int sectionY = chunkSectionComponent.getY();
            world.execute(() -> {
               if (worldChunkComponent != null && worldChunkComponent.getWorld() != null) {
                  worldChunkComponent.getWorld().getChunkLighting().invalidateLightInChunkSection(worldChunkComponent, sectionY);
               }
            });
            Collection<PlayerRef> playerRefs = store.getExternalData().getWorld().getPlayerRefs();
            if (playerRefs.isEmpty()) {
               changes.clear();
            } else {
               long chunkIndex = ChunkUtil.indexChunk(fluidSectionComponent.getX(), fluidSectionComponent.getZ());
               if (changes.size() >= 1024) {
                  ObjectArrayList<PlayerRef> playersCopy = new ObjectArrayList(playerRefs);
                  fluidSectionComponent.getCachedPacket().whenComplete((packetx, throwable) -> {
                     if (throwable != null) {
                        ((HytaleLogger.Api)FluidSystems.LOGGER.at(Level.SEVERE).withCause(throwable)).log("Exception when compressing chunk fluids:");
                     } else {
                        Iterator i$ = playersCopy.iterator();

                        while (i$.hasNext()) {
                           PlayerRef playerRefx = (PlayerRef)i$.next();
                           Ref<EntityStore> refx = playerRefx.getReference();
                           if (refx != null && refx.isValid()) {
                              ChunkTracker trackerx = playerRefx.getChunkTracker();
                              if (trackerx.isLoaded(chunkIndex)) {
                                 playerRefx.getPacketHandler().writeNoCache(packetx);
                              }
                           }
                        }
                     }
                  });
                  changes.clear();
               } else {
                  if (changes.size() == 1) {
                     int change = changes.iterator().nextInt();
                     int x = ChunkUtil.minBlock(fluidSectionComponent.getX()) + ChunkUtil.xFromIndex(change);
                     int y = ChunkUtil.minBlock(fluidSectionComponent.getY()) + ChunkUtil.yFromIndex(change);
                     int z = ChunkUtil.minBlock(fluidSectionComponent.getZ()) + ChunkUtil.zFromIndex(change);
                     int fluid = fluidSectionComponent.getFluidId(change);
                     byte level = fluidSectionComponent.getFluidLevel(change);
                     ServerSetFluid packet = new ServerSetFluid(x, y, z, fluid, level);

                     for (PlayerRef playerRef : playerRefs) {
                        Ref<EntityStore> ref = playerRef.getReference();
                        if (ref != null && ref.isValid()) {
                           ChunkTracker tracker = playerRef.getChunkTracker();
                           if (tracker.isLoaded(chunkIndex)) {
                              playerRef.getPacketHandler().writeNoCache(packet);
                           }
                        }
                     }
                  } else {
                     SetFluidCmd[] cmds = new SetFluidCmd[changes.size()];
                     IntIterator iter = changes.intIterator();
                     int i = 0;

                     while (iter.hasNext()) {
                        int change = iter.nextInt();
                        int fluid = fluidSectionComponent.getFluidId(change);
                        byte level = fluidSectionComponent.getFluidLevel(change);
                        cmds[i++] = new SetFluidCmd((short)change, fluid, level);
                     }

                     ServerSetFluids packet = new ServerSetFluids(
                        fluidSectionComponent.getX(), fluidSectionComponent.getY(), fluidSectionComponent.getZ(), cmds
                     );

                     for (PlayerRef playerRefx : playerRefs) {
                        Ref<EntityStore> ref = playerRefx.getReference();
                        if (ref != null && ref.isValid()) {
                           ChunkTracker tracker = playerRefx.getChunkTracker();
                           if (tracker.isLoaded(chunkIndex)) {
                              playerRefx.getPacketHandler().writeNoCache(packet);
                           }
                        }
                     }
                  }

                  changes.clear();
               }
            }
         }
      }

      @Nonnull
      @Override
      public Query<ChunkStore> getQuery() {
         return this.query;
      }

      @Nonnull
      @Override
      public Set<Dependency<ChunkStore>> getDependencies() {
         return RootDependency.lastSet();
      }
   }

   public static class SetupSection extends HolderSystem<ChunkStore> {
      @Nonnull
      private final ComponentType<ChunkStore, ChunkSection> chunkSectionComponentType;
      @Nonnull
      private final ComponentType<ChunkStore, FluidSection> fluidSectionComponentType;
      @Nonnull
      private final Query<ChunkStore> query;
      @Nonnull
      private final Set<Dependency<ChunkStore>> dependencies = Set.of(new SystemDependency<>(Order.AFTER, FluidSystems.MigrateFromColumn.class));

      public SetupSection(
         @Nonnull ComponentType<ChunkStore, ChunkSection> chunkSectionComponentType, @Nonnull ComponentType<ChunkStore, FluidSection> fluidSectionComponentType
      ) {
         this.chunkSectionComponentType = chunkSectionComponentType;
         this.fluidSectionComponentType = fluidSectionComponentType;
         this.query = Query.and(chunkSectionComponentType, fluidSectionComponentType);
      }

      @Override
      public void onEntityAdd(@Nonnull Holder<ChunkStore> holder, @Nonnull AddReason reason, @Nonnull Store<ChunkStore> store) {
         ChunkSection chunkSectionComponent = holder.getComponent(this.chunkSectionComponentType);

         assert chunkSectionComponent != null;

         FluidSection fluidSectionComponent = holder.getComponent(this.fluidSectionComponentType);

         assert fluidSectionComponent != null;

         fluidSectionComponent.load(chunkSectionComponent.getX(), chunkSectionComponent.getY(), chunkSectionComponent.getZ());
      }

      @Override
      public void onEntityRemoved(@Nonnull Holder<ChunkStore> holder, @Nonnull RemoveReason reason, @Nonnull Store<ChunkStore> store) {
      }

      @Nonnull
      @Override
      public Query<ChunkStore> getQuery() {
         return this.query;
      }

      @Nonnull
      @Override
      public Set<Dependency<ChunkStore>> getDependencies() {
         return this.dependencies;
      }
   }

   public static class Ticking extends EntityTickingSystem<ChunkStore> {
      @Nonnull
      private final ComponentType<ChunkStore, ChunkSection> chunkSectionComponentType;
      @Nonnull
      private final ComponentType<ChunkStore, FluidSection> fluidSectionComponentType;
      @Nonnull
      private final ComponentType<ChunkStore, BlockChunk> blockChunkComponentType;
      @Nonnull
      private final Query<ChunkStore> query;
      @Nonnull
      private final Set<Dependency<ChunkStore>> dependencies = Set.of(
         new SystemDependency<>(Order.AFTER, ChunkBlockTickSystem.PreTick.class), new SystemDependency<>(Order.BEFORE, ChunkBlockTickSystem.Ticking.class)
      );

      public Ticking(
         @Nonnull ComponentType<ChunkStore, ChunkSection> chunkSectionComponentType,
         @Nonnull ComponentType<ChunkStore, FluidSection> fluidSectionComponentType,
         @Nonnull ComponentType<ChunkStore, BlockChunk> blockChunkComponentType
      ) {
         this.chunkSectionComponentType = chunkSectionComponentType;
         this.fluidSectionComponentType = fluidSectionComponentType;
         this.blockChunkComponentType = blockChunkComponentType;
         this.query = Query.and(fluidSectionComponentType, chunkSectionComponentType);
      }

      @Override
      public boolean isParallel(int archetypeChunkSize, int taskCount) {
         return EntityTickingSystem.useParallel(archetypeChunkSize, taskCount);
      }

      @Override
      public void tick(
         float dt,
         int index,
         @Nonnull ArchetypeChunk<ChunkStore> archetypeChunk,
         @Nonnull Store<ChunkStore> store,
         @Nonnull CommandBuffer<ChunkStore> commandBuffer
      ) {
         ChunkSection chunkSectionComponent = archetypeChunk.getComponent(index, this.chunkSectionComponentType);

         assert chunkSectionComponent != null;

         FluidSection fluidSectionComponent = archetypeChunk.getComponent(index, this.fluidSectionComponentType);

         assert fluidSectionComponent != null;

         Ref<ChunkStore> chunkRef = chunkSectionComponent.getChunkColumnReference();
         BlockChunk blockChunkComponent = commandBuffer.getComponent(chunkRef, this.blockChunkComponentType);
         if (blockChunkComponent != null) {
            BlockSection blockSection = blockChunkComponent.getSectionAtIndex(fluidSectionComponent.getY());
            if (blockSection != null) {
               if (blockSection.getTickingBlocksCountCopy() != 0) {
                  FluidTicker.CachedAccessor accessor = FluidTicker.CachedAccessor.of(commandBuffer, fluidSectionComponent, blockSection, 5);
                  blockSection.forEachTicking(accessor, commandBuffer, fluidSectionComponent.getY(), (accessor1, commandBuffer1, x, y, z, block) -> {
                     FluidSection fluidSection1 = accessor1.selfFluidSection;
                     BlockSection blockSection1 = accessor1.selfBlockSection;
                     int fluidId = fluidSection1.getFluidId(x, y, z);
                     if (fluidId == 0) {
                        return BlockTickStrategy.IGNORED;
                     } else {
                        int blockX = fluidSection1.getX() << 5 | x;
                        int blockZ = fluidSection1.getZ() << 5 | z;
                        Fluid fluid = Fluid.getAssetMap().getAsset(fluidId);
                        return fluid.getTicker().tick(commandBuffer1, accessor1, fluidSection1, blockSection1, fluid, fluidId, blockX, y, blockZ);
                     }
                  });
               }
            }
         }
      }

      @Nonnull
      @Override
      public Query<ChunkStore> getQuery() {
         return this.query;
      }

      @Nonnull
      @Override
      public Set<Dependency<ChunkStore>> getDependencies() {
         return this.dependencies;
      }
   }
}
