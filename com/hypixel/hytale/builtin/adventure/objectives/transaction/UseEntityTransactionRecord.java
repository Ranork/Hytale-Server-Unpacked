package com.hypixel.hytale.builtin.adventure.objectives.transaction;

import com.hypixel.hytale.builtin.adventure.objectives.ObjectivePlugin;
import java.util.UUID;
import javax.annotation.Nonnull;

public class UseEntityTransactionRecord extends TransactionRecord {
   protected UUID objectiveUUID;
   protected String taskId;

   public UseEntityTransactionRecord(UUID objectiveUUID, String taskId) {
      this.objectiveUUID = objectiveUUID;
      this.taskId = taskId;
   }

   @Override
   public void revert() {
      ObjectivePlugin.get().getObjectiveDataStore().removeEntityTask(this.objectiveUUID, this.taskId);
   }

   @Override
   public void complete() {
      ObjectivePlugin.get().getObjectiveDataStore().removeEntityTask(this.objectiveUUID, this.taskId);
   }

   @Override
   public void unload() {
      ObjectivePlugin.get().getObjectiveDataStore().removeEntityTask(this.objectiveUUID, this.taskId);
   }

   @Override
   public boolean shouldBeSerialized() {
      return false;
   }

   @Nonnull
   @Override
   public String toString() {
      return "UseEntityTransactionRecord{objectiveUUID=" + this.objectiveUUID + ", taskId='" + this.taskId + "'} " + super.toString();
   }
}
