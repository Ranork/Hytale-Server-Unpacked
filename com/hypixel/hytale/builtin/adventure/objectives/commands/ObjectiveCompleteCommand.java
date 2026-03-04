package com.hypixel.hytale.builtin.adventure.objectives.commands;

import com.hypixel.hytale.builtin.adventure.objectives.Objective;
import com.hypixel.hytale.builtin.adventure.objectives.ObjectiveDataStore;
import com.hypixel.hytale.builtin.adventure.objectives.ObjectivePlugin;
import com.hypixel.hytale.builtin.adventure.objectives.task.ObjectiveTask;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ObjectiveCompleteCommand extends AbstractCommandCollection {
   @Nonnull
   private static final Message MESSAGE_COMMANDS_OBJECTIVE_OBJECTIVE_NOT_FOUND = Message.translation("server.commands.objective.objectiveNotFound");
   @Nonnull
   private static final Message MESSAGE_COMMANDS_OBJECTIVE_NO_TASK_FOR_INDEX = Message.translation("server.commands.objective.noTaskForIndex");
   @Nonnull
   private static final Message MESSAGE_COMMANDS_OBJECTIVE_TASK_ALREADY_COMPLETED = Message.translation("server.commands.objective.taskAlreadyCompleted");

   public ObjectiveCompleteCommand() {
      super("complete", "server.commands.objective.complete");
      this.addSubCommand(new ObjectiveCompleteCommand.CompleteTaskCommand());
      this.addSubCommand(new ObjectiveCompleteCommand.CompleteTaskSetCommand());
      this.addSubCommand(new ObjectiveCompleteCommand.CompleteObjectiveCommand());
   }

   @Nullable
   private static Objective getObjectiveFromId(
      @Nonnull Ref<EntityStore> participantRef, @Nonnull String objectiveId, @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      Player playerComponent = componentAccessor.getComponent(participantRef, Player.getComponentType());
      if (playerComponent == null) {
         return null;
      } else {
         Set<UUID> activeObjectiveUUIDs = playerComponent.getPlayerConfigData().getActiveObjectiveUUIDs();
         ObjectiveDataStore objectiveDataStore = ObjectivePlugin.get().getObjectiveDataStore();

         for (UUID objectiveUUID : activeObjectiveUUIDs) {
            Objective objective = objectiveDataStore.getObjective(objectiveUUID);
            if (objective != null && objective.getObjectiveId().equals(objectiveId)) {
               return objective;
            }
         }

         return null;
      }
   }

   public static class CompleteObjectiveCommand extends AbstractPlayerCommand {
      @Nonnull
      private final RequiredArg<String> objectiveArg = this.withRequiredArg(
         "objectiveId", "server.commands.objective.complete.objective.arg.objectiveId.desc", ArgTypes.STRING
      );

      public CompleteObjectiveCommand() {
         super("objective", "server.commands.objective.complete.objective");
      }

      @Override
      protected void execute(
         @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
      ) {
         String objectiveId = this.objectiveArg.get(context);
         Objective objective = ObjectiveCompleteCommand.getObjectiveFromId(ref, objectiveId, store);
         if (objective == null) {
            context.sendMessage(ObjectiveCompleteCommand.MESSAGE_COMMANDS_OBJECTIVE_OBJECTIVE_NOT_FOUND.param("id", objectiveId));
         } else {
            ObjectiveTask[] tasks = objective.getCurrentTasks();
            if (tasks == null) {
               context.sendMessage(ObjectiveCompleteCommand.MESSAGE_COMMANDS_OBJECTIVE_NO_TASK_FOR_INDEX);
            } else {
               for (ObjectiveTask task : tasks) {
                  task.completeTransactionRecords();
               }

               objective.complete(store);
            }
         }
      }
   }

   public static class CompleteTaskCommand extends AbstractPlayerCommand {
      @Nonnull
      private final RequiredArg<String> objectiveArg = this.withRequiredArg(
         "objectiveId", "server.commands.objective.complete.task.arg.objectiveId.desc", ArgTypes.STRING
      );
      @Nonnull
      private final RequiredArg<Integer> taskIndexArg = this.withRequiredArg(
         "taskIndex", "server.commands.objective.complete.task.arg.taskIndex.desc", ArgTypes.INTEGER
      );

      public CompleteTaskCommand() {
         super("task", "server.commands.objective.complete.task");
      }

      @Override
      protected void execute(
         @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
      ) {
         String objectiveId = this.objectiveArg.get(context);
         int taskIndex = this.taskIndexArg.get(context);
         Objective objective = ObjectiveCompleteCommand.getObjectiveFromId(ref, objectiveId, store);
         if (objective == null) {
            context.sendMessage(ObjectiveCompleteCommand.MESSAGE_COMMANDS_OBJECTIVE_OBJECTIVE_NOT_FOUND.param("id", objectiveId));
         } else {
            ObjectiveTask[] tasks = objective.getCurrentTasks();
            if (taskIndex >= tasks.length) {
               context.sendMessage(ObjectiveCompleteCommand.MESSAGE_COMMANDS_OBJECTIVE_NO_TASK_FOR_INDEX);
            } else if (tasks[taskIndex].isComplete()) {
               context.sendMessage(ObjectiveCompleteCommand.MESSAGE_COMMANDS_OBJECTIVE_TASK_ALREADY_COMPLETED);
            } else {
               tasks[taskIndex].complete(objective, store);
               objective.checkTaskSetCompletion(store);
            }
         }
      }
   }

   public static class CompleteTaskSetCommand extends AbstractPlayerCommand {
      @Nonnull
      private final RequiredArg<String> objectiveArg = this.withRequiredArg(
         "objectiveId", "server.commands.objective.complete.taskSet.arg.objectiveId.desc", ArgTypes.STRING
      );

      public CompleteTaskSetCommand() {
         super("taskSet", "server.commands.objective.complete.taskSet");
      }

      @Override
      protected void execute(
         @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
      ) {
         String objectiveId = this.objectiveArg.get(context);
         Objective objective = ObjectiveCompleteCommand.getObjectiveFromId(ref, objectiveId, store);
         if (objective == null) {
            context.sendMessage(ObjectiveCompleteCommand.MESSAGE_COMMANDS_OBJECTIVE_OBJECTIVE_NOT_FOUND.param("id", objectiveId));
         } else {
            ObjectiveTask[] tasks = objective.getCurrentTasks();
            if (tasks != null && tasks.length != 0) {
               for (ObjectiveTask task : tasks) {
                  task.complete(objective, store);
               }

               objective.checkTaskSetCompletion(store);
            } else {
               context.sendMessage(ObjectiveCompleteCommand.MESSAGE_COMMANDS_OBJECTIVE_NO_TASK_FOR_INDEX);
            }
         }
      }
   }
}
