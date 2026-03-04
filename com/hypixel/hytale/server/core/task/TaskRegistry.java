package com.hypixel.hytale.server.core.task;

import com.hypixel.hytale.function.consumer.BooleanConsumer;
import com.hypixel.hytale.registry.Registry;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledFuture;
import java.util.function.BooleanSupplier;
import javax.annotation.Nonnull;

public class TaskRegistry extends Registry<TaskRegistration> {
   public TaskRegistry(@Nonnull List<BooleanConsumer> registrations, BooleanSupplier precondition, String preconditionMessage) {
      super(registrations, precondition, preconditionMessage, TaskRegistration::new);
   }

   public TaskRegistration registerTask(@Nonnull CompletableFuture<Void> task) {
      return this.register(new TaskRegistration(task));
   }

   public TaskRegistration registerTask(@Nonnull ScheduledFuture<Void> task) {
      return this.register(new TaskRegistration(task));
   }
}
