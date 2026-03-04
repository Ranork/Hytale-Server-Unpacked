package com.hypixel.hytale.builtin.adventure.objectives;

import com.hypixel.hytale.builtin.adventure.objectives.config.ObjectiveAsset;
import com.hypixel.hytale.builtin.adventure.objectives.task.ObjectiveTask;
import com.hypixel.hytale.builtin.adventure.objectives.task.ObjectiveTaskRef;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.universe.datastore.DataStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ObjectiveDataStore {
   @Nonnull
   private final Map<UUID, Objective> objectives = new ConcurrentHashMap<>();
   @Nonnull
   private final Map<UUID, Map<String, Set<UUID>>> entityObjectiveUUIDsPerPlayer = new ConcurrentHashMap<>();
   @Nonnull
   private final DataStore<Objective> dataStore;
   @Nonnull
   private final Map<Class<? extends ObjectiveTask>, Set<ObjectiveTaskRef<? extends ObjectiveTask>>> taskRefByType = new ConcurrentHashMap<>();
   @Nonnull
   private final HytaleLogger logger;

   public ObjectiveDataStore(@Nonnull DataStore<Objective> dataStore) {
      this.dataStore = dataStore;
      this.logger = ObjectivePlugin.get().getLogger();
   }

   public Objective getObjective(UUID objectiveUUID) {
      return this.objectives.get(objectiveUUID);
   }

   public Map<String, Set<UUID>> getEntityTasksForPlayer(UUID playerUUID) {
      return this.entityObjectiveUUIDsPerPlayer.get(playerUUID);
   }

   @Nonnull
   public Collection<Objective> getObjectiveCollection() {
      return this.objectives.values();
   }

   public <T extends ObjectiveTask> Set<ObjectiveTaskRef<T>> getTaskRefsForType(Class<T> taskClass) {
      return (Set<ObjectiveTaskRef<T>>)this.taskRefByType.get(taskClass);
   }

   public <T extends ObjectiveTask> void addTaskRef(@Nonnull ObjectiveTaskRef<T> taskRef) {
      this.taskRefByType.get(taskRef.getObjectiveTask().getClass()).add(taskRef);
   }

   public <T extends ObjectiveTask> void removeTaskRef(@Nullable ObjectiveTaskRef<T> taskRef) {
      if (taskRef != null) {
         this.taskRefByType.get(taskRef.getObjectiveTask().getClass()).remove(taskRef);
      }
   }

   public <T extends ObjectiveTask> void registerTaskRef(Class<T> taskClass) {
      this.taskRefByType.put(taskClass, ConcurrentHashMap.newKeySet());
   }

   public void saveToDisk(String objectiveId, @Nonnull Objective objective) {
      if (objective.consumeDirty()) {
         this.dataStore.save(objectiveId, objective);
      }
   }

   public void saveToDiskAllObjectives() {
      for (Entry<UUID, Objective> entry : this.objectives.entrySet()) {
         this.saveToDisk(entry.getKey().toString(), entry.getValue());
      }
   }

   public boolean removeFromDisk(String objectiveId) {
      try {
         this.dataStore.remove(objectiveId);
         return true;
      } catch (IOException var3) {
         ((HytaleLogger.Api)this.logger.at(Level.WARNING).withCause(var3)).log("Failed removal of objective with UUID: %s", objectiveId);
         return false;
      }
   }

   public boolean addObjective(UUID objectiveUUID, Objective objective) {
      return this.objectives.putIfAbsent(objectiveUUID, objective) == null;
   }

   public void removeObjective(UUID objectiveUUID) {
      this.objectives.remove(objectiveUUID);
   }

   public void addEntityTaskForPlayer(UUID playerUUID, String taskId, UUID objectiveUUID) {
      this.entityObjectiveUUIDsPerPlayer
         .computeIfAbsent(playerUUID, s -> new ConcurrentHashMap<>())
         .computeIfAbsent(taskId, s -> ConcurrentHashMap.newKeySet())
         .add(objectiveUUID);
   }

   public void removeEntityTask(UUID objectiveUUID, String taskId) {
      Iterator<Entry<UUID, Map<String, Set<UUID>>>> entityObjectiveUUIDsPerPlayerIterator = this.entityObjectiveUUIDsPerPlayer.entrySet().iterator();

      while (entityObjectiveUUIDsPerPlayerIterator.hasNext()) {
         Entry<UUID, Map<String, Set<UUID>>> entityObjectiveUUIDsEntry = entityObjectiveUUIDsPerPlayerIterator.next();
         Map<String, Set<UUID>> entityObjectiveUUIDs = entityObjectiveUUIDsEntry.getValue();
         Set<UUID> objectiveUUIDs = entityObjectiveUUIDs.get(taskId);
         if (objectiveUUIDs != null && objectiveUUIDs.remove(objectiveUUID)) {
            if (objectiveUUIDs.isEmpty()) {
               entityObjectiveUUIDs.remove(taskId);
            }

            if (entityObjectiveUUIDs.isEmpty()) {
               entityObjectiveUUIDsPerPlayerIterator.remove();
            }
         }
      }
   }

   public void removeEntityTaskForPlayer(UUID objectiveUUID, String taskId, UUID playerUUID) {
      Map<String, Set<UUID>> entityObjectiveUUIDs = this.entityObjectiveUUIDsPerPlayer.get(playerUUID);
      if (entityObjectiveUUIDs != null) {
         Set<UUID> objectiveUUIDs = entityObjectiveUUIDs.get(taskId);
         if (objectiveUUIDs != null) {
            if (objectiveUUIDs.remove(objectiveUUID)) {
               if (objectiveUUIDs.isEmpty()) {
                  entityObjectiveUUIDs.remove(taskId);
               }

               if (entityObjectiveUUIDs.isEmpty()) {
                  this.entityObjectiveUUIDsPerPlayer.remove(playerUUID);
               }
            }
         }
      }
   }

   @Nullable
   public Objective loadObjective(@Nonnull UUID objectiveUUID, @Nonnull Store<EntityStore> store) {
      Objective objective = this.objectives.get(objectiveUUID);
      if (objective != null) {
         return objective;
      } else {
         try {
            objective = this.dataStore.load(objectiveUUID.toString());
         } catch (IOException var5) {
            ((HytaleLogger.Api)this.logger.at(Level.WARNING).withCause(var5)).log("Unable to load objective with UUID '%s'", objectiveUUID);
            return null;
         }

         if (objective == null) {
            this.logger.at(Level.WARNING).log("No objective saved with UUID '%s'", objectiveUUID);
            return null;
         } else {
            String objectiveId = objective.getObjectiveId();
            if (ObjectiveAsset.getAssetMap().getAsset(objectiveId) == null) {
               this.logger.at(Level.WARNING).log("Couldn't find objective '%s'. Skipping objective.", objectiveId);
               return null;
            } else if (!objective.setupCurrentTasks(store)) {
               this.logger.at(Level.WARNING).log("A problem occurred while setting up the objective '%s'. Skipping objective.", objectiveId);
               return null;
            } else {
               this.addObjective(objectiveUUID, objective);
               return objective;
            }
         }
      }
   }

   public void unloadObjective(UUID objectiveUUID) {
      Objective objective = this.objectives.get(objectiveUUID);
      if (objective != null) {
         objective.unload();
         this.removeObjective(objective.getObjectiveUUID());
      }
   }
}
