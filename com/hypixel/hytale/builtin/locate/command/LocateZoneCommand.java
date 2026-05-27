package com.hypixel.hytale.builtin.locate.command;

import com.hypixel.hytale.builtin.locate.SpiralSearchUtil;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.command.system.ParseResult;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.arguments.types.SingleArgumentType;
import com.hypixel.hytale.server.core.command.system.suggestion.SuggestionResult;
import com.hypixel.hytale.server.core.permissions.HytalePermissions;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.worldgen.chunk.ChunkGenerator;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.joml.Vector3i;

public class LocateZoneCommand extends AbstractLocateSubcommand {
   private static final SingleArgumentType<String> ZONE_NAME = new SingleArgumentType<String>(
      "server.commands.parsing.argtype.string.name", "server.commands.parsing.argtype.string.usage"
   ) {
      public String parse(String input, ParseResult parseResult) {
         return input;
      }

      @Override
      public void suggest(@Nonnull CommandSender sender, @Nonnull String textAlreadyEntered, int numParametersTyped, @Nonnull SuggestionResult result) {
         if (sender instanceof PlayerRef playerRef) {
            UUID worldUuid = playerRef.getWorldUuid();
            if (worldUuid != null) {
               World world = Universe.get().getWorld(worldUuid);
               if (world != null) {
                  if (world.getChunkStore().getGenerator() instanceof ChunkGenerator generator) {
                     Arrays.stream(generator.getZonePatternProvider().getZones()).forEach(zone -> result.suggest(zone.name()));
                  }
               }
            }
         }
      }

      @Override
      public int getSuggestionValueCount() {
         return 1;
      }
   };
   @Nonnull
   private final RequiredArg<String> zoneNameArg = this.withRequiredArg("name", "server.commands.locate.zone.name.desc", ZONE_NAME);
   @Nonnull
   private final OptionalArg<Integer> radiusArg = this.withOptionalArg("radius", "server.commands.locate.radius.desc", ArgTypes.INTEGER);

   public LocateZoneCommand() {
      super("zone", "server.commands.locate.zone.desc");
      this.requirePermission(HytalePermissions.fromCommand("locate.zone"));
   }

   private static boolean zoneExists(@Nonnull ChunkGenerator generator, @Nonnull String name) {
      return Arrays.stream(generator.getZonePatternProvider().getZones()).anyMatch(zone -> name.equalsIgnoreCase(zone.name()));
   }

   @Override
   protected void execute(
      @Nonnull CommandContext context,
      @Nonnull ChunkGenerator generator,
      int seed,
      int playerX,
      int playerZ,
      @Nonnull World world,
      @Nonnull Ref<EntityStore> ref
   ) {
      String zoneName = context.get(this.zoneNameArg);
      int maxRadius = resolveRadius(context, this.radiusArg);
      if (maxRadius >= 0) {
         if (!zoneExists(generator, zoneName)) {
            context.sendMessage(Message.translation("server.commands.locate.notFoundInWorld").param("type", "zone").param("name", zoneName));
            LinkedHashSet<String> zoneNames = Arrays.stream(generator.getZonePatternProvider().getZones())
               .map(zone -> zone.name())
               .collect(Collectors.toCollection(LinkedHashSet::new));
            sendDidYouMean(context, zoneName, zoneNames);
         } else {
            Vector3i result = SpiralSearchUtil.search(
               generator, seed, playerX, playerZ, maxRadius, zbr -> zoneName.equalsIgnoreCase(zbr.getZoneResult().getZone().name())
            );
            if (result == null) {
               context.sendMessage(
                  Message.translation("server.commands.locate.notFoundInRadius").param("type", "zone").param("name", zoneName).param("radius", maxRadius)
               );
            } else {
               int distance = horizontalDistance(playerX, playerZ, result.x, result.z);
               context.sendMessage(
                  Message.translation("server.commands.locate.found2d")
                     .param("type", "zone")
                     .param("name", zoneName)
                     .param("x", result.x)
                     .param("z", result.z)
                     .param("distance", distance)
               );
               this.teleportIfRequested(context, world, ref, result.x, result.z);
            }
         }
      }
   }
}
