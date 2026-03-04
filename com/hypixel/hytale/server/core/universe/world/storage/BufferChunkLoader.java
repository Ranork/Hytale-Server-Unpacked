package com.hypixel.hytale.server.core.universe.world.storage;

import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.util.BsonUtil;
import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nonnull;
import org.bson.BsonDocument;

public abstract class BufferChunkLoader implements IChunkLoader {
   @Nonnull
   private final Store<ChunkStore> store;

   public BufferChunkLoader(@Nonnull Store<ChunkStore> store) {
      Objects.requireNonNull(store);
      this.store = store;
   }

   @Nonnull
   public Store<ChunkStore> getStore() {
      return this.store;
   }

   public abstract CompletableFuture<ByteBuffer> loadBuffer(int var1, int var2);

   @Nonnull
   @Override
   public CompletableFuture<Holder<ChunkStore>> loadHolder(int x, int z) {
      return this.loadBuffer(x, z).thenApplyAsync(buffer -> {
         if (buffer == null) {
            return null;
         } else {
            BsonDocument bsonDocument = BsonUtil.readFromBuffer(buffer);
            Holder<ChunkStore> holder = ChunkStore.REGISTRY.deserialize(bsonDocument);
            WorldChunk worldChunkComponent = holder.getComponent(WorldChunk.getComponentType());

            assert worldChunkComponent != null;

            worldChunkComponent.loadFromHolder(this.store.getExternalData().getWorld(), x, z, holder);
            return holder;
         }
      });
   }
}
