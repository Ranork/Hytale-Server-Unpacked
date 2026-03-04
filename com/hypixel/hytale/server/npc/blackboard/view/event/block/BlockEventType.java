package com.hypixel.hytale.server.npc.blackboard.view.event.block;

import java.util.function.Supplier;

public enum BlockEventType implements Supplier<String> {
   DAMAGE("On block damage"),
   DESTRUCTION("On block destruction"),
   INTERACTION("On block use interaction");

   public static final BlockEventType[] VALUES = values();
   private final String description;

   private BlockEventType(String description) {
      this.description = description;
   }

   public String get() {
      return this.description;
   }
}
