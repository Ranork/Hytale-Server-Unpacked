package com.hypixel.hytale.server.core.universe.world.commands.block.bulk;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.iterator.SpiralIterator;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.DefaultArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.FlagArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nonnull;

public class BlockBulkFindHereCommand extends AbstractPlayerCommand {
   @Nonnull
   private final FlagArg printNameArg = this.withFlagArg("print", "server.commands.block.find-here.print.desc");
   @Nonnull
   private final RequiredArg<String> blockTypeArg = this.withRequiredArg("block", "server.commands.block.find-here.block.desc", ArgTypes.BLOCK_TYPE_KEY);
   @Nonnull
   private final DefaultArg<Integer> radiusArg = this.withDefaultArg(
      "radius", "server.commands.block.find-here.radius.desc", ArgTypes.INTEGER, 3, "server.commands.block.bulk.find-here.radius.default"
   );

   public BlockBulkFindHereCommand() {
      super("find-here", "server.commands.block.find-here.desc");
   }

   @Override
   protected void execute(
      @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
   ) {
      String blockTypeKey = this.blockTypeArg.get(context);
      int blockId = BlockType.getAssetMap().getIndex(blockTypeKey);
      IntList blockIdList = BlockBulkReplaceCommand.getBlockIdList(blockId);
      int radius = this.radiusArg.get(context);
      boolean printBlockName = this.printNameArg.get(context);
      TransformComponent transformComponent = store.getComponent(ref, TransformComponent.getComponentType());

      assert transformComponent != null;

      Vector3d playerPos = transformComponent.getPosition();
      int originChunkX = MathUtil.floor(playerPos.getX()) >> 5;
      int originChunkZ = MathUtil.floor(playerPos.getZ()) >> 5;
      CompletableFuture.runAsync(
         () -> {
            long start = System.nanoTime();
            IntOpenHashSet temp = new IntOpenHashSet();
            ChunkStore chunkComponentStore = world.getChunkStore();
            AtomicInteger found = new AtomicInteger();
            SpiralIterator iterator = new SpiralIterator(originChunkX, originChunkZ, radius);

            while (iterator.hasNext()) {
               long key = iterator.next();
               BlockChunk blockChunk = chunkComponentStore.getChunkReferenceAsync(key)
                  .thenApplyAsync(chunkRef -> chunkComponentStore.getStore().getComponent((Ref<ChunkStore>)chunkRef, BlockChunk.getComponentType()), world)
                  .join();

               for (int sectionIndex = 0; sectionIndex < 10; sectionIndex++) {
                  BlockSection section = blockChunk.getSectionAtIndex(sectionIndex);
                  if (section.containsAny(blockIdList)) {
                     section.find(blockIdList, temp, blockIndex -> found.getAndIncrement());
                     temp.clear();
                  }
               }
            }

            long diff = System.nanoTime() - start;
            BlockType findBlock = BlockType.getAssetMap().getAsset(blockId);
            String blockName = printBlockName ? " " + findBlock.getId() : "";
            playerRef.sendMessage(
               Message.translation("server.commands.block.find-here.result")
                  .param("count", found.get())
                  .param("blockName", blockName)
                  .param("time", TimeUnit.NANOSECONDS.toSeconds(diff))
            );
         }
      );
   }
}
