package com.hypixel.hytale.builtin.locate.command;

import com.hypixel.hytale.builtin.locate.SpiralSearchUtil;
import com.hypixel.hytale.common.util.StringUtil;
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
import com.hypixel.hytale.server.core.modules.i18n.I18nModule;
import com.hypixel.hytale.server.core.permissions.HytalePermissions;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.worldgen.chunk.ChunkGenerator;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.joml.Vector3i;

public class LocateRegionCommand extends AbstractLocateSubcommand {
   private static final SingleArgumentType<String> REGION_NAME = new SingleArgumentType<String>(
      "server.commands.parsing.argtype.string.name", "server.commands.parsing.argtype.string.usage"
   ) {
      public String parse(String input, ParseResult parseResult) {
         return StringUtil.stripQuotes(input);
      }

      @Override
      public void suggest(@Nonnull CommandSender sender, @Nonnull String textAlreadyEntered, int numParametersTyped, @Nonnull SuggestionResult result) {
         if (sender instanceof PlayerRef playerRef) {
            UUID worldUuid = playerRef.getWorldUuid();
            if (worldUuid != null) {
               World world = Universe.get().getWorld(worldUuid);
               if (world != null) {
                  if (world.getChunkStore().getGenerator() instanceof ChunkGenerator generator) {
                     String language = playerRef.getLanguage();
                     Arrays.stream(generator.getZonePatternProvider().getZones())
                        .map(zone -> I18nModule.get().getMessage(language, "server.map.region." + zone.name()))
                        .filter(Objects::nonNull)
                        .forEach(displayName -> result.suggest("\"" + displayName + "\""));
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
   private final RequiredArg<String> regionNameArg = this.withRequiredArg("name", "server.commands.locate.region.name.desc", REGION_NAME);
   @Nonnull
   private final OptionalArg<Integer> radiusArg = this.withOptionalArg("radius", "server.commands.locate.radius.desc", ArgTypes.INTEGER);

   public LocateRegionCommand() {
      super("region", "server.commands.locate.region.desc");
      this.requirePermission(HytalePermissions.fromCommand("locate.region"));
   }

   @Nullable
   private static String resolveZoneName(@Nonnull ChunkGenerator generator, @Nonnull String language, @Nonnull String regionDisplayName) {
      return Arrays.stream(generator.getZonePatternProvider().getZones())
         .filter(zone -> regionDisplayName.equalsIgnoreCase(I18nModule.get().getMessage(language, "server.map.region." + zone.name())))
         .map(zone -> zone.name())
         .findFirst()
         .orElse(null);
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
      String regionName = context.get(this.regionNameArg);
      int maxRadius = resolveRadius(context, this.radiusArg);
      if (maxRadius >= 0) {
         String language = context.senderAs(PlayerRef.class).getLanguage();
         String resolvedZoneName = resolveZoneName(generator, language, regionName);
         if (resolvedZoneName == null) {
            context.sendMessage(Message.translation("server.commands.locate.notFoundInWorld").param("type", "region").param("name", regionName));
            LinkedHashSet<String> candidates = Arrays.stream(generator.getZonePatternProvider().getZones()).map(zone -> {
               String displayName = I18nModule.get().getMessage(language, "server.map.region." + zone.name());
               return displayName != null ? displayName : zone.name();
            }).collect(Collectors.toCollection(LinkedHashSet::new));
            sendDidYouMean(context, regionName, candidates);
         } else {
            Vector3i result = SpiralSearchUtil.search(
               generator, seed, playerX, playerZ, maxRadius, zbr -> resolvedZoneName.equalsIgnoreCase(zbr.getZoneResult().getZone().name())
            );
            if (result == null) {
               context.sendMessage(
                  Message.translation("server.commands.locate.notFoundInRadius").param("type", "region").param("name", regionName).param("radius", maxRadius)
               );
            } else {
               int distance = horizontalDistance(playerX, playerZ, result.x, result.z);
               context.sendMessage(
                  Message.translation("server.commands.locate.found2d")
                     .param("type", "region")
                     .param("name", regionName)
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
