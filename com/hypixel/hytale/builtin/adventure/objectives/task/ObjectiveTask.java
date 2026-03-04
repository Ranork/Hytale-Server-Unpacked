package com.hypixel.hytale.builtin.adventure.objectives.task;

import com.hypixel.hytale.builtin.adventure.objectives.Objective;
import com.hypixel.hytale.builtin.adventure.objectives.ObjectivePlugin;
import com.hypixel.hytale.builtin.adventure.objectives.config.task.ObjectiveTaskAsset;
import com.hypixel.hytale.builtin.adventure.objectives.config.taskcondition.TaskConditionAsset;
import com.hypixel.hytale.builtin.adventure.objectives.markers.ObjectiveTaskMarker;
import com.hypixel.hytale.builtin.adventure.objectives.transaction.TransactionRecord;
import com.hypixel.hytale.builtin.adventure.objectives.transaction.TransactionUtil;
import com.hypixel.hytale.builtin.adventure.objectives.transaction.WorldTransactionRecord;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.lookup.CodecMapCodec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.event.EventRegistry;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.packets.assets.UpdateObjectiveTask;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.io.NetworkSerializer;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class ObjectiveTask implements NetworkSerializer<Objective, com.hypixel.hytale.protocol.ObjectiveTask> {
   @Nonnull
   public static final CodecMapCodec<ObjectiveTask> CODEC = new CodecMapCodec<>("Type");
   @Nonnull
   public static final BuilderCodec<ObjectiveTask> BASE_CODEC = BuilderCodec.abstractBuilder(ObjectiveTask.class)
      .append(
         new KeyedCodec<>("Task", ObjectiveTaskAsset.CODEC),
         (aObjectiveTask, objectiveTaskAsset) -> aObjectiveTask.asset = objectiveTaskAsset,
         aObjectiveTask -> aObjectiveTask.asset
      )
      .add()
      .append(
         new KeyedCodec<>("Complete", Codec.BOOLEAN),
         (aObjectiveTask, aBoolean) -> aObjectiveTask.complete = aBoolean,
         aObjectiveTask -> aObjectiveTask.complete
      )
      .add()
      .append(
         new KeyedCodec<>("TransactionRecords", new ArrayCodec<>(TransactionRecord.CODEC, TransactionRecord[]::new)),
         (objectiveTask, transactionRecords) -> objectiveTask.serializedTransactionRecords = transactionRecords,
         objectiveTask -> objectiveTask.serializedTransactionRecords
      )
      .add()
      .append(
         new KeyedCodec<>("TaskIndex", Codec.INTEGER), (objectiveTask, integer) -> objectiveTask.taskIndex = integer, objectiveTask -> objectiveTask.taskIndex
      )
      .add()
      .append(
         new KeyedCodec<>("TaskSetIndex", Codec.INTEGER),
         (objectiveTask, integer) -> objectiveTask.taskSetIndex = integer,
         objectiveTask -> objectiveTask.taskSetIndex
      )
      .add()
      .append(
         new KeyedCodec<>("Markers", ObjectiveTaskMarker.ARRAY_CODEC),
         (objectiveTask, markers) -> objectiveTask.markers = new ObjectArrayList(Arrays.asList(markers)),
         objectiveTask -> objectiveTask.markers.toArray(ObjectiveTaskMarker[]::new)
      )
      .add()
      .build();
   protected ObjectiveTaskAsset asset;
   protected boolean complete = false;
   @Nullable
   protected EventRegistry eventRegistry;
   @Nullable
   protected TransactionRecord[] serializedTransactionRecords;
   @Nullable
   protected TransactionRecord[] nonSerializedTransactionRecords;
   protected int taskIndex;
   @Nonnull
   protected List<ObjectiveTaskMarker> markers = new ObjectArrayList();
   protected int taskSetIndex;
   protected ObjectiveTaskRef<? extends ObjectiveTask> taskRef;

   public ObjectiveTask(@Nonnull ObjectiveTaskAsset asset, int taskSetIndex, int taskIndex) {
      this.asset = asset;
      this.taskIndex = taskIndex;
      this.taskSetIndex = taskSetIndex;
   }

   protected ObjectiveTask() {
   }

   @Nonnull
   public ObjectiveTaskAsset getAsset() {
      return this.asset;
   }

   public void setAsset(@Nonnull ObjectiveTaskAsset asset) {
      this.asset = asset;
   }

   public boolean isComplete() {
      return this.complete;
   }

   @Nullable
   public TransactionRecord[] getSerializedTransactionRecords() {
      return this.serializedTransactionRecords;
   }

   @Nullable
   public TransactionRecord[] getNonSerializedTransactionRecords() {
      return this.nonSerializedTransactionRecords;
   }

   @Nonnull
   public List<ObjectiveTaskMarker> getMarkers() {
      return this.markers;
   }

   public void addMarker(@Nonnull ObjectiveTaskMarker marker) {
      this.markers.add(marker);
   }

   public void removeMarker(String id) {
      for (ObjectiveTaskMarker marker : this.markers) {
         if (marker.getId().equals(id)) {
            this.markers.remove(marker);
            return;
         }
      }
   }

   public abstract boolean checkCompletion();

   @Nullable
   protected abstract TransactionRecord[] setup0(@Nonnull Objective var1, @Nonnull World var2, @Nonnull Store<EntityStore> var3);

   @Nullable
   public final TransactionRecord[] setup(@Nonnull Objective objective, @Nonnull Store<EntityStore> store) {
      World world = Universe.get().getWorld(objective.getWorldUUID());
      if (world == null) {
         String transactionMessage = "This World doesn't exist in this Universe: " + objective.getWorldUUID();
         return TransactionRecord.appendFailedTransaction(this.nonSerializedTransactionRecords, new WorldTransactionRecord(), transactionMessage);
      } else if (this.eventRegistry != null) {
         throw new IllegalStateException("ObjectiveTask.eventRegistry is not null, setup() shouldn't be run more than once!");
      } else {
         this.eventRegistry = new EventRegistry(new CopyOnWriteArrayList<>(), () -> true, null, world.getEventRegistry());
         Vector3i[] mapMarkerPositions = this.asset.getMapMarkers();
         if (mapMarkerPositions != null) {
            String objectiveIdStr = objective.getObjectiveUUID().toString();

            for (int i = 0; i < mapMarkerPositions.length; i++) {
               Transform mapMarkerPosition = new Transform(mapMarkerPositions[i]);
               String markerId = "ObjectiveMarker_" + objectiveIdStr + "_" + i;
               this.addMarker(new ObjectiveTaskMarker(markerId, mapMarkerPosition, "Home.png", Message.translation("server.assetTypes.ObjectiveAsset.title")));
            }
         }

         this.taskRef = new ObjectiveTaskRef<>(objective.getObjectiveUUID(), this);
         this.registerTaskRef();
         TransactionRecord[] transactionRecords = this.setup0(objective, world, store);
         if (transactionRecords == null) {
            return null;
         } else {
            int serializedCount = 0;

            for (TransactionRecord transactionRecord : transactionRecords) {
               if (transactionRecord.shouldBeSerialized()) {
                  serializedCount++;
               }
            }

            this.serializedTransactionRecords = new TransactionRecord[serializedCount];
            this.nonSerializedTransactionRecords = new TransactionRecord[transactionRecords.length - serializedCount];
            int serializedIndex = 0;
            int nonSerializedIndex = 0;

            for (TransactionRecord transactionRecordx : transactionRecords) {
               if (transactionRecordx.shouldBeSerialized()) {
                  this.serializedTransactionRecords[serializedIndex++] = transactionRecordx;
               } else {
                  this.nonSerializedTransactionRecords[nonSerializedIndex++] = transactionRecordx;
               }
            }

            return transactionRecords;
         }
      }
   }

   public void complete(@Nonnull Objective objective, @Nullable ComponentAccessor<EntityStore> componentAccessor) {
      if (!this.complete) {
         this.markers.clear();
         this.complete = true;
         this.completeTransactionRecords();
      }
   }

   private void registerTaskRef() {
      ObjectivePlugin.get().getObjectiveDataStore().addTaskRef(this.taskRef);
   }

   private void unregisterTaskRef() {
      ObjectivePlugin.get().getObjectiveDataStore().removeTaskRef(this.taskRef);
   }

   public void completeTransactionRecords() {
      TransactionUtil.completeAll(this.serializedTransactionRecords);
      this.serializedTransactionRecords = null;
      TransactionUtil.completeAll(this.nonSerializedTransactionRecords);
      this.nonSerializedTransactionRecords = null;
      this.shutdownEventRegistry();
      this.unregisterTaskRef();
   }

   public void revertTransactionRecords() {
      TransactionUtil.revertAll(this.serializedTransactionRecords);
      this.serializedTransactionRecords = null;
      TransactionUtil.revertAll(this.nonSerializedTransactionRecords);
      this.nonSerializedTransactionRecords = null;
      this.shutdownEventRegistry();
      this.unregisterTaskRef();
   }

   public void unloadTransactionRecords() {
      TransactionUtil.unloadAll(this.serializedTransactionRecords);
      this.serializedTransactionRecords = null;
      TransactionUtil.unloadAll(this.nonSerializedTransactionRecords);
      this.nonSerializedTransactionRecords = null;
      this.shutdownEventRegistry();
      this.unregisterTaskRef();
   }

   private void shutdownEventRegistry() {
      if (this.eventRegistry != null) {
         this.eventRegistry.shutdownAndCleanup(true);
         this.eventRegistry = null;
      }
   }

   public void assetChanged(@Nonnull Objective objective) {
      if (!this.complete) {
         if (this.checkCompletion()) {
            this.consumeTaskConditions(null, null, objective.getActivePlayerUUIDs());
            this.complete(objective, null);
         }
      }
   }

   public void sendUpdateObjectiveTaskPacket(@Nonnull Objective objective) {
      UpdateObjectiveTask updateObjectiveTaskPacket = new UpdateObjectiveTask(objective.getObjectiveUUID(), this.taskIndex, this.toPacket(objective));
      Universe universe = Universe.get();

      for (UUID playerUUID : objective.getActivePlayerUUIDs()) {
         PlayerRef player = universe.getPlayer(playerUUID);
         if (player != null) {
            player.getPacketHandler().writeNoCache(updateObjectiveTaskPacket);
         }
      }
   }

   public boolean areTaskConditionsFulfilled(
      @Nullable ComponentAccessor<EntityStore> componentAccessor, @Nullable Ref<EntityStore> ref, @Nullable Set<UUID> objectivePlayers
   ) {
      TaskConditionAsset[] taskConditions = this.asset.getTaskConditions();
      if (taskConditions == null) {
         return true;
      } else {
         for (TaskConditionAsset taskCondition : taskConditions) {
            if (!taskCondition.isConditionFulfilled(componentAccessor, ref, objectivePlayers)) {
               return false;
            }
         }

         return true;
      }
   }

   public void consumeTaskConditions(
      @Nullable ComponentAccessor<EntityStore> componentAccessor, @Nullable Ref<EntityStore> ref, @Nonnull Set<UUID> objectivePlayers
   ) {
      TaskConditionAsset[] taskConditions = this.asset.getTaskConditions();
      if (taskConditions != null) {
         for (TaskConditionAsset taskCondition : taskConditions) {
            taskCondition.consumeCondition(componentAccessor, ref, objectivePlayers);
         }
      }
   }

   @Nonnull
   @Override
   public String toString() {
      return "ObjectiveTask{asset="
         + this.asset
         + ", complete="
         + this.complete
         + ", eventRegistry="
         + this.eventRegistry
         + ", serializedTransactionRecords="
         + Arrays.toString((Object[])this.serializedTransactionRecords)
         + ", nonSerializedTransactionRecords="
         + Arrays.toString((Object[])this.nonSerializedTransactionRecords)
         + ", taskIndex="
         + this.taskIndex
         + ", markers="
         + this.markers
         + ", taskSetIndex="
         + this.taskSetIndex
         + "}";
   }
}
