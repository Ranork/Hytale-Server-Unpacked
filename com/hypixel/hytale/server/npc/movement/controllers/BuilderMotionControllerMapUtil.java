package com.hypixel.hytale.server.npc.movement.controllers;

import java.util.HashMap;
import java.util.Map;

public class BuilderMotionControllerMapUtil {
   private static final Map<String, MotionController> MAP_OBJECT_REFERENCE = new HashMap<>();
   public static final Class<Map<String, MotionController>> CLASS_REFERENCE = (Class<Map<String, MotionController>>)MAP_OBJECT_REFERENCE.getClass();
}
