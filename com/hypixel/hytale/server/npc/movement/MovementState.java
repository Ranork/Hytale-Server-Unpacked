package com.hypixel.hytale.server.npc.movement;

import java.util.function.Supplier;

public enum MovementState implements Supplier<String> {
   JUMPING("Jumping"),
   FLYING("Flying"),
   CROUCHING("Crouching"),
   RUNNING("Running"),
   SPRINTING("Sprinting"),
   FALLING("Falling"),
   CLIMBING("Climbing"),
   WALKING("Walking"),
   IDLE("Idle"),
   ANY("Any");

   private final String name;

   private MovementState(String name) {
      this.name = name;
   }

   public String get() {
      return this.name;
   }
}
