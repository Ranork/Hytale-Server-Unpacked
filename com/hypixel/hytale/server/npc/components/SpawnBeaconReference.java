package com.hypixel.hytale.server.npc.components;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.spawning.SpawningPlugin;
import javax.annotation.Nonnull;

public class SpawnBeaconReference extends SpawnReference {
   public static final BuilderCodec<SpawnBeaconReference> CODEC = BuilderCodec.builder(SpawnBeaconReference.class, SpawnBeaconReference::new, BASE_CODEC)
      .build();

   public static ComponentType<EntityStore, SpawnBeaconReference> getComponentType() {
      return SpawningPlugin.get().getSpawnBeaconReferenceComponentType();
   }

   @Nonnull
   @Override
   public Component<EntityStore> clone() {
      SpawnBeaconReference reference = new SpawnBeaconReference();
      reference.reference = this.reference;
      return reference;
   }
}
