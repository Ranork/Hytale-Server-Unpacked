package com.hypixel.hytale.server.core.event.events.player;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class PlayerReadyEvent extends PlayerEvent<String> {
   private final int readyId;

   public PlayerReadyEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Player player, int readyId) {
      super(ref, player);
      this.readyId = readyId;
   }

   public int getReadyId() {
      return this.readyId;
   }

   @Nonnull
   @Override
   public String toString() {
      return "PlayerReadyEvent{readyId=" + this.readyId + "} " + super.toString();
   }
}
