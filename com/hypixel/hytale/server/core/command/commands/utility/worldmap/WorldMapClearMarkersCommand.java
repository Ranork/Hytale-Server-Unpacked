package com.hypixel.hytale.server.core.command.commands.utility.worldmap;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.data.PlayerWorldData;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class WorldMapClearMarkersCommand extends AbstractPlayerCommand {
   @Nonnull
   private static final Message MESSAGE_COMMANDS_WORLD_MAP_MARKERS_CLEARED = Message.translation("server.commands.worldmap.markersCleared");

   public WorldMapClearMarkersCommand() {
      super("clearmarkers", "server.commands.worldmap.clearmarkers.desc");
   }

   @Override
   protected void execute(
      @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
   ) {
      Player playerComponent = store.getComponent(ref, Player.getComponentType());

      assert playerComponent != null;

      PlayerWorldData perWorldData = playerComponent.getPlayerConfigData().getPerWorldData(world.getName());
      perWorldData.setUserMapMarkers(null);
      context.sendMessage(MESSAGE_COMMANDS_WORLD_MAP_MARKERS_CLEARED);
   }
}
