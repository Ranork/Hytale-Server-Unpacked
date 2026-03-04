package com.hypixel.hytale.server.core.universe.world.events;

import com.hypixel.hytale.event.IEvent;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import javax.annotation.Nonnull;

public abstract class ChunkEvent implements IEvent<String> {
   @Nonnull
   private final WorldChunk chunk;

   public ChunkEvent(@Nonnull WorldChunk chunk) {
      this.chunk = chunk;
   }

   public WorldChunk getChunk() {
      return this.chunk;
   }

   @Nonnull
   @Override
   public String toString() {
      return "ChunkEvent{chunk=" + this.chunk + "}";
   }
}
