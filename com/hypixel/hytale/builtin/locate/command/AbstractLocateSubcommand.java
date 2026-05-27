package com.hypixel.hytale.builtin.locate.command;

import com.hypixel.hytale.common.util.StringUtil;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.CommandUtil;
import com.hypixel.hytale.server.core.command.system.arguments.system.FlagArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncPlayerCommand;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.worldgen.chunk.ChunkGenerator;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import javax.annotation.Nonnull;
import org.joml.Vector3d;

abstract class AbstractLocateSubcommand extends AbstractAsyncPlayerCommand {
   protected static final int DEFAULT_MAX_RADIUS = 6400;
   protected static final int MAX_ALLOWED_RADIUS = 100000;
   protected static final int MIN_ALLOWED_RADIUS = 32;
   @Nonnull
   private static final Message MESSAGE_COMMANDS_LOCATE_NO_GENERATOR = Message.translation("server.commands.locate.noGenerator");
   @Nonnull
   private static final Message MESSAGE_COMMANDS_LOCATE_NO_POSITION = Message.translation("server.commands.locate.noPosition");
   @Nonnull
   private static final Message MESSAGE_COMMANDS_LOCATE_SEARCHING = Message.translation("server.commands.locate.searching");
   @Nonnull
   private static final Message MESSAGE_COMMANDS_LOCATE_INVALID_RADIUS = Message.translation("server.commands.locate.invalidRadius");
   @Nonnull
   private final FlagArg tpFlag = this.withFlagArg("tp", "server.commands.locate.tp.desc");

   AbstractLocateSubcommand(@Nonnull String name, @Nonnull String description) {
      super(name, description);
   }

   protected static int horizontalDistance(int x1, int z1, int x2, int z2) {
      long dx = (long)x2 - x1;
      long dz = (long)z2 - z1;
      return (int)Math.sqrt(dx * dx + dz * dz);
   }

   protected static void sendDidYouMean(@Nonnull CommandContext context, @Nonnull String input, @Nonnull Collection<String> validNames) {
      if (!validNames.isEmpty()) {
         List<String> suggestions = StringUtil.sortByFuzzyDistance(input, validNames, CommandUtil.RECOMMEND_COUNT);
         if (!suggestions.isEmpty()) {
            context.sendMessage(Message.translation("server.general.failed.didYouMean").param("choices", suggestions.toString()));
         }
      }
   }

   protected static int resolveRadius(@Nonnull CommandContext context, @Nonnull OptionalArg<Integer> radiusArg) {
      if (!radiusArg.provided(context)) {
         return 6400;
      } else {
         int radius = radiusArg.get(context);
         if (radius >= 32 && radius <= 100000) {
            return radius;
         } else {
            context.sendMessage(MESSAGE_COMMANDS_LOCATE_INVALID_RADIUS.param("min", 32).param("max", 100000));
            return -1;
         }
      }
   }

   private static int findSafeY(@Nonnull BlockChunk blockChunk, int x, int startY, int z) {
      int maxScan = Math.min(startY + 32, 319);

      for (int y = startY; y < maxScan; y++) {
         if (blockChunk.getBlock(x, y, z) == 0 && blockChunk.getBlock(x, y + 1, z) == 0) {
            return y;
         }
      }

      return startY + 2;
   }

   @Nonnull
   @Override
   protected final CompletableFuture<Void> executeAsync(
      @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
   ) {
      if (world.getChunkStore().getGenerator() instanceof ChunkGenerator generator) {
         TransformComponent transform = store.getComponent(ref, TransformComponent.getComponentType());
         if (transform == null) {
            context.sendMessage(MESSAGE_COMMANDS_LOCATE_NO_POSITION);
            return CompletableFuture.completedFuture(null);
         } else {
            Vector3d position = transform.getPosition();
            int playerX = (int)position.x;
            int playerZ = (int)position.z;
            int seed = (int)world.getWorldConfig().getSeed();
            context.sendMessage(MESSAGE_COMMANDS_LOCATE_SEARCHING);
            return this.runAsync(context, () -> this.execute(context, generator, seed, playerX, playerZ, world, ref), ForkJoinPool.commonPool());
         }
      } else {
         context.sendMessage(MESSAGE_COMMANDS_LOCATE_NO_GENERATOR);
         return CompletableFuture.completedFuture(null);
      }
   }

   protected abstract void execute(
      @Nonnull CommandContext var1, @Nonnull ChunkGenerator var2, int var3, int var4, int var5, @Nonnull World var6, @Nonnull Ref<EntityStore> var7
   );

   protected void teleportIfRequested(@Nonnull CommandContext context, @Nonnull World world, @Nonnull Ref<EntityStore> ref, int x, int z) {
      this.teleportIfRequested(context, world, ref, x, -1, z);
   }

   protected void teleportIfRequested(@Nonnull CommandContext context, @Nonnull World world, @Nonnull Ref<EntityStore> ref, int x, int knownY, int z) {
      if (this.tpFlag.provided(context)) {
         world.getChunkStore().getChunkReferenceAsync(ChunkUtil.indexChunkFromBlock(x, z)).thenAcceptAsync(chunkRef -> {
            BlockChunk blockChunk = world.getChunkStore().getStore().getComponent((Ref<ChunkStore>)chunkRef, BlockChunk.getComponentType());
            int y;
            if (knownY >= 0 && blockChunk != null) {
               y = findSafeY(blockChunk, x, knownY, z);
            } else {
               if (blockChunk == null) {
                  context.sendMessage(Message.translation("server.commands.locate.tp.chunkUnavailable"));
                  return;
               }

               y = blockChunk.getHeight(x, z) + 2;
            }

            Vector3d position = new Vector3d(x, y, z);
            Teleport teleport = Teleport.createForPlayer(position, new Rotation3f(0.0F, 0.0F, 0.0F));
            world.getEntityStore().getStore().addComponent(ref, Teleport.getComponentType(), teleport);
         }, world);
      }
   }
}
