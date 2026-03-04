package com.hypixel.hytale.server.core.entity.entities.player.hud;

import com.hypixel.hytale.protocol.packets.interface_.CustomHud;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import javax.annotation.Nonnull;

public abstract class CustomUIHud {
   @Nonnull
   private final PlayerRef playerRef;

   public CustomUIHud(@Nonnull PlayerRef playerRef) {
      this.playerRef = playerRef;
   }

   public void show() {
      UICommandBuilder commandBuilder = new UICommandBuilder();
      this.build(commandBuilder);
      this.update(true, commandBuilder);
   }

   public void update(boolean clear, @Nonnull UICommandBuilder commandBuilder) {
      CustomHud customHud = new CustomHud(clear, commandBuilder.getCommands());
      this.playerRef.getPacketHandler().writeNoCache(customHud);
   }

   @Nonnull
   public PlayerRef getPlayerRef() {
      return this.playerRef;
   }

   protected abstract void build(@Nonnull UICommandBuilder var1);
}
