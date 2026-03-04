package com.hypixel.hytale.server.core.universe.world.commands.block;

import com.hypixel.hytale.assetstore.map.BlockTypeAssetMap;
import com.hypixel.hytale.common.util.CompletableFutureUtil;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.Rotation;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.RotationTuple;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.modules.debug.DebugUtils;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class BlockInspectRotationCommand extends AbstractPlayerCommand {
   @Nonnull
   private static final Message MESSAGE_COMMANDS_BLOCK_INSPECT_ROTATION_NO_BLOCKS = Message.translation("server.commands.block.inspectrotation.noblocks");
   @Nonnull
   private static final Message MESSAGE_COMMANDS_BLOCK_INSPECT_ROTATION_DONE = Message.translation("server.commands.block.inspectrotation.done");

   public BlockInspectRotationCommand() {
      super("inspectrotation", "server.commands.block.inspectrotation.desc");
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
            playerRef.sendMessage(MESSAGE_COMMANDS_BLOCK_INSPECT_ROTATION_NO_BLOCKS);
         } else {
            BlockTypeAssetMap<String, BlockType> blockTypeMap = BlockType.getAssetMap();
            Vector3d offset = new Vector3d(ChunkUtil.minBlock(chunkX), ChunkUtil.minBlock(chunkY), ChunkUtil.minBlock(chunkZ));

            for (int idx = 0; idx < 32768; idx++) {
               int blockId = blockSection.get(idx);
               BlockType blockType = blockTypeMap.getAsset(blockId);
               if (blockType != null) {
                  int bx = ChunkUtil.xFromIndex(idx);
                  int by = ChunkUtil.yFromIndex(idx);
                  int bz = ChunkUtil.zFromIndex(idx);
                  Vector3d pos = new Vector3d(bx, by, bz);
                  pos.add(0.5, 0.5, 0.5);
                  pos.add(offset);
                  RotationTuple rotation = blockSection.getRotation(idx);
                  if (rotation.index() != 0) {
                     Vector3f colour = new Vector3f();
                     colour.x = rotation.yaw().ordinal() / (Rotation.VALUES.length - 1.0F);
                     colour.y = rotation.pitch().ordinal() / (Rotation.VALUES.length - 1.0F);
                     colour.z = rotation.roll().ordinal() / (Rotation.VALUES.length - 1.0F);
                     DebugUtils.addCube(chunkStore.getExternalData().getWorld(), pos, colour, 1.05, 30.0F);
                  }
               }
            }

            playerRef.sendMessage(MESSAGE_COMMANDS_BLOCK_INSPECT_ROTATION_DONE);
         }
      }, world));
   }
}
