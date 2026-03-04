package com.hypixel.hytale.server.core.universe.world.commands.block;

import com.hypixel.hytale.assetstore.map.BlockTypeAssetMap;
import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.common.util.CompletableFutureUtil;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.blockhitbox.BlockBoundingBoxes;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.modules.debug.DebugUtils;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.FillerBlockUtil;
import javax.annotation.Nonnull;

public class BlockInspectFillerCommand extends AbstractPlayerCommand {
   @Nonnull
   private static final Message MESSAGE_COMMANDS_BLOCK_INSPECT_FILLER_DONE = Message.translation("server.commands.block.inspectfiller.done");
   @Nonnull
   private static final Message MESSAGE_COMMANDS_BLOCK_INSPECT_FILLER_NO_BLOCKS = Message.translation("server.commands.block.inspectfiller.noblocks");

   public BlockInspectFillerCommand() {
      super("inspectfiller", "server.commands.block.inspectfiller.desc");
      this.setPermissionGroup(null);
   }

   @Override
   protected void execute(
      @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
   ) {
      TransformComponent transformComponent = store.getComponent(ref, TransformComponent.getComponentType());

      assert transformComponent != null;

      Vector3d position = transformComponent.getPosition();
      int x = MathUtil.floor(position.getX());
      int z = MathUtil.floor(position.getZ());
      int y = MathUtil.floor(position.getY());
      int chunkX = ChunkUtil.chunkCoordinate(x);
      int chunkY = ChunkUtil.chunkCoordinate(y);
      int chunkZ = ChunkUtil.chunkCoordinate(z);
      CompletableFutureUtil._catch(world.getChunkStore().getChunkSectionReferenceAsync(chunkX, chunkY, chunkZ).thenAcceptAsync(chunk -> {
         Store<ChunkStore> chunkStore = chunk.getStore();
         BlockSection blockSection = chunkStore.getComponent((Ref<ChunkStore>)chunk, BlockSection.getComponentType());
         if (blockSection == null) {
            playerRef.sendMessage(MESSAGE_COMMANDS_BLOCK_INSPECT_FILLER_NO_BLOCKS);
         } else {
            BlockTypeAssetMap<String, BlockType> blockTypeMap = BlockType.getAssetMap();
            IndexedLookupTableAssetMap<String, BlockBoundingBoxes> hitboxMap = BlockBoundingBoxes.getAssetMap();
            Vector3d offset = new Vector3d(ChunkUtil.minBlock(chunkX), ChunkUtil.minBlock(chunkY), ChunkUtil.minBlock(chunkZ));

            for (int idx = 0; idx < 32768; idx++) {
               int blockId = blockSection.get(idx);
               BlockType blockType = blockTypeMap.getAsset(blockId);
               if (blockType != null) {
                  BlockBoundingBoxes hitbox = hitboxMap.getAsset(blockType.getHitboxTypeIndex());
                  if (hitbox != null && hitbox.protrudesUnitBox()) {
                     int filler = blockSection.getFiller(idx);
                     int bx = ChunkUtil.xFromIndex(idx);
                     int by = ChunkUtil.yFromIndex(idx);
                     int bz = ChunkUtil.zFromIndex(idx);
                     Vector3d pos = new Vector3d(bx, by, bz);
                     pos.add(0.5, 0.5, 0.5);
                     pos.add(offset);
                     int rotation = blockSection.getRotationIndex(idx);
                     BlockBoundingBoxes.RotatedVariantBoxes rotatedHitbox = hitbox.get(rotation);
                     int fillerX = FillerBlockUtil.unpackX(filler);
                     int fillerY = FillerBlockUtil.unpackY(filler);
                     int fillerZ = FillerBlockUtil.unpackZ(filler);
                     Box boundingBox = rotatedHitbox.getBoundingBox();
                     int minX = (int)boundingBox.min.x;
                     int minY = (int)boundingBox.min.y;
                     int minZ = (int)boundingBox.min.z;
                     if (minX - boundingBox.min.x > 0.0) {
                        minX--;
                     }

                     if (minY - boundingBox.min.y > 0.0) {
                        minY--;
                     }

                     if (minZ - boundingBox.min.z > 0.0) {
                        minZ--;
                     }

                     int maxX = (int)boundingBox.max.x;
                     int maxY = (int)boundingBox.max.y;
                     int maxZ = (int)boundingBox.max.z;
                     if (boundingBox.max.x - maxX > 0.0) {
                        maxX++;
                     }

                     if (boundingBox.max.y - maxY > 0.0) {
                        maxY++;
                     }

                     if (boundingBox.max.z - maxZ > 0.0) {
                        maxZ++;
                     }

                     Vector3f colour = new Vector3f();
                     colour.x = (float)(fillerX - minX) / (maxX - minX);
                     colour.y = (float)(fillerY - minY) / (maxY - minY);
                     colour.z = (float)(fillerZ - minZ) / (maxZ - minZ);
                     DebugUtils.addCube(chunkStore.getExternalData().getWorld(), pos, colour, 1.05, 30.0F);
                  }
               }
            }

            playerRef.sendMessage(MESSAGE_COMMANDS_BLOCK_INSPECT_FILLER_DONE);
         }
      }, world));
   }
}
