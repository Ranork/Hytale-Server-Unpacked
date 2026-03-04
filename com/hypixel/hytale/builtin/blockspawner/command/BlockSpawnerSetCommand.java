package com.hypixel.hytale.builtin.blockspawner.command;

import com.hypixel.hytale.builtin.blockspawner.BlockSpawnerTable;
import com.hypixel.hytale.builtin.blockspawner.state.BlockSpawner;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.FlagArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.arguments.types.AssetArgumentType;
import com.hypixel.hytale.server.core.command.system.arguments.types.RelativeIntPosition;
import com.hypixel.hytale.server.core.command.system.arguments.types.SingleArgumentType;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractWorldCommand;
import com.hypixel.hytale.server.core.command.system.exceptions.GeneralCommandException;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import javax.annotation.Nonnull;

public class BlockSpawnerSetCommand extends AbstractWorldCommand {
   @Nonnull
   private static final Message MESSAGE_GENERAL_BLOCK_TARGET_NOT_IN_RANGE = Message.translation("server.general.blockTargetNotInRange");
   @Nonnull
   private static final Message MESSAGE_COMMANDS_ERRORS_PROVIDE_POSITION = Message.translation("server.commands.errors.providePosition");
   @Nonnull
   private static final Message MESSAGE_COMMANDS_ERRORS_PLAYER_NOT_IN_WORLD = Message.translation("server.commands.errors.playerNotInWorld");
   @Nonnull
   private static final SingleArgumentType<BlockSpawnerTable> BLOCK_SPAWNER_ASSET_TYPE = new AssetArgumentType(
      "server.commands.parsing.argtype.asset.blockspawnertable.name", BlockSpawnerTable.class, "server.commands.parsing.argtype.asset.blockspawnertable.usage"
   );
   @Nonnull
   private final RequiredArg<BlockSpawnerTable> blockSpawnerIdArg = this.withRequiredArg(
      "blockSpawnerId", "server.commands.blockspawner.set.blockSpawnerId.desc", BLOCK_SPAWNER_ASSET_TYPE
   );
   @Nonnull
   private final OptionalArg<RelativeIntPosition> positionArg = this.withOptionalArg(
      "position", "server.commands.blockspawner.position.desc", ArgTypes.RELATIVE_BLOCK_POSITION
   );
   @Nonnull
   private final FlagArg ignoreChecksFlag = this.withFlagArg("ignoreChecks", "server.commands.blockspawner.arg.ignoreChecks");

   public BlockSpawnerSetCommand() {
      super("set", "server.commands.blockspawner.set.desc");
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

      WorldChunk worldChunk = world.getChunk(ChunkUtil.indexChunkFromBlock(position.x, position.z));
      if (worldChunk == null) {
         context.sendMessage(Message.translation("server.general.containerNotFound").param("block", position.toString()));
      } else {
         Ref<ChunkStore> blockRef = worldChunk.getBlockComponentEntity(position.x, position.y, position.z);
         if (blockRef != null && blockRef.isValid()) {
            Store<ChunkStore> chunkStore = world.getChunkStore().getStore();
            BlockSpawner spawnerState = chunkStore.getComponent(blockRef, BlockSpawner.getComponentType());
            if (spawnerState == null) {
               context.sendMessage(Message.translation("server.general.containerNotFound").param("block", position.toString()));
            } else {
               String spawnerId;
               if (this.ignoreChecksFlag.get(context)) {
                  String[] input = context.getInput(this.blockSpawnerIdArg);
                  spawnerId = input != null && input.length > 0 ? input[0] : null;
                  if (spawnerId == null) {
                     context.sendMessage(
                        Message.translation("errors.validation_failure").param("message", "blockSpawnerId is required when --ignoreChecks is set")
                     );
                     return;
                  }
               } else {
                  spawnerId = this.blockSpawnerIdArg.get(context).getId();
               }

               spawnerState.setBlockSpawnerId(spawnerId);
               worldChunk.markNeedsSaving();
               context.sendMessage(Message.translation("server.commands.blockspawner.blockSpawnerSet").param("id", spawnerId));
            }
         } else {
            context.sendMessage(Message.translation("server.general.containerNotFound").param("block", position.toString()));
         }
      }
   }
}
