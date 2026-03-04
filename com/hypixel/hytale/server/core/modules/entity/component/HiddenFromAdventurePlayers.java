package com.hypixel.hytale.server.core.modules.entity.component;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class HiddenFromAdventurePlayers implements Component<EntityStore> {
   public static final HiddenFromAdventurePlayers INSTANCE = new HiddenFromAdventurePlayers();
   public static final BuilderCodec<HiddenFromAdventurePlayers> CODEC = BuilderCodec.builder(HiddenFromAdventurePlayers.class, () -> INSTANCE).build();

   public static ComponentType<EntityStore, HiddenFromAdventurePlayers> getComponentType() {
      return EntityModule.get().getHiddenFromAdventurePlayerComponentType();
   }

   private HiddenFromAdventurePlayers() {
   }

   @Override
   public Component<EntityStore> clone() {
      return INSTANCE;
   }
}
