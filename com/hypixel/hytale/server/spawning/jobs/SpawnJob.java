package com.hypixel.hytale.server.spawning.jobs;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.spawning.ISpawnableWithModel;
import com.hypixel.hytale.server.spawning.SpawningContext;
import com.hypixel.hytale.server.spawning.SpawningPlugin;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class SpawnJob {
   protected static final HytaleLogger LOGGER = SpawningPlugin.get().getLogger();
   private static int jobIdCounter;
   protected final int jobId;
   protected final SpawningContext spawningContext = new SpawningContext();
   protected int columnBudget;
   protected int budgetUsed;
   protected boolean terminated;

   public SpawnJob() {
      this.jobId = jobIdCounter++;
   }

   public int getJobId() {
      return this.jobId;
   }

   public int getBudgetUsed() {
      return this.budgetUsed;
   }

   public void setBudgetUsed(int budgetUsed) {
      this.budgetUsed = budgetUsed;
   }

   public int getColumnBudget() {
      return this.columnBudget;
   }

   public void setColumnBudget(int columnBudget) {
      this.columnBudget = columnBudget;
   }

   @Nonnull
   public SpawningContext getSpawningContext() {
      return this.spawningContext;
   }

   protected void beginProbing() {
      this.reset();
      this.terminated = false;
   }

   public void reset() {
      this.spawningContext.releaseFull();
   }

   public boolean budgetAvailable() {
      return this.budgetUsed < this.columnBudget;
   }

   public boolean isTerminated() {
      return this.terminated;
   }

   public void setTerminated(boolean terminated) {
      this.terminated = terminated;
   }

   @Nullable
   public abstract ISpawnableWithModel getSpawnable();

   public abstract boolean shouldTerminate();

   @Nullable
   public abstract String getSpawnableName();
}
