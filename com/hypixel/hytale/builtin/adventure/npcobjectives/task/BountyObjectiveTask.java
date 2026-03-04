package com.hypixel.hytale.builtin.adventure.npcobjectives.task;

import com.hypixel.hytale.builtin.adventure.npcobjectives.assets.BountyObjectiveTaskAsset;
import com.hypixel.hytale.builtin.adventure.npcobjectives.resources.KillTrackerResource;
import com.hypixel.hytale.builtin.adventure.npcobjectives.transaction.KillTaskTransaction;
import com.hypixel.hytale.builtin.adventure.objectives.Objective;
import com.hypixel.hytale.builtin.adventure.objectives.ObjectivePlugin;
import com.hypixel.hytale.builtin.adventure.objectives.config.task.ObjectiveTaskAsset;
import com.hypixel.hytale.builtin.adventure.objectives.markers.ObjectiveTaskMarker;
import com.hypixel.hytale.builtin.adventure.objectives.task.ObjectiveTask;
import com.hypixel.hytale.builtin.adventure.objectives.transaction.RegistrationTransactionRecord;
import com.hypixel.hytale.builtin.adventure.objectives.transaction.SpawnEntityTransactionRecord;
import com.hypixel.hytale.builtin.adventure.objectives.transaction.TransactionRecord;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.npc.INonPlayerCharacter;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import it.unimi.dsi.fastutil.Pair;
import java.util.UUID;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BountyObjectiveTask extends ObjectiveTask implements KillTask {
   @Nonnull
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   @Nonnull
   public static final BuilderCodec<BountyObjectiveTask> CODEC = BuilderCodec.builder(
         BountyObjectiveTask.class, BountyObjectiveTask::new, ObjectiveTask.BASE_CODEC
      )
      .append(
         new KeyedCodec<>("Completed", Codec.BOOLEAN),
         (bountyObjectiveTask, aBoolean) -> bountyObjectiveTask.completed = aBoolean,
         bountyObjectiveTask -> bountyObjectiveTask.completed
      )
      .add()
      .append(
         new KeyedCodec<>("EntityUUID", Codec.UUID_BINARY),
         (bountyObjectiveTask, uuid) -> bountyObjectiveTask.entityUuid = uuid,
         bountyObjectiveTask -> bountyObjectiveTask.entityUuid
      )
      .add()
      .build();
   boolean completed;
   UUID entityUuid;

   public BountyObjectiveTask(@Nonnull ObjectiveTaskAsset asset, int taskSetIndex, int taskIndex) {
      super(asset, taskSetIndex, taskIndex);
   }

   protected BountyObjectiveTask() {
   }

   @Nonnull
   public BountyObjectiveTaskAsset getAsset() {
      return (BountyObjectiveTaskAsset)super.getAsset();
   }

   @Nullable
   @Override
   protected TransactionRecord[] setup0(@Nonnull Objective objective, @Nonnull World world, @Nonnull Store<EntityStore> store) {
      if (this.serializedTransactionRecords != null) {
         return RegistrationTransactionRecord.append(this.serializedTransactionRecords, this.eventRegistry);
      } else {
         Vector3d objectivePosition = objective.getPosition(store);
         if (objectivePosition == null) {
            return null;
         } else {
            Vector3i spawnPosition = this.getAsset().getWorldLocationProvider().runCondition(world, objectivePosition.clone().floor().toVector3i());
            if (spawnPosition == null) {
               return null;
            } else {
               TransactionRecord[] transactionRecords = new TransactionRecord[2];
               String npcId = this.getAsset().getNpcId();
               Pair<Ref<EntityStore>, INonPlayerCharacter> npcPair = NPCPlugin.get().spawnNPC(store, npcId, null, spawnPosition.toVector3d(), Vector3f.ZERO);
               if (npcPair == null) {
                  return null;
               } else {
                  Ref<EntityStore> npcReference = (Ref<EntityStore>)npcPair.first();
                  UUIDComponent npcUuidComponent = store.getComponent(npcReference, UUIDComponent.getComponentType());
                  if (npcUuidComponent == null) {
                     return null;
                  } else {
                     UUID npcUuid = npcUuidComponent.getUuid();
                     ObjectivePlugin.get().getLogger().at(Level.INFO).log("Spawned Entity '" + npcId + "' at position: " + spawnPosition);
                     transactionRecords[0] = new SpawnEntityTransactionRecord(world.getWorldConfig().getUuid(), npcUuid);
                     this.entityUuid = npcUuid;
                     ObjectiveTaskMarker marker = new ObjectiveTaskMarker(
                        getBountyMarkerIDFromUUID(npcUuid), new Transform(spawnPosition), "Home.png", Message.translation("server.objectives.bounty.marker")
                     );
                     this.addMarker(marker);
                     KillTaskTransaction transaction = new KillTaskTransaction(this, objective, store);
                     store.getResource(KillTrackerResource.getResourceType()).watch(transaction);
                     transactionRecords[1] = transaction;
                     return transactionRecords;
                  }
               }
            }
         }
      }
   }

   @Override
   public boolean checkCompletion() {
      return this.completed;
   }

   @Nonnull
   public static String getBountyMarkerIDFromUUID(@Nonnull UUID uuid) {
      return "Bounty_" + uuid;
   }

   @Override
   public void checkKilledEntity(
      @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> npcRef, @Nonnull Objective objective, @Nonnull NPCEntity npc, @Nonnull Damage damageInfo
   ) {
      UUIDComponent uuidComponent = store.getComponent(npcRef, UUIDComponent.getComponentType());

      assert uuidComponent != null;

      UUID uuid = uuidComponent.getUuid();
      if (this.entityUuid.equals(uuid)) {
         this.completed = true;
         this.consumeTaskConditions(store, npcRef, objective.getPlayerUUIDs());
         this.complete(objective, store);
         objective.checkTaskSetCompletion(store);
         this.removeMarker(getBountyMarkerIDFromUUID(uuid));
      }
   }

   @Nonnull
   public com.hypixel.hytale.protocol.ObjectiveTask toPacket(@Nonnull Objective objective) {
      com.hypixel.hytale.protocol.ObjectiveTask packet = new com.hypixel.hytale.protocol.ObjectiveTask();
      packet.taskDescriptionKey = Message.translation(this.asset.getDescriptionKey(objective.getObjectiveId(), this.taskSetIndex, this.taskIndex))
         .getFormattedMessage();
      packet.currentCompletion = this.completed ? 1 : 0;
      packet.completionNeeded = 1;
      return packet;
   }

   @Nonnull
   @Override
   public String toString() {
      return "BountyObjectiveTask{completed=" + this.completed + ", entityUuid=" + this.entityUuid + "} " + super.toString();
   }
}
