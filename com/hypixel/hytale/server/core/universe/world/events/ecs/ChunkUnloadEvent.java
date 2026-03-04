package com.hypixel.hytale.server.core.universe.world.events.ecs;

import com.hypixel.hytale.component.system.CancellableEcsEvent;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import javax.annotation.Nonnull;

public class ChunkUnloadEvent extends CancellableEcsEvent {
   @Nonnull
   private final WorldChunk chunk;
   private boolean resetKeepAlive = true;

   public ChunkUnloadEvent(@Nonnull WorldChunk chunk) {
      this.chunk = chunk;
   }

   @Nonnull
   public WorldChunk getChunk() {
      return this.chunk;
   }

   public void setResetKeepAlive(boolean willResetKeepAlive) {
      this.resetKeepAlive = willResetKeepAlive;
   }

   public boolean willResetKeepAlive() {
      return this.resetKeepAlive;
   }
}
