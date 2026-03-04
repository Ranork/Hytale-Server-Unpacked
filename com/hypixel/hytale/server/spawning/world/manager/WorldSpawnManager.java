package com.hypixel.hytale.server.spawning.world.manager;

import com.hypixel.fastutil.ints.Int2ObjectConcurrentHashMap;
import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.asset.type.environment.config.Environment;
import com.hypixel.hytale.server.core.modules.time.WorldTimeResource;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.spawning.SpawningPlugin;
import com.hypixel.hytale.server.spawning.assets.spawns.config.RoleSpawnParameters;
import com.hypixel.hytale.server.spawning.assets.spawns.config.WorldNPCSpawn;
import com.hypixel.hytale.server.spawning.managers.SpawnManager;
import com.hypixel.hytale.server.spawning.world.WorldEnvironmentSpawnData;
import com.hypixel.hytale.server.spawning.world.component.SpawnJobData;
import com.hypixel.hytale.server.spawning.world.component.WorldSpawnData;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.Iterator;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class WorldSpawnManager extends SpawnManager<WorldSpawnWrapper, WorldNPCSpawn> {
   protected final Int2ObjectConcurrentHashMap<EnvironmentSpawnParameters> environmentSpawnParametersMap = new Int2ObjectConcurrentHashMap<>();
   protected final Long2IntMap npcEnvCombinations = new Long2IntOpenHashMap();
   protected final Int2ObjectMap<IntSet> npcTypesPerEnvironment = new Int2ObjectOpenHashMap();

   public WorldSpawnManager() {
      this.npcEnvCombinations.defaultReturnValue(Integer.MIN_VALUE);
   }

   @Nullable
   public WorldSpawnWrapper removeSpawnWrapper(int spawnConfigurationIndex) {
      WorldSpawnWrapper spawnWrapper = (WorldSpawnWrapper)super.removeSpawnWrapper(spawnConfigurationIndex);
      if (spawnWrapper == null) {
         return null;
      } else {
         IntSet environments = spawnWrapper.getSpawn().getEnvironmentIds();
         IntIterator var4 = environments.iterator();

         while (var4.hasNext()) {
            int environmentIndex = (Integer)var4.next();
            EnvironmentSpawnParameters envConfigs = this.environmentSpawnParametersMap.get(environmentIndex);
            if (envConfigs != null) {
               envConfigs.getSpawnWrappers().remove(spawnWrapper);
            }
         }

         var4 = spawnWrapper.getRoles().keySet().iterator();

         while (var4.hasNext()) {
            int npcIndex = (Integer)var4.next();

            for (World world : Universe.get().getWorlds().values()) {
               onRoleRemoved(world, npcIndex, environments);
            }

            IntIterator var11 = environments.iterator();

            while (var11.hasNext()) {
               Integer environment = (Integer)var11.next();
               this.removeCombination(npcIndex, environment);
            }
         }

         return spawnWrapper;
      }
   }

   public boolean addSpawnWrapper(@Nonnull WorldSpawnWrapper spawnWrapper) {
      WorldNPCSpawn spawn = spawnWrapper.getSpawn();
      IndexedLookupTableAssetMap<String, Environment> assetMap = Environment.getAssetMap();
      IntSet environments = spawnWrapper.getSpawn().getEnvironmentIds();
      Int2ObjectMap<RoleSpawnParameters> npcs = spawnWrapper.getRoles();
      int spawnConfigIndex = spawnWrapper.getSpawnIndex();
      IntIterator var7 = npcs.keySet().iterator();

      while (var7.hasNext()) {
         Integer npcIndex = (Integer)var7.next();
         IntIterator environmentSpawnParameters = environments.iterator();

         while (environmentSpawnParameters.hasNext()) {
            Integer environmentIndex = (Integer)environmentSpawnParameters.next();
            if (this.haveCombination(npcIndex, environmentIndex)) {
               SpawningPlugin.get()
                  .getLogger()
                  .at(Level.SEVERE)
                  .log(
                     "Spawning Configuration %s can't be utilised: Combination NPC %s with Environment %s defined before in configuration %s",
                     spawn.getId(),
                     NPCPlugin.get().getName(npcIndex),
                     assetMap.getAsset(environmentIndex).getId(),
                     this.getCombination(npcIndex, environmentIndex)
                  );
               return false;
            }
         }
      }

      var7 = environments.iterator();

      while (var7.hasNext()) {
         Integer environmentIndex = (Integer)var7.next();
         EnvironmentSpawnParameters environmentSpawnParameters = this.environmentSpawnParametersMap.get(environmentIndex);
         if (environmentSpawnParameters == null) {
            environmentSpawnParameters = this.createEnvironmentSpawnParameters(environmentIndex, assetMap.getAsset(environmentIndex));
         }

         environmentSpawnParameters.getSpawnWrappers().add(spawnWrapper);
      }

      for (World world : Universe.get().getWorlds().values()) {
         ObjectIterator var20 = npcs.int2ObjectEntrySet().iterator();

         while (var20.hasNext()) {
            Entry<RoleSpawnParameters> roleSpawnParametersEntry = (Entry<RoleSpawnParameters>)var20.next();
            int npcIndex = roleSpawnParametersEntry.getIntKey();
            RoleSpawnParameters roleSpawnParameters = (RoleSpawnParameters)roleSpawnParametersEntry.getValue();
            onRoleAdded(world, npcIndex, environments, spawnWrapper, roleSpawnParameters);
         }
      }

      var7 = npcs.keySet().iterator();

      while (var7.hasNext()) {
         Integer npcIndex = (Integer)var7.next();
         IntIterator var21 = environments.iterator();

         while (var21.hasNext()) {
            Integer environmentIndex = (Integer)var21.next();
            this.addCombination(npcIndex, environmentIndex, spawnConfigIndex);
         }
      }

      super.addSpawnWrapper(spawnWrapper);
      return true;
   }

   public IntSet getRolesForEnvironment(int environment) {
      return (IntSet)this.npcTypesPerEnvironment.get(environment);
   }

   @Nonnull
   public EnvironmentSpawnParameters createEnvironmentSpawnParameters(int environmentIndex, @Nullable Environment environment) {
      EnvironmentSpawnParameters environmentSpawnParameters = new EnvironmentSpawnParameters(environment != null ? environment.getSpawnDensity() : 0.0);
      this.environmentSpawnParametersMap.put(environmentIndex, environmentSpawnParameters);
      return environmentSpawnParameters;
   }

   public EnvironmentSpawnParameters getEnvironmentSpawnParameters(int environmentIndex) {
      return this.environmentSpawnParametersMap.get(environmentIndex);
   }

   public void updateSpawnParameters(int environmentIndex, @Nullable Environment environment) {
      EnvironmentSpawnParameters spawnParameters = this.getEnvironmentSpawnParameters(environmentIndex);
      if (spawnParameters == null) {
         this.createEnvironmentSpawnParameters(environmentIndex, environment);
      } else {
         spawnParameters.setDensity(environment != null ? environment.getSpawnDensity() : 0.0);
      }
   }

   public void rebuildConfigurations(@Nullable IntSet changeSet) {
      if (changeSet != null && !changeSet.isEmpty()) {
         untrackNPCs(changeSet);
         int setupCount = 0;
         IntIterator var3 = changeSet.iterator();

         while (var3.hasNext()) {
            Integer configIndex = (Integer)var3.next();
            this.removeSpawnWrapper(configIndex);
            WorldNPCSpawn spawn = WorldNPCSpawn.getAssetMap().getAssetOrDefault(configIndex, null);
            if (spawn != null && this.addSpawnWrapper(new WorldSpawnWrapper(spawn))) {
               setupCount++;
            }
         }

         trackNPCs(changeSet);
         SpawningPlugin.get().getLogger().at(Level.INFO).log("Successfully rebuilt %s world spawn configurations", setupCount);
      }
   }

   public static void trackNPCs(@Nonnull IntSet spawnConfigs) {
      Universe.get().getWorlds().forEach((name, world) -> world.execute(() -> {
         Store<EntityStore> store = world.getEntityStore().getStore();
         WorldSpawnData worldSpawnData = store.getResource(WorldSpawnData.getResourceType());
         store.forEachChunk(NPCEntity.getComponentType(), (archetypeChunk, commandBuffer) -> {
            for (int index = 0; index < archetypeChunk.size(); index++) {
               NPCEntity npc = archetypeChunk.getComponent(index, NPCEntity.getComponentType());
               int spawnConfiguration = npc.getSpawnConfiguration();
               if (spawnConfiguration >= 0 && spawnConfigs.contains(spawnConfiguration)) {
                  boolean isTracked = npc.updateSpawnTrackingState(true);
                  if (!isTracked) {
                     worldSpawnData.trackNPC(npc.getEnvironment(), npc.getRoleIndex(), 1, world, commandBuffer);
                  }
               }
            }
         });
      }));
   }

   @Override
   public void untrackNPCs(int spawnConfig) {
      if (spawnConfig >= 0) {
         Universe.get()
            .getWorlds()
            .forEach(
               (name, world) -> world.execute(
                  () -> world.getEntityStore().getStore().forEachChunk(NPCEntity.getComponentType(), (archetypeChunk, commandBuffer) -> {
                     for (int index = 0; index < archetypeChunk.size(); index++) {
                        NPCEntity npc = archetypeChunk.getComponent(index, NPCEntity.getComponentType());
                        if (npc.getSpawnConfiguration() == spawnConfig) {
                           untrackNPC(world, npc);
                        }
                     }
                  })
               )
            );
      }
   }

   public static void untrackNPCs(@Nonnull IntSet spawnConfigs) {
      Universe.get()
         .getWorlds()
         .forEach(
            (name, world) -> world.execute(
               () -> world.getEntityStore().getStore().forEachChunk(NPCEntity.getComponentType(), (archetypeChunk, commandBuffer) -> {
                  for (int index = 0; index < archetypeChunk.size(); index++) {
                     NPCEntity npc = archetypeChunk.getComponent(index, NPCEntity.getComponentType());
                     int spawnConfiguration = npc.getSpawnConfiguration();
                     if (spawnConfiguration >= 0 && spawnConfigs.contains(spawnConfiguration)) {
                        untrackNPC(world, npc);
                     }
                  }
               })
            )
         );
   }

   public static void onEnvironmentChanged() {
      Universe.get().getWorlds().forEach((name, world) -> world.execute(() -> onEnvironmentChanged(world)));
   }

   private static void untrackNPC(@Nonnull World world, @Nonnull NPCEntity npc) {
      boolean isTracked = npc.updateSpawnTrackingState(false);
      if (isTracked) {
         WorldSpawnData worldSpawnData = world.getEntityStore().getStore().getResource(WorldSpawnData.getResourceType());
         worldSpawnData.untrackNPC(npc.getEnvironment(), npc.getRoleIndex(), 1);
      }
   }

   private static void onEnvironmentChanged(@Nonnull World world) {
      Store<EntityStore> store = world.getEntityStore().getStore();
      Store<ChunkStore> chunkStore = world.getChunkStore().getStore();
      WorldTimeResource worldTimeResource = store.getResource(WorldTimeResource.getResourceType());
      WorldSpawnData worldSpawnData = store.getResource(WorldSpawnData.getResourceType());
      worldSpawnData.forEachEnvironmentSpawnData(
         worldEnvironmentSpawnData -> {
            EnvironmentSpawnParameters environmentSpawnParameters = SpawningPlugin.get()
               .getWorldEnvironmentSpawnParameters(worldEnvironmentSpawnData.getEnvironmentIndex());
            if (environmentSpawnParameters != null) {
               worldEnvironmentSpawnData.setDensity(environmentSpawnParameters.getSpawnDensity(), chunkStore);

               for (WorldSpawnWrapper config : environmentSpawnParameters.getSpawnWrappers()) {
                  worldEnvironmentSpawnData.updateNPCs(config, world);
               }

               int moonPhase = worldTimeResource.getMoonPhase();
               worldEnvironmentSpawnData.recalculateWeight(moonPhase);
               worldEnvironmentSpawnData.resetUnspawnable();
            } else {
               worldEnvironmentSpawnData.setDensity(0.0, chunkStore);
               worldEnvironmentSpawnData.clearNPCs();
            }
         }
      );
      worldSpawnData.recalculateWorldCount();
   }

   private static void onRoleRemoved(@Nonnull World world, int roleIndex, @Nonnull IntSet environments) {
      world.execute(() -> {
         world.getChunkStore().getStore().forEachEntityParallel(SpawnJobData.getComponentType(), (index, chunk, commandBuffer) -> {
            SpawnJobData spawnJobData = chunk.getComponent(index, SpawnJobData.getComponentType());

            assert spawnJobData != null;

            if (spawnJobData.getRoleIndex() == roleIndex) {
               spawnJobData.terminate();
            }
         });
         Store<EntityStore> store = world.getEntityStore().getStore();
         WorldSpawnData worldSpawnData = store.getResource(WorldSpawnData.getResourceType());
         Iterator i$ = environments.iterator();

         while (i$.hasNext()) {
            int environmentIndex = (Integer)i$.next();
            WorldEnvironmentSpawnData worldEnvironmentSpawnStats = worldSpawnData.getWorldEnvironmentSpawnData(environmentIndex);
            if (worldEnvironmentSpawnStats != null) {
               worldEnvironmentSpawnStats.removeNPC(roleIndex, store);
            }
         }
      });
   }

   private static void onRoleAdded(
      @Nonnull World world, int roleIndex, @Nonnull IntSet environments, WorldSpawnWrapper spawnWrapper, @Nonnull RoleSpawnParameters spawnParams
   ) {
      world.execute(() -> {
         Store<EntityStore> store = world.getEntityStore().getStore();
         WorldSpawnData worldSpawnData = store.getResource(WorldSpawnData.getResourceType());
         Iterator i$ = environments.iterator();

         while (i$.hasNext()) {
            int environmentIndex = (Integer)i$.next();
            worldSpawnData.getOrCreateWorldEnvironmentSpawnData(environmentIndex, world, store).addNPC(roleIndex, spawnWrapper, spawnParams, world, store);
         }

         onEnvironmentChanged();
      });
   }

   private static long combinedIndex(int npc, int environment) {
      return ((long)npc << 32) + environment;
   }

   private boolean haveCombination(int npc, int environment) {
      return this.npcEnvCombinations.containsKey(combinedIndex(npc, environment));
   }

   private void addCombination(int npc, int environment, int config) {
      this.npcEnvCombinations.put(combinedIndex(npc, environment), config);
      ((IntSet)this.npcTypesPerEnvironment.computeIfAbsent(environment, i -> new IntOpenHashSet())).add(npc);
   }

   private void removeCombination(int npc, int environment) {
      this.npcEnvCombinations.remove(combinedIndex(npc, environment));
      ((IntSet)this.npcTypesPerEnvironment.get(environment)).remove(npc);
   }

   private String getCombination(int npc, int environment) {
      return WorldNPCSpawn.getAssetMap().getAsset(this.npcEnvCombinations.get(combinedIndex(npc, environment))).getId();
   }
}
