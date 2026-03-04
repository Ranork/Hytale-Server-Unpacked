package com.hypixel.hytale.server.core.universe.world;

public class SetBlockSettings {
   public static final int NONE = 0;
   public static final int NO_NOTIFY = 1;
   public static final int NO_UPDATE_STATE = 2;
   public static final int NO_SEND_PARTICLES = 4;
   public static final int NO_SET_FILLER = 8;
   public static final int NO_BREAK_FILLER = 16;
   public static final int PHYSICS = 32;
   public static final int FORCE_CHANGED = 64;
   public static final int NO_UPDATE_NEIGHBOR_CONNECTIONS = 128;
   public static final int PERFORM_BLOCK_UPDATE = 256;
   public static final int NO_UPDATE_HEIGHTMAP = 512;
   public static final int NO_SEND_AUDIO = 1024;
   public static final int NO_DROP_ITEMS = 2048;
}
