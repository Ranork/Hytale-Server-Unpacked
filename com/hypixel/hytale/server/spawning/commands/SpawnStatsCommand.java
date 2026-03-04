package com.hypixel.hytale.server.spawning.commands;

import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.environment.config.Environment;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.FlagArg;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractWorldCommand;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.components.SpawnMarkerReference;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.spawning.SpawnRejection;
import com.hypixel.hytale.server.spawning.spawnmarkers.SpawnMarkerEntity;
import com.hypixel.hytale.server.spawning.world.ChunkEnvironmentSpawnData;
import com.hypixel.hytale.server.spawning.world.component.ChunkSpawnData;
import com.hypixel.hytale.server.spawning.world.component.WorldSpawnData;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import javax.annotation.Nonnull;

public class SpawnStatsCommand extends AbstractWorldCommand {
   @Nonnull
   private final FlagArg environmentsArg = this.withFlagArg("environments", "server.commands.spawning.stats.arg.environments.desc");
   @Nonnull
   private final FlagArg markersArg = this.withFlagArg("markers", "server.commands.spawning.stats.arg.markers.desc");
   @Nonnull
   private final FlagArg verboseArg = this.withFlagArg("verbose", "server.commands.spawning.stats.arg.verbose.desc");

   public SpawnStatsCommand() {
      super("stats", "server.commands.spawning.stats.desc");
   }

   @Override
   protected void execute(@Nonnull CommandContext context, @Nonnull World world, @Nonnull Store<EntityStore> store) {
      if (this.environmentsArg.get(context)) {
         WorldSpawnData worldSpawnData = store.getResource(WorldSpawnData.getResourceType());
         AtomicInteger filtered = new AtomicInteger();
         boolean verbose = this.verboseArg.get(context);
         worldSpawnData.forEachEnvironmentSpawnData(
            worldEnvironmentSpawnData -> {
               if (verbose || worldEnvironmentSpawnData.hasNPCs() && worldEnvironmentSpawnData.getExpectedNPCs() != 0.0) {
                  int environmentIndex = worldEnvironmentSpawnData.getEnvironmentIndex();
                  String name = Environment.getAssetMap().getAsset(environmentIndex).getId();
                  Store<ChunkStore> chunkStore = world.getChunkStore().getStore();
                  double[] chunkExpected = new double[]{0.0};
                  worldEnvironmentSpawnData.getChunkRefSet()
                     .forEach(
                        ref -> {
                           ChunkEnvironmentSpawnData chunkEnvironmentSpawnData = chunkStore.getComponent(
                                 (Ref<ChunkStore>)ref, ChunkSpawnData.getComponentType()
                              )
                              .getEnvironmentSpawnData(environmentIndex);
                           chunkExpected[0] += chunkEnvironmentSpawnData.getExpectedNPCs();
                        }
                     );
                  String message = String.format(
                     "Environment: %-30s Exp %6.2f Act %4d Blk %s Chunk exp: %6.2f",
                     name,
                     worldEnvironmentSpawnData.getExpectedNPCs(),
                     worldEnvironmentSpawnData.getActualNPCs(),
                     worldEnvironmentSpawnData.getSegmentCount(),
                     chunkExpected[0]
                  );
                  ((HytaleLogger.Api)NPCPlugin.get().getLogger().atInfo()).log(message);
                  worldEnvironmentSpawnData.forEachNpcStat(
                     (npcIndex, stats) -> {
                        int all = stats.getSpansTried();
                        double failPercent = all > 0 ? MathUtil.percent(all - stats.getSpansSuccess(), all) : 0.0;
                        String message1;
                        if (verbose) {
                           int successfulJobCount = stats.getSuccessfulJobCount();
                           int successfulBudget = stats.getSuccessfulJobTotalBudget();
                           int failedJobCount = stats.getFailedJobCount();
                           int failedBudget = stats.getFailedJobTotalBudget();
                           message1 = String.format(
                              "        NPC: %-30s Exp %6.2f Act %4d | Spns %8d: Succ %4d Lgt %5.1f Blk %5.1f Pos %5.1f Geo %5.1f Bre %5.1f Oth %5.1f | Fail%% %5.1f | Spwnbl %s | Succ Job Bgt %6d Avg Bgt %6.2f | Fail Jobs %6d Bgt %6d Avg Bgt %6.2f ",
                              NPCPlugin.get().getName(npcIndex),
                              stats.getExpected(),
                              stats.getActual(),
                              all,
                              stats.getSpansSuccess(),
                              MathUtil.percent(stats.getRejectionCount(SpawnRejection.OUTSIDE_LIGHT_RANGE), all),
                              MathUtil.percent(stats.getRejectionCount(SpawnRejection.INVALID_SPAWN_BLOCK), all),
                              MathUtil.percent(stats.getRejectionCount(SpawnRejection.NO_POSITION), all),
                              MathUtil.percent(stats.getRejectionCount(SpawnRejection.INVALID_POSITION), all),
                              MathUtil.percent(stats.getRejectionCount(SpawnRejection.NOT_BREATHABLE), all),
                              MathUtil.percent(stats.getRejectionCount(SpawnRejection.OTHER), all),
                              failPercent,
                              !stats.isUnspawnable(),
                              successfulBudget,
                              successfulJobCount > 0 ? (double)successfulBudget / successfulJobCount : 0.0,
                              failedJobCount,
                              failedBudget,
                              failedJobCount > 0 ? (double)failedBudget / failedJobCount : 0.0
                           );
                        } else {
                           message1 = String.format(
                              "        NPC: %-30s Exp %6.2f Act %4d | Spns %8d: Succ %4d Lgt %5.1f Blk %5.1f Pos %5.1f Geo %5.1f Bre %5.1f Oth %5.1f | Fail%% %5.1f | Spwnbl %s ",
                              NPCPlugin.get().getName(npcIndex),
                              stats.getExpected(),
                              stats.getActual(),
                              all,
                              stats.getSpansSuccess(),
                              MathUtil.percent(stats.getRejectionCount(SpawnRejection.OUTSIDE_LIGHT_RANGE), all),
                              MathUtil.percent(stats.getRejectionCount(SpawnRejection.INVALID_SPAWN_BLOCK), all),
                              MathUtil.percent(stats.getRejectionCount(SpawnRejection.NO_POSITION), all),
                              MathUtil.percent(stats.getRejectionCount(SpawnRejection.INVALID_POSITION), all),
                              MathUtil.percent(stats.getRejectionCount(SpawnRejection.NOT_BREATHABLE), all),
                              MathUtil.percent(stats.getRejectionCount(SpawnRejection.OTHER), all),
                              failPercent,
                              !stats.isUnspawnable()
                           );
                        }

                        NPCPlugin.get().getLogger().at(failPercent < 60.0 ? Level.INFO : Level.WARNING).log(message1);
                     }
                  );
               } else {
                  filtered.getAndIncrement();
               }
            }
         );
         AtomicInteger trackedNPC = new AtomicInteger();
         AtomicInteger totalNPC = new AtomicInteger();
         store.forEachEntityParallel(NPCEntity.getComponentType(), (index, archetypeChunk, commandBuffer) -> {
            totalNPC.getAndIncrement();
            NPCEntity npc = archetypeChunk.getComponent(index, NPCEntity.getComponentType());
            if (npc.getEnvironment() != Integer.MIN_VALUE && npc.getSpawnConfiguration() != Integer.MIN_VALUE) {
               trackedNPC.getAndIncrement();
            }
         });
         int spawnJobsCompleted = worldSpawnData.getTotalSpawnJobsCompleted();
         String message = String.format(
            "Total: Exp %.2f Exp-Empty %.2f Act %d Job Pending Act %d Tracked %d Total %d Unspawnable %s AvgSegCount %s Chunks %s Filtered empty envs %d Active Jobs %d Total Jobs Run %d Avg Job Budget %.2f",
            worldSpawnData.getExpectedNPCs(),
            worldSpawnData.getExpectedNPCsInEmptyEnvironments(),
            worldSpawnData.getActualNPCs(),
            worldSpawnData.getTrackedCountFromJobs(),
            trackedNPC.get(),
            totalNPC.get(),
            worldSpawnData.isUnspawnable(),
            worldSpawnData.averageSegmentCount(),
            worldSpawnData.getChunkCount(),
            filtered.get(),
            worldSpawnData.getActiveSpawnJobs(),
            spawnJobsCompleted,
            spawnJobsCompleted > 0 ? (double)worldSpawnData.getTotalSpawnJobBudgetUsed() / spawnJobsCompleted : 0.0
         );
         ((HytaleLogger.Api)NPCPlugin.get().getLogger().atInfo()).log(message);
      }

      if (this.markersArg.get(context)) {
         AtomicInteger spawnMarkerCount = new AtomicInteger();
         AtomicInteger inactiveSpawnMarkerCount = new AtomicInteger();
         Object2IntOpenHashMap<String> spawnMarkerTypeCounts = new Object2IntOpenHashMap();
         store.forEachChunk(SpawnMarkerEntity.getComponentType(), (archetypeChunk, componentStoreCommandBuffer) -> {
            for (int index = 0; index < archetypeChunk.size(); index++) {
               SpawnMarkerEntity entity = archetypeChunk.getComponent(index, SpawnMarkerEntity.getComponentType());
               spawnMarkerCount.getAndIncrement();
               spawnMarkerTypeCounts.mergeInt(entity.getSpawnMarkerId(), 1, Integer::sum);
               if (entity.getSpawnCount() == 0) {
                  inactiveSpawnMarkerCount.getAndIncrement();
               }
            }
         });
         AtomicInteger spawnMarkerNPCCount = new AtomicInteger();
         Object2IntOpenHashMap<String> roleCounts = new Object2IntOpenHashMap();
         HashMap<String, Object2IntMap<String>> roleCountsPerMarkerType = new HashMap<>();
         store.forEachChunk(
            Archetype.of(NPCEntity.getComponentType(), SpawnMarkerReference.getComponentType()), (archetypeChunk, componentStoreCommandBuffer) -> {
               for (int index = 0; index < archetypeChunk.size(); index++) {
                  NPCEntity entity = archetypeChunk.getComponent(index, NPCEntity.getComponentType());
                  SpawnMarkerReference spawnMarkerReference = archetypeChunk.getComponent(index, SpawnMarkerReference.getComponentType());
                  spawnMarkerNPCCount.getAndIncrement();
                  String roleName = entity.getRoleName();
                  roleCounts.mergeInt(roleName, 1, Integer::sum);
                  Ref<EntityStore> markerRef = spawnMarkerReference.getReference().getEntity(componentStoreCommandBuffer);
                  SpawnMarkerEntity marker = componentStoreCommandBuffer.getComponent(markerRef, SpawnMarkerEntity.getComponentType());
                  Object2IntMap<String> spawnedRoles = roleCountsPerMarkerType.computeIfAbsent(marker.getSpawnMarkerId(), key -> new Object2IntOpenHashMap());
                  spawnedRoles.mergeInt(roleName, 1, Integer::sum);
               }
            }
         );
         StringBuilder sb = new StringBuilder();
         sb.append("Markers: ")
            .append(spawnMarkerCount.get())
            .append(" (With zero spawns: ")
            .append(inactiveSpawnMarkerCount.get())
            .append(")\nSpawned NPCs: ")
            .append(spawnMarkerNPCCount.get());
         roleCounts.object2IntEntrySet()
            .fastForEach(stringEntry -> sb.append("\n  ").append((String)stringEntry.getKey()).append(": ").append(stringEntry.getIntValue()));
         sb.append("\nRoles by marker type:");
         roleCountsPerMarkerType.forEach((key, spawnedRoles) -> {
            sb.append("\n  ").append(key).append(" (Instances: ").append(spawnMarkerTypeCounts.getInt(key)).append(")");
            spawnedRoles.object2IntEntrySet().forEach(entry -> sb.append("\n    ").append((String)entry.getKey()).append(": ").append(entry.getIntValue()));
         });
         ((HytaleLogger.Api)NPCPlugin.get().getLogger().atInfo()).log(sb.toString());
      }

      context.sendMessage(Message.translation("server.commands.spawning.stats.results"));
   }
}
