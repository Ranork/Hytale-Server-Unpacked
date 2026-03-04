package com.hypixel.hytale.server.core.event.events.ecs;

import com.hypixel.hytale.component.system.CancellableEcsEvent;
import com.hypixel.hytale.protocol.GameMode;
import javax.annotation.Nonnull;

public class ChangeGameModeEvent extends CancellableEcsEvent {
   @Nonnull
   private GameMode gameMode;

   public ChangeGameModeEvent(@Nonnull GameMode gameMode) {
      this.gameMode = gameMode;
   }

   @Nonnull
   public GameMode getGameMode() {
      return this.gameMode;
   }

   public void setGameMode(@Nonnull GameMode gameMode) {
      this.gameMode = gameMode;
   }
}
