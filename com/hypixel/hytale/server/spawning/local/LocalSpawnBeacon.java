package com.hypixel.hytale.server.spawning.local;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.spawning.SpawningPlugin;
import javax.annotation.Nonnull;

public class LocalSpawnBeacon implements Component<EntityStore> {
   public static final BuilderCodec<LocalSpawnBeacon> CODEC = BuilderCodec.builder(LocalSpawnBeacon.class, LocalSpawnBeacon::new).build();

   public static ComponentType<EntityStore, LocalSpawnBeacon> getComponentType() {
      return SpawningPlugin.get().getLocalSpawnBeaconComponentType();
   }

   @Nonnull
   @Override
   public Component<EntityStore> clone() {
      return new LocalSpawnBeacon();
   }
}
