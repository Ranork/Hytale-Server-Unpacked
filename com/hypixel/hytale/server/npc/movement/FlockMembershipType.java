package com.hypixel.hytale.server.npc.movement;

import java.util.function.Supplier;

public enum FlockMembershipType implements Supplier<String> {
   Leader("Is leader of a flock"),
   Follower("Is part of a flock but not leader"),
   Member("Is part of a flock"),
   NotMember("Is not part of a flock"),
   Any("Don't care");

   private final String description;

   private FlockMembershipType(String description) {
      this.description = description;
   }

   public String get() {
      return this.description;
   }
}
