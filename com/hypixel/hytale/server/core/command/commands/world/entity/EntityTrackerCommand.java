package com.hypixel.hytale.server.core.command.commands.world.entity;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractWorldCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.tracker.EntityTrackerSystems;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class EntityTrackerCommand extends AbstractWorldCommand {
   @Nonnull
   private static final Message MESSAGE_COMMANDS_ERRORS_PLAYER_NOT_IN_WORLD = Message.translation("server.commands.errors.playerNotInWorld");
   @Nonnull
   private static final Message MESSAGE_COMMANDS_ENTITY_TRACKER_NO_VIEWER_COMPONENT = Message.translation("server.commands.entity.tracker.noViewerComponent");
   @Nonnull
   private final RequiredArg<PlayerRef> playerArg = this.withRequiredArg("player", "server.commands.entity.tracker.player.desc", ArgTypes.PLAYER_REF);

   public EntityTrackerCommand() {
      super("tracker", "server.commands.entity.tracker.desc");
   }

   @Override
   protected void execute(@Nonnull CommandContext context, @Nonnull World world, @Nonnull Store<EntityStore> store) {
      PlayerRef playerRef = this.playerArg.get(context);
      Ref<EntityStore> ref = playerRef.getReference();
      if (ref != null && ref.isValid()) {
         EntityTrackerSystems.EntityViewer entityViewerComponent = store.getComponent(ref, EntityTrackerSystems.EntityViewer.getComponentType());
         if (entityViewerComponent == null) {
            context.sendMessage(MESSAGE_COMMANDS_ENTITY_TRACKER_NO_VIEWER_COMPONENT);
         } else {
            Player playerComponent = store.getComponent(ref, Player.getComponentType());
            if (playerComponent == null) {
               context.sendMessage(MESSAGE_COMMANDS_ERRORS_PLAYER_NOT_IN_WORLD);
            } else {
               context.sendMessage(
                  Message.translation("server.commands.entityTracker.summary")
                     .param("visibleCount", entityViewerComponent.visible.size())
                     .param("lodExcludedCount", entityViewerComponent.lodExcludedCount)
                     .param("hiddenCount", entityViewerComponent.hiddenCount)
                     .param("totalCount", entityViewerComponent.visible.size() + entityViewerComponent.lodExcludedCount + entityViewerComponent.hiddenCount)
                     .param("worldTotalCount", store.getEntityCount())
                     .param("viewRadius", playerComponent.getViewRadius())
                     .param("viewRadiusBlocks", entityViewerComponent.viewRadiusBlocks)
               );
            }
         }
      } else {
         context.sendMessage(MESSAGE_COMMANDS_ERRORS_PLAYER_NOT_IN_WORLD);
      }
   }
}
