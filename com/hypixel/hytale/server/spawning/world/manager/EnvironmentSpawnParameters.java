package com.hypixel.hytale.server.spawning.world.manager;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;

public class EnvironmentSpawnParameters {
   private double density;
   private final Set<WorldSpawnWrapper> spawnWrappers = ConcurrentHashMap.newKeySet();

   public EnvironmentSpawnParameters(double density) {
      this.density = density;
   }

   @Nonnull
   public Set<WorldSpawnWrapper> getSpawnWrappers() {
      return this.spawnWrappers;
   }

   public double getSpawnDensity() {
      return this.density;
   }

   public void setDensity(double density) {
      this.density = density;
   }
}
