package com.hypixel.hytale.server.core.universe.world.storage.provider;

import com.hypixel.hytale.codec.lookup.BuilderCodecMapCodec;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.IChunkLoader;
import com.hypixel.hytale.server.core.universe.world.storage.IChunkSaver;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongList;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface IChunkStorageProvider<Data> {
   @Nonnull
   BuilderCodecMapCodec<IChunkStorageProvider<?>> CODEC = new BuilderCodecMapCodec<>("Type", true);
   int MIGRATION_COMPACT_INTERVAL = 5000;
   int MIGRATION_MAX_PENDING_COMPACTS = 3;

   Data initialize(@Nonnull Store<ChunkStore> var1) throws IOException;

   default <OtherData> Data migrateFrom(@Nonnull Store<ChunkStore> store, IChunkStorageProvider<OtherData> other) throws IOException {
      OtherData oldData = other.initialize(store);
      Data newData = this.initialize(store);

      try (
         IChunkLoader oldLoader = other.getLoader(oldData, store);
         IChunkSaver oldSaver = other.getSaver(oldData, store);
         IChunkSaver newSaver = this.getSaver(newData, store);
      ) {
         World world = store.getExternalData().getWorld();
         HytaleLogger logger = world.getLogger();
         LongList chunks = oldLoader.getIndexes();
         LongIterator iterator = chunks.iterator();
         ((HytaleLogger.Api)logger.atInfo()).log("Migrating %d chunks", chunks.size());
         HytaleServer.get().reportSingleplayerStatus(Message.translation("client.gameLoadingView.status.migratingChunks").param("name", world.getName()), 0.0);
         int count = 0;
         ArrayList<CompletableFuture<Void>> inFlight = new ArrayList<>();
         ArrayList<CompletableFuture<Void>> pendingCompacts = new ArrayList<>();
         long[] compactHint = new long[5000];
         int hintLen = 0;

         while (iterator.hasNext()) {
            long chunk = iterator.nextLong();
            int chunkX = ChunkUtil.xOfChunkIndex(chunk);
            int chunkZ = ChunkUtil.zOfChunkIndex(chunk);
            inFlight.add(
               oldLoader.loadHolder(chunkX, chunkZ)
                  .thenCompose(v -> newSaver.saveHolder(chunkX, chunkZ, (Holder<ChunkStore>)v))
                  .thenCompose(v -> oldSaver.removeHolder(chunkX, chunkZ))
                  .exceptionally(t -> {
                     ((HytaleLogger.Api)((HytaleLogger.Api)logger.atSevere()).withCause(t)).log("Failed to load chunk at %d, %d, skipping", chunkX, chunkZ);
                     return null;
                  })
            );
            compactHint[hintLen++] = chunk;
            if (++count % 100 == 0) {
               ((HytaleLogger.Api)logger.atInfo()).log("Migrated %d/%d chunks", count, chunks.size());
               double progress = MathUtil.round((double)count / chunks.size(), 2) * 100.0;
               HytaleServer.get()
                  .reportSingleplayerStatus(Message.translation("client.gameLoadingView.status.migratingChunks").param("name", world.getName()), progress);
            }

            if (hintLen == compactHint.length) {
               CompletableFuture[] batchFutures = inFlight.toArray(CompletableFuture[]::new);
               long[] batchHint = (long[])compactHint.clone();
               inFlight.clear();
               hintLen = 0;
               pendingCompacts.add(CompletableFuture.allOf(batchFutures).thenRun(() -> {
                  try {
                     oldSaver.compact(batchHint);
                  } catch (IOException var4x) {
                     ((HytaleLogger.Api)((HytaleLogger.Api)logger.atWarning()).withCause(var4x)).log("Compaction during migration failed; continuing");
                  }
               }));
               pendingCompacts.removeIf(CompletableFuture::isDone);

               while (pendingCompacts.size() >= 3) {
                  CompletableFuture.anyOf(pendingCompacts.toArray(CompletableFuture[]::new)).join();
                  pendingCompacts.removeIf(CompletableFuture::isDone);
               }
            } else {
               inFlight.removeIf(CompletableFuture::isDone);
               if (inFlight.size() >= ForkJoinPool.getCommonPoolParallelism()) {
                  CompletableFuture.anyOf(inFlight.toArray(CompletableFuture[]::new)).join();
                  inFlight.removeIf(CompletableFuture::isDone);
               }
            }
         }

         CompletableFuture.allOf(inFlight.toArray(CompletableFuture[]::new)).join();
         inFlight.clear();
         if (hintLen > 0) {
            long[] finalHint = Arrays.copyOf(compactHint, hintLen);
            pendingCompacts.add(CompletableFuture.runAsync(() -> {
               try {
                  oldSaver.compact(finalHint);
               } catch (IOException var4x) {
                  ((HytaleLogger.Api)((HytaleLogger.Api)logger.atWarning()).withCause(var4x)).log("Final compaction during migration failed; continuing");
               }
            }));
         }

         CompletableFuture.allOf(pendingCompacts.toArray(CompletableFuture[]::new)).join();
         ((HytaleLogger.Api)logger.atInfo()).log("Finished migrating %d chunks", chunks.size());
      } finally {
         other.delete(oldData, store);
      }

      return newData;
   }

   void delete(@Nonnull Data var1, @Nonnull Store<ChunkStore> var2) throws IOException;

   void close(@Nonnull Data var1, @Nonnull Store<ChunkStore> var2) throws IOException;

   @Nonnull
   IChunkLoader getLoader(@Nonnull Data var1, @Nonnull Store<ChunkStore> var2) throws IOException;

   @Nonnull
   IChunkSaver getSaver(@Nonnull Data var1, @Nonnull Store<ChunkStore> var2) throws IOException;

   @Nullable
   default IChunkLoader getRecoveryLoader(@Nonnull Store<ChunkStore> store, Path backupPath) {
      return null;
   }

   default void beginRecovery(Path file, Path recoveryPath) throws IOException {
      throw new UnsupportedOperationException();
   }

   default void revertRecovery(Path file, Path recoveryPath) throws IOException {
      throw new UnsupportedOperationException();
   }

   default boolean isSame(IChunkStorageProvider<?> other) {
      return other.getClass().equals(this.getClass());
   }
}
