package com.hypixel.hytale.server.core.event.events.player;

import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.event.IEvent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PlayerConnectEvent implements IEvent<Void> {
   private final Holder<EntityStore> holder;
   private final PlayerRef playerRef;
   @Nullable
   private World world;

   public PlayerConnectEvent(@Nonnull Holder<EntityStore> holder, @Nonnull PlayerRef playerRef, @Nullable World world) {
      this.holder = holder;
      this.playerRef = playerRef;
      this.world = world;
   }

   public Holder<EntityStore> getHolder() {
      return this.holder;
   }

   public PlayerRef getPlayerRef() {
      return this.playerRef;
   }

   @Nullable
   @Deprecated
   public Player getPlayer() {
      return this.holder.getComponent(Player.getComponentType());
   }

   @Nullable
   public World getWorld() {
      return this.world;
   }

   public void setWorld(@Nullable World world) {
      this.world = world;
   }

   @Nonnull
   @Override
   public String toString() {
      return "PlayerConnectEvent{world=" + this.world + "} " + super.toString();
   }
}
