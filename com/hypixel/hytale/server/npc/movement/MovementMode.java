package com.hypixel.hytale.server.npc.movement;

import java.util.function.Supplier;

public enum MovementMode implements Supplier<String> {
   WALK("Walk"),
   WADE("Walk"),
   UNDERWATER_WALK("Walk"),
   DIVE("Dive"),
   FLY("Fly");

   private final String name;

   private MovementMode(String name) {
      this.name = name;
   }

   public String get() {
      return this.name;
   }
}
