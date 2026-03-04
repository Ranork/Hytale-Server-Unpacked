package com.hypixel.hytale.server.core.universe.world.storage.provider;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.IChunkLoader;
import com.hypixel.hytale.server.core.universe.world.storage.IChunkSaver;
import java.io.IOException;
import javax.annotation.Nonnull;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class DefaultChunkStorageProvider implements IChunkStorageProvider<Object> {
   public static final int VERSION = 0;
   public static final String ID = "Hytale";
   @Nonnull
   private static final IChunkStorageProvider<?> DEFAULT_INDEXED = new IndexedStorageChunkStorageProvider();
   @Nonnull
   public static final BuilderCodec<DefaultChunkStorageProvider> CODEC = BuilderCodec.builder(
         DefaultChunkStorageProvider.class, DefaultChunkStorageProvider::new
      )
      .versioned()
      .codecVersion(0)
      .documentation("Selects the default recommended storage as decided by the server.")
      .build();
   private IChunkStorageProvider<?> provider = DEFAULT_INDEXED;

   @Override
   public Object initialize(@Nonnull Store<ChunkStore> store) throws IOException {
      return this.provider.initialize(store);
   }

   @Override
   public void close(@Nonnull Object o, @NonNullDecl Store<ChunkStore> store) throws IOException {
      ((IChunkStorageProvider<Object>)this.provider).close(o, store);
   }

   @Nonnull
   @Override
   public IChunkLoader getLoader(@Nonnull Object o, @Nonnull Store<ChunkStore> store) throws IOException {
      return ((IChunkStorageProvider<Object>)this.provider).getLoader(o, store);
   }

   @Nonnull
   @Override
   public IChunkSaver getSaver(@Nonnull Object o, @Nonnull Store<ChunkStore> store) throws IOException {
      return ((IChunkStorageProvider<Object>)this.provider).getSaver(o, store);
   }

   @Override
   public boolean isSame(IChunkStorageProvider<?> other) {
      return other.getClass().equals(this.getClass()) || this.provider.isSame(other);
   }

   @Nonnull
   @Override
   public String toString() {
      return "DefaultChunkStorageProvider{DEFAULT=" + this.provider + "}";
   }
}
