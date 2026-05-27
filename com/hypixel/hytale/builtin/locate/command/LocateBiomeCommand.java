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
import com.hypixel.hytale.server.worldgen.biome.Biome;
import com.hypixel.hytale.server.worldgen.chunk.ChunkGenerator;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.joml.Vector3i;

public class LocateBiomeCommand extends AbstractLocateSubcommand {
   private static final SingleArgumentType<String> BIOME_NAME = new SingleArgumentType<String>(
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
                     Arrays.stream(generator.getZonePatternProvider().getZones())
                        .flatMap(zone -> Arrays.stream(zone.biomePatternGenerator().getBiomes()))
                        .forEach(biome -> result.suggest(biome.getName()));
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
   private final RequiredArg<String> biomeNameArg = this.withRequiredArg("name", "server.commands.locate.biome.name.desc", BIOME_NAME);
   @Nonnull
   private final OptionalArg<Integer> radiusArg = this.withOptionalArg("radius", "server.commands.locate.radius.desc", ArgTypes.INTEGER);

   public LocateBiomeCommand() {
      super("biome", "server.commands.locate.biome.desc");
      this.requirePermission(HytalePermissions.fromCommand("locate.biome"));
   }

   private static boolean biomeExists(@Nonnull ChunkGenerator generator, @Nonnull String name) {
      return Arrays.stream(generator.getZonePatternProvider().getZones())
         .flatMap(zone -> Arrays.stream(zone.biomePatternGenerator().getBiomes()))
         .anyMatch(biome -> name.equalsIgnoreCase(biome.getName()));
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
      String biomeName = context.get(this.biomeNameArg);
      int maxRadius = resolveRadius(context, this.radiusArg);
      if (maxRadius >= 0) {
         if (!biomeExists(generator, biomeName)) {
            context.sendMessage(Message.translation("server.commands.locate.notFoundInWorld").param("type", "biome").param("name", biomeName));
            LinkedHashSet<String> biomeNames = Arrays.stream(generator.getZonePatternProvider().getZones())
               .flatMap(zone -> Arrays.stream(zone.biomePatternGenerator().getBiomes()))
               .map(Biome::getName)
               .collect(Collectors.toCollection(LinkedHashSet::new));
            sendDidYouMean(context, biomeName, biomeNames);
         } else {
            Vector3i result = SpiralSearchUtil.search(generator, seed, playerX, playerZ, maxRadius, zbr -> biomeName.equalsIgnoreCase(zbr.getBiome().getName()));
            if (result == null) {
               context.sendMessage(
                  Message.translation("server.commands.locate.notFoundInRadius").param("type", "biome").param("name", biomeName).param("radius", maxRadius)
               );
            } else {
               int distance = horizontalDistance(playerX, playerZ, result.x, result.z);
               context.sendMessage(
                  Message.translation("server.commands.locate.found2d")
                     .param("type", "biome")
                     .param("name", biomeName)
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
