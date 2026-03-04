package com.hypixel.hytale.server.core.command.commands.world.worldgen;

import com.hypixel.hytale.common.util.FormatUtil;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector2i;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.worldgen.GeneratedChunk;
import com.hypixel.hytale.server.core.universe.world.worldgen.IBenchmarkableWorldGen;
import com.hypixel.hytale.server.core.universe.world.worldgen.IWorldGen;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import java.io.File;
import java.io.FileWriter;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import javax.annotation.Nonnull;

public class WorldGenBenchmarkCommand extends CommandBase {
   private static final AtomicBoolean IS_RUNNING = new AtomicBoolean(false);
   public static final Message MESSAGE_COMMANDS_WORLD_GEN_BENCHMARK_SAVING = Message.translation("server.commands.worldgenbenchmark.saving");
   public static final Message MESSAGE_COMMANDS_WORLD_GEN_BENCHMARK_SAVE_FAILED = Message.translation("server.commands.worldgenbenchmark.saveFailed");
   public static final Message MESSAGE_COMMANDS_WORLD_GEN_BENCHMARK_ABORT = Message.translation("server.commands.worldgenbenchmark.abort");
   public static final Message MESSAGE_COMMANDS_WORLD_GEN_BENCHMARK_BENCHMARK_NOT_SUPPORTED = Message.translation(
      "server.commands.worldgenbenchmark.benchmarkNotSupported"
   );
   public static final Message MESSAGE_COMMANDS_WORLD_GEN_BENCHMARK_ALREADY_IN_PROGRESS = Message.translation(
      "server.commands.worldgenbenchmark.alreadyInProgress"
   );
   @Nonnull
   private final OptionalArg<World> worldArg = this.withOptionalArg("world", "server.commands.worldthread.arg.desc", ArgTypes.WORLD);
   @Nonnull
   private final OptionalArg<Integer> seedArg = this.withOptionalArg("seed", "server.commands.worldgenbenchmark.seed.desc", ArgTypes.INTEGER);
   @Nonnull
   private final RequiredArg<Vector2i> pos1Arg = this.withRequiredArg("pos1", "server.commands.worldgenbenchmark.pos1.desc", ArgTypes.VECTOR2I);
   @Nonnull
   private final RequiredArg<Vector2i> pos2Arg = this.withRequiredArg("pos2", "server.commands.worldgenbenchmark.pos2.desc", ArgTypes.VECTOR2I);

   public WorldGenBenchmarkCommand() {
      super("benchmark", "server.commands.worldgenbenchmark.desc");
   }

   @Override
   protected void executeSync(@Nonnull CommandContext context) {
      if (IS_RUNNING.get()) {
         context.sendMessage(MESSAGE_COMMANDS_WORLD_GEN_BENCHMARK_ALREADY_IN_PROGRESS);
      } else {
         World world = this.worldArg.getProcessed(context);
         String worldName = world.getName();
         int seed = this.seedArg.provided(context) ? this.seedArg.get(context) : (int)world.getWorldConfig().getSeed();
         IWorldGen worldGen = world.getChunkStore().getGenerator();
         if (!(worldGen instanceof IBenchmarkableWorldGen benchmarkableWorldGen)) {
            context.sendMessage(MESSAGE_COMMANDS_WORLD_GEN_BENCHMARK_BENCHMARK_NOT_SUPPORTED);
         } else {
            Vector2i corner1 = this.pos1Arg.get(context);
            Vector2i corner2 = this.pos2Arg.get(context);
            int minX;
            int maxX;
            if (corner1.x < corner2.x) {
               minX = ChunkUtil.chunkCoordinate(corner1.x);
               maxX = ChunkUtil.chunkCoordinate(corner2.x);
            } else {
               minX = ChunkUtil.chunkCoordinate(corner2.x);
               maxX = ChunkUtil.chunkCoordinate(corner1.x);
            }

            int minZ;
            int maxZ;
            if (corner1.y < corner2.y) {
               minZ = ChunkUtil.chunkCoordinate(corner1.y);
               maxZ = ChunkUtil.chunkCoordinate(corner2.y);
            } else {
               minZ = ChunkUtil.chunkCoordinate(corner2.y);
               maxZ = ChunkUtil.chunkCoordinate(corner1.y);
            }

            LongArrayList generatingChunks = new LongArrayList();

            for (int x = minX; x <= maxX; x++) {
               for (int z = minZ; z <= maxZ; z++) {
                  generatingChunks.add(ChunkUtil.indexChunk(x, z));
               }
            }

            if (IS_RUNNING.getAndSet(true)) {
               context.sendMessage(MESSAGE_COMMANDS_WORLD_GEN_BENCHMARK_ABORT);
            } else {
               context.sendMessage(
                  Message.translation("server.commands.worldgenbenchmark.started")
                     .param("seed", seed)
                     .param("worldName", worldName)
                     .param("size", generatingChunks.size())
               );
               benchmarkableWorldGen.getBenchmark().start();
               int chunkCount = generatingChunks.size();
               long startTime = System.nanoTime();
               new Thread(
                     () -> {
                        try {
                           Set<CompletableFuture<GeneratedChunk>> currentChunks = new HashSet<>();
                           long nextBroadcast = System.nanoTime();

                           do {
                              long thisTime = System.nanoTime();
                              if (thisTime >= nextBroadcast) {
                                 world.execute(
                                    () -> world.sendMessage(
                                       Message.translation("server.commands.worldgenbenchmark.progress")
                                          .param("percent", Math.round((1.0 - (double)generatingChunks.size() / chunkCount) * 1000.0) / 10.0)
                                    )
                                 );
                                 nextBroadcast = thisTime + 5000000000L;
                              }

                              currentChunks.removeIf(CompletableFuture::isDone);

                              for (int i = currentChunks.size(); i < 20 && !generatingChunks.isEmpty(); i++) {
                                 long index = generatingChunks.removeLong(generatingChunks.size() - 1);
                                 CompletableFuture<GeneratedChunk> future = worldGen.generate(
                                    seed, index, ChunkUtil.xOfChunkIndex(index), ChunkUtil.zOfChunkIndex(index), idx -> true
                                 );
                                 currentChunks.add(future);
                              }
                           } while (!currentChunks.isEmpty());

                           String duration = FormatUtil.nanosToString(System.nanoTime() - startTime);
                           world.execute(() -> world.sendMessage(Message.translation("server.commands.worldgenbenchmark.done").param("duration", duration)));
                           world.execute(() -> world.sendMessage(MESSAGE_COMMANDS_WORLD_GEN_BENCHMARK_SAVING));
                           String fileName = "quant." + System.currentTimeMillis() + "." + (maxX - minX) + "x" + (maxZ - minZ) + "." + worldName + ".txt";
                           File folder = new File("quantification");
                           File file = new File("quantification" + File.separator + fileName);
                           folder.mkdirs();

                           try (FileWriter fw = new FileWriter(file)) {
                              fw.write(benchmarkableWorldGen.getBenchmark().buildReport().join());
                              world.execute(
                                 () -> world.sendMessage(Message.translation("server.commands.worldgenbenchmark.saveDone").param("fileName", fileName))
                              );
                           } catch (Exception var31) {
                              ((HytaleLogger.Api)HytaleLogger.getLogger().at(Level.SEVERE).withCause(var31)).log("Failed to save worldgen benchmark report!");
                              world.execute(() -> world.sendMessage(MESSAGE_COMMANDS_WORLD_GEN_BENCHMARK_SAVE_FAILED));
                           }

                           benchmarkableWorldGen.getBenchmark().stop();
                        } catch (RejectedExecutionException var32) {
                           HytaleLogger.getLogger().at(Level.SEVERE).log("Cancelled worldgen benchmark due to generator shutdown");
                        } finally {
                           IS_RUNNING.set(false);
                        }
                     },
                     "WorldGenBenchmarkCommand"
                  )
                  .start();
            }
         }
      }
   }
}
