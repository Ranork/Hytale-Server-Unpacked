package com.hypixel.hytale.server.core.event.events.player;

import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.event.IEvent;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class RemovedPlayerFromWorldEvent implements IEvent<String> {
   @Nonnull
   private final Holder<EntityStore> holder;
   @Nonnull
   private final World world;
   private boolean broadcastLeaveMessage = true;
   @Nullable
   private Message leaveMessage;

   public RemovedPlayerFromWorldEvent(@Nonnull Holder<EntityStore> holder, @Nonnull World world, @Nullable Message leaveMessage) {
      this.holder = holder;
      this.world = world;
      this.leaveMessage = leaveMessage;
   }

   @Nonnull
   public Holder<EntityStore> getHolder() {
      return this.holder;
   }

   @Nonnull
   public World getWorld() {
      return this.world;
   }

   public boolean shouldBroadcastLeaveMessage() {
      return this.broadcastLeaveMessage && this.leaveMessage != null;
   }

   public void setBroadcastLeaveMessage(boolean broadcastLeaveMessage) {
      this.broadcastLeaveMessage = broadcastLeaveMessage;
   }

   @Nullable
   public Message getLeaveMessage() {
      return this.leaveMessage;
   }

   public void setLeaveMessage(@Nullable Message leaveMessage) {
      this.leaveMessage = leaveMessage;
   }

   @Override
   public String toString() {
      return "RemovePlayerFromWorldEvent{, world="
         + this.world
         + ", broadcastLeaveMessage="
         + this.broadcastLeaveMessage
         + ", leaveMessage="
         + this.leaveMessage
         + "} "
         + super.toString();
   }
}
