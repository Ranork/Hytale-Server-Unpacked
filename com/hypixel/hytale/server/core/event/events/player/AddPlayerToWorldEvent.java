package com.hypixel.hytale.server.core.event.events.player;

import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.event.IEvent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class AddPlayerToWorldEvent implements IEvent<String> {
   @Nonnull
   private final Holder<EntityStore> holder;
   @Nonnull
   private final World world;
   private boolean broadcastJoinMessage = true;

   public AddPlayerToWorldEvent(@Nonnull Holder<EntityStore> holder, @Nonnull World world) {
      this.holder = holder;
      this.world = world;
   }

   @Nonnull
   public Holder<EntityStore> getHolder() {
      return this.holder;
   }

   @Nonnull
   public World getWorld() {
      return this.world;
   }

   public boolean shouldBroadcastJoinMessage() {
      return this.broadcastJoinMessage;
   }

   public void setBroadcastJoinMessage(boolean broadcastJoinMessage) {
      this.broadcastJoinMessage = broadcastJoinMessage;
   }

   @Nonnull
   @Override
   public String toString() {
      return "AddPlayerToWorldEvent{world=" + this.world + ", broadcastJoinMessage=" + this.broadcastJoinMessage + "} " + super.toString();
   }
}
