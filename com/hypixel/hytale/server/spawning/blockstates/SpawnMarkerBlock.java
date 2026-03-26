package com.hypixel.hytale.server.spawning.blockstates;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.entity.reference.PersistentRef;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.spawning.SpawningPlugin;
import com.hypixel.hytale.server.spawning.assets.spawnmarker.config.SpawnMarker;
import javax.annotation.Nullable;

public class SpawnMarkerBlock implements Component<ChunkStore> {
   public static final BuilderCodec<SpawnMarkerBlock> CODEC = BuilderCodec.builder(SpawnMarkerBlock.class, SpawnMarkerBlock::new)
      .append(new KeyedCodec<>("MarkerReference", PersistentRef.CODEC), (spawn, o) -> spawn.spawnMarkerReference = o, spawn -> spawn.spawnMarkerReference)
      .add()
      .append(new KeyedCodec<>("Config", SpawnMarkerBlock.Data.CODEC), (spawn, o) -> spawn.config = o, spawn -> spawn.config)
      .add()
      .build();
   private PersistentRef spawnMarkerReference;
   private float markerLostTimeout = 30.0F;
   @Nullable
   private SpawnMarkerBlock.Data config;

   public static ComponentType<ChunkStore, SpawnMarkerBlock> getComponentType() {
      return SpawningPlugin.get().getSpawnMarkerBlockComponentType();
   }

   public SpawnMarkerBlock() {
   }

   public SpawnMarkerBlock(PersistentRef spawnMarkerReference) {
      this.spawnMarkerReference = spawnMarkerReference;
   }

   public PersistentRef getSpawnMarkerReference() {
      return this.spawnMarkerReference;
   }

   @Nullable
   public SpawnMarkerBlock.Data getConfig() {
      return this.config;
   }

   public void setSpawnMarkerReference(PersistentRef spawnMarkerReference) {
      this.spawnMarkerReference = spawnMarkerReference;
   }

   public void refreshMarkerLostTimeout() {
      this.markerLostTimeout = 30.0F;
   }

   public boolean tickMarkerLostTimeout(float dt) {
      return (this.markerLostTimeout -= dt) <= 0.0F;
   }

   @Nullable
   @Override
   public Component<ChunkStore> clone() {
      return new SpawnMarkerBlock(this.spawnMarkerReference != null ? new PersistentRef(this.spawnMarkerReference.getUuid()) : null);
   }

   public static class Data {
      public static final BuilderCodec<SpawnMarkerBlock.Data> CODEC = BuilderCodec.builder(SpawnMarkerBlock.Data.class, SpawnMarkerBlock.Data::new)
         .appendInherited(
            new KeyedCodec<>("SpawnMarker", Codec.STRING),
            (spawn, s) -> spawn.spawnMarker = s,
            spawn -> spawn.spawnMarker,
            (spawn, parent) -> spawn.spawnMarker = parent.spawnMarker
         )
         .documentation("The spawn marker to use.")
         .addValidator(Validators.nonNull())
         .addValidatorLate(() -> SpawnMarker.VALIDATOR_CACHE.getValidator().late())
         .add()
         .<Vector3i>appendInherited(
            new KeyedCodec<>("MarkerOffset", Vector3i.CODEC),
            (spawn, o) -> spawn.markerOffset = o,
            spawn -> spawn.markerOffset,
            (spawn, parent) -> spawn.markerOffset = parent.markerOffset
         )
         .documentation("An offset from the block at which the marker entity should be spawned.")
         .add()
         .build();
      private String spawnMarker;
      private Vector3i markerOffset;

      protected Data() {
      }

      public String getSpawnMarker() {
         return this.spawnMarker;
      }

      public Vector3i getMarkerOffset() {
         return this.markerOffset;
      }
   }
}
