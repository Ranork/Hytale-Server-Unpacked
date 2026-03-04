package com.hypixel.hytale.server.core.universe.world.events.ecs;

import com.hypixel.hytale.component.system.CancellableEcsEvent;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import javax.annotation.Nonnull;

public class ChunkSaveEvent extends CancellableEcsEvent {
   @Nonnull
   private final WorldChunk chunk;

   public ChunkSaveEvent(@Nonnull WorldChunk chunk) {
      this.chunk = chunk;
   }

   @Nonnull
   public WorldChunk getChunk() {
      return this.chunk;
   }
}
