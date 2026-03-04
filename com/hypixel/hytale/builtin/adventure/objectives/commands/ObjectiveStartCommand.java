package com.hypixel.hytale.builtin.adventure.objectives.commands;

import com.hypixel.hytale.builtin.adventure.objectives.Objective;
import com.hypixel.hytale.builtin.adventure.objectives.ObjectivePlugin;
import com.hypixel.hytale.builtin.adventure.objectives.config.ObjectiveAsset;
import com.hypixel.hytale.builtin.adventure.objectives.config.ObjectiveLineAsset;
import com.hypixel.hytale.common.util.StringUtil;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.CommandUtil;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.HashSet;
import java.util.UUID;
import javax.annotation.Nonnull;

public class ObjectiveStartCommand extends AbstractCommandCollection {
   public ObjectiveStartCommand() {
      super("start", "server.commands.objective.start");
      this.addSubCommand(new ObjectiveStartCommand.StartObjectiveCommand());
      this.addSubCommand(new ObjectiveStartCommand.StartObjectiveLineCommand());
   }

   public static class StartObjectiveCommand extends AbstractPlayerCommand {
      @Nonnull
      private final RequiredArg<String> objectiveArg = this.withRequiredArg(
         "objectiveId", "server.commands.objective.start.objective.arg.objectiveId.desc", ArgTypes.STRING
      );

      public StartObjectiveCommand() {
         super("objective", "server.commands.objective.start.objective");
      }

      @Override
      protected void execute(
         @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
      ) {
         String objectiveId = this.objectiveArg.get(context);
         ObjectiveAsset asset = ObjectiveAsset.getAssetMap().getAsset(objectiveId);
         if (asset == null) {
            context.sendMessage(Message.translation("server.commands.objective.objectiveNotFound").param("id", objectiveId));
            context.sendMessage(
               Message.translation("server.general.failed.didYouMean")
                  .param(
                     "choices",
                     StringUtil.sortByFuzzyDistance(objectiveId, ObjectiveAsset.getAssetMap().getAssetMap().keySet(), CommandUtil.RECOMMEND_COUNT).toString()
                  )
            );
         } else {
            HashSet<UUID> playerSet = new HashSet<>();
            playerSet.add(playerRef.getUuid());
            Objective objective = ObjectivePlugin.get().startObjective(objectiveId, playerSet, world.getWorldConfig().getUuid(), null, store);
            if (objective != null) {
               objective.checkTaskSetCompletion(store);
            }
         }
      }
   }

   public static class StartObjectiveLineCommand extends AbstractPlayerCommand {
      @Nonnull
      private final RequiredArg<String> objectiveLineArg = this.withRequiredArg(
         "objectiveLineId", "server.commands.objective.start.objectiveLine.arg.objectiveLineId.desc", ArgTypes.STRING
      );

      public StartObjectiveLineCommand() {
         super("line", "server.commands.objective.start.objectiveLine");
      }

      @Override
      protected void execute(
         @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
      ) {
         String objectiveLineId = this.objectiveLineArg.get(context);
         if (ObjectiveLineAsset.getAssetMap().getAsset(objectiveLineId) == null) {
            context.sendMessage(Message.translation("server.commands.objective.objectiveLineNotFound").param("id", objectiveLineId));
            context.sendMessage(
               Message.translation("server.general.failed.didYouMean")
                  .param(
                     "choices",
                     StringUtil.sortByFuzzyDistance(objectiveLineId, ObjectiveLineAsset.getAssetMap().getAssetMap().keySet(), CommandUtil.RECOMMEND_COUNT)
                        .toString()
                  )
            );
         } else {
            HashSet<UUID> playerSet = new HashSet<>();
            playerSet.add(playerRef.getUuid());
            ObjectivePlugin.get().startObjectiveLine(store, objectiveLineId, playerSet, world.getWorldConfig().getUuid(), null);
         }
      }
   }
}
