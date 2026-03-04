package com.hypixel.hytale.server.spawning.controllers;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.spawning.SpawningPlugin;
import com.hypixel.hytale.server.spawning.jobs.SpawnJob;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.ArrayDeque;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class SpawnController<T extends SpawnJob> {
   protected World world;
   protected boolean unspawnable;
   protected double expectedNPCs;
   protected int actualNPCs;
   protected final List<T> activeJobs = new ObjectArrayList();
   protected final ArrayDeque<T> idleJobs = new ArrayDeque<>();
   protected final int baseMaxActiveJobs;
   protected boolean debugSpawnFrozen;

   public SpawnController(World world) {
      this.world = world;
      this.expectedNPCs = 0.0;
      this.actualNPCs = 0;
      this.unspawnable = false;
      this.baseMaxActiveJobs = SpawningPlugin.get().getMaxActiveJobs();
   }

   public World getWorld() {
      return this.world;
   }

   public boolean isUnspawnable() {
      return this.unspawnable;
   }

   public boolean isDebugSpawnFrozen() {
      return this.debugSpawnFrozen;
   }

   public int getActualNPCs() {
      return this.actualNPCs;
   }

   public double getExpectedNPCs() {
      return this.expectedNPCs;
   }

   public int getActiveJobCount() {
      return this.activeJobs.size();
   }

   public int getMaxActiveJobs() {
      return this.baseMaxActiveJobs;
   }

   public T getSpawnJob(int index) {
      return this.activeJobs.get(index);
   }

   @Nonnull
   public List<T> getActiveJobs() {
      return this.activeJobs;
   }

   public void addIdleJob(@Nonnull T job) {
      this.idleJobs.push(job);
   }

   @Nullable
   public abstract T createRandomSpawnJob(ComponentAccessor<EntityStore> var1);
}
