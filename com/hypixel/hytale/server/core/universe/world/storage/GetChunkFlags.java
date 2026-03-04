package com.hypixel.hytale.server.core.universe.world.storage;

public class GetChunkFlags {
   public static final int NONE = 0;
   public static final int NO_LOAD = 1;
   public static final int NO_GENERATE = 2;
   public static final int SET_TICKING = 4;
   public static final int BYPASS_LOADED = 8;
   public static final int POLL_STILL_NEEDED = 16;
   public static final int NO_SET_TICKING_SYNC = Integer.MIN_VALUE;
}
