package com.hypixel.hytale.server.core.event.events.player;

import com.hypixel.hytale.server.core.io.PacketHandler;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import javax.annotation.Nonnull;

public class PlayerDisconnectEvent extends PlayerRefEvent<Void> {
   public PlayerDisconnectEvent(@Nonnull PlayerRef playerRef) {
      super(playerRef);
   }

   @Nonnull
   public PacketHandler.DisconnectReason getDisconnectReason() {
      return this.playerRef.getPacketHandler().getDisconnectReason();
   }

   @Nonnull
   @Override
   public String toString() {
      return "PlayerDisconnectEvent{playerRef=" + this.playerRef + "} " + super.toString();
   }
}
