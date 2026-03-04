package com.hypixel.hytale.server.core.universe.world.commands.block.bulk;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.iterator.SpiralIterator;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.arguments.types.IntCoord;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractWorldCommand;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntLists;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;

public class BlockBulkFindCommand extends AbstractWorldCommand {
   @Nonnull
   private static final Message MESSAGE_COMMANDS_BLOCK_FIND_TIME_OUT = Message.translation("server.commands.block.find.timeout");
   @Nonnull
   private static final Message MESSAGE_COMMANDS_BLOCK_FIND_DONE = Message.translation("server.commands.block.find.done");
   @Nonnull
   private final RequiredArg<IntCoord> chunkXArg = this.withRequiredArg("chunkX", "server.commands.block.find.chunkX.desc", ArgTypes.RELATIVE_INT_COORD);
   @Nonnull
   private final RequiredArg<IntCoord> chunkZArg = this.withRequiredArg("chunkZ", "server.commands.block.find.chunkZ.desc", ArgTypes.RELATIVE_INT_COORD);
   @Nonnull
   private final RequiredArg<String> blockTypeArg = this.withRequiredArg("block", "server.commands.block.find.block.desc", ArgTypes.BLOCK_TYPE_KEY);
   @Nonnull
   private final RequiredArg<Integer> countArg = this.withRequiredArg("count", "server.commands.block.find.count.desc", ArgTypes.INTEGER);
   @Nonnull
   private final RequiredArg<Integer> timeoutArg = this.withRequiredArg("timeout", "server.commands.block.find.timeout.desc", ArgTypes.INTEGER);

   public BlockBulkFindCommand() {
      super("find", "server.commands.block.find.desc", true);
   }

   @Override
   protected void execute(@Nonnull CommandContext context, @Nonnull World world, @Nonnull Store<EntityStore> store) {
      CommandSender sender = context.sender();
      IntCoord relChunkX = this.chunkXArg.get(context);
      IntCoord relChunkZ = this.chunkZArg.get(context);
      int baseChunkX = 0;
      int baseChunkZ = 0;
      if (context.isPlayer()) {
         Ref<EntityStore> playerRef = context.senderAsPlayerRef();
         if (playerRef != null) {
            TransformComponent transformComponent = store.getComponent(playerRef, TransformComponent.getComponentType());

            assert transformComponent != null;

            Vector3d playerPos = transformComponent.getPosition();
            baseChunkX = MathUtil.floor(playerPos.getX()) >> 5;
            baseChunkZ = MathUtil.floor(playerPos.getZ()) >> 5;
         }
      }

      int originChunkX = relChunkX.resolveXZ(baseChunkX);
      int originChunkZ = relChunkZ.resolveXZ(baseChunkZ);
      String blockTypeKey = this.blockTypeArg.get(context);
      int blockId = BlockType.getAssetMap().getIndex(blockTypeKey);
      IntList idAsList = IntLists.singleton(blockId);
      int count = this.countArg.get(context);
      int timeout = this.timeoutArg.get(context);
      CompletableFuture.runAsync(
         () -> {
            long start = System.nanoTime();
            int tested = 0;
            int[] found = new int[]{0};
            IntOpenHashSet temp = new IntOpenHashSet();
            ChunkStore chunkComponentStore = world.getChunkStore();
            SpiralIterator iterator = new SpiralIterator(originChunkX, originChunkZ, SpiralIterator.MAX_RADIUS);

            label34:
            while (iterator.hasNext()) {
               long key = iterator.next();
               BlockChunk blockChunk = chunkComponentStore.getChunkReferenceAsync(key)
                  .thenApplyAsync(chunkRef -> chunkComponentStore.getStore().getComponent((Ref<ChunkStore>)chunkRef, BlockChunk.getComponentType()), world)
                  .join();

               for (int sectionIndex = 0; sectionIndex < 10; sectionIndex++) {
                  BlockSection section = blockChunk.getSectionAtIndex(sectionIndex);
                  if (section.contains(blockId)) {
                     int chunkX = ChunkUtil.xOfChunkIndex(key);
                     int chunkY = sectionIndex;
                     int chunkZ = ChunkUtil.zOfChunkIndex(key);
                     section.find(idAsList, temp, blockIndex -> {
                        if (found[0] < count) {
                           found[0]++;
                           int x = chunkX << 5 | ChunkUtil.xFromIndex(blockIndex);
                           int y = chunkY << 5 | ChunkUtil.yFromIndex(blockIndex);
                           int z = chunkZ << 5 | ChunkUtil.zFromIndex(blockIndex);
                           sender.sendMessage(Message.translation("server.commands.block.find.blockFound").param("x", x).param("y", y).param("z", z));
                        }
                     });
                     if (found[0] >= count) {
                        break label34;
                     }

                     temp.clear();
                  }
               }

               if (++tested % 100 == 0) {
                  sender.sendMessage(Message.translation("server.commands.block.find.chunksTested").param("nb", tested));
               }

               long diff = System.nanoTime() - start;
               if (diff > TimeUnit.SECONDS.toNanos(timeout)) {
                  sender.sendMessage(MESSAGE_COMMANDS_BLOCK_FIND_TIME_OUT);
                  return;
               }
            }

            sender.sendMessage(MESSAGE_COMMANDS_BLOCK_FIND_DONE);
         }
      );
   }
}
