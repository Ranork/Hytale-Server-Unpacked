package com.hypixel.hytale.server.npc.components;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.spawning.SpawningPlugin;
import javax.annotation.Nonnull;

public class SpawnMarkerReference extends SpawnReference {
   public static final BuilderCodec<SpawnMarkerReference> CODEC = BuilderCodec.builder(SpawnMarkerReference.class, SpawnMarkerReference::new, BASE_CODEC)
      .build();

   public static ComponentType<EntityStore, SpawnMarkerReference> getComponentType() {
      return SpawningPlugin.get().getSpawnMarkerReferenceComponentType();
   }

   @Nonnull
   @Override
   public Component<EntityStore> clone() {
      SpawnMarkerReference reference = new SpawnMarkerReference();
      reference.reference = this.reference;
      return reference;
   }
}
