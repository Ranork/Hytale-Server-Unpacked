package com.hypixel.hytale.builtin.adventure.npcobjectives.assets;

import com.hypixel.hytale.builtin.adventure.objectives.config.task.ObjectiveTaskAsset;
import com.hypixel.hytale.builtin.adventure.objectives.config.taskcondition.TaskConditionAsset;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.LegacyValidator;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.codec.validation.validator.ArrayValidator;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.spawning.assets.spawnmarker.config.SpawnMarker;
import java.util.Arrays;
import javax.annotation.Nonnull;

public class KillSpawnMarkerObjectiveTaskAsset extends KillObjectiveTaskAsset {
   @Nonnull
   public static final BuilderCodec<KillSpawnMarkerObjectiveTaskAsset> CODEC = BuilderCodec.builder(
         KillSpawnMarkerObjectiveTaskAsset.class, KillSpawnMarkerObjectiveTaskAsset::new, KillObjectiveTaskAsset.CODEC
      )
      .append(
         new KeyedCodec<>("Radius", Codec.FLOAT),
         (killSpawnMarkerObjectiveTaskAsset, aFloat) -> killSpawnMarkerObjectiveTaskAsset.radius = aFloat,
         killSpawnMarkerObjectiveTaskAsset -> killSpawnMarkerObjectiveTaskAsset.radius
      )
      .addValidator(Validators.greaterThan(0.0F))
      .add()
      .<String[]>append(
         new KeyedCodec<>("SpawnMarkerIds", Codec.STRING_ARRAY),
         (killSpawnMarkerObjectiveTaskAsset, s) -> killSpawnMarkerObjectiveTaskAsset.spawnMarkerIds = s,
         killSpawnMarkerObjectiveTaskAsset -> killSpawnMarkerObjectiveTaskAsset.spawnMarkerIds
      )
      .addValidator(Validators.nonEmptyArray())
      .addValidator(SpawnMarker.VALIDATOR_CACHE.getArrayValidator())
      .addValidator(new ArrayValidator<>((LegacyValidator<? super String>)((o, results) -> {
         SpawnMarker spawnMarker = SpawnMarker.getAssetMap().getAsset(o);
         if (spawnMarker != null && !spawnMarker.isManualTrigger()) {
            results.fail("SpawnMarker '" + o + "' can't be triggered manually!");
         }
      })))
      .add()
      .build();
   protected String[] spawnMarkerIds;
   protected float radius = 1.0F;

   public KillSpawnMarkerObjectiveTaskAsset(
      String descriptionId, TaskConditionAsset[] taskConditions, Vector3i[] mapMarkers, int count, String npcGroupId, String[] spawnMarkerIds, float radius
   ) {
      super(descriptionId, taskConditions, mapMarkers, count, npcGroupId);
      this.spawnMarkerIds = spawnMarkerIds;
      this.radius = radius;
   }

   protected KillSpawnMarkerObjectiveTaskAsset() {
   }

   @Nonnull
   public String[] getSpawnMarkerIds() {
      return this.spawnMarkerIds;
   }

   public float getRadius() {
      return this.radius;
   }

   @Override
   protected boolean matchesAsset0(ObjectiveTaskAsset task) {
      if (!super.matchesAsset0(task)) {
         return false;
      } else if (task instanceof KillSpawnMarkerObjectiveTaskAsset killSpawnMarkerObjectiveTaskAsset) {
         return !Arrays.equals((Object[])killSpawnMarkerObjectiveTaskAsset.spawnMarkerIds, (Object[])this.spawnMarkerIds)
            ? false
            : killSpawnMarkerObjectiveTaskAsset.radius == this.radius;
      } else {
         return false;
      }
   }

   @Nonnull
   @Override
   public String toString() {
      return "KillSpawnMarkerObjectiveTaskAsset{spawnMarkerIds="
         + Arrays.toString((Object[])this.spawnMarkerIds)
         + ", radius="
         + this.radius
         + "} "
         + super.toString();
   }
}
