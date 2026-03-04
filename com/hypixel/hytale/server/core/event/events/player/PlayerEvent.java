package com.hypixel.hytale.server.core.event.events.player;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.event.IEvent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public abstract class PlayerEvent<KeyType> implements IEvent<KeyType> {
   @Nonnull
   private final Ref<EntityStore> playerRef;
   @Nonnull
   private final Player player;

   public PlayerEvent(@Nonnull Ref<EntityStore> playerRef, @Nonnull Player player) {
      this.playerRef = playerRef;
      this.player = player;
   }

   @Nonnull
   public Ref<EntityStore> getPlayerRef() {
      return this.playerRef;
   }

   @Nonnull
   public Player getPlayer() {
      return this.player;
   }

   @Nonnull
   @Override
   public String toString() {
      return "PlayerEvent{player=" + this.player + "}";
   }
}
