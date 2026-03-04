package com.hypixel.hytale.server.core.universe.world.storage;

import com.hypixel.fastutil.longs.Long2ObjectConcurrentHashMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.store.CodecKey;
import com.hypixel.hytale.codec.store.CodecStore;
import com.hypixel.hytale.common.util.FormatUtil;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.ComponentRegistry;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.IResourceStorage;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Resource;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.SystemGroup;
import com.hypixel.hytale.component.SystemType;
import com.hypixel.hytale.component.system.StoreSystem;
import com.hypixel.hytale.component.system.data.EntityDataSystem;
import com.hypixel.hytale.event.IEventDispatcher;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.metrics.MetricProvider;
import com.hypixel.hytale.metrics.MetricsRegistry;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.WorldProvider;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.ChunkColumn;
import com.hypixel.hytale.server.core.universe.world.chunk.ChunkFlag;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.events.ChunkPreLoadProcessEvent;
import com.hypixel.hytale.server.core.universe.world.storage.component.ChunkSavingSystems;
import com.hypixel.hytale.server.core.universe.world.storage.component.ChunkUnloadingSystem;
import com.hypixel.hytale.server.core.universe.world.storage.provider.IChunkStorageProvider;
import com.hypixel.hytale.server.core.universe.world.worldgen.GeneratedChunk;
import com.hypixel.hytale.server.core.universe.world.worldgen.IWorldGen;
import com.hypixel.hytale.sneakythrow.SneakyThrow;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSets;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.StampedLock;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ChunkStore implements WorldProvider {
   @Nonnull
   public static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   public static final MetricsRegistry<ChunkStore> METRICS_REGISTRY = new MetricsRegistry<ChunkStore>()
      .register("Store", ChunkStore::getStore, Store.METRICS_REGISTRY)
      .register("ChunkLoader", MetricProvider.maybe(ChunkStore::getLoader))
      .register("ChunkSaver", MetricProvider.maybe(ChunkStore::getSaver))
      .register("WorldGen", MetricProvider.maybe(ChunkStore::getGenerator))
      .register("TotalGeneratedChunkCount", chunkComponentStore -> (long)chunkComponentStore.totalGeneratedChunksCount.get(), Codec.LONG)
      .register("TotalLoadedChunkCount", chunkComponentStore -> (long)chunkComponentStore.totalLoadedChunksCount.get(), Codec.LONG);
   public static final long MAX_FAILURE_BACKOFF_NANOS = TimeUnit.SECONDS.toNanos(10L);
   public static final long FAILURE_BACKOFF_NANOS = TimeUnit.MILLISECONDS.toNanos(1L);
   public static final ComponentRegistry<ChunkStore> REGISTRY = new ComponentRegistry<>();
   public static final CodecKey<Holder<ChunkStore>> HOLDER_CODEC_KEY = new CodecKey<>("ChunkHolder");
   @Nonnull
   public static final SystemType<ChunkStore, ChunkStore.LoadPacketDataQuerySystem> LOAD_PACKETS_DATA_QUERY_SYSTEM_TYPE = REGISTRY.registerSystemType(
      ChunkStore.LoadPacketDataQuerySystem.class
   );
   @Nonnull
   public static final SystemType<ChunkStore, ChunkStore.LoadFuturePacketDataQuerySystem> LOAD_FUTURE_PACKETS_DATA_QUERY_SYSTEM_TYPE = REGISTRY.registerSystemType(
      ChunkStore.LoadFuturePacketDataQuerySystem.class
   );
   @Nonnull
   public static final SystemType<ChunkStore, ChunkStore.UnloadPacketDataQuerySystem> UNLOAD_PACKETS_DATA_QUERY_SYSTEM_TYPE = REGISTRY.registerSystemType(
      ChunkStore.UnloadPacketDataQuerySystem.class
   );
   @Nonnull
   public static final ResourceType<ChunkStore, ChunkUnloadingSystem.Data> UNLOAD_RESOURCE = REGISTRY.registerResource(
      ChunkUnloadingSystem.Data.class, ChunkUnloadingSystem.Data::new
   );
   @Nonnull
   public static final ResourceType<ChunkStore, ChunkSavingSystems.Data> SAVE_RESOURCE = REGISTRY.registerResource(
      ChunkSavingSystems.Data.class, ChunkSavingSystems.Data::new
   );
   public static final SystemGroup<ChunkStore> INIT_GROUP = REGISTRY.registerSystemGroup();
   @Nonnull
   private final World world;
   @Nonnull
   private final Long2ObjectConcurrentHashMap<ChunkStore.ChunkLoadState> chunks = new Long2ObjectConcurrentHashMap<>(true, ChunkUtil.NOT_FOUND);
   private Store<ChunkStore> store;
   private Object storageData;
   @Nullable
   private IChunkLoader loader;
   @Nullable
   private IChunkSaver saver;
   @Nullable
   private IWorldGen generator;
   @Nonnull
   private CompletableFuture<Void> generatorLoaded = new CompletableFuture<>();
   private final StampedLock generatorLock = new StampedLock();
   private final AtomicInteger totalGeneratedChunksCount = new AtomicInteger();
   private final AtomicInteger totalLoadedChunksCount = new AtomicInteger();

   public ChunkStore(@Nonnull World world) {
      this.world = world;
   }

   @Nonnull
   @Override
   public World getWorld() {
      return this.world;
   }

   @Nonnull
   public Store<ChunkStore> getStore() {
      return this.store;
   }

   public Object getStorageData() {
      return this.storageData;
   }

   @Nullable
   public IChunkLoader getLoader() {
      return this.loader;
   }

   @Nullable
   public IChunkSaver getSaver() {
      return this.saver;
   }

   @Nullable
   public IWorldGen getGenerator() {
      long readStamp = this.generatorLock.readLock();

      IWorldGen var3;
      try {
         var3 = this.generator;
      } finally {
         this.generatorLock.unlockRead(readStamp);
      }

      return var3;
   }

   public void shutdownGenerator() {
      this.setGenerator(null);
   }

   public void setGenerator(@Nullable IWorldGen generator) {
      long writeStamp = this.generatorLock.writeLock();

      try {
         if (this.generator != null) {
            this.generator.shutdown();
         }

         this.totalGeneratedChunksCount.set(0);
         this.generator = generator;
         if (generator != null) {
            this.generatorLoaded.complete(null);
            this.generatorLoaded = new CompletableFuture<>();
         }
      } finally {
         this.generatorLock.unlockWrite(writeStamp);
      }
   }

   @Nonnull
   public LongSet getChunkIndexes() {
      return LongSets.unmodifiable(this.chunks.keySet());
   }

   public int getLoadedChunksCount() {
      return this.chunks.size();
   }

   public int getTotalGeneratedChunksCount() {
      return this.totalGeneratedChunksCount.get();
   }

   public int getTotalLoadedChunksCount() {
      return this.totalLoadedChunksCount.get();
   }

   public void start(@Nonnull IResourceStorage resourceStorage) {
      this.store = REGISTRY.addStore(this, resourceStorage, store -> this.store = store);
   }

   public void waitForLoadingChunks() {
      long start = System.nanoTime();

      boolean hasLoadingChunks;
      do {
         this.world.consumeTaskQueue();
         Thread.yield();
         hasLoadingChunks = false;
         ObjectIterator var4 = this.chunks.long2ObjectEntrySet().iterator();

         while (var4.hasNext()) {
            Entry<ChunkStore.ChunkLoadState> entry = (Entry<ChunkStore.ChunkLoadState>)var4.next();
            ChunkStore.ChunkLoadState chunkState = (ChunkStore.ChunkLoadState)entry.getValue();
            long stamp = chunkState.lock.readLock();

            try {
               CompletableFuture<Ref<ChunkStore>> future = chunkState.future;
               if (future != null && !future.isDone()) {
                  hasLoadingChunks = true;
                  break;
               }
            } finally {
               chunkState.lock.unlockRead(stamp);
            }
         }
      } while (hasLoadingChunks && System.nanoTime() - start <= 5000000000L);

      this.world.consumeTaskQueue();
   }

   public void shutdown() {
      this.store.shutdown();
      this.chunks.clear();
   }

   @Nonnull
   private Ref<ChunkStore> add(@Nonnull Holder<ChunkStore> holder) {
      this.world.debugAssertInTickingThread();
      WorldChunk worldChunkComponent = holder.getComponent(WorldChunk.getComponentType());

      assert worldChunkComponent != null;

      ChunkStore.ChunkLoadState chunkState = this.chunks.get(worldChunkComponent.getIndex());
      if (chunkState == null) {
         throw new IllegalStateException("Expected the ChunkLoadState to exist!");
      } else {
         Ref<ChunkStore> oldReference = null;
         long stamp = chunkState.lock.writeLock();

         try {
            if (chunkState.future == null) {
               throw new IllegalStateException("Expected the ChunkLoadState to have a future!");
            }

            if (chunkState.reference != null) {
               oldReference = chunkState.reference;
               chunkState.reference = null;
            }
         } finally {
            chunkState.lock.unlockWrite(stamp);
         }

         if (oldReference != null) {
            WorldChunk oldWorldChunkComponent = this.store.getComponent(oldReference, WorldChunk.getComponentType());

            assert oldWorldChunkComponent != null;

            oldWorldChunkComponent.setFlag(ChunkFlag.TICKING, false);
            this.store.removeEntity(oldReference, RemoveReason.REMOVE);
            this.world.getNotificationHandler().updateChunk(worldChunkComponent.getIndex());
         }

         oldReference = this.store.addEntity(holder, AddReason.SPAWN);
         if (oldReference == null) {
            throw new UnsupportedOperationException("Unable to add the chunk to the world!");
         } else {
            worldChunkComponent.setReference(oldReference);
            stamp = chunkState.lock.writeLock();

            Ref var17;
            try {
               chunkState.reference = oldReference;
               chunkState.flags = 0;
               chunkState.future = null;
               chunkState.throwable = null;
               chunkState.failedWhen = 0L;
               chunkState.failedCounter = 0;
               var17 = oldReference;
            } finally {
               chunkState.lock.unlockWrite(stamp);
            }

            return var17;
         }
      }
   }

   public void remove(@Nonnull Ref<ChunkStore> reference, @Nonnull RemoveReason reason) {
      this.world.debugAssertInTickingThread();
      WorldChunk worldChunkComponent = this.store.getComponent(reference, WorldChunk.getComponentType());

      assert worldChunkComponent != null;

      long index = worldChunkComponent.getIndex();
      ChunkStore.ChunkLoadState chunkState = this.chunks.get(index);
      long stamp = chunkState.lock.readLock();

      try {
         worldChunkComponent.setFlag(ChunkFlag.TICKING, false);
         this.store.removeEntity(reference, reason);
         if (chunkState.future != null) {
            chunkState.reference = null;
         } else {
            this.chunks.remove(index, chunkState);
         }
      } finally {
         chunkState.lock.unlockRead(stamp);
      }
   }

   @Nullable
   public Ref<ChunkStore> getChunkReference(long index) {
      ChunkStore.ChunkLoadState chunkState = this.chunks.get(index);
      if (chunkState == null) {
         return null;
      } else {
         long stamp = chunkState.lock.tryOptimisticRead();
         Ref<ChunkStore> reference = chunkState.reference;
         if (chunkState.lock.validate(stamp)) {
            return reference;
         } else {
            stamp = chunkState.lock.readLock();

            Ref var7;
            try {
               var7 = chunkState.reference;
            } finally {
               chunkState.lock.unlockRead(stamp);
            }

            return var7;
         }
      }
   }

   @Nullable
   public Ref<ChunkStore> getChunkSectionReference(int x, int y, int z) {
      Ref<ChunkStore> ref = this.getChunkReference(ChunkUtil.indexChunk(x, z));
      if (ref == null) {
         return null;
      } else {
         ChunkColumn chunkColumnComponent = this.store.getComponent(ref, ChunkColumn.getComponentType());
         return chunkColumnComponent == null ? null : chunkColumnComponent.getSection(y);
      }
   }

   @Nullable
   public Ref<ChunkStore> getChunkSectionReference(@Nonnull ComponentAccessor<ChunkStore> commandBuffer, int x, int y, int z) {
      Ref<ChunkStore> ref = this.getChunkReference(ChunkUtil.indexChunk(x, z));
      if (ref == null) {
         return null;
      } else {
         ChunkColumn chunkColumnComponent = commandBuffer.getComponent(ref, ChunkColumn.getComponentType());
         return chunkColumnComponent == null ? null : chunkColumnComponent.getSection(y);
      }
   }

   @Nonnull
   public CompletableFuture<Ref<ChunkStore>> getChunkSectionReferenceAsync(int x, int y, int z) {
      return y >= 0 && y < 10 ? this.getChunkReferenceAsync(ChunkUtil.indexChunk(x, z)).thenApplyAsync(ref -> {
         if (ref != null && ref.isValid()) {
            Store<ChunkStore> store = ref.getStore();
            ChunkColumn chunkColumnComponent = store.getComponent((Ref<ChunkStore>)ref, ChunkColumn.getComponentType());
            return chunkColumnComponent == null ? null : chunkColumnComponent.getSection(y);
         } else {
            return null;
         }
      }, this.store.getExternalData().getWorld()) : CompletableFuture.failedFuture(new IndexOutOfBoundsException("Invalid y: " + y));
   }

   @Nullable
   public <T extends Component<ChunkStore>> T getChunkComponent(long index, @Nonnull ComponentType<ChunkStore, T> componentType) {
      Ref<ChunkStore> reference = this.getChunkReference(index);
      return reference != null && reference.isValid() ? this.store.getComponent(reference, componentType) : null;
   }

   @Nonnull
   public CompletableFuture<Ref<ChunkStore>> getChunkReferenceAsync(long index) {
      return this.getChunkReferenceAsync(index, 0);
   }

   @Nonnull
   public CompletableFuture<Ref<ChunkStore>> getChunkReferenceAsync(long index, int flags) {
      if (this.store.isShutdown()) {
         return CompletableFuture.completedFuture(null);
      } else {
         ChunkStore.ChunkLoadState chunkState;
         if ((flags & 3) == 3) {
            chunkState = this.chunks.get(index);
            if (chunkState == null) {
               return CompletableFuture.completedFuture(null);
            }

            long stamp = chunkState.lock.readLock();

            try {
               if ((flags & 4) == 0 || (chunkState.flags & 4) != 0) {
                  if (chunkState.reference != null) {
                     return CompletableFuture.completedFuture(chunkState.reference);
                  }

                  if (chunkState.future != null) {
                     return chunkState.future;
                  }

                  return CompletableFuture.completedFuture(null);
               }
            } finally {
               chunkState.lock.unlockRead(stamp);
            }
         } else {
            chunkState = this.chunks.computeIfAbsent(index, l -> new ChunkStore.ChunkLoadState());
         }

         long stamp = chunkState.lock.writeLock();
         if (chunkState.future == null && chunkState.reference != null && (flags & 8) == 0) {
            Ref<ChunkStore> reference = chunkState.reference;
            if ((flags & 4) == 0) {
               chunkState.lock.unlockWrite(stamp);
               return CompletableFuture.completedFuture(reference);
            } else if (this.world.isInThread() && (flags & -2147483648) == 0) {
               chunkState.lock.unlockWrite(stamp);
               WorldChunk worldChunkComponent = this.store.getComponent(reference, WorldChunk.getComponentType());

               assert worldChunkComponent != null;

               worldChunkComponent.setFlag(ChunkFlag.TICKING, true);
               return CompletableFuture.completedFuture(reference);
            } else {
               chunkState.lock.unlockWrite(stamp);
               return CompletableFuture.supplyAsync(() -> {
                  WorldChunk worldChunkComponent = this.store.getComponent(reference, WorldChunk.getComponentType());

                  assert worldChunkComponent != null;

                  worldChunkComponent.setFlag(ChunkFlag.TICKING, true);
                  return reference;
               }, this.world);
            }
         } else {
            try {
               if (chunkState.throwable != null) {
                  long nanosSince = System.nanoTime() - chunkState.failedWhen;
                  int count = chunkState.failedCounter;
                  if (nanosSince < Math.min(MAX_FAILURE_BACKOFF_NANOS, count * count * FAILURE_BACKOFF_NANOS)) {
                     return CompletableFuture.failedFuture(new RuntimeException("Chunk failure backoff", chunkState.throwable));
                  }

                  chunkState.throwable = null;
                  chunkState.failedWhen = 0L;
               }

               boolean isNew = chunkState.future == null;
               if (isNew) {
                  chunkState.flags = flags;
               }

               int x = ChunkUtil.xOfChunkIndex(index);
               int z = ChunkUtil.zOfChunkIndex(index);
               if ((isNew || (chunkState.flags & 1) != 0) && (flags & 1) == 0) {
                  if (chunkState.future == null) {
                     chunkState.future = this.loader.loadHolder(x, z).thenApplyAsync(holder -> {
                        if (holder != null && !this.store.isShutdown()) {
                           this.totalLoadedChunksCount.getAndIncrement();
                           return this.preLoadChunkAsync(index, (Holder<ChunkStore>)holder, false);
                        } else {
                           return null;
                        }
                     }).thenApplyAsync(this::postLoadChunk, this.world).exceptionally(throwable -> {
                        ((HytaleLogger.Api)LOGGER.at(Level.SEVERE).withCause(throwable)).log("Failed to load chunk! %s, %s", x, z);
                        chunkState.fail(throwable);
                        throw SneakyThrow.sneakyThrow(throwable);
                     });
                  } else {
                     chunkState.flags &= -2;
                     chunkState.future = chunkState.future
                        .thenCompose(
                           reference -> reference != null
                              ? CompletableFuture.completedFuture((Ref<ChunkStore>)reference)
                              : this.loader.loadHolder(x, z).thenApplyAsync(holder -> {
                                 if (holder != null && !this.store.isShutdown()) {
                                    this.totalLoadedChunksCount.getAndIncrement();
                                    return this.preLoadChunkAsync(index, (Holder<ChunkStore>)holder, false);
                                 } else {
                                    return null;
                                 }
                              }).thenApplyAsync(this::postLoadChunk, this.world).exceptionally(throwable -> {
                                 ((HytaleLogger.Api)LOGGER.at(Level.SEVERE).withCause(throwable)).log("Failed to load chunk! %s, %s", x, z);
                                 chunkState.fail(throwable);
                                 throw SneakyThrow.sneakyThrow(throwable);
                              })
                        );
                  }
               }

               if ((isNew || (chunkState.flags & 2) != 0) && (flags & 2) == 0) {
                  int seed = (int)this.world.getWorldConfig().getSeed();
                  if (chunkState.future == null) {
                     long readStamp = this.generatorLock.readLock();

                     CompletableFuture<GeneratedChunk> future;
                     try {
                        if (this.generator == null) {
                           future = this.generatorLoaded
                              .thenCompose(aVoid -> this.generator.generate(seed, index, x, z, (flags & 16) != 0 ? this::isChunkStillNeeded : null));
                        } else {
                           future = this.generator.generate(seed, index, x, z, (flags & 16) != 0 ? this::isChunkStillNeeded : null);
                        }
                     } finally {
                        this.generatorLock.unlockRead(readStamp);
                     }

                     chunkState.future = future.<Holder<ChunkStore>>thenApplyAsync(generatedChunk -> {
                        if (generatedChunk != null && !this.store.isShutdown()) {
                           this.totalGeneratedChunksCount.getAndIncrement();
                           return this.preLoadChunkAsync(index, generatedChunk.toHolder(this.world), true);
                        } else {
                           return null;
                        }
                     }).thenApplyAsync(this::postLoadChunk, this.world).exceptionally(throwable -> {
                        ((HytaleLogger.Api)LOGGER.at(Level.SEVERE).withCause(throwable)).log("Failed to generate chunk! %s, %s", x, z);
                        chunkState.fail(throwable);
                        throw SneakyThrow.sneakyThrow(throwable);
                     });
                  } else {
                     chunkState.flags &= -3;
                     chunkState.future = chunkState.future.thenCompose(reference -> {
                        if (reference != null) {
                           return CompletableFuture.completedFuture((Ref<ChunkStore>)reference);
                        } else {
                           long readStampx = this.generatorLock.readLock();

                           CompletableFuture<GeneratedChunk> future;
                           try {
                              if (this.generator == null) {
                                 futurex = this.generatorLoaded.thenCompose(aVoid -> this.generator.generate(seed, index, x, z, null));
                              } else {
                                 futurex = this.generator.generate(seed, index, x, z, null);
                              }
                           } finally {
                              this.generatorLock.unlockRead(readStampx);
                           }

                           return futurex.<Holder<ChunkStore>>thenApplyAsync(generatedChunk -> {
                              if (generatedChunk != null && !this.store.isShutdown()) {
                                 this.totalGeneratedChunksCount.getAndIncrement();
                                 return this.preLoadChunkAsync(index, generatedChunk.toHolder(this.world), true);
                              } else {
                                 return null;
                              }
                           }).thenApplyAsync(this::postLoadChunk, this.world).exceptionally(throwable -> {
                              ((HytaleLogger.Api)LOGGER.at(Level.SEVERE).withCause(throwable)).log("Failed to generate chunk! %s, %s", x, z);
                              chunkState.fail(throwable);
                              throw SneakyThrow.sneakyThrow(throwable);
                           });
                        }
                     });
                  }
               }

               if ((isNew || (chunkState.flags & 4) == 0) && (flags & 4) != 0) {
                  chunkState.flags |= 4;
                  if (chunkState.future != null) {
                     chunkState.future = chunkState.future.<Ref<ChunkStore>>thenApplyAsync(reference -> {
                        if (reference != null) {
                           WorldChunk worldChunkComponent = this.store.getComponent((Ref<ChunkStore>)reference, WorldChunk.getComponentType());

                           assert worldChunkComponent != null;

                           worldChunkComponent.setFlag(ChunkFlag.TICKING, true);
                        }

                        return reference;
                     }, this.world).exceptionally(throwable -> {
                        ((HytaleLogger.Api)LOGGER.at(Level.SEVERE).withCause(throwable)).log("Failed to set chunk ticking! %s, %s", x, z);
                        chunkState.fail(throwable);
                        throw SneakyThrow.sneakyThrow(throwable);
                     });
                  }
               }

               return chunkState.future != null ? chunkState.future : CompletableFuture.completedFuture(null);
            } finally {
               chunkState.lock.unlockWrite(stamp);
            }
         }
      }
   }

   private boolean isChunkStillNeeded(long index) {
      for (PlayerRef playerRef : this.world.getPlayerRefs()) {
         if (playerRef.getChunkTracker().shouldBeVisible(index)) {
            return true;
         }
      }

      return false;
   }

   public boolean isChunkOnBackoff(long index, long maxFailureBackoffNanos) {
      ChunkStore.ChunkLoadState chunkState = this.chunks.get(index);
      if (chunkState == null) {
         return false;
      } else {
         long stamp = chunkState.lock.readLock();

         boolean nanosSince;
         try {
            if (chunkState.throwable != null) {
               long nanosSincex = System.nanoTime() - chunkState.failedWhen;
               int count = chunkState.failedCounter;
               return nanosSincex < Math.min(maxFailureBackoffNanos, count * count * FAILURE_BACKOFF_NANOS);
            }

            nanosSince = false;
         } finally {
            chunkState.lock.unlockRead(stamp);
         }

         return nanosSince;
      }
   }

   @Nonnull
   private Holder<ChunkStore> preLoadChunkAsync(long index, @Nonnull Holder<ChunkStore> holder, boolean newlyGenerated) {
      WorldChunk worldChunkComponent = holder.getComponent(WorldChunk.getComponentType());
      if (worldChunkComponent == null) {
         throw new IllegalStateException(
            String.format("Holder missing WorldChunk component! (%d, %d)", ChunkUtil.xOfChunkIndex(index), ChunkUtil.zOfChunkIndex(index))
         );
      } else if (worldChunkComponent.getIndex() != index) {
         throw new IllegalStateException(
            String.format(
               "Incorrect chunk index! Got (%d, %d) expected (%d, %d)",
               worldChunkComponent.getX(),
               worldChunkComponent.getZ(),
               ChunkUtil.xOfChunkIndex(index),
               ChunkUtil.zOfChunkIndex(index)
            )
         );
      } else {
         BlockChunk blockChunk = holder.getComponent(BlockChunk.getComponentType());
         if (blockChunk == null) {
            throw new IllegalStateException(
               String.format("Holder missing BlockChunk component! (%d, %d)", ChunkUtil.xOfChunkIndex(index), ChunkUtil.zOfChunkIndex(index))
            );
         } else {
            blockChunk.loadFromHolder(holder);
            worldChunkComponent.setFlag(ChunkFlag.NEWLY_GENERATED, newlyGenerated);
            worldChunkComponent.setLightingUpdatesEnabled(false);
            if (newlyGenerated && this.world.getWorldConfig().shouldSaveNewChunks()) {
               worldChunkComponent.markNeedsSaving();
            }

            try {
               long start = System.nanoTime();
               IEventDispatcher<ChunkPreLoadProcessEvent, ChunkPreLoadProcessEvent> dispatcher = HytaleServer.get()
                  .getEventBus()
                  .dispatchFor(ChunkPreLoadProcessEvent.class, this.world.getName());
               if (dispatcher.hasListener()) {
                  ChunkPreLoadProcessEvent event = dispatcher.dispatch(new ChunkPreLoadProcessEvent(holder, worldChunkComponent, newlyGenerated, start));
                  if (!event.didLog()) {
                     long end = System.nanoTime();
                     long diff = end - start;
                     if (diff > this.world.getTickStepNanos()) {
                        LOGGER.at(Level.SEVERE)
                           .log(
                              "Took too long to pre-load process chunk: %s > TICK_STEP, Has GC Run: %s, %s",
                              FormatUtil.nanosToString(diff),
                              this.world.consumeGCHasRun(),
                              worldChunkComponent
                           );
                     }
                  }
               }
            } finally {
               worldChunkComponent.setLightingUpdatesEnabled(true);
            }

            return holder;
         }
      }
   }

   @Nullable
   private Ref<ChunkStore> postLoadChunk(@Nullable Holder<ChunkStore> holder) {
      this.world.debugAssertInTickingThread();
      if (holder != null && !this.store.isShutdown()) {
         long start = System.nanoTime();
         WorldChunk worldChunkComponent = holder.getComponent(WorldChunk.getComponentType());

         assert worldChunkComponent != null;

         worldChunkComponent.setFlag(ChunkFlag.START_INIT, true);
         if (worldChunkComponent.is(ChunkFlag.TICKING)) {
            holder.tryRemoveComponent(REGISTRY.getNonTickingComponentType());
         } else {
            holder.ensureComponent(REGISTRY.getNonTickingComponentType());
         }

         Ref<ChunkStore> reference = this.add(holder);
         worldChunkComponent.initFlags();
         this.world.getChunkLighting().init(worldChunkComponent);
         long end = System.nanoTime();
         long diff = end - start;
         if (diff > this.world.getTickStepNanos()) {
            LOGGER.at(Level.SEVERE)
               .log(
                  "Took too long to post-load process chunk: %s > TICK_STEP, Has GC Run: %s, %s",
                  FormatUtil.nanosToString(diff),
                  this.world.consumeGCHasRun(),
                  worldChunkComponent
               );
         }

         return reference;
      } else {
         return null;
      }
   }

   static {
      CodecStore.STATIC.putCodecSupplier(HOLDER_CODEC_KEY, REGISTRY::getEntityCodec);
      REGISTRY.registerSystem(new ChunkStore.ChunkLoaderSaverSetupSystem());
      REGISTRY.registerSystem(new ChunkUnloadingSystem());
      REGISTRY.registerSystem(new ChunkSavingSystems.WorldRemoved());
      REGISTRY.registerSystem(new ChunkSavingSystems.Ticking());
   }

   private static class ChunkLoadState {
      private final StampedLock lock = new StampedLock();
      private int flags = 0;
      @Nullable
      private CompletableFuture<Ref<ChunkStore>> future;
      @Nullable
      private Ref<ChunkStore> reference;
      @Nullable
      private Throwable throwable;
      private long failedWhen;
      private int failedCounter;

      private void fail(Throwable throwable) {
         long stamp = this.lock.writeLock();

         try {
            this.flags = 0;
            this.future = null;
            this.throwable = throwable;
            this.failedWhen = System.nanoTime();
            this.failedCounter++;
         } finally {
            this.lock.unlockWrite(stamp);
         }
      }
   }

   public static class ChunkLoaderSaverSetupSystem extends StoreSystem<ChunkStore> {
      private final ResourceType<ChunkStore, ChunkStore.ChunkStorage> chunkStorageResourceType = this.registerResource(
         ChunkStore.ChunkStorage.class, "ChunkStorage", ChunkStore.ChunkStorage.CODEC
      );

      @Nullable
      @Override
      public SystemGroup<ChunkStore> getGroup() {
         return ChunkStore.INIT_GROUP;
      }

      @Override
      public void onSystemAddedToStore(@Nonnull Store<ChunkStore> store) {
         ChunkStore data = store.getExternalData();
         World world = data.getWorld();
         IChunkStorageProvider<?> chunkStorageProvider = world.getWorldConfig().getChunkStorageProvider();
         ChunkStore.ChunkStorage chunkStorage = store.getResource(this.chunkStorageResourceType);

         try {
            if (chunkStorage.currentProvider != null && !chunkStorage.currentProvider.isSame(chunkStorageProvider)) {
               data.storageData = chunkStorageProvider.migrateFrom(store, chunkStorage.currentProvider);
            } else {
               data.storageData = chunkStorageProvider.initialize(store);
            }

            chunkStorage.currentProvider = chunkStorageProvider;
            data.loader = ((IChunkStorageProvider<Object>)chunkStorageProvider).getLoader(data.storageData, store);
            data.saver = ((IChunkStorageProvider<Object>)chunkStorageProvider).getSaver(data.storageData, store);
         } catch (IOException var7) {
            throw SneakyThrow.sneakyThrow(var7);
         }
      }

      @Override
      public void onSystemRemovedFromStore(@Nonnull Store<ChunkStore> store) {
         ChunkStore data = store.getExternalData();

         try {
            if (data.loader != null) {
               IChunkLoader oldLoader = data.loader;
               data.loader = null;
               oldLoader.close();
            }

            if (data.saver != null) {
               IChunkSaver oldSaver = data.saver;
               data.saver = null;
               oldSaver.close();
            }

            World world = data.getWorld();
            IChunkStorageProvider<?> chunkStorageProvider = world.getWorldConfig().getChunkStorageProvider();
            ((IChunkStorageProvider<Object>)chunkStorageProvider).close(data.storageData, store);
         } catch (IOException var5) {
            ((HytaleLogger.Api)ChunkStore.LOGGER.at(Level.SEVERE).withCause(var5)).log("Failed to close storage!");
         }
      }
   }

   private static class ChunkStorage implements Resource<ChunkStore> {
      public static final BuilderCodec<ChunkStore.ChunkStorage> CODEC = BuilderCodec.builder(ChunkStore.ChunkStorage.class, ChunkStore.ChunkStorage::new)
         .append(new KeyedCodec<>("CurrentProvider", IChunkStorageProvider.CODEC), (o, i) -> o.currentProvider = i, o -> o.currentProvider)
         .add()
         .build();
      @Nullable
      private IChunkStorageProvider<?> currentProvider;

      public ChunkStorage(@Nullable IChunkStorageProvider<?> currentProvider) {
         this.currentProvider = currentProvider;
      }

      public ChunkStorage() {
      }

      @Nullable
      @Override
      public Resource<ChunkStore> clone() {
         return new ChunkStore.ChunkStorage(this.currentProvider);
      }
   }

   public abstract static class LoadFuturePacketDataQuerySystem extends EntityDataSystem<ChunkStore, PlayerRef, CompletableFuture<ToClientPacket>> {
   }

   public abstract static class LoadPacketDataQuerySystem extends EntityDataSystem<ChunkStore, PlayerRef, ToClientPacket> {
   }

   public abstract static class UnloadPacketDataQuerySystem extends EntityDataSystem<ChunkStore, PlayerRef, ToClientPacket> {
   }
}
