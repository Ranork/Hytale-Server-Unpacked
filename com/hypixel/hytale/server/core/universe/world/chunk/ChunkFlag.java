package com.hypixel.hytale.server.core.universe.world.chunk;

import com.hypixel.hytale.common.collection.Flag;

public enum ChunkFlag implements Flag {
   START_INIT,
   INIT,
   NEWLY_GENERATED,
   ON_DISK,
   TICKING;

   public static final ChunkFlag[] VALUES = values();
   private final int mask = 1 << this.ordinal();

   @Override
   public int mask() {
      return this.mask;
   }
}
