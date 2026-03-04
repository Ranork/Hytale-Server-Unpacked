package com.hypixel.hytale.server.spawning.managers;

import com.hypixel.hytale.server.spawning.SpawningPlugin;
import com.hypixel.hytale.server.spawning.assets.spawns.config.NPCSpawn;
import com.hypixel.hytale.server.spawning.wrappers.SpawnWrapper;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.concurrent.locks.StampedLock;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class SpawnManager<T extends SpawnWrapper<U>, U extends NPCSpawn> {
   private final Int2ObjectMap<T> spawnWrapperCache = new Int2ObjectOpenHashMap();
   private final Object2IntMap<String> wrapperNameMap = new Object2IntOpenHashMap();
   private final StampedLock wrapperLock = new StampedLock();

   public T getSpawnWrapper(int spawnConfigIndex) {
      long stamp = this.wrapperLock.readLock();

      SpawnWrapper var4;
      try {
         var4 = (SpawnWrapper)this.spawnWrapperCache.get(spawnConfigIndex);
      } finally {
         this.wrapperLock.unlockRead(stamp);
      }

      return (T)var4;
   }

   @Nullable
   public T removeSpawnWrapper(int spawnConfigurationIndex) {
      long stamp = this.wrapperLock.writeLock();

      Object var5;
      try {
         T spawnWrapper = (T)this.spawnWrapperCache.remove(spawnConfigurationIndex);
         if (spawnWrapper != null) {
            this.wrapperNameMap.removeInt(spawnWrapper.getSpawn().getId());
            return spawnWrapper;
         }

         var5 = null;
      } finally {
         this.wrapperLock.unlockWrite(stamp);
      }

      return (T)var5;
   }

   public boolean addSpawnWrapper(@Nonnull T spawnWrapper) {
      U spawn = spawnWrapper.getSpawn();
      int spawnConfigIndex = spawnWrapper.getSpawnIndex();
      long stamp = this.wrapperLock.writeLock();

      try {
         this.spawnWrapperCache.put(spawnConfigIndex, spawnWrapper);
         this.wrapperNameMap.put(spawn.getId(), spawnConfigIndex);
      } finally {
         this.wrapperLock.unlockWrite(stamp);
      }

      SpawningPlugin.get().getLogger().at(Level.FINE).log("Set up NPCSpawn %s", spawn.getId());
      return true;
   }

   public void onNPCLoaded(String name, @Nonnull IntSet changeSet) {
      long stamp = this.wrapperLock.writeLock();

      try {
         ObjectIterator var5 = this.spawnWrapperCache.int2ObjectEntrySet().iterator();

         while (var5.hasNext()) {
            Entry<T> entry = (Entry<T>)var5.next();
            T wrapper = (T)entry.getValue();
            if (wrapper.hasInvalidNPC(name)) {
               changeSet.add(wrapper.getSpawnIndex());
            }
         }
      } finally {
         this.wrapperLock.unlockWrite(stamp);
      }
   }

   public void onNPCSpawnRemoved(String key) {
      long stamp = this.wrapperLock.readLock();

      int index;
      try {
         index = this.wrapperNameMap.getInt(key);
      } finally {
         this.wrapperLock.unlockRead(stamp);
      }

      this.untrackNPCs(index);
      this.removeSpawnWrapper(index);
   }

   protected void untrackNPCs(int index) {
   }
}
