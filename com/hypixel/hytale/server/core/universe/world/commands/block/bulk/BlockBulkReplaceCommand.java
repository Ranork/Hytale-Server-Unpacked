package com.hypixel.hytale.server.core.universe.world.commands.block.bulk;

import com.hypixel.hytale.common.util.CompletableFutureUtil;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.iterator.SpiralIterator;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.RotationTuple;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.VariantRotation;
import com.hypixel.hytale.server.core.command.system.CommandContext;
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
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nonnull;

public class BlockBulkReplaceCommand extends AbstractPlayerCommand {
   @Nonnull
   private final RequiredArg<String> findArg = this.withRequiredArg("find", "server.commands.block.bulk.replace.find.desc", ArgTypes.BLOCK_TYPE_KEY);
   @Nonnull
   private final RequiredArg<String> replaceArg = this.withRequiredArg(
      "replaceWith", "server.commands.block.bulk.replace.replaceWith.desc", ArgTypes.BLOCK_TYPE_KEY
   );
   @Nonnull
   private final RequiredArg<Integer> radiusArg = this.withRequiredArg("radius", "server.commands.block.bulk.replace.radius.desc", ArgTypes.INTEGER);

   public BlockBulkReplaceCommand() {
      super("replace", "server.commands.block.bulk.replace.desc");
   }

   @Override
   protected void execute(
      @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
   ) {
      TransformComponent transformComponent = store.getComponent(ref, TransformComponent.getComponentType());

      assert transformComponent != null;

      String findBlockTypeKey = this.findArg.get(context);
      String replaceBlockTypeKey = this.replaceArg.get(context);
      int findBlockId = BlockType.getAssetMap().getIndex(findBlockTypeKey);
      int replaceBlockId = BlockType.getAssetMap().getIndex(replaceBlockTypeKey);
      IntList findBlockIdList = getBlockIdList(findBlockId);
      IntList replaceBlockIdList = getBlockIdList(replaceBlockId);
      int radius = this.radiusArg.get(context);
      Vector3d playerPos = transformComponent.getPosition();
      int originChunkX = MathUtil.floor(playerPos.getX()) >> 5;
      int originChunkZ = MathUtil.floor(playerPos.getZ()) >> 5;
      CompletableFuture.runAsync(
         () -> {
            long start = System.nanoTime();
            IntOpenHashSet temp = new IntOpenHashSet();
            ChunkStore chunkComponentStore = world.getChunkStore();
            AtomicInteger replaced = new AtomicInteger();
            SpiralIterator iterator = new SpiralIterator(originChunkX, originChunkZ, radius);

            while (iterator.hasNext()) {
               long key = iterator.next();
               BlockChunk blockChunk = chunkComponentStore.getChunkReferenceAsync(key)
                  .thenApplyAsync(chunkRef -> chunkComponentStore.getStore().getComponent((Ref<ChunkStore>)chunkRef, BlockChunk.getComponentType()), world)
                  .join();

               for (int sectionIndex = 0; sectionIndex < 10; sectionIndex++) {
                  BlockSection section = blockChunk.getSectionAtIndex(sectionIndex);
                  if (section.containsAny(findBlockIdList)) {
                     int chunkX = ChunkUtil.xOfChunkIndex(key);
                     int chunkY = sectionIndex;
                     int chunkZ = ChunkUtil.zOfChunkIndex(key);
                     section.find(findBlockIdList, temp, blockIndex -> {
                        int x = chunkX << 5 | ChunkUtil.xFromIndex(blockIndex);
                        int y = chunkY << 5 | ChunkUtil.yFromIndex(blockIndex);
                        int z = chunkZ << 5 | ChunkUtil.zFromIndex(blockIndex);
                        CompletableFutureUtil._catch(world.getChunkAsync(ChunkUtil.indexChunkFromBlock(x, z))).thenAccept(chunk -> {
                           int foundBlock = chunk.getBlock(x, y, z);
                           int replaceIndex = findBlockIdList.indexOf(foundBlock);
                           chunk.setBlock(x, y, z, replaceBlockIdList.getInt(replaceIndex));
                           replaced.getAndIncrement();
                        });
                     });
                  }
               }
            }

            long diff = System.nanoTime() - start;
            playerRef.sendMessage(
               Message.translation("server.commands.block.bulk.replace.result")
                  .param("count", replaced.get())
                  .param("time", TimeUnit.NANOSECONDS.toSeconds(diff))
            );
         }
      );
   }

   @Nonnull
   protected static IntList getBlockIdList(int blockId) {
      IntList blockIdList = new IntArrayList();
      BlockType findBlock = BlockType.getAssetMap().getAsset(blockId);
      if (findBlock.getVariantRotation().equals(VariantRotation.NESW)) {
         blockIdList = createNESWRotationLists(findBlock, blockIdList);
      } else {
         blockIdList.add(blockId);
      }

      return blockIdList;
   }

   @Nonnull
   private static IntList createNESWRotationLists(@Nonnull BlockType block, @Nonnull IntList blockIdList) {
      RotationTuple[] rotations = block.getVariantRotation().getRotations();
      String blockName = block.getId();
      blockIdList.add(BlockType.getAssetMap().getIndex(blockName));

      for (RotationTuple rp : rotations) {
         String newBlockRotation = blockName + "|Yaw=" + rp.yaw().getDegrees();
         blockIdList.add(BlockType.getAssetMap().getIndex(newBlockRotation));
      }

      return blockIdList;
   }
}
