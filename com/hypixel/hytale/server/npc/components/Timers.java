package com.hypixel.hytale.server.npc.components;

import com.hypixel.hytale.common.thread.ticking.Tickable;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import javax.annotation.Nonnull;

public class Timers implements Component<EntityStore> {
   private final Tickable[] timers;

   public static ComponentType<EntityStore, Timers> getComponentType() {
      return NPCPlugin.get().getTimersComponentType();
   }

   public Timers(Tickable[] timers) {
      this.timers = timers;
   }

   public Tickable[] getTimers() {
      return this.timers;
   }

   @Nonnull
   @Override
   public Component<EntityStore> clone() {
      return new Timers(this.timers);
   }
}
