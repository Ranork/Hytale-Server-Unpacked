package com.hypixel.hytale.server.core.modules.interaction;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class InteractionSimulationHandler implements IInteractionSimulationHandler {
   private final boolean[] isDown = new boolean[InteractionType.VALUES.length];

   @Override
   public void setState(@Nonnull InteractionType type, boolean state) {
      this.isDown[type.ordinal()] = state;
   }

   @Override
   public boolean isCharging(
      boolean firstRun, float time, @Nonnull InteractionType type, InteractionContext context, Ref<EntityStore> ref, CooldownHandler cooldownHandler
   ) {
      return this.isDown[type.ordinal()];
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
      return time;
   }
}
