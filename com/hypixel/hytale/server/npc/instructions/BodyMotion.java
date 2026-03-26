package com.hypixel.hytale.server.npc.instructions;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nullable;

public interface BodyMotion extends Motion {
   @Nullable
   default BodyMotion getSteeringMotion() {
      return this;
   }

   default double getDesiredTargetDistance() {
      return Double.MAX_VALUE;
   }

   @Nullable
   default Ref<EntityStore> getDesiredTargetEntity() {
      return null;
   }
}
