package com.hypixel.hytale.builtin.adventure.objectives.task;

import com.hypixel.hytale.builtin.adventure.objectives.Objective;
import com.hypixel.hytale.builtin.adventure.objectives.ObjectivePlugin;
import com.hypixel.hytale.builtin.adventure.objectives.blockstates.TreasureChestState;
import com.hypixel.hytale.builtin.adventure.objectives.config.task.TreasureMapObjectiveTaskAsset;
import com.hypixel.hytale.builtin.adventure.objectives.events.TreasureChestOpeningEvent;
import com.hypixel.hytale.builtin.adventure.objectives.markers.ObjectiveTaskMarker;
import com.hypixel.hytale.builtin.adventure.objectives.transaction.RegistrationTransactionRecord;
import com.hypixel.hytale.builtin.adventure.objectives.transaction.SpawnTreasureChestTransactionRecord;
import com.hypixel.hytale.builtin.adventure.objectives.transaction.TransactionRecord;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.util.TrigMathUtil;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.item.ItemModule;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.meta.BlockState;
import com.hypixel.hytale.server.core.universe.world.meta.BlockStateModule;
import com.hypixel.hytale.server.core.universe.world.meta.state.ItemContainerState;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TreasureMapObjectiveTask extends ObjectiveTask {
   @Nonnull
   public static final BuilderCodec<TreasureMapObjectiveTask> CODEC = BuilderCodec.builder(
         TreasureMapObjectiveTask.class, TreasureMapObjectiveTask::new, BASE_CODEC
      )
      .append(
         new KeyedCodec<>("CurrentCompletion", Codec.INTEGER),
         (treasureMapObjectiveTask, integer) -> treasureMapObjectiveTask.currentCompletion = integer,
         treasureMapObjectiveTask -> treasureMapObjectiveTask.currentCompletion
      )
      .add()
      .append(
         new KeyedCodec<>("ChestCount", Codec.INTEGER),
         (treasureMapObjectiveTask, integer) -> treasureMapObjectiveTask.chestCount = integer,
         treasureMapObjectiveTask -> treasureMapObjectiveTask.chestCount
      )
      .add()
      .append(new KeyedCodec<>("ChestUUIDs", new ArrayCodec<>(Codec.UUID_BINARY, UUID[]::new)), (treasureMapObjectiveTask, uuids) -> {
         treasureMapObjectiveTask.chestUUIDs.clear();
         Collections.addAll(treasureMapObjectiveTask.chestUUIDs, uuids);
      }, treasureMapObjectiveTask -> treasureMapObjectiveTask.chestUUIDs.toArray(UUID[]::new))
      .add()
      .build();
   public static final int CHEST_SPAWN_TRY = 500;
   private int currentCompletion;
   private int chestCount;
   @Nonnull
   private final List<UUID> chestUUIDs = new ObjectArrayList();

   public TreasureMapObjectiveTask(@Nonnull TreasureMapObjectiveTaskAsset asset, int taskSetIndex, int taskIndex) {
      super(asset, taskSetIndex, taskIndex);
   }

   protected TreasureMapObjectiveTask() {
   }

   @Nonnull
   public TreasureMapObjectiveTaskAsset getAsset() {
      return (TreasureMapObjectiveTaskAsset)super.getAsset();
   }

   @Nonnull
   public String getChestMarkerIDFromUUID(@Nonnull UUID uuid) {
      return "TreasureChest_" + uuid.toString();
   }

   @Nonnull
   @Override
   protected TransactionRecord[] setup0(@Nonnull Objective objective, @Nonnull World world, @Nonnull Store<EntityStore> store) {
      this.eventRegistry.register(TreasureChestOpeningEvent.class, world.getName(), event -> this.onTreasureChestOpeningEvent(objective, event));
      TransactionRecord[] transactionRecords = this.serializedTransactionRecords;
      if (transactionRecords != null) {
         return RegistrationTransactionRecord.append(transactionRecords, this.eventRegistry);
      } else {
         TreasureMapObjectiveTaskAsset.ChestConfig[] chestConfigs = this.getAsset().getChestConfigs();
         transactionRecords = new TransactionRecord[chestConfigs.length];
         this.chestCount = chestConfigs.length;

         for (int i = 0; i < chestConfigs.length; i++) {
            transactionRecords[i] = this.spawnChest(objective, world, chestConfigs[i], store);
         }

         return RegistrationTransactionRecord.append(transactionRecords, this.eventRegistry);
      }
   }

   @Override
   public boolean checkCompletion() {
      return this.currentCompletion >= this.chestCount;
   }

   private void onTreasureChestOpeningEvent(@Nonnull Objective objective, @Nonnull TreasureChestOpeningEvent event) {
      UUID chestUUID = event.getChestUUID();
      if (this.chestUUIDs.contains(chestUUID)) {
         this.currentCompletion++;
         objective.markDirty();
         this.sendUpdateObjectiveTaskPacket(objective);
         String chestMarkerID = this.getChestMarkerIDFromUUID(chestUUID);
         this.removeMarker(chestMarkerID);
         Ref<EntityStore> playerRef = event.getPlayerRef();
         Store<EntityStore> store = event.getStore();
         if (this.checkCompletion()) {
            this.consumeTaskConditions(store, playerRef, objective.getActivePlayerUUIDs());
            this.complete(objective, store);
            objective.checkTaskSetCompletion(store);
         }
      }
   }

   @Nonnull
   private TransactionRecord spawnChest(
      @Nonnull Objective objective,
      @Nonnull World world,
      @Nonnull TreasureMapObjectiveTaskAsset.ChestConfig chestConfig,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      Vector3i conditionPosition = this.calculateChestSpawnPosition(chestConfig, objective, world, componentAccessor);
      SpawnTreasureChestTransactionRecord transactionRecord = new SpawnTreasureChestTransactionRecord(world.getWorldConfig().getUuid(), conditionPosition);
      if (conditionPosition == null) {
         return transactionRecord.fail("Position not safe to spawn chest");
      } else {
         TreasureChestState treasureChestState = this.spawnChestBlock(world, conditionPosition, chestConfig.getChestBlockTypeKey(), transactionRecord);
         if (treasureChestState == null) {
            return transactionRecord;
         } else {
            UUID chestUUID = UUID.randomUUID();
            List<ItemStack> stacks = ItemModule.get().getRandomItemDrops(chestConfig.getDroplistId());
            treasureChestState.setObjectiveData(objective.getObjectiveUUID(), chestUUID, stacks);
            this.chestUUIDs.add(chestUUID);
            treasureChestState.getChunk().setState(conditionPosition.getX(), conditionPosition.getY(), conditionPosition.getZ(), treasureChestState);
            ObjectivePlugin.get().getLogger().at(Level.INFO).log("Spawned chest at: " + conditionPosition);
            ObjectiveTaskMarker marker = new ObjectiveTaskMarker(
               this.getChestMarkerIDFromUUID(chestUUID), new Transform(conditionPosition), "Home.png", Message.translation("server.objectives.treasure.marker")
            );
            this.addMarker(marker);
            return transactionRecord;
         }
      }
   }

   @Nullable
   private TreasureChestState spawnChestBlock(
      @Nonnull World world, @Nonnull Vector3i conditionPosition, String chestBlockTypeKey, @Nonnull SpawnTreasureChestTransactionRecord transactionRecord
   ) {
      long chunkIndex = ChunkUtil.indexChunkFromBlock(conditionPosition.x, conditionPosition.z);
      WorldChunk worldChunk = world.getChunk(chunkIndex);
      if (worldChunk == null) {
         return null;
      } else {
         worldChunk.setBlock(conditionPosition.x, conditionPosition.y, conditionPosition.z, chestBlockTypeKey);
         BlockState blockState = worldChunk.getState(conditionPosition.x, conditionPosition.y, conditionPosition.z);
         if (!(blockState instanceof ItemContainerState)) {
            transactionRecord.fail("BlockState is not a container");
            return null;
         } else {
            TreasureChestState treasureChestState = BlockStateModule.get()
               .createBlockState(TreasureChestState.class, worldChunk, conditionPosition.clone(), blockState.getBlockType());
            if (treasureChestState == null) {
               transactionRecord.fail("Failed to create TreasureChestState!");
               return null;
            } else {
               return treasureChestState;
            }
         }
      }
   }

   @Nullable
   private Vector3i calculateChestSpawnPosition(
      @Nonnull TreasureMapObjectiveTaskAsset.ChestConfig chestConfig,
      @Nonnull Objective objective,
      @Nonnull World world,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      int currentTry = 0;

      Vector3i conditionPosition;
      for (conditionPosition = null; currentTry < 500 && conditionPosition == null; currentTry++) {
         double angle = Math.random() * (float) (Math.PI * 2);
         float radius = MathUtil.randomFloat(chestConfig.getMinRadius(), chestConfig.getMaxRadius());
         Vector3d objectivePosition = objective.getPosition(componentAccessor);
         Vector3d position = objectivePosition.clone().floor();
         position.add(radius * TrigMathUtil.cos(angle), 0.0, radius * TrigMathUtil.sin(angle));
         position.y = world.getChunk(ChunkUtil.indexChunkFromBlock(position.x, position.z)).getHeight(MathUtil.floor(position.x), MathUtil.floor(position.z));
         conditionPosition = chestConfig.getWorldLocationProvider().runCondition(world, position.toVector3i());
      }

      return conditionPosition;
   }

   @Nonnull
   public com.hypixel.hytale.protocol.ObjectiveTask toPacket(@Nonnull Objective objective) {
      com.hypixel.hytale.protocol.ObjectiveTask packet = new com.hypixel.hytale.protocol.ObjectiveTask();
      packet.taskDescriptionKey = Message.translation(this.asset.getDescriptionKey(objective.getObjectiveId(), this.taskSetIndex, this.taskIndex))
         .getFormattedMessage();
      packet.currentCompletion = this.currentCompletion;
      packet.completionNeeded = this.chestCount;
      return packet;
   }

   @Nonnull
   @Override
   public String toString() {
      return "TreasureMapObjectiveTask{currentCompletion="
         + this.currentCompletion
         + ", chestCount="
         + this.chestCount
         + ", chestUUIDs="
         + this.chestUUIDs
         + "} "
         + super.toString();
   }
}
