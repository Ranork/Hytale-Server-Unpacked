package com.hypixel.hytale.server.core.entity.entities.player.hud;

import com.hypixel.hytale.protocol.packets.interface_.CustomHud;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import javax.annotation.Nonnull;

public abstract class CustomUIHud {
   @Nonnull
   private final PlayerRef playerRef;
   @Nonnull
   private final String key;
   private int zOrder;

   public CustomUIHud(@Nonnull PlayerRef playerRef, @Nonnull String key) {
      this(playerRef, key, 0);
   }

   public CustomUIHud(@Nonnull PlayerRef playerRef, @Nonnull String key, int zOrder) {
      this.playerRef = playerRef;
      this.key = key;
      this.zOrder = zOrder;
   }

   public void show() {
      UICommandBuilder commandBuilder = new UICommandBuilder();
      this.build(commandBuilder);
      this.update(true, commandBuilder);
   }

   public void update(boolean clear, @Nonnull UICommandBuilder commandBuilder) {
      CustomHud customHud = new CustomHud(this.key, this.zOrder, clear, commandBuilder.getCommands());
      this.playerRef.getPacketHandler().writeNoCache(customHud);
   }

   @Nonnull
   public PlayerRef getPlayerRef() {
      return this.playerRef;
   }

   @Nonnull
   public String getKey() {
      return this.key;
   }

   public int getZOrder() {
      return this.zOrder;
   }

   public void setZOrder(int zOrder) {
      this.zOrder = zOrder;
      this.update(false, new UICommandBuilder());
   }

   protected void onRemove() {
   }

   protected abstract void build(@Nonnull UICommandBuilder var1);
}
