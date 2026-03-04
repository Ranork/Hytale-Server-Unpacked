package com.hypixel.hytale.server.core.universe.world.accessor;

public interface OverridableChunkAccessor<X extends BlockAccessor> extends ChunkAccessor<X> {
   void overwrite(X var1);
}
