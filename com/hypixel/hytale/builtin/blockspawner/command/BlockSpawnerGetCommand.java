package com.hypixel.hytale.builtin.blockspawner.command;

import com.hypixel.hytale.builtin.blockspawner.state.BlockSpawner;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.arguments.types.RelativeIntPosition;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractWorldCommand;
import com.hypixel.hytale.server.core.command.system.exceptions.GeneralCommandException;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import javax.annotation.Nonnull;

public class BlockSpawnerGetCommand extends AbstractWorldCommand {
   @Nonnull
   private static final Message MESSAGE_GENERAL_BLOCK_TARGET_NOT_IN_RANGE = Message.translation("server.general.blockTargetNotInRange");
   @Nonnull
   private static final Message MESSAGE_COMMANDS_ERRORS_PROVIDE_POSITION = Message.translation("server.commands.errors.providePosition");
   @Nonnull
   private static final Message MESSAGE_COMMANDS_ERRORS_PLAYER_NOT_IN_WORLD = Message.translation("server.commands.errors.playerNotInWorld");
   @Nonnull
   private static final Message MESSAGE_COMMANDS_BLOCK_SPAWNER_NO_BLOCK_SPAWNER_SET = Message.translation("server.commands.blockspawner.noBlockSpawnerSet");
   @Nonnull
   private final OptionalArg<RelativeIntPosition> positionArg = this.withOptionalArg(
      "position", "server.commands.blockspawner.position.desc", ArgTypes.RELATIVE_BLOCK_POSITION
   );

   public BlockSpawnerGetCommand() {
      super("get", "server.commands.blockspawner.get.desc");
   }

   @Override
   protected void execute(@Nonnull CommandContext context, @Nonnull World world, @Nonnull Store<EntityStore> store) {
      Vector3i position;
      if (this.positionArg.provided(context)) {
         RelativeIntPosition relativePosition = this.positionArg.get(context);
         position = relativePosition.getBlockPosition(context, store);
      } else {
         if (!context.isPlayer()) {
            throw new GeneralCommandException(MESSAGE_COMMANDS_ERRORS_PROVIDE_POSITION);
         }

         Ref<EntityStore> ref = context.senderAsPlayerRef();
         if (ref == null || !ref.isValid()) {
            throw new GeneralCommandException(MESSAGE_COMMANDS_ERRORS_PLAYER_NOT_IN_WORLD);
         }

         Vector3i targetBlock = TargetUtil.getTargetBlock(ref, 10.0, store);
         if (targetBlock == null) {
            throw new GeneralCommandException(MESSAGE_GENERAL_BLOCK_TARGET_NOT_IN_RANGE);
         }

         position = targetBlock;
      }

      ChunkStore chunkStore = world.getChunkStore();
      long chunkIndex = ChunkUtil.indexChunkFromBlock(position.x, position.z);
      Ref<ChunkStore> chunkRef = chunkStore.getChunkReference(chunkIndex);
      if (chunkRef != null && chunkRef.isValid()) {
         WorldChunk worldChunkComponent = chunkStore.getStore().getComponent(chunkRef, WorldChunk.getComponentType());

         assert worldChunkComponent != null;

         Ref<ChunkStore> blockRef = worldChunkComponent.getBlockComponentEntity(position.x, position.y, position.z);
         if (blockRef != null && blockRef.isValid()) {
            BlockSpawner spawnerState = chunkStore.getStore().getComponent(blockRef, BlockSpawner.getComponentType());
            if (spawnerState == null) {
               context.sendMessage(Message.translation("server.general.containerNotFound").param("block", position.toString()));
            } else {
               if (spawnerState.getBlockSpawnerId() == null) {
                  context.sendMessage(MESSAGE_COMMANDS_BLOCK_SPAWNER_NO_BLOCK_SPAWNER_SET);
               } else {
                  context.sendMessage(Message.translation("server.commands.blockspawner.currentBlockSpawner").param("id", spawnerState.getBlockSpawnerId()));
               }
            }
         } else {
            context.sendMessage(Message.translation("server.general.containerNotFound").param("block", position.toString()));
         }
      } else {
         context.sendMessage(Message.translation("server.general.containerNotFound").param("block", position.toString()));
      }
   }
}
