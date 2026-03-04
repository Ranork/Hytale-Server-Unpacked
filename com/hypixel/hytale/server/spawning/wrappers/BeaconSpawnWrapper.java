package com.hypixel.hytale.server.spawning.wrappers;

import com.hypixel.hytale.common.map.IWeightedMap;
import com.hypixel.hytale.common.map.WeightedMap;
import com.hypixel.hytale.server.spawning.assets.spawns.config.BeaconNPCSpawn;
import com.hypixel.hytale.server.spawning.assets.spawns.config.RoleSpawnParameters;
import java.util.Random;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BeaconSpawnWrapper extends SpawnWrapper<BeaconNPCSpawn> {
   @Nonnull
   private final IWeightedMap<RoleSpawnParameters> weightedRoles;
   private final double minDistanceFromPlayerSquared;
   private final double targetDistanceFromPlayerSquared;

   public BeaconSpawnWrapper(@Nonnull BeaconNPCSpawn spawn) {
      super(BeaconNPCSpawn.getAssetMap().getIndex(spawn.getId()), spawn);
      WeightedMap.Builder<RoleSpawnParameters> mapBuilder = WeightedMap.builder(RoleSpawnParameters.EMPTY_ARRAY);

      for (RoleSpawnParameters npc : spawn.getNPCs()) {
         if (!this.hasInvalidNPC(npc.getId())) {
            mapBuilder.put(npc, npc.getWeight());
         }
      }

      this.weightedRoles = mapBuilder.build();
      double minDistance = spawn.getMinDistanceFromPlayer();
      this.minDistanceFromPlayerSquared = minDistance * minDistance;
      double targetDistance = spawn.getTargetDistanceFromPlayer();
      this.targetDistanceFromPlayerSquared = targetDistance * targetDistance;
   }

   public double getMinDistanceFromPlayerSquared() {
      return this.minDistanceFromPlayerSquared;
   }

   public double getTargetDistanceFromPlayerSquared() {
      return this.targetDistanceFromPlayerSquared;
   }

   public double getBeaconRadius() {
      return this.spawn.getBeaconRadius();
   }

   public double getSpawnRadius() {
      return this.spawn.getSpawnRadius();
   }

   @Nullable
   public RoleSpawnParameters pickRole(Random chanceProvider) {
      return this.weightedRoles.size() == 0 ? null : this.weightedRoles.get(chanceProvider);
   }
}
