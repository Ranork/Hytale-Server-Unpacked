package com.hypixel.hytale.server.spawning;

public enum SpawnRejection {
   OUTSIDE_LIGHT_RANGE,
   INVALID_SPAWN_BLOCK,
   INVALID_POSITION,
   NO_POSITION,
   NOT_BREATHABLE,
   NO_MOTION_CONTROLLERS,
   NO_MOTION_CONTROLLER_MATCH,
   BREATHING_INCOMPATIBLE,
   OTHER;

   public static final SpawnRejection[] VALUES = values();
}
