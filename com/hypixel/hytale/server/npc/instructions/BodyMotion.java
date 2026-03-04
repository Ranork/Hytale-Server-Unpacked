package com.hypixel.hytale.server.npc.instructions;

import javax.annotation.Nullable;

public interface BodyMotion extends Motion {
   @Nullable
   default BodyMotion getSteeringMotion() {
      return this;
   }
}
