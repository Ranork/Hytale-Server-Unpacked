package com.hypixel.hytale.server.core.universe.world.storage.provider;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.IChunkLoader;
import com.hypixel.hytale.server.core.universe.world.storage.IChunkSaver;
import com.hypixel.hytale.sneakythrow.SneakyThrow;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import javax.annotation.Nonnull;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class MigrationChunkStorageProvider implements IChunkStorageProvider<MigrationChunkStorageProvider.MigrationData> {
   public static final String ID = "Migration";
   @Nonnull
   public static final BuilderCodec<MigrationChunkStorageProvider> CODEC = BuilderCodec.builder(
         MigrationChunkStorageProvider.class, MigrationChunkStorageProvider::new
      )
      .documentation(
         "A provider that combines multiple storage providers in a chain to assist with migrating worlds between storage formats.\n\nCan also be used to set storage to load chunks but block saving them if combined with the **Empty** storage provider"
      )
      .<IChunkStorageProvider<?>[]>append(
         new KeyedCodec<>("Loaders", new ArrayCodec<>(IChunkStorageProvider.CODEC, IChunkStorageProvider[]::new)),
         (migration, o) -> migration.from = o,
         migration -> migration.from
      )
      .documentation(
         "A list of storage providers to use as chunk loaders.\n\nEach loader will be tried in order to load a chunk, returning the chunk if found otherwise trying the next loaded until found or none are left."
      )
      .add()
      .<IChunkStorageProvider<?>>append(new KeyedCodec<>("Saver", IChunkStorageProvider.CODEC), (migration, o) -> migration.to = o, migration -> migration.to)
      .documentation("The storage provider to use to save chunks.")
      .add()
      .build();
   private IChunkStorageProvider<?>[] from;
   private IChunkStorageProvider<?> to;

   public MigrationChunkStorageProvider() {
   }

   public MigrationChunkStorageProvider(@Nonnull IChunkStorageProvider[] from, @Nonnull IChunkStorageProvider to) {
      this.from = from;
      this.to = to;
   }

   public MigrationChunkStorageProvider.MigrationData initialize(@NonNullDecl Store<ChunkStore> store) throws IOException {
      MigrationChunkStorageProvider.MigrationData data = new MigrationChunkStorageProvider.MigrationData();
      data.loaderData = new Object[this.from.length];

      for (int i = 0; i < this.from.length; i++) {
         data.loaderData[i] = this.from[i].initialize(store);
      }

      data.saverData = this.to.initialize(store);
      return data;
   }

   public void close(@NonNullDecl MigrationChunkStorageProvider.MigrationData migrationData, @NonNullDecl Store<ChunkStore> store) throws IOException {
      for (int i = 0; i < this.from.length; i++) {
         ((IChunkStorageProvider<Object>)this.from[i]).close(migrationData.loaderData[i], store);
      }

      ((IChunkStorageProvider<Object>)this.to).close(migrationData.saverData, store);
   }

   @Nonnull
   public IChunkLoader getLoader(@Nonnull MigrationChunkStorageProvider.MigrationData migrationData, @Nonnull Store<ChunkStore> store) throws IOException {
      IChunkLoader[] loaders = new IChunkLoader[this.from.length];

      for (int i = 0; i < this.from.length; i++) {
         loaders[i] = ((IChunkStorageProvider<Object>)this.from[i]).getLoader(migrationData.loaderData[i], store);
      }

      return new MigrationChunkStorageProvider.MigrationChunkLoader(loaders);
   }

   @Nonnull
   public IChunkSaver getSaver(@Nonnull MigrationChunkStorageProvider.MigrationData migrationData, @Nonnull Store<ChunkStore> store) throws IOException {
      return ((IChunkStorageProvider<Object>)this.to).getSaver(migrationData.saverData, store);
   }

   @Nonnull
   @Override
   public String toString() {
      return "MigrationChunkStorageProvider{from=" + Arrays.toString((Object[])this.from) + ", to=" + this.to + "}";
   }

   public static class MigrationChunkLoader implements IChunkLoader {
      @Nonnull
      private final IChunkLoader[] loaders;

      public MigrationChunkLoader(@Nonnull IChunkLoader... loaders) {
         this.loaders = loaders;
      }

      @Override
      public void close() throws IOException {
         IOException exception = null;

         for (IChunkLoader loader : this.loaders) {
            try {
               loader.close();
            } catch (Exception var7) {
               if (exception == null) {
                  exception = new IOException("Failed to close one or more loaders!");
               }

               exception.addSuppressed(var7);
            }
         }

         if (exception != null) {
            throw exception;
         }
      }

      @Nonnull
      @Override
      public CompletableFuture<Holder<ChunkStore>> loadHolder(int x, int z) {
         CompletableFuture<Holder<ChunkStore>> future = this.loaders[0].loadHolder(x, z);

         for (int i = 1; i < this.loaders.length; i++) {
            IChunkLoader loader = this.loaders[i];
            CompletableFuture<Holder<ChunkStore>> previous = future;
            future = previous.<CompletableFuture<Holder<ChunkStore>>>handle((worldChunk, throwable) -> {
               if (throwable != null) {
                  return loader.loadHolder(x, z).exceptionally(throwable1 -> {
                     throwable1.addSuppressed(throwable);
                     throw SneakyThrow.sneakyThrow(throwable1);
                  });
               } else {
                  return worldChunk == null ? loader.loadHolder(x, z) : previous;
               }
            }).thenCompose(Function.identity());
         }

         return future;
      }

      @Nonnull
      @Override
      public LongSet getIndexes() throws IOException {
         LongOpenHashSet indexes = new LongOpenHashSet();

         for (IChunkLoader loader : this.loaders) {
            indexes.addAll(loader.getIndexes());
         }

         return indexes;
      }
   }

   public static class MigrationData {
      private Object[] loaderData;
      private Object saverData;
   }
}
