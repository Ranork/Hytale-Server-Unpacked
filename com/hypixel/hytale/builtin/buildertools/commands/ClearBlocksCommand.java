package com.hypixel.hytale.builtin.buildertools.commands;

import com.hypixel.hytale.builtin.buildertools.BuilderToolsPlugin;
import com.hypixel.hytale.builtin.buildertools.PrototypePlayerBuilderToolSettings;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.arguments.types.RelativeIntPosition;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.prefab.selection.mask.BlockPattern;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class ClearBlocksCommand extends AbstractPlayerCommand {
   @Nonnull
   private static final Message MESSAGE_COMMANDS_CLEAR_NO_SELECTION = Message.translation("server.commands.clear.noSelection");
   @Nonnull
   private static final Message MESSAGE_COMMANDS_CLEAR_SUCCESS = Message.translation("server.commands.clear.success");

   public ClearBlocksCommand() {
      super("clearBlocks", "server.commands.clear.desc");
      this.setPermissionGroup(GameMode.Creative);
      this.addAliases("clear");
      this.addUsageVariant(
         new AbstractPlayerCommand("server.commands.clear.desc") {
            @Nonnull
            private final RequiredArg<RelativeIntPosition> positionOneArg = this.withRequiredArg(
               "positionOne", "server.commands.clear.positionOne.desc", ArgTypes.RELATIVE_BLOCK_POSITION
            );
            @Nonnull
            private final RequiredArg<RelativeIntPosition> positionTwoArg = this.withRequiredArg(
               "positionTwo", "server.commands.clear.positionTwo.desc", ArgTypes.RELATIVE_BLOCK_POSITION
            );

            @Override
            protected void execute(
               @Nonnull CommandContext context,
               @Nonnull Store<EntityStore> store,
               @Nonnull Ref<EntityStore> ref,
               @Nonnull PlayerRef playerRef,
               @Nonnull World world
            ) {
               ChunkStore chunkStore = world.getChunkStore();
               Player playerComponent = store.getComponent(ref, Player.getComponentType());

               assert playerComponent != null;

               TransformComponent transformComponent = store.getComponent(ref, TransformComponent.getComponentType());

               assert transformComponent != null;

               if (PrototypePlayerBuilderToolSettings.isOkayToDoCommandsOnSelection(ref, playerComponent, store)) {
                  Vector3d position = transformComponent.getPosition();
                  RelativeIntPosition relativeIntPositionOne = this.positionOneArg.get(context);
                  RelativeIntPosition relativeIntPositionTwo = this.positionTwoArg.get(context);
                  Vector3i posOne = relativeIntPositionOne.getBlockPosition(position, chunkStore);
                  Vector3i posTwo = relativeIntPositionTwo.getBlockPosition(position, chunkStore);
                  Vector3i min = Vector3i.min(posOne, posTwo);
                  Vector3i max = Vector3i.max(posOne, posTwo);
                  BuilderToolsPlugin.addToQueue(
                     playerComponent, playerRef, (r, s, componentAccessor) -> s.clear(min.x, min.y, min.z, max.x, max.y, max.z, componentAccessor)
                  );
                  playerRef.sendMessage(ClearBlocksCommand.MESSAGE_COMMANDS_CLEAR_SUCCESS);
               }
            }
         }
      );
   }

   @Override
   protected void execute(
      @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
   ) {
      Player playerComponent = store.getComponent(ref, Player.getComponentType());

      assert playerComponent != null;

      if (PrototypePlayerBuilderToolSettings.isOkayToDoCommandsOnSelection(ref, playerComponent, store)) {
         BuilderToolsPlugin.BuilderState builderState = BuilderToolsPlugin.getState(playerComponent, playerRef);
         if (builderState.getSelection() == null) {
            playerRef.sendMessage(MESSAGE_COMMANDS_CLEAR_NO_SELECTION);
         } else {
            BuilderToolsPlugin.addToQueue(playerComponent, playerRef, (r, s, componentAccessor) -> s.set(BlockPattern.EMPTY, componentAccessor));
            playerRef.sendMessage(MESSAGE_COMMANDS_CLEAR_SUCCESS);
         }
      }
   }
}
