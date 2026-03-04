package com.hypixel.hytale.builtin.adventure.objectives.commands;

import com.hypixel.hytale.builtin.adventure.objectives.ObjectivePlugin;
import com.hypixel.hytale.builtin.adventure.objectives.components.ObjectiveHistoryComponent;
import com.hypixel.hytale.builtin.adventure.objectives.historydata.ObjectiveHistoryData;
import com.hypixel.hytale.builtin.adventure.objectives.historydata.ObjectiveLineHistoryData;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Map;
import javax.annotation.Nonnull;

public class ObjectiveHistoryCommand extends AbstractPlayerCommand {
   public ObjectiveHistoryCommand() {
      super("history", "server.commands.objective.history");
   }

   @Override
   protected void execute(
      @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
   ) {
      StringBuilder sb = new StringBuilder("Completed objectives\n");
      ObjectiveHistoryComponent objectiveHistoryComponent = store.getComponent(ref, ObjectivePlugin.get().getObjectiveHistoryComponentType());

      assert objectiveHistoryComponent != null;

      Map<String, ObjectiveHistoryData> objectiveDataMap = objectiveHistoryComponent.getObjectiveHistoryMap();

      for (ObjectiveHistoryData objectiveHistory : objectiveDataMap.values()) {
         sb.append(objectiveHistory).append("\n");
      }

      sb.append("\nCompleted objective lines\n");
      Map<String, ObjectiveLineHistoryData> objectiveLineDataMap = objectiveHistoryComponent.getObjectiveLineHistoryMap();

      for (ObjectiveLineHistoryData objectiveLineHistory : objectiveLineDataMap.values()) {
         sb.append(objectiveLineHistory).append("\n");
      }

      context.sendMessage(Message.raw(sb.toString()));
   }
}
