package com.hypixel.hytale.server.spawning;

public enum SpawnRejection {
   OUTSIDE_LIGHT_RANGE,
   INVALID_SPAWN_BLOCK,
   INVALID_POSITION,
   NO_POSITION,
   NOT_BREATHABLE,
   OTHER;

   public static final SpawnRejection[] VALUES = values();
}
