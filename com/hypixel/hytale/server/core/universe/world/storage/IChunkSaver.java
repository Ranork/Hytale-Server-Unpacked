package com.hypixel.hytale.server.core.universe.world.storage;

import com.hypixel.hytale.component.Holder;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nonnull;

public interface IChunkSaver extends Closeable {
   @Nonnull
   CompletableFuture<Void> saveHolder(int var1, int var2, @Nonnull Holder<ChunkStore> var3);

   @Nonnull
   CompletableFuture<Void> removeHolder(int var1, int var2);

   @Nonnull
   LongSet getIndexes() throws IOException;

   void flush() throws IOException;
}
