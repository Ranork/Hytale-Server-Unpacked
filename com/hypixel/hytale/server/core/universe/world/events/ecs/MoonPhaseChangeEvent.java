package com.hypixel.hytale.server.core.universe.world.events.ecs;

import com.hypixel.hytale.component.system.EcsEvent;

public class MoonPhaseChangeEvent extends EcsEvent {
   private final int newMoonPhase;

   public MoonPhaseChangeEvent(int newMoonPhase) {
      this.newMoonPhase = newMoonPhase;
   }

   public int getNewMoonPhase() {
      return this.newMoonPhase;
   }
}
