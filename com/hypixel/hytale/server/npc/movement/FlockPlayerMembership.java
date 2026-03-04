package com.hypixel.hytale.server.npc.movement;

import java.util.function.Supplier;

public enum FlockPlayerMembership implements Supplier<String> {
   Member("Player is member of a flock"),
   NotMember("Player is not member of a flock"),
   Any("Don't care");

   private final String description;

   private FlockPlayerMembership(String description) {
      this.description = description;
   }

   public String get() {
      return this.description;
   }
}
