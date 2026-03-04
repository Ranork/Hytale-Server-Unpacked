package com.hypixel.hytale.server.core.universe.world.storage.provider;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.IChunkLoader;
import com.hypixel.hytale.server.core.universe.world.storage.IChunkSaver;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSets;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nonnull;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class EmptyChunkStorageProvider implements IChunkStorageProvider<Void> {
   public static final String ID = "Empty";
   @Nonnull
   public static final EmptyChunkStorageProvider INSTANCE = new EmptyChunkStorageProvider();
   @Nonnull
   public static final BuilderCodec<EmptyChunkStorageProvider> CODEC = BuilderCodec.builder(EmptyChunkStorageProvider.class, () -> INSTANCE)
      .documentation("A chunk storage provider that discards any chunks to save and will always fail to find chunks.")
      .build();
   @Nonnull
   private static final EmptyChunkStorageProvider.EmptyChunkLoader EMPTY_CHUNK_LOADER = new EmptyChunkStorageProvider.EmptyChunkLoader();
   @Nonnull
   private static final EmptyChunkStorageProvider.EmptyChunkSaver EMPTY_CHUNK_SAVER = new EmptyChunkStorageProvider.EmptyChunkSaver();

   public Void initialize(@NonNullDecl Store<ChunkStore> store) throws IOException {
      return null;
   }

   public void close(@NonNullDecl Void o, @NonNullDecl Store<ChunkStore> store) throws IOException {
   }

   @Nonnull
   public IChunkLoader getLoader(@Nonnull Void object, @Nonnull Store<ChunkStore> store) {
      return EMPTY_CHUNK_LOADER;
   }

   @Nonnull
   public IChunkSaver getSaver(@Nonnull Void object, @Nonnull Store<ChunkStore> store) {
      return EMPTY_CHUNK_SAVER;
   }

   @Nonnull
   @Override
   public String toString() {
      return "EmptyChunkStorageProvider{}";
   }

   private static class EmptyChunkLoader implements IChunkLoader {
      @Override
      public void close() {
      }

      @Nonnull
      @Override
      public CompletableFuture<Holder<ChunkStore>> loadHolder(int x, int z) {
         return CompletableFuture.completedFuture(null);
      }

      @Nonnull
      @Override
      public LongSet getIndexes() {
         return LongSets.EMPTY_SET;
      }
   }

   private static class EmptyChunkSaver implements IChunkSaver {
      @Override
      public void close() {
      }

      @Nonnull
      @Override
      public CompletableFuture<Void> saveHolder(int x, int z, @Nonnull Holder<ChunkStore> holder) {
         return CompletableFuture.completedFuture(null);
      }

      @Nonnull
      @Override
      public CompletableFuture<Void> removeHolder(int x, int z) {
         return CompletableFuture.completedFuture(null);
      }

      @Nonnull
      @Override
      public LongSet getIndexes() {
         return LongSets.EMPTY_SET;
      }

      @Override
      public void flush() {
      }
   }
}
