package com.hypixel.hytale.server.spawning.world;

import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.spawning.SpawningPlugin;
import com.hypixel.hytale.server.spawning.util.ChunkColumnMask;
import com.hypixel.hytale.server.spawning.util.RandomChunkColumnIterator;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import javax.annotation.Nonnull;

public class ChunkEnvironmentSpawnData {
   private IntSet possibleRoleTypes;
   private final IntSet unspawnableRoles = new IntOpenHashSet();
   private boolean processedAsUnspawnable;
   private RandomChunkColumnIterator randomChunkColumnIterator;
   private int segmentCount;
   private double expectedNPCs;

   public double getExpectedNPCs() {
      return this.expectedNPCs;
   }

   public RandomChunkColumnIterator getRandomChunkColumnIterator() {
      return this.randomChunkColumnIterator;
   }

   public void init(int environmentIndex, @Nonnull WorldChunk chunk) {
      this.randomChunkColumnIterator = new RandomChunkColumnIterator(new ChunkColumnMask(), chunk);
      this.possibleRoleTypes = SpawningPlugin.get().getRolesForEnvironment(environmentIndex);
      this.processedAsUnspawnable = false;
   }

   public void registerSegment(int x, int z) {
      this.randomChunkColumnIterator.getInitialPositions().set(x, z);
      this.segmentCount++;
   }

   public int getSegmentCount() {
      return this.segmentCount;
   }

   public void updateDensity(double density) {
      this.expectedNPCs = density * this.segmentCount / 1024.0;
   }

   public double getWeight(double spawnedNPCs) {
      double missingNPCs = this.expectedNPCs - spawnedNPCs;
      return MathUtil.maxValue(0.0, missingNPCs);
   }

   public boolean isFullyPopulated(double spawnedNPCs) {
      return this.expectedNPCs <= spawnedNPCs;
   }

   public void markRoleAsUnspawnable(int roleIndex) {
      this.unspawnableRoles.add(roleIndex);
   }

   public boolean isRoleSpawnable(int roleIndex) {
      return !this.unspawnableRoles.contains(roleIndex);
   }

   public boolean allRolesUnspawnable() {
      return this.unspawnableRoles.size() >= this.possibleRoleTypes.size();
   }

   public boolean wasProcessedAsUnspawnable() {
      return this.processedAsUnspawnable;
   }

   public void markProcessedAsUnspawnable() {
      this.processedAsUnspawnable = true;
   }
}
