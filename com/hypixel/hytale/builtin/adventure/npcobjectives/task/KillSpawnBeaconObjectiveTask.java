package com.hypixel.hytale.builtin.adventure.npcobjectives.task;

import com.hypixel.hytale.builtin.adventure.npcobjectives.assets.KillSpawnBeaconObjectiveTaskAsset;
import com.hypixel.hytale.builtin.adventure.npcobjectives.resources.KillTrackerResource;
import com.hypixel.hytale.builtin.adventure.npcobjectives.transaction.KillTaskTransaction;
import com.hypixel.hytale.builtin.adventure.objectives.Objective;
import com.hypixel.hytale.builtin.adventure.objectives.ObjectivePlugin;
import com.hypixel.hytale.builtin.adventure.objectives.config.worldlocationproviders.WorldLocationProvider;
import com.hypixel.hytale.builtin.adventure.objectives.transaction.SpawnEntityTransactionRecord;
import com.hypixel.hytale.builtin.adventure.objectives.transaction.TransactionRecord;
import com.hypixel.hytale.builtin.adventure.objectives.transaction.TransactionUtil;
import com.hypixel.hytale.builtin.adventure.objectives.transaction.WorldTransactionRecord;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.common.util.ArrayUtil;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.spawning.SpawningPlugin;
import com.hypixel.hytale.server.spawning.assets.spawns.config.BeaconNPCSpawn;
import com.hypixel.hytale.server.spawning.beacons.LegacySpawnBeaconEntity;
import com.hypixel.hytale.server.spawning.wrappers.BeaconSpawnWrapper;
import it.unimi.dsi.fastutil.Pair;
import java.util.Arrays;
import java.util.logging.Level;
import javax.annotation.Nonnull;

public class KillSpawnBeaconObjectiveTask extends KillObjectiveTask {
   @Nonnull
   public static final BuilderCodec<KillSpawnBeaconObjectiveTask> CODEC = BuilderCodec.builder(
         KillSpawnBeaconObjectiveTask.class, KillSpawnBeaconObjectiveTask::new, KillObjectiveTask.CODEC
      )
      .build();

   public KillSpawnBeaconObjectiveTask(@Nonnull KillSpawnBeaconObjectiveTaskAsset asset, int taskSetIndex, int taskIndex) {
      super(asset, taskSetIndex, taskIndex);
   }

   protected KillSpawnBeaconObjectiveTask() {
   }

   @Nonnull
   public KillSpawnBeaconObjectiveTaskAsset getAsset() {
      return (KillSpawnBeaconObjectiveTaskAsset)super.getAsset();
   }

   @Nonnull
   @Override
   protected TransactionRecord[] setup0(@Nonnull Objective objective, @Nonnull World world, @Nonnull Store<EntityStore> store) {
      TransactionRecord[] transactionRecords = this.serializedTransactionRecords;
      if (transactionRecords == null) {
         transactionRecords = this.setupSpawnBeacons(objective, world, store);
         if (TransactionUtil.anyFailed(transactionRecords)) {
            return transactionRecords;
         }
      }

      KillTaskTransaction transaction = new KillTaskTransaction(this, objective, store);
      store.getResource(KillTrackerResource.getResourceType()).watch(transaction);
      return ArrayUtil.append(transactionRecords, transaction);
   }

   @Nonnull
   private TransactionRecord[] setupSpawnBeacons(@Nonnull Objective objective, @Nonnull World world, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      Vector3d position = objective.getPosition(componentAccessor);
      if (position == null) {
         return TransactionRecord.appendFailedTransaction(null, new WorldTransactionRecord(), "No valid position found for the objective.");
      } else {
         KillSpawnBeaconObjectiveTaskAsset.ObjectiveSpawnBeacon[] spawnBeaconConfigs = this.getAsset().getSpawnBeacons();
         TransactionRecord[] transactionRecords = new TransactionRecord[spawnBeaconConfigs.length];
         HytaleLogger logger = ObjectivePlugin.get().getLogger();

         for (int i = 0; i < spawnBeaconConfigs.length; i++) {
            Vector3d spawnPosition = position.clone();
            KillSpawnBeaconObjectiveTaskAsset.ObjectiveSpawnBeacon spawnBeaconConfig = spawnBeaconConfigs[i];
            String spawnBeaconId = spawnBeaconConfig.getSpawnBeaconId();
            int index = BeaconNPCSpawn.getAssetMap().getIndex(spawnBeaconId);
            if (index == Integer.MIN_VALUE) {
               transactionRecords[i] = new WorldTransactionRecord().fail("Failed to find spawn beacon " + spawnBeaconId);
               return Arrays.copyOf(transactionRecords, i + 1);
            }

            Vector3d offset = spawnBeaconConfig.getOffset();
            if (offset != null) {
               spawnPosition.add(offset);
            }

            WorldLocationProvider worldLocationCondition = spawnBeaconConfig.getWorldLocationProvider();
            if (worldLocationCondition != null) {
               Vector3i potentialSpawnLocation = worldLocationCondition.runCondition(world, spawnPosition.toVector3i());
               if (potentialSpawnLocation != null) {
                  spawnPosition = potentialSpawnLocation.toVector3d();
               } else {
                  spawnPosition = null;
               }
            }

            if (spawnPosition == null) {
               transactionRecords[i] = new WorldTransactionRecord().fail("Failed to find a valid position to spawn beacon " + spawnBeaconId);
            } else {
               BeaconSpawnWrapper wrapper = SpawningPlugin.get().getBeaconSpawnWrapper(index);
               Pair<Ref<EntityStore>, LegacySpawnBeaconEntity> spawnBeaconPair = LegacySpawnBeaconEntity.create(
                  wrapper, spawnPosition, Vector3f.FORWARD, componentAccessor
               );
               ((LegacySpawnBeaconEntity)spawnBeaconPair.second()).setObjectiveUUID(objective.getObjectiveUUID());
               UUIDComponent spawnBeaconUuidComponent = componentAccessor.getComponent(
                  (Ref<EntityStore>)spawnBeaconPair.first(), UUIDComponent.getComponentType()
               );
               if (spawnBeaconUuidComponent == null) {
                  transactionRecords[i] = new WorldTransactionRecord().fail("Failed to retrieve UUID component for spawned beacon " + spawnBeaconId);
               } else {
                  logger.at(Level.INFO).log("Spawned SpawnBeacon '" + spawnBeaconId + "' at position: " + position);
                  transactionRecords[i] = new SpawnEntityTransactionRecord(world.getWorldConfig().getUuid(), spawnBeaconUuidComponent.getUuid());
               }
            }
         }

         return transactionRecords;
      }
   }

   @Nonnull
   @Override
   public String toString() {
      return "KillSpawnBeaconObjectiveTask{} " + super.toString();
   }
}
