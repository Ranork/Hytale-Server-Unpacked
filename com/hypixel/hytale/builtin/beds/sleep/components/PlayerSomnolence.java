package com.hypixel.hytale.builtin.beds.sleep.components;

import com.hypixel.hytale.builtin.beds.BedsPlugin;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PlayerSomnolence implements Component<EntityStore> {
   @Nonnull
   public static PlayerSomnolence AWAKE = new PlayerSomnolence(PlayerSleep.FullyAwake.INSTANCE);
   @Nonnull
   private PlayerSleep state = PlayerSleep.FullyAwake.INSTANCE;

   public static ComponentType<EntityStore, PlayerSomnolence> getComponentType() {
      return BedsPlugin.getInstance().getPlayerSomnolenceComponentType();
   }

   public PlayerSomnolence() {
   }

   public PlayerSomnolence(@Nonnull PlayerSleep state) {
      this.state = state;
   }

   @Nonnull
   public PlayerSleep getSleepState() {
      return this.state;
   }

   @Nullable
   @Override
   public Component<EntityStore> clone() {
      PlayerSomnolence clone = new PlayerSomnolence();
      clone.state = this.state;
      return clone;
   }
}
