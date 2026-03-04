package com.hypixel.hytale.server.spawning.world;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.flock.config.FlockAsset;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.asset.builder.BuilderInfo;
import com.hypixel.hytale.server.spawning.SpawnRejection;
import com.hypixel.hytale.server.spawning.assets.spawns.config.RoleSpawnParameters;
import com.hypixel.hytale.server.spawning.world.manager.WorldSpawnWrapper;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.lang.ref.WeakReference;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class WorldNPCSpawnStat {
   private final int roleIndex;
   @Nullable
   private final World world;
   @Nullable
   private WeakReference<BuilderInfo> builderInfoReference;
   private int minSpawnSize;
   private double expected;
   private int actual;
   private boolean unspawnable;
   @Nullable
   private final WorldSpawnWrapper spawnWrapper;
   @Nullable
   private final RoleSpawnParameters spawnParams;
   private int spansTried;
   private final Object2IntMap<SpawnRejection> rejections = new Object2IntOpenHashMap();
   private int spansSuccess;
   private int successfulJobCount;
   private int successfulJobTotalBudget;
   private int failedJobCount;
   private int failedJobTotalBudget;
   private final double weight;

   public WorldNPCSpawnStat(int roleIndex, WorldSpawnWrapper spawnWrapper, @Nonnull RoleSpawnParameters spawnParams, World world) {
      this.roleIndex = roleIndex;
      this.world = world;
      this.builderInfoReference = new WeakReference<>(NPCPlugin.get().getRoleBuilderInfo(roleIndex));
      this.weight = spawnParams.getWeight();
      this.spawnWrapper = spawnWrapper;
      this.spawnParams = spawnParams;
   }

   private WorldNPCSpawnStat(int roleIndex) {
      this.roleIndex = roleIndex;
      this.world = null;
      this.weight = 0.0;
      this.spawnWrapper = null;
      this.spawnParams = null;
   }

   public int getRoleIndex() {
      return this.roleIndex;
   }

   public double getExpected() {
      return this.expected;
   }

   public void setExpected(double expected) {
      this.expected = expected;
   }

   public int getActual() {
      return this.actual;
   }

   public void adjustActual(int count) {
      this.actual += count;
   }

   public boolean isUnspawnable() {
      return this.unspawnable;
   }

   public void setUnspawnable(boolean unspawnable) {
      this.unspawnable = unspawnable;
   }

   @Nullable
   public WorldSpawnWrapper getSpawnWrapper() {
      return this.spawnWrapper;
   }

   @Nullable
   public RoleSpawnParameters getSpawnParams() {
      return this.spawnParams;
   }

   public int getSpansTried() {
      return this.spansTried;
   }

   public int getSpansSuccess() {
      return this.spansSuccess;
   }

   public int getSuccessfulJobCount() {
      return this.successfulJobCount;
   }

   public int getSuccessfulJobTotalBudget() {
      return this.successfulJobTotalBudget;
   }

   public int getFailedJobCount() {
      return this.failedJobCount;
   }

   public int getFailedJobTotalBudget() {
      return this.failedJobTotalBudget;
   }

   public double getWeight(int moonPhase) {
      return this.weight * this.spawnWrapper.getMoonPhaseWeightModifier(moonPhase);
   }

   public double getMissingCount(@Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      if (!this.unspawnable && this.spawnWrapper.spawnParametersMatch(componentAccessor) && this.isSpawnable()) {
         double slotsLeft = Math.max(this.expected - this.actual, 0.0);
         return MathUtil.fastCeil(slotsLeft) < this.minSpawnSize ? 0.0 : slotsLeft;
      } else {
         return 0.0;
      }
   }

   public int getAvailableSlots() {
      return Math.max((int)MathUtil.fastCeil(this.expected - this.actual), 0);
   }

   public int getRejectionCount(SpawnRejection rejection) {
      return this.rejections.getInt(rejection);
   }

   public void updateSpawnStats(int spansTried, int spansSuccess, int budgetUsed, @Nonnull Object2IntMap<SpawnRejection> rejections, boolean success) {
      this.spansTried += spansTried;
      this.spansSuccess += spansSuccess;

      for (SpawnRejection rejection : SpawnRejection.VALUES) {
         this.rejections.mergeInt(rejection, rejections.getInt(rejection), Integer::sum);
      }

      if (success) {
         this.successfulJobCount++;
         this.successfulJobTotalBudget += budgetUsed;
      } else {
         this.failedJobCount++;
         this.failedJobTotalBudget += budgetUsed;
      }
   }

   public void resetUnspawnable() {
      this.unspawnable = false;
      if (this.builderInfoReference == null || this.builderInfoReference.get() != null) {
         this.builderInfoReference = new WeakReference<>(null);
      }
   }

   private boolean isSpawnable() {
      if (this.builderInfoReference == null) {
         return false;
      } else {
         BuilderInfo builderInfo = this.builderInfoReference.get();
         NPCPlugin npcModule = NPCPlugin.get();
         if (builderInfo != null && !builderInfo.isRemoved()) {
            return npcModule.testAndValidateRole(builderInfo);
         } else {
            builderInfo = npcModule.getRoleBuilderInfo(this.roleIndex);
            if (builderInfo == null) {
               this.builderInfoReference = null;
               return false;
            } else {
               this.builderInfoReference = new WeakReference<>(builderInfo);
               if (!npcModule.testAndValidateRole(builderInfo)) {
                  return false;
               } else {
                  this.recomputeSpawnSize();
                  return true;
               }
            }
         }
      }
   }

   private void recomputeSpawnSize() {
      FlockAsset flockDefinition = this.spawnParams.getFlockDefinition();
      if (flockDefinition == null) {
         this.minSpawnSize = 1;
      } else {
         this.minSpawnSize = flockDefinition.getMinFlockSize();
      }
   }

   public static class CountOnly extends WorldNPCSpawnStat {
      public CountOnly(int roleIndex) {
         super(roleIndex);
      }

      @Override
      public double getWeight(int moonPhase) {
         return 0.0;
      }

      @Override
      public double getMissingCount(@Nonnull ComponentAccessor<EntityStore> componentAccessor) {
         return 0.0;
      }

      @Override
      public int getAvailableSlots() {
         return 0;
      }
   }
}
