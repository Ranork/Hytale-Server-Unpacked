package com.hypixel.hytale.server.spawning.world.component;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.asset.type.environment.config.Environment;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.flock.config.FlockAsset;
import com.hypixel.hytale.server.spawning.SpawnRejection;
import com.hypixel.hytale.server.spawning.SpawningContext;
import com.hypixel.hytale.server.spawning.SpawningPlugin;
import com.hypixel.hytale.server.spawning.suppression.SuppressionSpanHelper;
import com.hypixel.hytale.server.spawning.wrappers.SpawnWrapper;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import javax.annotation.Nonnull;

public class SpawnJobData implements Component<ChunkStore> {
   private static int jobIdCounter;
   private final int jobId;
   private int environmentIndex;
   private int totalColumnsTested;
   private int totalColumnsBlocked;
   private int budgetUsed;
   private int totalBudgetUsed;
   private boolean spawnFrozen;
   private final SpawningContext spawningContext;
   private int roleIndex;
   private SpawnWrapper<?> spawnConfig;
   private int spawnConfigIndex;
   private int flockSize;
   private FlockAsset flockAsset;
   private final SuppressionSpanHelper suppressionSpanHelper;
   private Environment environment;
   private int spansTried;
   private int spansSuccess;
   private final Object2IntMap<SpawnRejection> rejectionMap;
   private boolean ignoreFullyPopulated;
   private boolean terminated;

   public SpawnJobData() {
      this.jobId = jobIdCounter++;
      this.environmentIndex = Integer.MIN_VALUE;
      this.spawningContext = new SpawningContext();
      this.roleIndex = Integer.MIN_VALUE;
      this.suppressionSpanHelper = new SuppressionSpanHelper();
      this.rejectionMap = new Object2IntOpenHashMap();
   }

   public static ComponentType<ChunkStore, SpawnJobData> getComponentType() {
      return SpawningPlugin.get().getSpawnJobDataComponentType();
   }

   public int getJobId() {
      return this.jobId;
   }

   public int getEnvironmentIndex() {
      return this.environmentIndex;
   }

   public int getTotalColumnsTested() {
      return this.totalColumnsTested;
   }

   public void incrementTotalColumnsTested() {
      this.totalColumnsTested++;
   }

   public int getTotalColumnsBlocked() {
      return this.totalColumnsBlocked;
   }

   public void incrementTotalColumnsBlocked() {
      this.totalColumnsBlocked++;
   }

   public int getBudgetUsed() {
      return this.budgetUsed;
   }

   public void setBudgetUsed(int budgetUsed) {
      this.budgetUsed = budgetUsed;
   }

   public void adjustBudgetUsed(int amount) {
      this.budgetUsed += amount;
      this.totalBudgetUsed += amount;
   }

   public int getTotalBudgetUsed() {
      return this.totalBudgetUsed;
   }

   public boolean isSpawnFrozen() {
      return this.spawnFrozen;
   }

   public void setSpawnFrozen(boolean spawnFrozen) {
      this.spawnFrozen = spawnFrozen;
   }

   @Nonnull
   public SpawningContext getSpawningContext() {
      return this.spawningContext;
   }

   public int getRoleIndex() {
      return this.roleIndex;
   }

   public SpawnWrapper<?> getSpawnConfig() {
      return this.spawnConfig;
   }

   public int getSpawnConfigIndex() {
      return this.spawnConfigIndex;
   }

   public int getFlockSize() {
      return this.flockSize;
   }

   public FlockAsset getFlockAsset() {
      return this.flockAsset;
   }

   @Nonnull
   public SuppressionSpanHelper getSuppressionSpanHelper() {
      return this.suppressionSpanHelper;
   }

   public Environment getEnvironment() {
      return this.environment;
   }

   public int getSpansTried() {
      return this.spansTried;
   }

   public void incrementSpansTried() {
      this.spansTried++;
   }

   public int getSpansSuccess() {
      return this.spansSuccess;
   }

   public void incrementSpansSuccess() {
      this.spansSuccess++;
   }

   @Nonnull
   public Object2IntMap<SpawnRejection> getRejectionMap() {
      return this.rejectionMap;
   }

   public boolean isIgnoreFullyPopulated() {
      return this.ignoreFullyPopulated;
   }

   public void setIgnoreFullyPopulated(boolean ignoreFullyPopulated) {
      this.ignoreFullyPopulated = ignoreFullyPopulated;
   }

   public boolean isTerminated() {
      return this.terminated;
   }

   public void terminate() {
      this.terminated = true;
   }

   public void init(
      int roleIndex, Environment environment, int environmentIndex, @Nonnull SpawnWrapper<?> spawnConfig, FlockAsset flockDefinition, int flockSize
   ) {
      this.totalColumnsTested = 0;
      this.totalColumnsBlocked = 0;
      this.environmentIndex = environmentIndex;
      this.roleIndex = roleIndex;
      this.spawnConfig = spawnConfig;
      this.spawnConfigIndex = spawnConfig.getSpawnIndex();
      this.environment = environment;
      this.flockAsset = flockDefinition;
      this.flockSize = flockSize;
   }

   @Override
   public Component<ChunkStore> clone() {
      throw new UnsupportedOperationException("Not implemented!");
   }
}
