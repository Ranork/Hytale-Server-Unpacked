package com.hypixel.hytale.server.npc.blackboard.view.event.entity;

import java.util.function.Supplier;

public enum EntityEventType implements Supplier<String> {
   DAMAGE("On taking damage"),
   DEATH("On dying"),
   INTERACTION("On use interaction");

   public static final EntityEventType[] VALUES = values();
   private final String description;

   private EntityEventType(String description) {
      this.description = description;
   }

   public String get() {
      return this.description;
   }
}
