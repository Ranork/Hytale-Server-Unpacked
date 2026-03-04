package com.hypixel.hytale.server.spawning;

public enum SpawnTestResult {
   TEST_OK,
   FAIL_NO_POSITION,
   FAIL_INVALID_POSITION,
   FAIL_INTERSECT_ENTITY,
   FAIL_NO_MOTION_CONTROLLERS,
   FAIL_NOT_SPAWNABLE,
   FAIL_NOT_BREATHABLE;
}
