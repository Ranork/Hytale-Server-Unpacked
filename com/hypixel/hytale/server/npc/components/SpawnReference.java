package com.hypixel.hytale.server.npc.components;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.server.core.entity.reference.InvalidatablePersistentRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public abstract class SpawnReference implements Component<EntityStore> {
   protected static final BuilderCodec<SpawnReference> BASE_CODEC = BuilderCodec.abstractBuilder(SpawnReference.class)
      .append(
         new KeyedCodec<>("SpawnMarker", InvalidatablePersistentRef.CODEC),
         (reference, entityReference) -> reference.reference = entityReference,
         reference -> reference.reference
      )
      .add()
      .build();
   public static final float MARKER_LOST_TIMEOUT = 30.0F;
   protected InvalidatablePersistentRef reference = new InvalidatablePersistentRef();
   private float markerLostTimeoutCounter;

   public InvalidatablePersistentRef getReference() {
      return this.reference;
   }

   public boolean tickMarkerLostTimeoutCounter(float dt) {
      return (this.markerLostTimeoutCounter -= dt) <= 0.0F;
   }

   public void refreshTimeoutCounter() {
      this.markerLostTimeoutCounter = 30.0F;
   }

   @Override
   public abstract Component<EntityStore> clone();
}
