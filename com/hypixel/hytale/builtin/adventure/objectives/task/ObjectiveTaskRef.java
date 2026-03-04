package com.hypixel.hytale.builtin.adventure.objectives.task;

import java.util.UUID;

public class ObjectiveTaskRef<T extends ObjectiveTask> {
   private final UUID objectiveUUID;
   private final T objectiveTask;

   public ObjectiveTaskRef(UUID objectiveUUID, T objectiveTask) {
      this.objectiveUUID = objectiveUUID;
      this.objectiveTask = objectiveTask;
   }

   public UUID getObjectiveUUID() {
      return this.objectiveUUID;
   }

   public T getObjectiveTask() {
      return this.objectiveTask;
   }
}
