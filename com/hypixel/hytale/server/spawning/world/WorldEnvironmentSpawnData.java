package com.hypixel.hytale.server.spawning.world;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.function.consumer.IntObjectConsumer;
import com.hypixel.hytale.math.random.RandomExtra;
import com.hypixel.hytale.server.core.modules.time.WorldTimeResource;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.spawning.SpawnRejection;
import com.hypixel.hytale.server.spawning.SpawningPlugin;
import com.hypixel.hytale.server.spawning.assets.spawns.config.RoleSpawnParameters;
import com.hypixel.hytale.server.spawning.world.component.ChunkSpawnData;
import com.hypixel.hytale.server.spawning.world.manager.WorldSpawnWrapper;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class WorldEnvironmentSpawnData {
   public static final double K_COLUMNS = 1024.0;
   private final int environmentIndex;
   private double expectedNPCs;
   private int actualNPCs;
   private int segmentCount;
   private double density;
   private double sumOfWeights;
   private boolean unspawnable;
   private boolean fullyPopulated;
   @Nonnull
   private final Int2ObjectMap<WorldNPCSpawnStat> npcStatMap;
   @Nonnull
   private final Set<Ref<ChunkStore>> chunkRefSet;

   public WorldEnvironmentSpawnData(int environmentIndex, double density) {
      this.environmentIndex = environmentIndex;
      this.npcStatMap = new Int2ObjectOpenHashMap();
      this.chunkRefSet = new HashSet<>();
      this.density = density;
      this.fullyPopulated = true;
   }

   public WorldEnvironmentSpawnData(int index) {
      this(index, SpawningPlugin.get().getEnvironmentDensity(index));
   }

   public int getEnvironmentIndex() {
      return this.environmentIndex;
   }

   public int getSegmentCount() {
      return this.segmentCount;
   }

   public boolean isUnspawnable() {
      return this.unspawnable;
   }

   public void setUnspawnable(boolean unspawnable) {
      this.unspawnable = unspawnable;
   }

   public double getExpectedNPCs() {
      return this.expectedNPCs;
   }

   public int getActualNPCs() {
      return this.actualNPCs;
   }

   public boolean isEmpty() {
      return this.getSegmentCount() == 0;
   }

   public boolean hasNPCs() {
      return !this.npcStatMap.isEmpty();
   }

   @Nonnull
   public Int2ObjectMap<WorldNPCSpawnStat> getNpcStatMap() {
      return this.npcStatMap;
   }

   public boolean isFullyPopulated() {
      return this.fullyPopulated;
   }

   public void setFullyPopulated(boolean fullyPopulated) {
      this.fullyPopulated = fullyPopulated;
   }

   @Nonnull
   public Set<Ref<ChunkStore>> getChunkRefSet() {
      return this.chunkRefSet;
   }

   public void adjustSegmentCount(int delta) {
      this.segmentCount += delta;
      this.expectedNPCs = this.segmentCount * this.density / 1024.0;
   }

   public void forEachNpcStat(@Nonnull IntObjectConsumer<WorldNPCSpawnStat> consumer) {
      this.npcStatMap.forEach(consumer::accept);
   }

   public void setDensity(double density, @Nonnull Store<ChunkStore> store) {
      this.density = density;
      this.expectedNPCs = this.segmentCount * density / 1024.0;

      for (Ref<ChunkStore> chunkRef : this.chunkRefSet) {
         store.getComponent(chunkRef, ChunkSpawnData.getComponentType()).getEnvironmentSpawnData(this.environmentIndex).updateDensity(density);
      }
   }

   public void updateNPCs(WorldSpawnWrapper spawnWrapper, World world) {
      Int2ObjectMap<RoleSpawnParameters> npcs = spawnWrapper.getRoles();
      if (!npcs.isEmpty()) {
         ObjectIterator var4 = npcs.int2ObjectEntrySet().iterator();

         while (var4.hasNext()) {
            Entry<RoleSpawnParameters> entry = (Entry<RoleSpawnParameters>)var4.next();
            if (!this.npcStatMap.containsKey(entry.getIntKey())) {
               this.npcStatMap.put(entry.getIntKey(), new WorldNPCSpawnStat(entry.getIntKey(), spawnWrapper, (RoleSpawnParameters)entry.getValue(), world));
            }
         }
      }
   }

   public void clearNPCs() {
      this.npcStatMap.clear();
      this.sumOfWeights = 0.0;
      this.actualNPCs = 0;
      this.unspawnable = true;
   }

   public void updateSpawnStats(
      int roleIndex, int spansTried, int spansSuccess, int budgetUsed, @Nonnull Object2IntMap<SpawnRejection> rejections, boolean success
   ) {
      WorldNPCSpawnStat stat = (WorldNPCSpawnStat)this.npcStatMap.get(roleIndex);
      if (stat != null) {
         stat.updateSpawnStats(spansTried, spansSuccess, budgetUsed, rejections, success);
      }
   }

   public void removeNPC(int roleIndex, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      WorldTimeResource worldTimeResource = componentAccessor.getResource(WorldTimeResource.getResourceType());
      this.npcStatMap.remove(roleIndex);
      this.recalculateWeight(worldTimeResource.getMoonPhase());
   }

   public void addNPC(
      int roleIndex,
      @Nonnull WorldSpawnWrapper spawnWrapper,
      @Nonnull RoleSpawnParameters spawnParams,
      @Nonnull World world,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      WorldTimeResource worldTimeResource = componentAccessor.getResource(WorldTimeResource.getResourceType());
      this.npcStatMap.computeIfAbsent(roleIndex, index -> new WorldNPCSpawnStat(index, spawnWrapper, spawnParams, world));
      this.recalculateWeight(worldTimeResource.getMoonPhase());
      this.resetUnspawnable();
   }

   public double spawnWeight() {
      return Math.max(0.0, this.getExpectedNPCs() - this.getActualNPCs());
   }

   @Nullable
   public WorldNPCSpawnStat pickRandomSpawnNPCStat(@Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      return RandomExtra.randomWeightedElement(this.npcStatMap.values(), worldNPCSpawnStat -> worldNPCSpawnStat.getMissingCount(componentAccessor));
   }

   public void resetUnspawnable() {
      if (this.npcStatMap.isEmpty()) {
         this.unspawnable = true;
      } else {
         this.unspawnable = false;
         ObjectIterator var1 = this.npcStatMap.values().iterator();

         while (var1.hasNext()) {
            WorldNPCSpawnStat stat = (WorldNPCSpawnStat)var1.next();
            stat.resetUnspawnable();
         }
      }
   }

   public void trackSpawn(int roleNameIndex, int npcCount) {
      WorldNPCSpawnStat stat = (WorldNPCSpawnStat)this.npcStatMap.get(roleNameIndex);
      if (stat == null) {
         stat = new WorldNPCSpawnStat.CountOnly(roleNameIndex);
         this.npcStatMap.put(roleNameIndex, stat);
      }

      stat.adjustActual(npcCount);
      this.actualNPCs += npcCount;
   }

   public void trackDespawn(int roleNameIndex, int npcCount) {
      WorldNPCSpawnStat stat = (WorldNPCSpawnStat)this.npcStatMap.get(roleNameIndex);
      if (stat != null && stat.getActual() > 0) {
         stat.adjustActual(-npcCount);
         this.actualNPCs -= npcCount;
      }
   }

   public void removeChunk(@Nonnull Ref<ChunkStore> ref, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      WorldTimeResource worldTimeResource = componentAccessor.getResource(WorldTimeResource.getResourceType());
      this.chunkRefSet.remove(ref);
      this.updateExpectedNPCs(worldTimeResource.getMoonPhase());
   }

   public void addChunk(@Nonnull Ref<ChunkStore> ref, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      WorldTimeResource worldTimeResource = componentAccessor.getResource(WorldTimeResource.getResourceType());
      this.chunkRefSet.add(ref);
      this.fullyPopulated = false;
      this.updateExpectedNPCs(worldTimeResource.getMoonPhase());
      this.resetUnspawnable();
   }

   public void recalculateWeight(int moonPhase) {
      this.sumOfWeights = 0.0;
      ObjectIterator var2 = this.npcStatMap.values().iterator();

      while (var2.hasNext()) {
         WorldNPCSpawnStat stat = (WorldNPCSpawnStat)var2.next();
         this.sumOfWeights = this.sumOfWeights + stat.getWeight(moonPhase);
      }

      this.updateExpectedNPCs(moonPhase);
   }

   public void updateExpectedNPCs(int moonPhase) {
      double segmentsPerWeightUnit = this.sumOfWeights == 0.0 ? 0.0 : this.expectedNPCs / this.sumOfWeights;
      this.actualNPCs = 0;
      ObjectIterator var4 = this.npcStatMap.values().iterator();

      while (var4.hasNext()) {
         WorldNPCSpawnStat stat = (WorldNPCSpawnStat)var4.next();
         stat.setExpected(stat.getWeight(moonPhase) * segmentsPerWeightUnit);
         this.actualNPCs = this.actualNPCs + stat.getActual();
      }
   }
}
