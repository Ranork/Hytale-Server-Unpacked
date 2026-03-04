package com.hypixel.hytale.server.core.universe.world.storage;

import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.util.BsonUtil;
import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nonnull;
import org.bson.BsonDocument;

public abstract class BufferChunkSaver implements IChunkSaver {
   @Nonnull
   private final Store<ChunkStore> store;

   protected BufferChunkSaver(@Nonnull Store<ChunkStore> store) {
      Objects.requireNonNull(store);
      this.store = store;
   }

   @Nonnull
   public Store<ChunkStore> getStore() {
      return this.store;
   }

   @Nonnull
   public abstract CompletableFuture<Void> saveBuffer(int var1, int var2, @Nonnull ByteBuffer var3);

   @Nonnull
   public abstract CompletableFuture<Void> removeBuffer(int var1, int var2);

   @Nonnull
   @Override
   public CompletableFuture<Void> saveHolder(int x, int z, @Nonnull Holder<ChunkStore> holder) {
      BsonDocument document = ChunkStore.REGISTRY.serialize(holder);
      ByteBuffer buffer = ByteBuffer.wrap(BsonUtil.writeToBytes(document));
      return this.saveBuffer(x, z, buffer);
   }

   @Nonnull
   @Override
   public CompletableFuture<Void> removeHolder(int x, int z) {
      return this.removeBuffer(x, z);
   }
}
