package com.hypixel.hytale.server.core.universe.world.storage.provider;

import com.hypixel.fastutil.longs.Long2ObjectConcurrentHashMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.component.Resource;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.metrics.MetricProvider;
import com.hypixel.hytale.metrics.MetricResults;
import com.hypixel.hytale.metrics.MetricsRegistry;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.BufferChunkLoader;
import com.hypixel.hytale.server.core.universe.world.storage.BufferChunkSaver;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.IChunkLoader;
import com.hypixel.hytale.server.core.universe.world.storage.IChunkSaver;
import com.hypixel.hytale.sneakythrow.SneakyThrow;
import com.hypixel.hytale.storage.IndexedStorageFile;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntListIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSets;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry;
import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class IndexedStorageChunkStorageProvider implements IChunkStorageProvider<IndexedStorageChunkStorageProvider.IndexedStorageCache> {
   public static final String ID = "IndexedStorage";
   @Nonnull
   public static final BuilderCodec<IndexedStorageChunkStorageProvider> CODEC = BuilderCodec.builder(
         IndexedStorageChunkStorageProvider.class, IndexedStorageChunkStorageProvider::new
      )
      .documentation("Uses the indexed storage file format to store chunks.")
      .<Boolean>appendInherited(
         new KeyedCodec<>("FlushOnWrite", Codec.BOOLEAN), (o, i) -> o.flushOnWrite = i, o -> o.flushOnWrite, (o, p) -> o.flushOnWrite = p.flushOnWrite
      )
      .documentation(
         "Controls whether the indexed storage flushes during writes.\nRecommended to be enabled to prevent corruption of chunks during unclean shutdowns."
      )
      .add()
      .build();
   private boolean flushOnWrite = false;

   public IndexedStorageChunkStorageProvider.IndexedStorageCache initialize(@NonNullDecl Store<ChunkStore> store) throws IOException {
      World world = store.getExternalData().getWorld();
      IndexedStorageChunkStorageProvider.IndexedStorageCache cache = new IndexedStorageChunkStorageProvider.IndexedStorageCache();
      cache.path = world.getSavePath().resolve("chunks");
      return cache;
   }

   public void close(@NonNullDecl IndexedStorageChunkStorageProvider.IndexedStorageCache cache, @NonNullDecl Store<ChunkStore> store) throws IOException {
      cache.close();
   }

   @Nonnull
   public IChunkLoader getLoader(@Nonnull IndexedStorageChunkStorageProvider.IndexedStorageCache cache, @Nonnull Store<ChunkStore> store) {
      return new IndexedStorageChunkStorageProvider.IndexedStorageChunkLoader(store, cache, this.flushOnWrite);
   }

   @Nonnull
   public IChunkSaver getSaver(@Nonnull IndexedStorageChunkStorageProvider.IndexedStorageCache cache, @Nonnull Store<ChunkStore> store) {
      return new IndexedStorageChunkStorageProvider.IndexedStorageChunkSaver(store, cache, this.flushOnWrite);
   }

   @Nonnull
   @Override
   public String toString() {
      return "IndexedStorageChunkStorageProvider{}";
   }

   @Nonnull
   private static String toFileName(int regionX, int regionZ) {
      return regionX + "." + regionZ + ".region.bin";
   }

   private static long fromFileName(@Nonnull String fileName) {
      String[] split = fileName.split("\\.");
      if (split.length != 4) {
         throw new IllegalArgumentException("Unexpected file name format!");
      } else if (!"region".equals(split[2])) {
         throw new IllegalArgumentException("Unexpected file name format!");
      } else if (!"bin".equals(split[3])) {
         throw new IllegalArgumentException("Unexpected file extension!");
      } else {
         int regionX = Integer.parseInt(split[0]);
         int regionZ = Integer.parseInt(split[1]);
         return ChunkUtil.indexChunk(regionX, regionZ);
      }
   }

   public static class IndexedStorageCache implements Closeable, MetricProvider, Resource<ChunkStore> {
      @Nonnull
      public static final MetricsRegistry<IndexedStorageChunkStorageProvider.IndexedStorageCache> METRICS_REGISTRY = new MetricsRegistry<IndexedStorageChunkStorageProvider.IndexedStorageCache>()
         .register(
            "Files",
            cache -> cache.cache
               .long2ObjectEntrySet()
               .stream()
               .map(IndexedStorageChunkStorageProvider.IndexedStorageCache.CacheEntryMetricData::new)
               .toArray(IndexedStorageChunkStorageProvider.IndexedStorageCache.CacheEntryMetricData[]::new),
            new ArrayCodec<>(
               IndexedStorageChunkStorageProvider.IndexedStorageCache.CacheEntryMetricData.CODEC,
               IndexedStorageChunkStorageProvider.IndexedStorageCache.CacheEntryMetricData[]::new
            )
         );
      private final Long2ObjectConcurrentHashMap<IndexedStorageFile> cache = new Long2ObjectConcurrentHashMap<>(true, ChunkUtil.NOT_FOUND);
      private Path path;

      @Nonnull
      public Long2ObjectConcurrentHashMap<IndexedStorageFile> getCache() {
         return this.cache;
      }

      @Override
      public void close() throws IOException {
         IOException exception = null;
         Iterator<IndexedStorageFile> iterator = this.cache.values().iterator();

         while (iterator.hasNext()) {
            try {
               iterator.next().close();
               iterator.remove();
            } catch (Exception var4) {
               if (exception == null) {
                  exception = new IOException("Failed to close one or more loaders!");
               }

               exception.addSuppressed(var4);
            }
         }

         if (exception != null) {
            throw exception;
         }
      }

      @Nullable
      public IndexedStorageFile getOrTryOpen(int regionX, int regionZ, boolean flushOnWrite) {
         return this.cache.computeIfAbsent(ChunkUtil.indexChunk(regionX, regionZ), k -> {
            Path regionFile = this.path.resolve(IndexedStorageChunkStorageProvider.toFileName(regionX, regionZ));
            if (!Files.exists(regionFile)) {
               return null;
            } else {
               try {
                  IndexedStorageFile open = IndexedStorageFile.open(regionFile, StandardOpenOption.READ, StandardOpenOption.WRITE);
                  open.setFlushOnWrite(flushOnWrite);
                  return open;
               } catch (FileNotFoundException var8) {
                  return null;
               } catch (IOException var9) {
                  throw SneakyThrow.sneakyThrow(var9);
               }
            }
         });
      }

      @Nonnull
      public IndexedStorageFile getOrCreate(int regionX, int regionZ, boolean flushOnWrite) {
         return this.cache.computeIfAbsent(ChunkUtil.indexChunk(regionX, regionZ), k -> {
            try {
               if (!Files.exists(this.path)) {
                  try {
                     Files.createDirectory(this.path);
                  } catch (FileAlreadyExistsException var8) {
                  }
               }

               Path regionFile = this.path.resolve(IndexedStorageChunkStorageProvider.toFileName(regionX, regionZ));
               IndexedStorageFile open = IndexedStorageFile.open(regionFile, StandardOpenOption.CREATE, StandardOpenOption.READ, StandardOpenOption.WRITE);
               open.setFlushOnWrite(flushOnWrite);
               return open;
            } catch (IOException var9) {
               throw SneakyThrow.sneakyThrow(var9);
            }
         });
      }

      @Nonnull
      public LongSet getIndexes() throws IOException {
         if (!Files.exists(this.path)) {
            return LongSets.EMPTY_SET;
         } else {
            LongOpenHashSet chunkIndexes = new LongOpenHashSet();

            try (Stream<Path> stream = Files.list(this.path)) {
               stream.forEach(path -> {
                  if (!Files.isDirectory(path)) {
                     long regionIndex;
                     try {
                        regionIndex = IndexedStorageChunkStorageProvider.fromFileName(path.getFileName().toString());
                     } catch (IllegalArgumentException var15) {
                        return;
                     }

                     int regionX = ChunkUtil.xOfChunkIndex(regionIndex);
                     int regionZ = ChunkUtil.zOfChunkIndex(regionIndex);
                     IndexedStorageFile regionFile = this.getOrTryOpen(regionX, regionZ, true);
                     if (regionFile != null) {
                        IntList blobIndexes = regionFile.keys();
                        IntListIterator iterator = blobIndexes.iterator();

                        while (iterator.hasNext()) {
                           int blobIndex = iterator.nextInt();
                           int localX = ChunkUtil.xFromColumn(blobIndex);
                           int localZ = ChunkUtil.zFromColumn(blobIndex);
                           int chunkX = regionX << 5 | localX;
                           int chunkZ = regionZ << 5 | localZ;
                           chunkIndexes.add(ChunkUtil.indexChunk(chunkX, chunkZ));
                        }
                     }
                  }
               });
            }

            return chunkIndexes;
         }
      }

      public void flush() throws IOException {
         IOException exception = null;

         for (IndexedStorageFile indexedStorageFile : this.cache.values()) {
            try {
               indexedStorageFile.force(false);
            } catch (Exception var5) {
               if (exception == null) {
                  exception = new IOException("Failed to close one or more loaders!");
               }

               exception.addSuppressed(var5);
            }
         }

         if (exception != null) {
            throw exception;
         }
      }

      @Nonnull
      @Override
      public MetricResults toMetricResults() {
         return METRICS_REGISTRY.toMetricResults(this);
      }

      @Nonnull
      @Override
      public Resource<ChunkStore> clone() {
         return new IndexedStorageChunkStorageProvider.IndexedStorageCache();
      }

      private static class CacheEntryMetricData {
         @Nonnull
         private static final Codec<IndexedStorageChunkStorageProvider.IndexedStorageCache.CacheEntryMetricData> CODEC = BuilderCodec.builder(
               IndexedStorageChunkStorageProvider.IndexedStorageCache.CacheEntryMetricData.class,
               IndexedStorageChunkStorageProvider.IndexedStorageCache.CacheEntryMetricData::new
            )
            .append(new KeyedCodec<>("Key", Codec.LONG), (entry, o) -> entry.key = o, entry -> entry.key)
            .add()
            .append(new KeyedCodec<>("File", IndexedStorageFile.METRICS_REGISTRY), (entry, o) -> entry.value = o, entry -> entry.value)
            .add()
            .build();
         private long key;
         private IndexedStorageFile value;

         public CacheEntryMetricData() {
         }

         public CacheEntryMetricData(@Nonnull Entry<IndexedStorageFile> entry) {
            this.key = entry.getLongKey();
            this.value = (IndexedStorageFile)entry.getValue();
         }
      }
   }

   public static class IndexedStorageChunkLoader extends BufferChunkLoader implements MetricProvider {
      @Nonnull
      private final IndexedStorageChunkStorageProvider.IndexedStorageCache cache;
      private final boolean flushOnWrite;

      public IndexedStorageChunkLoader(
         @Nonnull Store<ChunkStore> store, @Nonnull IndexedStorageChunkStorageProvider.IndexedStorageCache cache, boolean flushOnWrite
      ) {
         super(store);
         this.cache = cache;
         this.flushOnWrite = flushOnWrite;
      }

      @Override
      public void close() throws IOException {
      }

      @Nonnull
      @Override
      public CompletableFuture<ByteBuffer> loadBuffer(int x, int z) {
         int regionX = x >> 5;
         int regionZ = z >> 5;
         int localX = x & 31;
         int localZ = z & 31;
         int index = ChunkUtil.indexColumn(localX, localZ);
         return CompletableFuture.supplyAsync(SneakyThrow.sneakySupplier(() -> {
            IndexedStorageFile chunks = this.cache.getOrTryOpen(regionX, regionZ, this.flushOnWrite);
            return chunks == null ? null : chunks.readBlob(index);
         }));
      }

      @Nonnull
      @Override
      public LongSet getIndexes() throws IOException {
         return this.cache.getIndexes();
      }

      @Nullable
      @Override
      public MetricResults toMetricResults() {
         return this.getStore().getExternalData().getSaver() instanceof IndexedStorageChunkStorageProvider.IndexedStorageChunkSaver
            ? null
            : this.cache.toMetricResults();
      }
   }

   public static class IndexedStorageChunkSaver extends BufferChunkSaver implements MetricProvider {
      @Nonnull
      private final IndexedStorageChunkStorageProvider.IndexedStorageCache cache;
      private final boolean flushOnWrite;

      protected IndexedStorageChunkSaver(
         @Nonnull Store<ChunkStore> store, @Nonnull IndexedStorageChunkStorageProvider.IndexedStorageCache cache, boolean flushOnWrite
      ) {
         super(store);
         this.cache = cache;
         this.flushOnWrite = flushOnWrite;
      }

      @Override
      public void close() throws IOException {
      }

      @Nonnull
      @Override
      public CompletableFuture<Void> saveBuffer(int x, int z, @Nonnull ByteBuffer buffer) {
         int regionX = x >> 5;
         int regionZ = z >> 5;
         int localX = x & 31;
         int localZ = z & 31;
         int index = ChunkUtil.indexColumn(localX, localZ);
         return CompletableFuture.runAsync(SneakyThrow.sneakyRunnable(() -> {
            IndexedStorageFile chunks = this.cache.getOrCreate(regionX, regionZ, this.flushOnWrite);
            chunks.writeBlob(index, buffer);
         }));
      }

      @Nonnull
      @Override
      public CompletableFuture<Void> removeBuffer(int x, int z) {
         int regionX = x >> 5;
         int regionZ = z >> 5;
         int localX = x & 31;
         int localZ = z & 31;
         int index = ChunkUtil.indexColumn(localX, localZ);
         return CompletableFuture.runAsync(SneakyThrow.sneakyRunnable(() -> {
            IndexedStorageFile chunks = this.cache.getOrTryOpen(regionX, regionZ, this.flushOnWrite);
            if (chunks != null) {
               chunks.removeBlob(index);
            }
         }));
      }

      @Nonnull
      @Override
      public LongSet getIndexes() throws IOException {
         return this.cache.getIndexes();
      }

      @Override
      public void flush() throws IOException {
         this.cache.flush();
      }

      @Override
      public MetricResults toMetricResults() {
         return this.cache.toMetricResults();
      }
   }
}
