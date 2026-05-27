package com.hypixel.hytale.server.core.event.events.ecs;

import com.hypixel.hytale.component.system.EcsEvent;
import com.hypixel.hytale.protocol.BlockMaterial;
import javax.annotation.Nonnull;

public class BreathingCheckEvent extends EcsEvent {
   @Nonnull
   private final BlockMaterial breathingMaterial;
   private final int fluidId;
   private boolean canBreathe;

   public BreathingCheckEvent(@Nonnull BlockMaterial breathingMaterial, int fluidId, boolean canBreathe) {
      this.breathingMaterial = breathingMaterial;
      this.fluidId = fluidId;
      this.canBreathe = canBreathe;
   }

   @Nonnull
   public BlockMaterial getBreathingMaterial() {
      return this.breathingMaterial;
   }

   public int getFluidId() {
      return this.fluidId;
   }

   public boolean canBreathe() {
      return this.canBreathe;
   }

   public void setCanBreathe(boolean canBreathe) {
      this.canBreathe = canBreathe;
   }
}
