package com.hypixel.hytale.server.core.entity.entities.player;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;

public class HiddenPlayersManager {
   @Nonnull
   private final Set<UUID> hiddenPlayers = ConcurrentHashMap.newKeySet();

   public void hidePlayer(@Nonnull UUID uuid) {
      this.hiddenPlayers.add(uuid);
   }

   public void showPlayer(@Nonnull UUID uuid) {
      this.hiddenPlayers.remove(uuid);
   }

   public boolean isPlayerHidden(@Nonnull UUID uuid) {
      return this.hiddenPlayers.contains(uuid);
   }
}
