package com.hypixel.hytale.server.core.modules.entity.player;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.protocol.PlayerSkin;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class PlayerSkinComponent implements Component<EntityStore> {
   @Nonnull
   private final PlayerSkin playerSkin;
   private boolean isNetworkOutdated = true;

   @Nonnull
   public static ComponentType<EntityStore, PlayerSkinComponent> getComponentType() {
      return EntityModule.get().getPlayerSkinComponentType();
   }

   public PlayerSkinComponent(@Nonnull PlayerSkin playerSkin) {
      this.playerSkin = playerSkin;
   }

   public boolean consumeNetworkOutdated() {
      boolean temp = this.isNetworkOutdated;
      this.isNetworkOutdated = false;
      return temp;
   }

   @Nonnull
   public PlayerSkin getPlayerSkin() {
      return this.playerSkin;
   }

   public void setNetworkOutdated() {
      this.isNetworkOutdated = true;
   }

   @Nonnull
   @Override
   public Component<EntityStore> clone() {
      return new PlayerSkinComponent(this.playerSkin);
   }
}
