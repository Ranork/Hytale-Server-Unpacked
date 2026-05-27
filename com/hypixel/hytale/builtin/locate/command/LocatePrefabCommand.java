package com.hypixel.hytale.builtin.locate.command;

import com.hypixel.hytale.builtin.locate.PrefabPatternSearchUtil;
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
import com.hypixel.hytale.server.worldgen.container.UniquePrefabContainer;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import org.joml.Vector3i;

public class LocatePrefabCommand extends AbstractLocateSubcommand {
   private static final int MAX_NEAREST_COUNT = 20;
   private static final SingleArgumentType<String> PREFAB_NAME = new SingleArgumentType<String>(
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
                     int seed = (int)world.getWorldConfig().getSeed();
                     UniquePrefabContainer.UniquePrefabEntry[] uniquePrefabs = generator.getUniquePrefabs(seed);
                     if (uniquePrefabs != null) {
                        Arrays.stream(uniquePrefabs).forEach(entry -> result.suggest(entry.getName()));
                     }

                     PrefabPatternSearchUtil.collectAllPrefabKeys(generator).forEach(result::suggest);
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
   private final RequiredArg<String> prefabNameArg = this.withRequiredArg("name", "server.commands.locate.prefab.name.desc", PREFAB_NAME);
   @Nonnull
   private final OptionalArg<Integer> radiusArg = this.withOptionalArg("radius", "server.commands.locate.radius.desc", ArgTypes.INTEGER);
   @Nonnull
   private final OptionalArg<Integer> skipArg = this.withOptionalArg("skip", "server.commands.locate.prefab.skip.desc", ArgTypes.INTEGER);
   @Nonnull
   private final OptionalArg<Integer> nearestArg = this.withOptionalArg("nearest", "server.commands.locate.prefab.nearest.desc", ArgTypes.INTEGER);

   public LocatePrefabCommand() {
      super("prefab", "server.commands.locate.prefab.desc");
      this.requirePermission(HytalePermissions.fromCommand("locate.prefab"));
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
      String prefabName = context.get(this.prefabNameArg);
      int maxRadius = resolveRadius(context, this.radiusArg);
      if (maxRadius >= 0) {
         int nearest = this.nearestArg.provided(context) ? Math.min(this.nearestArg.get(context), 20) : 0;
         int skip = this.skipArg.provided(context) ? this.skipArg.get(context) : 0;
         if (nearest == 0 && skip == 0) {
            UniquePrefabContainer.UniquePrefabEntry uniqueResult = PrefabSearchUtil.findClosestPrefab(generator, seed, playerX, playerZ, prefabName, false);
            if (uniqueResult != null) {
               Vector3i pos = uniqueResult.getPosition();
               int distance = horizontalDistance(playerX, playerZ, pos.x, pos.z);
               context.sendMessage(
                  Message.translation("server.commands.locate.found3d")
                     .param("type", "prefab")
                     .param("name", prefabName)
                     .param("x", pos.x)
                     .param("y", pos.y)
                     .param("z", pos.z)
                     .param("distance", distance)
               );
               this.teleportIfRequested(context, world, ref, pos.x, pos.y, pos.z);
               return;
            }
         }

         int maxResults = nearest > 0 ? nearest : skip + 1;
         List<Entry<String, Vector3i>> results = PrefabPatternSearchUtil.search(generator, seed, playerX, playerZ, maxRadius, prefabName, maxResults);
         if (nearest <= 0) {
            if (skip >= results.size()) {
               if (results.isEmpty()) {
                  context.sendMessage(
                     Message.translation("server.commands.locate.notFoundInRadius")
                        .param("type", "prefab")
                        .param("name", prefabName)
                        .param("radius", maxRadius)
                  );
                  LinkedHashSet<String> allNames = new LinkedHashSet<>();
                  UniquePrefabContainer.UniquePrefabEntry[] uniquePrefabs = generator.getUniquePrefabs(seed);
                  if (uniquePrefabs != null) {
                     Arrays.stream(uniquePrefabs).map(entry -> entry.getName()).forEach(allNames::add);
                  }

                  allNames.addAll(PrefabPatternSearchUtil.collectAllPrefabKeys(generator));
                  sendDidYouMean(context, prefabName, allNames);
               } else {
                  context.sendMessage(Message.translation("server.commands.locate.prefab.skipTooHigh").param("skip", skip).param("count", results.size()));
               }
            } else {
               Entry<String, Vector3i> result = results.get(skip);
               Vector3i pos = result.getValue();
               context.sendMessage(
                  Message.translation("server.commands.locate.found3d")
                     .param("type", "prefab")
                     .param("name", result.getKey())
                     .param("x", pos.x)
                     .param("y", pos.y)
                     .param("z", pos.z)
                     .param("distance", horizontalDistance(playerX, playerZ, pos.x, pos.z))
               );
               this.teleportIfRequested(context, world, ref, pos.x, pos.y, pos.z);
            }
         } else {
            if (results.isEmpty()) {
               context.sendMessage(
                  Message.translation("server.commands.locate.notFoundInRadius").param("type", "prefab").param("name", prefabName).param("radius", maxRadius)
               );
            } else {
               context.sendMessage(Message.translation("server.commands.locate.prefab.nearestHeader").param("count", results.size()).param("name", prefabName));

               for (int i = 0; i < results.size(); i++) {
                  Vector3i pos = results.get(i).getValue();
                  context.sendMessage(
                     Message.translation("server.commands.locate.prefab.nearestEntry")
                        .param("index", i + 1)
                        .param("x", pos.x)
                        .param("y", pos.y)
                        .param("z", pos.z)
                        .param("distance", horizontalDistance(playerX, playerZ, pos.x, pos.z))
                  );
               }
            }
         }
      }
   }
}
