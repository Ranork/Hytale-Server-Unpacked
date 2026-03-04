package com.hypixel.hytale.server.spawning.managers;

import com.hypixel.fastutil.ints.Int2ObjectConcurrentHashMap;
import com.hypixel.hytale.server.spawning.assets.spawns.config.BeaconNPCSpawn;
import com.hypixel.hytale.server.spawning.wrappers.BeaconSpawnWrapper;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BeaconSpawnManager extends SpawnManager<BeaconSpawnWrapper, BeaconNPCSpawn> {
   @Nonnull
   private final Int2ObjectConcurrentHashMap<List<BeaconSpawnWrapper>> wrappersByEnvironment = new Int2ObjectConcurrentHashMap<>();

   @Nullable
   public BeaconSpawnWrapper removeSpawnWrapper(int spawnConfigurationIndex) {
      BeaconSpawnWrapper wrapper = (BeaconSpawnWrapper)super.removeSpawnWrapper(spawnConfigurationIndex);
      if (wrapper == null) {
         return null;
      } else {
         IntSet environmentIds = wrapper.getSpawn().getEnvironmentIds();
         environmentIds.forEach(environment -> this.wrappersByEnvironment.get(environment).remove(wrapper));
         return wrapper;
      }
   }

   public boolean addSpawnWrapper(@Nonnull BeaconSpawnWrapper spawnWrapper) {
      IntSet environmentIds = spawnWrapper.getSpawn().getEnvironmentIds();
      environmentIds.forEach(environment -> this.wrappersByEnvironment.computeIfAbsent(environment, key -> new ObjectArrayList()).add(spawnWrapper));
      super.addSpawnWrapper(spawnWrapper);
      return true;
   }

   public List<BeaconSpawnWrapper> getBeaconSpawns(int environment) {
      return this.wrappersByEnvironment.get(environment);
   }
}
