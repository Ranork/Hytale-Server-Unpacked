package com.hypixel.hytale.server.core.event.events.player;

import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.event.IEvent;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AddPlayerToWorldEvent implements IEvent<String> {
   @Nonnull
   private final Holder<EntityStore> holder;
   @Nonnull
   private final World world;
   private boolean broadcastJoinMessage = true;
   @Nullable
   private Message joinMessage;

   public AddPlayerToWorldEvent(@Nonnull Holder<EntityStore> holder, @Nonnull World world, @Nullable Message joinMessage) {
      this.holder = holder;
      this.world = world;
      this.joinMessage = joinMessage;
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
      return this.broadcastJoinMessage && this.joinMessage != null;
   }

   public void setBroadcastJoinMessage(boolean broadcastJoinMessage) {
      this.broadcastJoinMessage = broadcastJoinMessage;
   }

   @Nullable
   public Message getJoinMessage() {
      return this.joinMessage;
   }

   public void setJoinMessage(@Nullable Message joinMessage) {
      this.joinMessage = joinMessage;
   }

   @Nonnull
   @Override
   public String toString() {
      return "AddPlayerToWorldEvent{world="
         + this.world
         + ", broadcastJoinMessage="
         + this.broadcastJoinMessage
         + ", joinMessage="
         + this.joinMessage
         + "} "
         + super.toString();
   }
}
