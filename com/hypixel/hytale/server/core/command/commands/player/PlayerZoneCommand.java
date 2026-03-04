package com.hypixel.hytale.server.core.command.commands.player;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractTargetPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.WorldMapTracker;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PlayerZoneCommand extends AbstractTargetPlayerCommand {
   @Nonnull
   private static final Message MESSAGE_NO_DATA = Message.translation("server.commands.player.zone.noData");

   public PlayerZoneCommand() {
      super("zone", "server.commands.player.zone.desc");
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

      WorldMapTracker worldMapTracker = playerComponent.getWorldMapTracker();
      WorldMapTracker.ZoneDiscoveryInfo currentZone = worldMapTracker.getCurrentZone();
      String currentBiome = worldMapTracker.getCurrentBiomeName();
      if (currentZone != null && currentBiome != null) {
         context.sendMessage(
            Message.translation("server.commands.player.zone.currentZone")
               .param("zone", Message.translation(String.format("server.map.region.%s", currentZone.regionName())))
         );
         context.sendMessage(Message.translation("server.commands.player.zone.currentBiome").param("biome", currentBiome));
      } else {
         context.sendMessage(MESSAGE_NO_DATA);
      }
   }
}
