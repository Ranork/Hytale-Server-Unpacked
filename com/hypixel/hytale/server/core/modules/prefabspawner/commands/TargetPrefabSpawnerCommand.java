package com.hypixel.hytale.server.core.modules.prefabspawner.commands;

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
import com.hypixel.hytale.server.core.modules.prefabspawner.PrefabSpawnerState;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.meta.BlockState;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import javax.annotation.Nonnull;

public abstract class TargetPrefabSpawnerCommand extends AbstractWorldCommand {
   @Nonnull
   private static final Message MESSAGE_GENERAL_BLOCK_TARGET_NOT_IN_RANGE = Message.translation("server.general.blockTargetNotInRange");
   @Nonnull
   private static final Message MESSAGE_COMMANDS_ERRORS_PROVIDE_POSITION = Message.translation("server.commands.errors.providePosition");
   @Nonnull
   protected final OptionalArg<RelativeIntPosition> positionArg = this.withOptionalArg(
      "position", "server.commands.prefabspawner.position.desc", ArgTypes.RELATIVE_BLOCK_POSITION
   );

   public TargetPrefabSpawnerCommand(@Nonnull String name, @Nonnull String description) {
      super(name, description);
   }

   @Override
   protected void execute(@Nonnull CommandContext context, @Nonnull World world, @Nonnull Store<EntityStore> store) {
      Vector3i target;
      if (this.positionArg.provided(context)) {
         RelativeIntPosition relativePosition = this.positionArg.get(context);
         target = relativePosition.getBlockPosition(context, store);
      } else {
         if (!context.isPlayer()) {
            throw new GeneralCommandException(MESSAGE_COMMANDS_ERRORS_PROVIDE_POSITION);
         }

         Ref<EntityStore> playerRef = context.senderAsPlayerRef();
         Vector3i targetBlock = TargetUtil.getTargetBlock(playerRef, 10.0, store);
         if (targetBlock == null) {
            throw new GeneralCommandException(MESSAGE_GENERAL_BLOCK_TARGET_NOT_IN_RANGE);
         }

         target = targetBlock;
      }

      WorldChunk chunk = world.getChunk(ChunkUtil.indexChunkFromBlock(target.x, target.z));
      BlockState state = chunk.getState(target.x, target.y, target.z);
      if (!(state instanceof PrefabSpawnerState)) {
         context.sendMessage(Message.translation("server.commands.prefabspawner.spawnerNotFoundAtTarget").param("pos", target.toString()));
      } else {
         this.execute(context, chunk, (PrefabSpawnerState)state);
      }
   }

   protected abstract void execute(@Nonnull CommandContext var1, @Nonnull WorldChunk var2, @Nonnull PrefabSpawnerState var3);
}
