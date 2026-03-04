package com.hypixel.hytale.server.core.command.commands.player.viewradius;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractTargetPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.tracker.EntityTrackerSystems;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PlayerViewRadiusGetCommand extends AbstractTargetPlayerCommand {
   public PlayerViewRadiusGetCommand() {
      super("get", "server.commands.player.viewradius.get.desc");
   }

   @Override
   protected void execute(
      @Nonnull CommandContext context,
      @Nullable Ref<EntityStore> sourceRef,
      @Nonnull Ref<EntityStore> ref,
      @Nonnull PlayerRef playerRef,
      @Nonnull World world,
      @Nonnull Store<EntityStore> store
   ) {
      Player playerComponent = store.getComponent(ref, Player.getComponentType());

      assert playerComponent != null;

      EntityTrackerSystems.EntityViewer entityViewerComponent = store.getComponent(ref, EntityTrackerSystems.EntityViewer.getComponentType());

      assert entityViewerComponent != null;

      int viewRadiusChunks = playerComponent.getViewRadius();
      int clientViewRadiusChunks = playerComponent.getClientViewRadius();
      int viewRadiusBlocks = entityViewerComponent.viewRadiusBlocks;
      context.sendMessage(
         Message.translation("server.commands.player.viewradius.info")
            .param("radius", viewRadiusChunks)
            .param("clientRadius", clientViewRadiusChunks)
            .param("radiusBlocks", viewRadiusBlocks)
      );
   }
}
