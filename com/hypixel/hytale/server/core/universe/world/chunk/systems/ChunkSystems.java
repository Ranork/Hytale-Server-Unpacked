package com.hypixel.hytale.server.core.universe.world.chunk.systems;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.NonTicking;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.Order;
import com.hypixel.hytale.component.dependency.RootDependency;
import com.hypixel.hytale.component.dependency.SystemDependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.HolderSystem;
import com.hypixel.hytale.component.system.RefChangeSystem;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.component.system.tick.RunWhenPausedSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.protocol.CachedPacket;
import com.hypixel.hytale.protocol.packets.world.ServerSetBlock;
import com.hypixel.hytale.protocol.packets.world.ServerSetBlocks;
import com.hypixel.hytale.protocol.packets.world.SetBlockCmd;
import com.hypixel.hytale.protocol.packets.world.SetChunk;
import com.hypixel.hytale.server.core.modules.entity.player.ChunkTracker;
import com.hypixel.hytale.server.core.modules.migrations.ChunkColumnMigrationSystem;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.chunk.ChunkColumn;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.chunk.section.ChunkSection;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ChunkSystems {
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   private static final int MAX_CHANGES_PER_PACKET = 1024;

   public static class EnsureBlockSection extends HolderSystem<ChunkStore> {
      private static final Query<ChunkStore> QUERY = Query.and(ChunkSection.getComponentType(), Query.not(BlockSection.getComponentType()));

      @Override
      public void onEntityAdd(@Nonnull Holder<ChunkStore> holder, @Nonnull AddReason reason, @Nonnull Store<ChunkStore> store) {
         holder.ensureComponent(BlockSection.getComponentType());
      }

      @Override
      public void onEntityRemoved(@Nonnull Holder<ChunkStore> holder, @Nonnull RemoveReason reason, @Nonnull Store<ChunkStore> store) {
      }

      @Nonnull
      @Override
      public Query<ChunkStore> getQuery() {
         return QUERY;
      }

      @Nonnull
      @Override
      public Set<Dependency<ChunkStore>> getDependencies() {
         return RootDependency.firstSet();
      }
   }

   public static class LoadBlockSection extends HolderSystem<ChunkStore> {
      @Override
      public void onEntityAdd(@Nonnull Holder<ChunkStore> holder, @Nonnull AddReason reason, @Nonnull Store<ChunkStore> store) {
         BlockSection section = holder.getComponent(BlockSection.getComponentType());

         assert section != null;

         section.loaded = true;
      }

      @Override
      public void onEntityRemoved(@Nonnull Holder<ChunkStore> holder, @Nonnull RemoveReason reason, @Nonnull Store<ChunkStore> store) {
      }

      @Override
      public Query<ChunkStore> getQuery() {
         return BlockSection.getComponentType();
      }
   }

   public static class OnChunkLoad extends RefSystem<ChunkStore> {
      private static final Query<ChunkStore> QUERY = Query.and(ChunkColumn.getComponentType(), WorldChunk.getComponentType());
      private static final Set<Dependency<ChunkStore>> DEPENDENCIES = Set.of(new SystemDependency<>(Order.AFTER, ChunkSystems.OnNewChunk.class));

      @Override
      public void onEntityAdded(
         @Nonnull Ref<ChunkStore> ref, @Nonnull AddReason reason, @Nonnull Store<ChunkStore> store, @Nonnull CommandBuffer<ChunkStore> commandBuffer
      ) {
         ChunkColumn chunk = commandBuffer.getComponent(ref, ChunkColumn.getComponentType());

         assert chunk != null;

         WorldChunk worldChunk = commandBuffer.getComponent(ref, WorldChunk.getComponentType());

         assert worldChunk != null;

         Ref<ChunkStore>[] sections = chunk.getSections();
         Holder<ChunkStore>[] sectionHolders = chunk.takeSectionHolders();
         boolean isNonTicking = commandBuffer.getArchetype(ref).contains(ChunkStore.REGISTRY.getNonTickingComponentType());
         if (sectionHolders != null && sectionHolders.length > 0 && sectionHolders[0] != null) {
            for (int i = 0; i < sectionHolders.length; i++) {
               if (isNonTicking) {
                  sectionHolders[i].ensureComponent(ChunkStore.REGISTRY.getNonTickingComponentType());
               } else {
                  sectionHolders[i].tryRemoveComponent(ChunkStore.REGISTRY.getNonTickingComponentType());
               }

               ChunkSection section = sectionHolders[i].getComponent(ChunkSection.getComponentType());
               if (section == null) {
                  sectionHolders[i].addComponent(ChunkSection.getComponentType(), new ChunkSection(ref, worldChunk.getX(), i, worldChunk.getZ()));
               } else {
                  section.load(ref, worldChunk.getX(), i, worldChunk.getZ());
               }
            }

            commandBuffer.addEntities(sectionHolders, 0, sections, 0, sections.length, AddReason.LOAD);
         }

         for (int i = 0; i < sections.length; i++) {
            if (sections[i] == null) {
               Holder<ChunkStore> newSection = ChunkStore.REGISTRY.newHolder();
               if (isNonTicking) {
                  newSection.ensureComponent(ChunkStore.REGISTRY.getNonTickingComponentType());
               } else {
                  newSection.tryRemoveComponent(ChunkStore.REGISTRY.getNonTickingComponentType());
               }

               newSection.addComponent(ChunkSection.getComponentType(), new ChunkSection(ref, worldChunk.getX(), i, worldChunk.getZ()));
               sections[i] = commandBuffer.addEntity(newSection, AddReason.SPAWN);
            }
         }
      }

      @Override
      public void onEntityRemove(
         @Nonnull Ref<ChunkStore> ref, @Nonnull RemoveReason reason, @Nonnull Store<ChunkStore> store, @Nonnull CommandBuffer<ChunkStore> commandBuffer
      ) {
         ChunkColumn chunk = commandBuffer.getComponent(ref, ChunkColumn.getComponentType());

         assert chunk != null;

         Ref<ChunkStore>[] sections = chunk.getSections();
         Holder<ChunkStore>[] holders = new Holder[sections.length];

         for (int i = 0; i < sections.length; i++) {
            Ref<ChunkStore> section = sections[i];
            holders[i] = ChunkStore.REGISTRY.newHolder();
            commandBuffer.removeEntity(section, holders[i], reason);
         }

         chunk.putSectionHolders(holders);
         Arrays.fill(sections, null);
      }

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
   }

   public static class OnNewChunk extends ChunkColumnMigrationSystem {
      private static final Query<ChunkStore> QUERY = Query.and(WorldChunk.getComponentType(), Query.not(ChunkColumn.getComponentType()));

      @Override
      public void onEntityAdd(@Nonnull Holder<ChunkStore> holder, @Nonnull AddReason reason, @Nonnull Store<ChunkStore> store) {
         Holder[] sectionHolders = new Holder[10];

         for (int i = 0; i < sectionHolders.length; i++) {
            sectionHolders[i] = ChunkStore.REGISTRY.newHolder();
         }

         holder.addComponent(ChunkColumn.getComponentType(), new ChunkColumn(sectionHolders));
      }

      @Override
      public void onEntityRemoved(@Nonnull Holder<ChunkStore> holder, @Nonnull RemoveReason reason, @Nonnull Store<ChunkStore> store) {
      }

      @Nonnull
      @Override
      public Query<ChunkStore> getQuery() {
         return QUERY;
      }

      @Nonnull
      @Override
      public Set<Dependency<ChunkStore>> getDependencies() {
         return RootDependency.firstSet();
      }
   }

   public static class OnNonTicking extends RefChangeSystem<ChunkStore, NonTicking<ChunkStore>> {
      private final Archetype<ChunkStore> archetype = Archetype.of(WorldChunk.getComponentType(), ChunkColumn.getComponentType());

      @Nonnull
      @Override
      public ComponentType<ChunkStore, NonTicking<ChunkStore>> componentType() {
         return ChunkStore.REGISTRY.getNonTickingComponentType();
      }

      public void onComponentAdded(
         @Nonnull Ref<ChunkStore> ref,
         @Nonnull NonTicking<ChunkStore> component,
         @Nonnull Store<ChunkStore> store,
         @Nonnull CommandBuffer<ChunkStore> commandBuffer
      ) {
         ChunkColumn column = commandBuffer.getComponent(ref, ChunkColumn.getComponentType());

         assert column != null;

         Ref<ChunkStore>[] sections = column.getSections();

         for (int i = 0; i < sections.length; i++) {
            Ref<ChunkStore> section = sections[i];
            commandBuffer.ensureComponent(section, ChunkStore.REGISTRY.getNonTickingComponentType());
         }
      }

      public void onComponentSet(
         @Nonnull Ref<ChunkStore> ref,
         @Nullable NonTicking<ChunkStore> oldComponent,
         @Nonnull NonTicking<ChunkStore> newComponent,
         @Nonnull Store<ChunkStore> store,
         @Nonnull CommandBuffer<ChunkStore> commandBuffer
      ) {
      }

      public void onComponentRemoved(
         @Nonnull Ref<ChunkStore> ref,
         @Nonnull NonTicking<ChunkStore> component,
         @Nonnull Store<ChunkStore> store,
         @Nonnull CommandBuffer<ChunkStore> commandBuffer
      ) {
         ChunkColumn column = commandBuffer.getComponent(ref, ChunkColumn.getComponentType());

         assert column != null;

         Ref<ChunkStore>[] sections = column.getSections();

         for (int i = 0; i < sections.length; i++) {
            Ref<ChunkStore> section = sections[i];
            commandBuffer.tryRemoveComponent(section, ChunkStore.REGISTRY.getNonTickingComponentType());
         }
      }

      @Override
      public Query<ChunkStore> getQuery() {
         return this.archetype;
      }
   }

   public static class ReplicateChanges extends EntityTickingSystem<ChunkStore> implements RunWhenPausedSystem<ChunkStore> {
      private static final Query<ChunkStore> QUERY = Query.and(ChunkSection.getComponentType(), BlockSection.getComponentType());

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
         BlockSection blockSection = archetypeChunk.getComponent(index, BlockSection.getComponentType());

         assert blockSection != null;

         IntOpenHashSet changes = blockSection.getAndClearChangedPositions();
         if (!changes.isEmpty()) {
            ChunkSection section = archetypeChunk.getComponent(index, ChunkSection.getComponentType());

            assert section != null;

            Collection<PlayerRef> players = store.getExternalData().getWorld().getPlayerRefs();
            if (players.isEmpty()) {
               changes.clear();
            } else {
               long chunkIndex = ChunkUtil.indexChunk(section.getX(), section.getZ());
               if (changes.size() >= 1024) {
                  ObjectArrayList<PlayerRef> playersCopy = new ObjectArrayList(players);
                  CompletableFuture<CachedPacket<SetChunk>> set = blockSection.getCachedChunkPacket(section.getX(), section.getY(), section.getZ());
                  set.thenAccept(s -> {
                     Iterator i$ = playersCopy.iterator();

                     while (i$.hasNext()) {
                        PlayerRef playerx = (PlayerRef)i$.next();
                        Ref<EntityStore> refx = playerx.getReference();
                        if (refx != null) {
                           ChunkTracker trackerx = playerx.getChunkTracker();
                           if (trackerx != null && trackerx.isLoaded(chunkIndex)) {
                              playerx.getPacketHandler().writeNoCache(s);
                           }
                        }
                     }
                  }).exceptionally(throwable -> {
                     if (throwable != null) {
                        ((HytaleLogger.Api)ChunkSystems.LOGGER.at(Level.SEVERE).withCause(throwable)).log("Exception when compressing chunk fluids:");
                     }

                     return null;
                  });
                  changes.clear();
               } else {
                  if (changes.size() == 1) {
                     int change = changes.iterator().nextInt();
                     int x = ChunkUtil.minBlock(section.getX()) + ChunkUtil.xFromIndex(change);
                     int y = ChunkUtil.minBlock(section.getY()) + ChunkUtil.yFromIndex(change);
                     int z = ChunkUtil.minBlock(section.getZ()) + ChunkUtil.zFromIndex(change);
                     int blockId = blockSection.get(change);
                     int filler = blockSection.getFiller(change);
                     int rotation = blockSection.getRotationIndex(change);
                     ServerSetBlock packet = new ServerSetBlock(x, y, z, blockId, (short)filler, (byte)rotation);

                     for (PlayerRef player : players) {
                        Ref<EntityStore> ref = player.getReference();
                        if (ref != null) {
                           ChunkTracker tracker = player.getChunkTracker();
                           if (tracker != null && tracker.isLoaded(chunkIndex)) {
                              player.getPacketHandler().writeNoCache(packet);
                           }
                        }
                     }
                  } else {
                     SetBlockCmd[] cmds = new SetBlockCmd[changes.size()];
                     IntIterator iter = changes.intIterator();
                     int i = 0;

                     while (iter.hasNext()) {
                        int change = iter.nextInt();
                        int blockId = blockSection.get(change);
                        int filler = blockSection.getFiller(change);
                        int rotation = blockSection.getRotationIndex(change);
                        cmds[i++] = new SetBlockCmd((short)change, blockId, (short)filler, (byte)rotation);
                     }

                     ServerSetBlocks packet = new ServerSetBlocks(section.getX(), section.getY(), section.getZ(), cmds);

                     for (PlayerRef playerx : players) {
                        Ref<EntityStore> ref = playerx.getReference();
                        if (ref != null) {
                           ChunkTracker tracker = playerx.getChunkTracker();
                           if (tracker != null && tracker.isLoaded(chunkIndex)) {
                              playerx.getPacketHandler().writeNoCache(packet);
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
         return QUERY;
      }

      @Nonnull
      @Override
      public Set<Dependency<ChunkStore>> getDependencies() {
         return RootDependency.lastSet();
      }
   }
}
