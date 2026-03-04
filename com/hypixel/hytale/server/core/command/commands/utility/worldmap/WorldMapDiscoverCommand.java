package com.hypixel.hytale.server.core.command.commands.utility.worldmap;

import com.hypixel.hytale.common.util.StringUtil;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.worldmap.BiomeData;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.CommandUtil;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

public class WorldMapDiscoverCommand extends AbstractPlayerCommand {
   @Nonnull
   private static final Message MESSAGE_COMMANDS_WORLD_MAP_ALL_ZONES_DISCOVERED = Message.translation("server.commands.worldmap.allZonesDiscovered");
   @Nonnull
   private final OptionalArg<String> zoneArg = this.withOptionalArg("zone", "server.commands.worldmap.zone.desc", ArgTypes.STRING);

   public WorldMapDiscoverCommand() {
      super("discover", "server.commands.worldmap.discover.desc");
      this.addAliases("disc");
   }

   @Override
   protected void execute(
      @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
   ) {
      Map<Short, BiomeData> biomeDataMap = world.getWorldMapManager().getWorldMapSettings().getSettingsPacket().biomeDataMap;
      if (biomeDataMap != null) {
         Set<String> zoneNames = biomeDataMap.values().stream().map(biomeData -> biomeData.zoneName).collect(Collectors.toSet());
         if (!this.zoneArg.provided(context)) {
            context.sendMessage(Message.translation("server.commands.worldmap.zoneNames").param("zoneNames", zoneNames.toString()));
            Player playerComponent = store.getComponent(ref, Player.getComponentType());

            assert playerComponent != null;

            zoneNames.removeAll(playerComponent.getPlayerConfigData().getDiscoveredZones());
            context.sendMessage(Message.translation("server.commands.worldmap.zonesNotDiscovered").param("zoneNames", zoneNames.toString()));
         } else {
            String zoneName = this.zoneArg.get(context);
            if (zoneName.equalsIgnoreCase("all")) {
               Player playerComponent = store.getComponent(ref, Player.getComponentType());

               assert playerComponent != null;

               playerComponent.getWorldMapTracker().discoverZones(world, zoneNames);
               context.sendMessage(MESSAGE_COMMANDS_WORLD_MAP_ALL_ZONES_DISCOVERED);
            } else if (!zoneNames.contains(zoneName)) {
               context.sendMessage(Message.translation("server.commands.worldmap.zoneNotFound").param("zoneName", zoneName));
               context.sendMessage(
                  Message.translation("server.general.failed.didYouMean")
                     .param("choices", StringUtil.sortByFuzzyDistance(zoneName, zoneNames, CommandUtil.RECOMMEND_COUNT).toString())
               );
            } else {
               Player playerComponent = store.getComponent(ref, Player.getComponentType());

               assert playerComponent != null;

               boolean added = playerComponent.getWorldMapTracker().discoverZone(world, zoneName);
               if (added) {
                  context.sendMessage(Message.translation("server.commands.worldmap.zoneDiscovered").param("zoneName", zoneName));
               } else {
                  context.sendMessage(Message.translation("server.commands.worldmap.zoneAlreadyDiscovered").param("zoneName", zoneName));
               }
            }
         }
      }
   }
}
