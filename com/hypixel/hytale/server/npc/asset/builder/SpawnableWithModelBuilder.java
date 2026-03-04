package com.hypixel.hytale.server.npc.asset.builder;

import com.hypixel.hytale.server.spawning.ISpawnableWithModel;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

public abstract class SpawnableWithModelBuilder<T> extends BuilderBase<T> implements ISpawnableWithModel {
   private IntSet dynamicDependencies;

   @Override
   public boolean hasDynamicDependencies() {
      return this.dynamicDependencies != null;
   }

   @Override
   public void addDynamicDependency(int builderIndex) {
      if (this.dynamicDependencies == null) {
         this.dynamicDependencies = new IntOpenHashSet();
      }

      this.dynamicDependencies.add(builderIndex);
   }

   @Override
   public IntSet getDynamicDependencies() {
      return this.dynamicDependencies;
   }

   @Override
   public void clearDynamicDependencies() {
      if (this.dynamicDependencies != null) {
         this.dynamicDependencies.clear();
      }
   }

   @Override
   public boolean isSpawnable() {
      return true;
   }
}
