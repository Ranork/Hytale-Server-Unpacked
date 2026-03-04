package com.hypixel.hytale.server.npc.asset.builder;

import java.util.EnumSet;
import java.util.function.Supplier;

public enum ComponentContext implements Supplier<String> {
   SensorSelf("self sensor"),
   SensorTarget("target sensor"),
   SensorEntity("entity sensor");

   private final String description;
   public static final EnumSet<ComponentContext> NotSelfEntitySensor = EnumSet.of(SensorTarget, SensorEntity);

   private ComponentContext(String description) {
      this.description = description;
   }

   public String get() {
      return this.description;
   }
}
