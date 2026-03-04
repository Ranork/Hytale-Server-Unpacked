package com.hypixel.hytale.server.core.universe.world.storage;

import com.hypixel.hytale.component.Holder;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nonnull;

public interface IChunkLoader extends Closeable {
   @Nonnull
   CompletableFuture<Holder<ChunkStore>> loadHolder(int var1, int var2);

   @Nonnull
   LongSet getIndexes() throws IOException;
}
