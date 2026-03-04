package com.hypixel.hytale.builtin.adventure.objectives;

import com.hypixel.hytale.builtin.adventure.objectives.config.ObjectiveAsset;
import com.hypixel.hytale.builtin.adventure.objectives.config.ObjectiveLineAsset;
import com.hypixel.hytale.builtin.adventure.objectives.config.completion.ObjectiveCompletionAsset;
import com.hypixel.hytale.builtin.adventure.objectives.config.task.ObjectiveTaskAsset;
import com.hypixel.hytale.builtin.adventure.objectives.config.task.TaskSet;
import com.hypixel.hytale.builtin.adventure.objectives.historydata.ObjectiveHistoryData;
import com.hypixel.hytale.builtin.adventure.objectives.historydata.ObjectiveLineHistoryData;
import com.hypixel.hytale.builtin.adventure.objectives.task.ObjectiveTask;
import com.hypixel.hytale.builtin.adventure.objectives.transaction.TransactionRecord;
import com.hypixel.hytale.builtin.adventure.objectives.transaction.TransactionUtil;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.function.consumer.TriConsumer;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.protocol.packets.assets.TrackOrUpdateObjective;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.io.NetworkSerializable;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Objective implements NetworkSerializable<com.hypixel.hytale.protocol.Objective> {
   @Nonnull
   public static final BuilderCodec<Objective> CODEC = BuilderCodec.builder(Objective.class, Objective::new)
      .append(new KeyedCodec<>("ObjectiveUUID", Codec.UUID_BINARY), (objective, uuid) -> objective.objectiveUUID = uuid, objective -> objective.objectiveUUID)
      .add()
      .append(new KeyedCodec<>("ObjectiveId", Codec.STRING), (objective, s) -> objective.objectiveId = s, objective -> objective.objectiveId)
      .add()
      .append(
         new KeyedCodec<>("ObjectiveLineData", ObjectiveLineHistoryData.CODEC),
         (objective, objectiveLineData) -> objective.objectiveLineHistoryData = objectiveLineData,
         objective -> objective.objectiveLineHistoryData
      )
      .add()
      .append(
         new KeyedCodec<>("ObjectiveData", ObjectiveHistoryData.CODEC),
         (objective, objectiveHistoryData) -> objective.objectiveHistoryData = objectiveHistoryData,
         objective -> objective.objectiveHistoryData
      )
      .add()
      .append(new KeyedCodec<>("Players", new ArrayCodec<>(Codec.UUID_STRING, UUID[]::new)), (objective, o) -> {
         objective.playerUUIDs = new HashSet<>();
         Collections.addAll(objective.playerUUIDs, o);
      }, objective -> objective.playerUUIDs.toArray(UUID[]::new))
      .add()
      .append(
         new KeyedCodec<>("CurrentTasks", new ArrayCodec<>(ObjectiveTask.CODEC, ObjectiveTask[]::new)),
         (objective, aObjectiveTasks) -> objective.currentTasks = aObjectiveTasks,
         objective -> objective.currentTasks
      )
      .add()
      .append(
         new KeyedCodec<>("CurrentTaskSetIndex", Codec.INTEGER),
         (objective, integer) -> objective.currentTaskSetIndex = integer,
         objective -> objective.currentTaskSetIndex
      )
      .add()
      .append(new KeyedCodec<>("WorldUUID", Codec.UUID_BINARY), (objective, s) -> objective.worldUUID = s, objective -> objective.worldUUID)
      .add()
      .append(
         new KeyedCodec<>("ObjectiveItemStarter", ItemStack.CODEC),
         (objective, itemStack) -> objective.objectiveItemStarter = itemStack,
         objective -> objective.objectiveItemStarter
      )
      .add()
      .build();
   protected UUID objectiveUUID;
   protected String objectiveId;
   @Nullable
   protected ObjectiveLineHistoryData objectiveLineHistoryData;
   protected ObjectiveHistoryData objectiveHistoryData;
   protected Set<UUID> playerUUIDs;
   @Nonnull
   protected Set<UUID> activePlayerUUIDs = ConcurrentHashMap.newKeySet();
   @Nullable
   protected ObjectiveTask[] currentTasks;
   protected int currentTaskSetIndex;
   protected boolean completed;
   protected UUID worldUUID;
   @Nullable
   protected UUID markerUUID;
   protected boolean dirty;
   protected ItemStack objectiveItemStarter;

   public Objective(
      @Nonnull ObjectiveAsset asset, @Nullable UUID objectiveUUID, @Nonnull Set<UUID> playerUUIDs, @Nonnull UUID worldUUID, @Nullable UUID markerUUID
   ) {
      this.objectiveId = asset.getId();
      this.currentTaskSetIndex = 0;
      this.playerUUIDs = playerUUIDs;
      this.worldUUID = worldUUID;
      this.objectiveUUID = objectiveUUID == null ? UUID.randomUUID() : objectiveUUID;
      this.markerUUID = markerUUID;
      this.objectiveHistoryData = new ObjectiveHistoryData(asset.getId(), asset.getCategory());
   }

   protected Objective() {
   }

   @Nonnull
   public UUID getObjectiveUUID() {
      return this.objectiveUUID;
   }

   @Nonnull
   public String getObjectiveId() {
      return this.objectiveId;
   }

   @Nullable
   public ObjectiveAsset getObjectiveAsset() {
      return ObjectiveAsset.getAssetMap().getAsset(this.objectiveId);
   }

   @Nullable
   public ObjectiveLineHistoryData getObjectiveLineHistoryData() {
      return this.objectiveLineHistoryData;
   }

   public void setObjectiveLineHistoryData(@Nullable ObjectiveLineHistoryData objectiveLineHistoryData) {
      this.objectiveLineHistoryData = objectiveLineHistoryData;
   }

   @Nonnull
   public ObjectiveHistoryData getObjectiveHistoryData() {
      return this.objectiveHistoryData;
   }

   @Nullable
   public ObjectiveLineAsset getObjectiveLineAsset() {
      return this.objectiveLineHistoryData == null ? null : ObjectiveLineAsset.getAssetMap().getAsset(this.objectiveLineHistoryData.getId());
   }

   public Set<UUID> getPlayerUUIDs() {
      return this.playerUUIDs;
   }

   @Nonnull
   public Set<UUID> getActivePlayerUUIDs() {
      return this.activePlayerUUIDs;
   }

   @Nullable
   public ObjectiveTask[] getCurrentTasks() {
      return this.currentTasks;
   }

   public int getCurrentTaskSetIndex() {
      return this.currentTaskSetIndex;
   }

   public String getCurrentDescription() {
      ObjectiveAsset objectiveAsset = Objects.requireNonNull(this.getObjectiveAsset());
      TaskSet currentTaskSet = objectiveAsset.getTaskSets()[this.currentTaskSetIndex];
      return currentTaskSet.getDescriptionId() != null
         ? currentTaskSet.getDescriptionKey(this.objectiveId, this.currentTaskSetIndex)
         : objectiveAsset.getDescriptionKey();
   }

   public boolean isCompleted() {
      return this.completed;
   }

   public UUID getWorldUUID() {
      return this.worldUUID;
   }

   @Nullable
   public UUID getMarkerUUID() {
      return this.markerUUID;
   }

   public boolean isDirty() {
      return this.dirty;
   }

   public ItemStack getObjectiveItemStarter() {
      return this.objectiveItemStarter;
   }

   public void setObjectiveItemStarter(@Nonnull ItemStack objectiveItemStarter) {
      this.objectiveItemStarter = objectiveItemStarter;
   }

   public boolean setup(@Nonnull Store<EntityStore> componentAccessor) {
      ObjectiveAsset objectiveAsset = Objects.requireNonNull(this.getObjectiveAsset());
      ObjectivePlugin objectiveModule = ObjectivePlugin.get();
      TaskSet[] taskSets = objectiveAsset.getTaskSets();
      if (this.currentTaskSetIndex >= taskSets.length) {
         objectiveModule.getLogger().at(Level.WARNING).log("Current taskSet index is higher than total number of taskSets for objective %s", this.objectiveId);
         return false;
      } else {
         ObjectiveTaskAsset[] tasks = taskSets[this.currentTaskSetIndex].getTasks();
         ObjectiveTask[] newTasks = new ObjectiveTask[tasks.length];

         for (int i = 0; i < tasks.length; i++) {
            newTasks[i] = objectiveModule.createTask(tasks[i], this.currentTaskSetIndex, i);
         }

         this.currentTasks = newTasks;
         return this.setupCurrentTasks(componentAccessor);
      }
   }

   public boolean setupCurrentTasks(@Nonnull Store<EntityStore> store) {
      if (this.currentTasks == null) {
         return false;
      } else {
         for (ObjectiveTask task : this.currentTasks) {
            if (!task.isComplete()) {
               TransactionRecord[] taskTransactions = task.setup(this, store);
               if (taskTransactions != null && TransactionUtil.anyFailed(taskTransactions)) {
                  ObjectivePlugin.get()
                     .getLogger()
                     .at(Level.WARNING)
                     .log("Failed to setup objective tasks, transaction records:%s", Arrays.toString((Object[])taskTransactions));

                  for (ObjectiveTask taskSetup : this.currentTasks) {
                     taskSetup.revertTransactionRecords();
                     if (taskSetup == task) {
                        break;
                     }
                  }

                  return false;
               }
            }
         }

         return true;
      }
   }

   public boolean checkTaskSetCompletion(@Nonnull Store<EntityStore> store) {
      if (this.currentTasks == null) {
         return false;
      } else {
         for (ObjectiveTask task : this.currentTasks) {
            if (!task.isComplete()) {
               return false;
            }
         }

         this.taskSetComplete(store);
         return true;
      }
   }

   protected void taskSetComplete(@Nonnull Store<EntityStore> store) {
      ObjectiveAsset objectiveAsset = Objects.requireNonNull(this.getObjectiveAsset());
      this.currentTaskSetIndex++;
      TaskSet[] taskSets = objectiveAsset.getTaskSets();
      if (this.currentTaskSetIndex < taskSets.length) {
         if (!this.setup(store)) {
            this.taskSetComplete(store);
         } else {
            TrackOrUpdateObjective trackObjectivePacket = new TrackOrUpdateObjective(this.toPacket());
            this.forEachParticipant((participantReference, trackOrUpdateObjective) -> {
               PlayerRef playerRefComponent = store.getComponent(participantReference, PlayerRef.getComponentType());
               if (playerRefComponent != null) {
                  playerRefComponent.getPacketHandler().writeNoCache(trackOrUpdateObjective);
               }
            }, trackObjectivePacket);
            this.checkTaskSetCompletion(store);
         }
      } else {
         this.complete(store);
      }
   }

   public void complete(@Nonnull Store<EntityStore> store) {
      ObjectiveAsset objectiveAsset = Objects.requireNonNull(this.getObjectiveAsset());
      this.forEachParticipant((participantReference, message) -> {
         PlayerRef playerRefComponent = store.getComponent(participantReference, PlayerRef.getComponentType());
         if (playerRefComponent != null) {
            playerRefComponent.sendMessage(message);
         }
      }, Message.translation("server.modules.objective.completed").param("title", Message.translation(objectiveAsset.getTitleKey())));
      ObjectivePlugin objectiveModule = ObjectivePlugin.get();
      ObjectiveCompletionAsset[] completionHandlerAssets = objectiveAsset.getCompletionHandlers();
      if (completionHandlerAssets != null) {
         for (ObjectiveCompletionAsset objectiveCompletionAsset : completionHandlerAssets) {
            objectiveModule.createCompletion(objectiveCompletionAsset).handle(this, store);
         }
      }

      this.completed = true;
      objectiveModule.objectiveCompleted(this, store);
   }

   public void cancel() {
      if (this.currentTasks != null) {
         for (ObjectiveTask currentTask : this.currentTasks) {
            currentTask.revertTransactionRecords();
         }
      }
   }

   public void unload() {
      if (this.currentTasks != null) {
         for (ObjectiveTask currentTask : this.currentTasks) {
            currentTask.unloadTransactionRecords();
         }
      }
   }

   public void reloadObjectiveAsset(@Nonnull Map<String, ObjectiveAsset> reloadedAssets) {
      ObjectiveTaskAsset[] taskAssets = this.checkPossibleAssetReload(reloadedAssets);
      if (taskAssets != null) {
         World world = Universe.get().getWorld(this.worldUUID);
         if (world != null) {
            world.execute(() -> {
               Store<EntityStore> store = world.getEntityStore().getStore();
               ObjectiveTask[] newTasks = this.setupAndUpdateTasks(taskAssets, store);
               if (newTasks != null) {
                  this.revertRemovedTasks(newTasks);
                  this.currentTasks = newTasks;

                  for (ObjectiveTask currentTask : this.currentTasks) {
                     currentTask.assetChanged(this);
                  }

                  if (!this.checkTaskSetCompletion(store)) {
                     TrackOrUpdateObjective updatePacket = new TrackOrUpdateObjective(this.toPacket());
                     this.forEachParticipant((participantReference, packet) -> {
                        PlayerRef playerRefComponent = store.getComponent(participantReference, PlayerRef.getComponentType());
                        if (playerRefComponent != null) {
                           playerRefComponent.getPacketHandler().writeNoCache(packet);
                        }
                     }, updatePacket);
                  }
               }
            });
         }
      }
   }

   @Nullable
   private ObjectiveTaskAsset[] checkPossibleAssetReload(@Nonnull Map<String, ObjectiveAsset> reloadedAssets) {
      ObjectiveLineAsset objectiveLineAsset = this.getObjectiveLineAsset();
      if (this.objectiveLineHistoryData != null && objectiveLineAsset == null) {
         this.cancel();
         return null;
      } else {
         ObjectiveAsset objectiveAsset = reloadedAssets.get(this.objectiveId);
         if (objectiveAsset == null) {
            return null;
         } else {
            TaskSet[] taskSets = objectiveAsset.getTaskSets();
            if (this.currentTaskSetIndex > taskSets.length) {
               this.cancel();
               return null;
            } else {
               return taskSets[this.currentTaskSetIndex].getTasks();
            }
         }
      }
   }

   @Nullable
   private ObjectiveTask[] setupAndUpdateTasks(@Nonnull ObjectiveTaskAsset[] taskAssets, @Nonnull Store<EntityStore> store) {
      ObjectiveTask[] newTasks = new ObjectiveTask[taskAssets.length];

      for (int i = 0; i < taskAssets.length; i++) {
         ObjectiveTaskAsset taskAsset = taskAssets[i];
         ObjectiveTask objectiveTask = this.findMatchingObjectiveTask(taskAsset);
         if (objectiveTask != null) {
            objectiveTask.setAsset(taskAsset);
            newTasks[i] = objectiveTask;
         } else {
            ObjectiveTask newTask = newTasks[i] = ObjectivePlugin.get().createTask(taskAsset, this.currentTaskSetIndex, i);
            TransactionRecord[] transactionRecords = newTask.setup(this, store);
            if (TransactionUtil.anyFailed(transactionRecords)) {
               this.cancelReload(newTasks);
               return null;
            }
         }
      }

      return newTasks;
   }

   @Nullable
   private ObjectiveTask findMatchingObjectiveTask(@Nonnull ObjectiveTaskAsset taskAsset) {
      if (this.currentTasks == null) {
         return null;
      } else {
         for (ObjectiveTask objectiveTask : this.currentTasks) {
            if (objectiveTask.getAsset().matchesAsset(taskAsset)) {
               return objectiveTask;
            }
         }

         return null;
      }
   }

   private void cancelReload(@Nonnull ObjectiveTask[] newTasks) {
      for (ObjectiveTask taskToRevert : newTasks) {
         if (taskToRevert != null) {
            taskToRevert.revertTransactionRecords();
         }
      }

      this.cancel();
      this.currentTasks = null;
   }

   private void revertRemovedTasks(@Nonnull ObjectiveTask[] newTasks) {
      if (this.currentTasks != null) {
         for (ObjectiveTask objectiveTask : this.currentTasks) {
            boolean foundMatchingTask = false;

            for (ObjectiveTask newTask : newTasks) {
               if (newTask.equals(objectiveTask)) {
                  foundMatchingTask = true;
                  break;
               }
            }

            if (!foundMatchingTask) {
               objectiveTask.revertTransactionRecords();
            }
         }
      }
   }

   public void forEachParticipant(@Nonnull Consumer<Ref<EntityStore>> consumer) {
      for (UUID playerUUID : this.playerUUIDs) {
         PlayerRef playerRef = Universe.get().getPlayer(playerUUID);
         if (playerRef != null) {
            consumer.accept(playerRef.getReference());
         }
      }
   }

   public <T> void forEachParticipant(@Nonnull BiConsumer<Ref<EntityStore>, T> consumer, T meta) {
      for (UUID playerUUID : this.playerUUIDs) {
         PlayerRef playerRef = Universe.get().getPlayer(playerUUID);
         if (playerRef != null) {
            consumer.accept(playerRef.getReference(), meta);
         }
      }
   }

   public <T, U> void forEachParticipant(@Nonnull TriConsumer<Ref<EntityStore>, T, U> consumer, @Nonnull T t, @Nonnull U u) {
      for (UUID playerUUID : this.playerUUIDs) {
         PlayerRef playerRef = Universe.get().getPlayer(playerUUID);
         if (playerRef != null) {
            Ref<EntityStore> ref = playerRef.getReference();
            if (ref != null && ref.isValid()) {
               consumer.accept(playerRef.getReference(), t, u);
            }
         }
      }
   }

   @Nullable
   public Vector3d getPosition(@Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      UUID entityUUIDToFind = null;
      if (this.markerUUID != null) {
         entityUUIDToFind = this.markerUUID;
      } else if (!this.playerUUIDs.isEmpty()) {
         entityUUIDToFind = this.playerUUIDs.iterator().next();
      }

      if (entityUUIDToFind == null) {
         return null;
      } else {
         World world = componentAccessor.getExternalData().getWorld();
         Ref<EntityStore> markerEntityReference = world.getEntityRef(entityUUIDToFind);
         if (markerEntityReference != null && markerEntityReference.isValid()) {
            TransformComponent transformComponent = componentAccessor.getComponent(markerEntityReference, TransformComponent.getComponentType());
            return transformComponent != null ? transformComponent.getPosition() : null;
         } else {
            return null;
         }
      }
   }

   public void addActivePlayerUUID(UUID playerUUID) {
      this.activePlayerUUIDs.add(playerUUID);
   }

   public void removeActivePlayerUUID(UUID playerUUID) {
      this.activePlayerUUIDs.remove(playerUUID);
   }

   public void markDirty() {
      this.dirty = true;
   }

   public boolean consumeDirty() {
      boolean previous = this.dirty;
      this.dirty = false;
      return previous;
   }

   @Nonnull
   public com.hypixel.hytale.protocol.Objective toPacket() {
      ObjectiveAsset objectiveAsset = Objects.requireNonNull(this.getObjectiveAsset());
      com.hypixel.hytale.protocol.Objective packet = new com.hypixel.hytale.protocol.Objective();
      packet.objectiveUuid = this.objectiveUUID;
      packet.objectiveTitleKey = Message.translation(objectiveAsset.getTitleKey()).getFormattedMessage();
      packet.objectiveDescriptionKey = Message.translation(this.getCurrentDescription()).getFormattedMessage();
      if (this.objectiveLineHistoryData != null) {
         packet.objectiveLineId = this.objectiveLineHistoryData.getId();
      }

      packet.tasks = new com.hypixel.hytale.protocol.ObjectiveTask[this.currentTasks.length];

      for (int i = 0; i < this.currentTasks.length; i++) {
         packet.tasks[i] = this.currentTasks[i].toPacket(this);
      }

      return packet;
   }

   @Nonnull
   @Override
   public String toString() {
      return "Objective{objectiveUUID="
         + this.objectiveUUID
         + ", objectiveId='"
         + this.objectiveId
         + "', objectiveLineHistoryData="
         + this.objectiveLineHistoryData
         + ", objectiveHistoryData="
         + this.objectiveHistoryData
         + ", playerUUIDs="
         + this.playerUUIDs
         + ", activePlayerUUIDs="
         + this.activePlayerUUIDs
         + ", currentTasks="
         + Arrays.toString((Object[])this.currentTasks)
         + ", currentTaskSetIndex="
         + this.currentTaskSetIndex
         + ", completed="
         + this.completed
         + ", worldUUID="
         + this.worldUUID
         + ", markerUUID="
         + this.markerUUID
         + ", dirty="
         + this.dirty
         + "}";
   }
}
