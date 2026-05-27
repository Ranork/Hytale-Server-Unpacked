package com.hypixel.hytale.server.core.universe.world.storage;

import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.server.core.universe.world.storage.component.ChunkSavingSystems;
import it.unimi.dsi.fastutil.longs.LongList;
import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface IChunkSaver extends Closeable {
   @Nonnull
   CompletableFuture<Void> saveHolder(int var1, int var2, @Nonnull Holder<ChunkStore> var3);

   @Nonnull
   CompletableFuture<Void> removeHolder(int var1, int var2);

   @Nonnull
   LongList getIndexes() throws IOException;

   void flush() throws IOException;

   default void compact(@Nullable long[] removedHint) throws IOException {
   }

   default void pauseBackgroundSaving(ChunkSavingSystems.Data data) {
   }

   default void resumeBackgroundSaving() {
   }
}
