package com.hypixel.hytale.server.core.universe.world.commands.block;

import com.hypixel.hytale.common.util.CompletableFutureUtil;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.blocktype.component.BlockPhysics;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.FlagArg;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.modules.debug.DebugUtils;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class BlockInspectPhysicsCommand extends AbstractPlayerCommand {
   @Nonnull
   private static final Message MESSAGE_COMMANDS_BLOCK_INSPECT_PHYS_NO_BLOCKS = Message.translation("server.commands.block.inspectphys.noblocks");
   @Nonnull
   private static final Message MESSAGE_COMMANDS_BLOCK_INSPECT_PHYS_DONE = Message.translation("server.commands.block.inspectphys.done");
   private final FlagArg ALL = this.withFlagArg("all", "server.commands.block.inspectphys.all.desc");

   public BlockInspectPhysicsCommand() {
      super("inspectphys", "server.commands.block.inspectphys.desc");
      this.setPermissionGroup(null);
   }

   @Override
   protected void execute(
      @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
   ) {
      TransformComponent transformComponent = store.getComponent(ref, TransformComponent.getComponentType());

      assert transformComponent != null;

      Vector3d position = transformComponent.getPosition();
      boolean all = this.ALL.get(context);
      int x = MathUtil.floor(position.getX());
      int z = MathUtil.floor(position.getZ());
      int y = MathUtil.floor(position.getY());
      int chunkX = ChunkUtil.chunkCoordinate(x);
      int chunkY = ChunkUtil.chunkCoordinate(y);
      int chunkZ = ChunkUtil.chunkCoordinate(z);
      CompletableFutureUtil._catch(world.getChunkStore().getChunkSectionReferenceAsync(chunkX, chunkY, chunkZ).thenAcceptAsync(chunk -> {
         Store<ChunkStore> chunkStore = chunk.getStore();
         BlockPhysics blockPhysics = chunkStore.getComponent((Ref<ChunkStore>)chunk, BlockPhysics.getComponentType());
         if (blockPhysics == null) {
            playerRef.sendMessage(MESSAGE_COMMANDS_BLOCK_INSPECT_PHYS_NO_BLOCKS);
         } else {
            Vector3d offset = new Vector3d(ChunkUtil.minBlock(chunkX), ChunkUtil.minBlock(chunkY), ChunkUtil.minBlock(chunkZ));

            for (int idx = 0; idx < 32768; idx++) {
               int supportValue = blockPhysics.get(idx);
               if (supportValue != 0 || all) {
                  int bx = ChunkUtil.xFromIndex(idx);
                  int by = ChunkUtil.yFromIndex(idx);
                  int bz = ChunkUtil.zFromIndex(idx);
                  Vector3d pos = new Vector3d(bx, by, bz);
                  pos.add(0.5, 0.5, 0.5);
                  pos.add(offset);
                  Vector3f colour;
                  if (supportValue == 15) {
                     colour = new Vector3f(0.0F, 1.0F, 0.0F);
                  } else {
                     BlockType block = world.getBlockType(pos.toVector3i());
                     if (!block.hasSupport()) {
                        if (supportValue == 0) {
                           continue;
                        }

                        colour = new Vector3f(1.0F, 1.0F, 0.0F);
                     } else if (block.getMaxSupportDistance() != 0) {
                        float len = (float)supportValue / block.getMaxSupportDistance();
                        colour = new Vector3f(len, 0.0F, 1.0F - len);
                     } else {
                        colour = new Vector3f(0.0F, 1.0F, 1.0F);
                     }
                  }

                  DebugUtils.addCube(chunkStore.getExternalData().getWorld(), pos, colour, 1.05, 30.0F);
               }
            }

            playerRef.sendMessage(MESSAGE_COMMANDS_BLOCK_INSPECT_PHYS_DONE);
         }
      }, world));
   }
}
