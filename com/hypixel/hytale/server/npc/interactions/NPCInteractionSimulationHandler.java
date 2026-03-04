package com.hypixel.hytale.server.npc.interactions;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.modules.interaction.IInteractionSimulationHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class NPCInteractionSimulationHandler implements IInteractionSimulationHandler {
   private float requestedChargeTime;

   @Override
   public void setState(InteractionType type, boolean state) {
   }

   @Override
   public boolean isCharging(
      boolean firstRun, float time, InteractionType type, InteractionContext context, Ref<EntityStore> ref, CooldownHandler cooldownHandler
   ) {
      return time < this.requestedChargeTime;
   }

   @Override
   public boolean shouldCancelCharging(
      boolean firstRun, float time, InteractionType type, InteractionContext context, Ref<EntityStore> ref, CooldownHandler cooldownHandler
   ) {
      return false;
   }

   @Override
   public float getChargeValue(
      boolean firstRun, float time, InteractionType type, InteractionContext context, Ref<EntityStore> ref, CooldownHandler cooldownHandler
   ) {
      return this.requestedChargeTime;
   }

   public void requestChargeTime(float chargeTime) {
      this.requestedChargeTime = chargeTime;
   }
}
