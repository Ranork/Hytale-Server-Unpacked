package com.hypixel.hytale.server.spawning.blockstates;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.StateData;
import com.hypixel.hytale.server.core.entity.reference.PersistentRef;
import com.hypixel.hytale.server.core.universe.world.meta.BlockState;
import com.hypixel.hytale.server.spawning.assets.spawnmarker.config.SpawnMarker;

public class SpawnMarkerBlockState extends BlockState {
   public static final Codec<SpawnMarkerBlockState> CODEC = BuilderCodec.builder(SpawnMarkerBlockState.class, SpawnMarkerBlockState::new, BlockState.BASE_CODEC)
      .append(new KeyedCodec<>("MarkerReference", PersistentRef.CODEC), (spawn, o) -> spawn.spawnMarkerReference = o, spawn -> spawn.spawnMarkerReference)
      .add()
      .build();
   private PersistentRef spawnMarkerReference;
   private float markerLostTimeout = 30.0F;

   public PersistentRef getSpawnMarkerReference() {
      return this.spawnMarkerReference;
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

   public static class Data extends StateData {
      public static final BuilderCodec<SpawnMarkerBlockState.Data> CODEC = BuilderCodec.builder(
            SpawnMarkerBlockState.Data.class, SpawnMarkerBlockState.Data::new, StateData.DEFAULT_CODEC
         )
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
