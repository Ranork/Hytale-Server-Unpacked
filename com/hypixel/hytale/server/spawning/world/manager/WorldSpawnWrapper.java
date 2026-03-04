package com.hypixel.hytale.server.spawning.world.manager;

import com.hypixel.hytale.server.spawning.assets.spawns.config.WorldNPCSpawn;
import com.hypixel.hytale.server.spawning.wrappers.SpawnWrapper;
import javax.annotation.Nonnull;

public class WorldSpawnWrapper extends SpawnWrapper<WorldNPCSpawn> {
   public WorldSpawnWrapper(@Nonnull WorldNPCSpawn spawn) {
      super(WorldNPCSpawn.getAssetMap().getIndex(spawn.getId()), spawn);
   }

   public double getMoonPhaseWeightModifier(int moonPhase) {
      double[] moonPhaseWeights = this.spawn.getMoonPhaseWeightModifiers();
      if (moonPhaseWeights == null) {
         return 1.0;
      } else {
         return moonPhase >= moonPhaseWeights.length ? 0.0 : moonPhaseWeights[moonPhase];
      }
   }
}
