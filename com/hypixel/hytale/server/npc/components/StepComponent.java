package com.hypixel.hytale.server.npc.components;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import javax.annotation.Nonnull;

public class StepComponent implements Component<EntityStore> {
   private final float tickLength;

   public static ComponentType<EntityStore, StepComponent> getComponentType() {
      return NPCPlugin.get().getStepComponentType();
   }

   public StepComponent(float tickLength) {
      this.tickLength = tickLength;
   }

   public float getTickLength() {
      return this.tickLength;
   }

   @Nonnull
   @Override
   public Component<EntityStore> clone() {
      return new StepComponent(this.tickLength);
   }
}
