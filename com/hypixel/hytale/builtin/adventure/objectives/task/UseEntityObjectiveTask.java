package com.hypixel.hytale.builtin.adventure.objectives.task;

import com.hypixel.hytale.builtin.adventure.objectives.DialogPage;
import com.hypixel.hytale.builtin.adventure.objectives.Objective;
import com.hypixel.hytale.builtin.adventure.objectives.ObjectiveDataStore;
import com.hypixel.hytale.builtin.adventure.objectives.ObjectivePlugin;
import com.hypixel.hytale.builtin.adventure.objectives.config.task.UseEntityObjectiveTaskAsset;
import com.hypixel.hytale.builtin.adventure.objectives.transaction.TransactionRecord;
import com.hypixel.hytale.builtin.adventure.objectives.transaction.UseEntityTransactionRecord;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nonnull;

public class UseEntityObjectiveTask extends CountObjectiveTask {
   @Nonnull
   public static final BuilderCodec<UseEntityObjectiveTask> CODEC = BuilderCodec.builder(
         UseEntityObjectiveTask.class, UseEntityObjectiveTask::new, CountObjectiveTask.CODEC
      )
      .append(new KeyedCodec<>("NpcUUIDs", new ArrayCodec<>(Codec.UUID_BINARY, UUID[]::new)), (useEntityObjectiveTask, uuids) -> {
         useEntityObjectiveTask.npcUUIDs.clear();
         Collections.addAll(useEntityObjectiveTask.npcUUIDs, uuids);
      }, useEntityObjectiveTask -> useEntityObjectiveTask.npcUUIDs.toArray(UUID[]::new))
      .add()
      .build();
   @Nonnull
   private static final Message MESSAGE_SERVER_MODULES_OBJECTIVE_TASK_ALREADY_INTERACTED_WITH_NPC = Message.translation(
      "server.modules.objective.task.alreadyInteractedWithNPC"
   );
   @Nonnull
   protected Set<UUID> npcUUIDs = new HashSet<>();

   public UseEntityObjectiveTask(@Nonnull UseEntityObjectiveTaskAsset asset, int taskSetIndex, int taskIndex) {
      super(asset, taskSetIndex, taskIndex);
   }

   protected UseEntityObjectiveTask() {
   }

   @Nonnull
   public UseEntityObjectiveTaskAsset getAsset() {
      return (UseEntityObjectiveTaskAsset)super.getAsset();
   }

   @Nonnull
   @Override
   protected TransactionRecord[] setup0(@Nonnull Objective objective, @Nonnull World world, @Nonnull Store<EntityStore> store) {
      UUID objectiveUUID = objective.getObjectiveUUID();
      ObjectiveDataStore objectiveDataStore = ObjectivePlugin.get().getObjectiveDataStore();
      String taskId = this.getAsset().getTaskId();

      for (UUID playerUUID : objective.getActivePlayerUUIDs()) {
         objectiveDataStore.addEntityTaskForPlayer(playerUUID, taskId, objectiveUUID);
      }

      return TransactionRecord.appendTransaction(null, new UseEntityTransactionRecord(objectiveUUID, taskId));
   }

   public boolean increaseTaskCompletion(
      @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, int qty, @Nonnull Objective objective, @Nonnull PlayerRef playerRef, UUID npcUUID
   ) {
      if (!this.npcUUIDs.add(npcUUID)) {
         playerRef.sendMessage(MESSAGE_SERVER_MODULES_OBJECTIVE_TASK_ALREADY_INTERACTED_WITH_NPC);
         return false;
      } else {
         super.increaseTaskCompletion(store, ref, qty, objective);
         if (this.isComplete()) {
            UseEntityObjectiveTaskAsset.DialogOptions dialogOptions = this.getAsset().getDialogOptions();
            if (dialogOptions != null) {
               Player playerComponent = store.getComponent(ref, Player.getComponentType());

               assert playerComponent != null;

               playerRef.sendMessage(
                  Message.join(Message.translation(dialogOptions.getEntityNameKey()), Message.raw(": "), Message.translation(dialogOptions.getDialogKey()))
               );
               playerComponent.getPageManager().openCustomPage(ref, store, new DialogPage(playerRef, dialogOptions));
            }
         }

         return true;
      }
   }

   @Nonnull
   @Override
   public String toString() {
      return "UseEntityObjectiveTask{npcUUIDs=" + this.npcUUIDs + "} " + super.toString();
   }
}
