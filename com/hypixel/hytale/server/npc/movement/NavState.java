package com.hypixel.hytale.server.npc.movement;

import java.util.function.Supplier;

public enum NavState implements Supplier<String> {
   INIT("Doing nothing"),
   PROGRESSING("Moving or computing a path"),
   BLOCKED("Can't advance any further"),
   DEFER("Delaying/unable to advance"),
   AT_GOAL("Reached target"),
   ABORTED("Search stopped but target not reached");

   private final String description;

   private NavState(String description) {
      this.description = description;
   }

   public String get() {
      return this.description;
   }
}
