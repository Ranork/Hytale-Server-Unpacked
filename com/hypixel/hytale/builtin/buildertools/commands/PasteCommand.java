package com.hypixel.hytale.builtin.buildertools.commands;

import com.hypixel.hytale.builtin.buildertools.BuilderToolsPlugin;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.FlagArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.arguments.types.RelativeIntPosition;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class PasteCommand extends AbstractPlayerCommand {
   @Nonnull
   private final FlagArg technicalFlag = this.withFlagArg("technical", "server.commands.paste.technical.desc");

   public PasteCommand() {
      super("paste", "server.commands.paste.desc");
      this.setPermissionGroup(GameMode.Creative);
      this.requirePermission("hytale.editor.selection.clipboard");
      this.addUsageVariant(new PasteCommand.PasteAtPositionCommand());
   }

   @Override
   protected void execute(
      @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
   ) {
      ChunkStore chunkStore = world.getChunkStore();
      Player playerComponent = store.getComponent(ref, Player.getComponentType());

      assert playerComponent != null;

      TransformComponent transformComponent = store.getComponent(ref, TransformComponent.getComponentType());

      assert transformComponent != null;

      Vector3d position = transformComponent.getPosition();
      int x = MathUtil.floor(position.x);
      int y = MathUtil.floor(position.y);
      int z = MathUtil.floor(position.z);
      boolean technical = this.technicalFlag.get(context);
      BuilderToolsPlugin.addToQueue(playerComponent, playerRef, (r, s, componentAccessor) -> s.paste(r, x, y, z, technical, componentAccessor));
   }

   private static class PasteAtPositionCommand extends AbstractPlayerCommand {
      @Nonnull
      private final RequiredArg<RelativeIntPosition> positionArg = this.withRequiredArg(
         "position", "server.commands.paste.position.desc", ArgTypes.RELATIVE_BLOCK_POSITION
      );
      @Nonnull
      private final FlagArg technicalFlag = this.withFlagArg("technical", "server.commands.paste.technical.desc");

      public PasteAtPositionCommand() {
         super("server.commands.paste.desc");
      }

      @Override
      protected void execute(
         @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
      ) {
         ChunkStore chunkStore = world.getChunkStore();
         Player playerComponent = store.getComponent(ref, Player.getComponentType());

         assert playerComponent != null;

         TransformComponent transformComponent = store.getComponent(ref, TransformComponent.getComponentType());

         assert transformComponent != null;

         Vector3d position = transformComponent.getPosition();
         RelativeIntPosition relativePos = this.positionArg.get(context);
         Vector3i blockPos = relativePos.getBlockPosition(position, chunkStore);
         boolean technical = this.technicalFlag.get(context);
         BuilderToolsPlugin.addToQueue(
            playerComponent, playerRef, (r, s, componentAccessor) -> s.paste(r, blockPos.x, blockPos.y, blockPos.z, technical, componentAccessor)
         );
      }
   }
}
