package com.hypixel.hytale.builtin.teleport.commands.teleport;

import com.hypixel.hytale.builtin.teleport.components.TeleportHistory;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.permissions.HytalePermissions;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class TeleportTopCommand extends AbstractPlayerCommand {
   @Nonnull
   private static final Message MESSAGE_COMMANDS_TELEPORT_TOP_CHUNK_NOT_LOADED_AT_POS = Message.translation("server.commands.teleport.top.chunkNotLoadedAtPos");
   @Nonnull
   private static final Message MESSAGE_COMMANDS_TELEPORT_TELEPORTED_TO_TOP = Message.translation("server.commands.teleport.teleportedToTop");
   private static final String TELEPORT_HISTORY_KEY = "Underground";

   public TeleportTopCommand() {
      super("top", "server.commands.top.desc");
      this.requirePermission(HytalePermissions.fromCommand("teleport.top"));
   }

   @Override
   protected void execute(
      @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
   ) {
      TransformComponent transformComponent = store.getComponent(ref, TransformComponent.getComponentType());

      assert transformComponent != null;

      Vector3d position = transformComponent.getPosition();
      WorldChunk worldChunk = world.getChunkIfInMemory(ChunkUtil.indexChunkFromBlock(position.getX(), position.getZ()));
      if (worldChunk == null) {
         context.sendMessage(MESSAGE_COMMANDS_TELEPORT_TOP_CHUNK_NOT_LOADED_AT_POS);
      } else {
         HeadRotation headRotationComponent = store.getComponent(ref, HeadRotation.getComponentType());

         assert headRotationComponent != null;

         Vector3f headRotation = headRotationComponent.getRotation().clone();
         int height = worldChunk.getHeight(MathUtil.floor(position.getX()), MathUtil.floor(position.getZ()));
         store.ensureAndGetComponent(ref, TeleportHistory.getComponentType()).append(world, position.clone(), headRotation.clone(), "Underground");
         store.addComponent(
            ref, Teleport.getComponentType(), Teleport.createForPlayer(new Vector3d(position.getX(), height + 2, position.getZ()), Vector3f.NaN)
         );
         context.sendMessage(MESSAGE_COMMANDS_TELEPORT_TELEPORTED_TO_TOP);
      }
   }
}
