package com.hypixel.hytale.builtin.adventure.npcobjectives;

import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.builtin.adventure.npcobjectives.assets.BountyObjectiveTaskAsset;
import com.hypixel.hytale.builtin.adventure.npcobjectives.assets.KillObjectiveTaskAsset;
import com.hypixel.hytale.builtin.adventure.npcobjectives.assets.KillSpawnBeaconObjectiveTaskAsset;
import com.hypixel.hytale.builtin.adventure.npcobjectives.assets.KillSpawnMarkerObjectiveTaskAsset;
import com.hypixel.hytale.builtin.adventure.npcobjectives.npc.builders.BuilderActionCompleteTask;
import com.hypixel.hytale.builtin.adventure.npcobjectives.npc.builders.BuilderActionStartObjective;
import com.hypixel.hytale.builtin.adventure.npcobjectives.npc.builders.BuilderSensorHasTask;
import com.hypixel.hytale.builtin.adventure.npcobjectives.resources.KillTrackerResource;
import com.hypixel.hytale.builtin.adventure.npcobjectives.systems.KillTrackerSystem;
import com.hypixel.hytale.builtin.adventure.npcobjectives.systems.SpawnBeaconCheckRemovalSystem;
import com.hypixel.hytale.builtin.adventure.npcobjectives.task.BountyObjectiveTask;
import com.hypixel.hytale.builtin.adventure.npcobjectives.task.KillNPCObjectiveTask;
import com.hypixel.hytale.builtin.adventure.npcobjectives.task.KillSpawnBeaconObjectiveTask;
import com.hypixel.hytale.builtin.adventure.npcobjectives.task.KillSpawnMarkerObjectiveTask;
import com.hypixel.hytale.builtin.adventure.objectives.Objective;
import com.hypixel.hytale.builtin.adventure.objectives.ObjectiveDataStore;
import com.hypixel.hytale.builtin.adventure.objectives.ObjectivePlugin;
import com.hypixel.hytale.builtin.adventure.objectives.config.ObjectiveAsset;
import com.hypixel.hytale.builtin.adventure.objectives.config.task.UseEntityObjectiveTaskAsset;
import com.hypixel.hytale.builtin.adventure.objectives.task.ObjectiveTask;
import com.hypixel.hytale.builtin.adventure.objectives.task.UseEntityObjectiveTask;
import com.hypixel.hytale.builtin.tagset.config.NPCGroup;
import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.spawning.assets.spawnmarker.config.SpawnMarker;
import com.hypixel.hytale.server.spawning.assets.spawns.config.BeaconNPCSpawn;
import com.hypixel.hytale.server.spawning.beacons.LegacySpawnBeaconEntity;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class NPCObjectivesPlugin extends JavaPlugin {
   protected static NPCObjectivesPlugin instance;
   private ResourceType<EntityStore, KillTrackerResource> killTrackerResourceType;

   public static NPCObjectivesPlugin get() {
      return instance;
   }

   public NPCObjectivesPlugin(@Nonnull JavaPluginInit init) {
      super(init);
   }

   @Override
   protected void setup() {
      instance = this;
      ObjectivePlugin objectivePlugin = ObjectivePlugin.get();
      ComponentRegistryProxy<EntityStore> entityStoreRegistry = this.getEntityStoreRegistry();
      objectivePlugin.registerTask(
         "KillSpawnBeacon",
         KillSpawnBeaconObjectiveTaskAsset.class,
         KillSpawnBeaconObjectiveTaskAsset.CODEC,
         KillSpawnBeaconObjectiveTask.class,
         KillSpawnBeaconObjectiveTask.CODEC,
         KillSpawnBeaconObjectiveTask::new
      );
      objectivePlugin.registerTask(
         "KillSpawnMarker",
         KillSpawnMarkerObjectiveTaskAsset.class,
         KillSpawnMarkerObjectiveTaskAsset.CODEC,
         KillSpawnMarkerObjectiveTask.class,
         KillSpawnMarkerObjectiveTask.CODEC,
         KillSpawnMarkerObjectiveTask::new
      );
      objectivePlugin.registerTask(
         "Bounty",
         BountyObjectiveTaskAsset.class,
         BountyObjectiveTaskAsset.CODEC,
         BountyObjectiveTask.class,
         BountyObjectiveTask.CODEC,
         BountyObjectiveTask::new
      );
      objectivePlugin.registerTask(
         "KillNPC",
         KillObjectiveTaskAsset.class,
         KillObjectiveTaskAsset.CODEC,
         KillNPCObjectiveTask.class,
         KillNPCObjectiveTask.CODEC,
         KillNPCObjectiveTask::new
      );
      this.killTrackerResourceType = entityStoreRegistry.registerResource(KillTrackerResource.class, KillTrackerResource::new);
      ComponentType<EntityStore, LegacySpawnBeaconEntity> legacySpawnBeaconEntityComponentType = LegacySpawnBeaconEntity.getComponentType();
      ComponentType<EntityStore, NPCEntity> npcEntityComponentType = NPCEntity.getComponentType();
      entityStoreRegistry.registerSystem(new SpawnBeaconCheckRemovalSystem(legacySpawnBeaconEntityComponentType));
      entityStoreRegistry.registerSystem(new KillTrackerSystem(npcEntityComponentType, this.killTrackerResourceType));
      NPCPlugin.get()
         .registerCoreComponentType("CompleteTask", BuilderActionCompleteTask::new)
         .registerCoreComponentType("StartObjective", BuilderActionStartObjective::new)
         .registerCoreComponentType("HasTask", BuilderSensorHasTask::new);
      AssetRegistry.getAssetStore(ObjectiveAsset.class).injectLoadsAfter(SpawnMarker.class);
      AssetRegistry.getAssetStore(ObjectiveAsset.class).injectLoadsAfter(BeaconNPCSpawn.class);
      AssetRegistry.getAssetStore(ObjectiveAsset.class).injectLoadsAfter(NPCGroup.class);
   }

   public static boolean hasTask(@Nonnull UUID playerUUID, @Nonnull UUID npcId, @Nonnull String taskId) {
      Map<String, Set<UUID>> entityObjectives = ObjectivePlugin.get().getObjectiveDataStore().getEntityTasksForPlayer(playerUUID);
      return entityObjectives == null ? false : entityObjectives.get(taskId) != null;
   }

   @Nullable
   public static String updateTaskCompletion(
      @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull UUID npcId, @Nonnull String taskId
   ) {
      UUIDComponent uuidComponent = store.getComponent(ref, UUIDComponent.getComponentType());
      if (uuidComponent == null) {
         return null;
      } else {
         ObjectiveDataStore objectiveDataStore = ObjectivePlugin.get().getObjectiveDataStore();
         Map<String, Set<UUID>> entityObjectiveUUIDs = objectiveDataStore.getEntityTasksForPlayer(uuidComponent.getUuid());
         if (entityObjectiveUUIDs == null) {
            return null;
         } else {
            Set<UUID> objectiveUUIDsForTaskId = entityObjectiveUUIDs.get(taskId);
            if (objectiveUUIDsForTaskId == null) {
               return null;
            } else {
               for (UUID objectiveUUID : objectiveUUIDsForTaskId) {
                  Objective objective = objectiveDataStore.getObjective(objectiveUUID);
                  if (objective != null) {
                     ObjectiveTask[] currentTasks = objective.getCurrentTasks();
                     if (currentTasks != null) {
                        for (ObjectiveTask task : currentTasks) {
                           if (task instanceof UseEntityObjectiveTask useEntityTask) {
                              UseEntityObjectiveTaskAsset taskAsset = useEntityTask.getAsset();
                              if (taskAsset.getTaskId().equals(taskId)) {
                                 if (!useEntityTask.increaseTaskCompletion(store, ref, 1, objective, playerRef, npcId)) {
                                    return null;
                                 }

                                 return taskAsset.getAnimationIdToPlay();
                              }
                           }
                        }
                     }
                  }
               }

               return null;
            }
         }
      }
   }

   public static void startObjective(@Nonnull Ref<EntityStore> playerRef, @Nonnull String taskId, @Nonnull Store<EntityStore> store) {
      UUIDComponent uuidComponent = store.getComponent(playerRef, UUIDComponent.getComponentType());
      if (uuidComponent != null) {
         World world = store.getExternalData().getWorld();
         ObjectivePlugin.get().startObjective(taskId, Set.of(uuidComponent.getUuid()), world.getWorldConfig().getUuid(), null, store);
      }
   }

   public ResourceType<EntityStore, KillTrackerResource> getKillTrackerResourceType() {
      return this.killTrackerResourceType;
   }
}
