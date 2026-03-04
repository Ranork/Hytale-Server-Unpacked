package com.hypixel.hytale.server.core.command.commands.player;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.Axis;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.permissions.HytalePermissions;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class WhereAmICommand extends AbstractPlayerCommand {
   @Nonnull
   private static final Message MESSAGE_COMMANDS_WHERE_AM_I_CHUNK_NOT_LOADED = Message.translation("server.commands.whereami.chunkNotLoaded");

   public WhereAmICommand() {
      super("whereami", "server.commands.whereami.desc");
      this.setPermissionGroup(GameMode.Creative);
      this.requirePermission(HytalePermissions.fromCommand("whereami.self"));
      this.addUsageVariant(new WhereAmICommand.WhereAmIOtherCommand());
   }

   @Override
   protected void execute(
      @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
   ) {
      sendLocationInfo(context, store, ref, world, null);
   }

   private static void sendLocationInfo(
      @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull World world, @Nullable String targetUsername
   ) {
      TransformComponent transformComponent = store.getComponent(ref, TransformComponent.getComponentType());

      assert transformComponent != null;

      HeadRotation headRotationComponent = store.getComponent(ref, HeadRotation.getComponentType());

      assert headRotationComponent != null;

      Vector3d position = transformComponent.getPosition();
      Vector3f headRotation = headRotationComponent.getRotation();
      Vector3i axisDirection = headRotationComponent.getAxisDirection();
      Axis axis = headRotationComponent.getAxis();
      Vector3d direction = headRotationComponent.getDirection();
      int chunkX = MathUtil.floor(position.getX()) >> 5;
      int chunkY = MathUtil.floor(position.getY()) >> 5;
      int chunkZ = MathUtil.floor(position.getZ()) >> 5;
      long chunkIndex = ChunkUtil.indexChunk(chunkX, chunkZ);
      WorldChunk playerChunk = world.getChunkIfInMemory(chunkIndex);
      String headerKey = targetUsername != null ? "server.commands.whereami.header.other" : "server.commands.whereami.header";
      Message message = Message.translation(headerKey)
         .param("username", targetUsername)
         .param("world", world.getName())
         .param("chunkX", chunkX)
         .param("chunkY", chunkY)
         .param("chunkZ", chunkZ)
         .param("posX", position.getX())
         .param("posY", position.getY())
         .param("posZ", position.getZ())
         .param("yaw", headRotation.getYaw())
         .param("pitch", headRotation.getPitch())
         .param("roll", headRotation.getRoll())
         .param("direction", direction.toString())
         .param("axisDirection", axisDirection.toString())
         .param("axis", axis.toString());
      if (playerChunk == null) {
         message.insert(MESSAGE_COMMANDS_WHERE_AM_I_CHUNK_NOT_LOADED);
      } else {
         message.insert(Message.translation("server.commands.whereami.needsSaving").param("needsSaving", Boolean.toString(playerChunk.getNeedsSaving())));
      }

      context.sendMessage(message);
   }

   private static class WhereAmIOtherCommand extends CommandBase {
      private static final Message MESSAGE_COMMANDS_ERRORS_PLAYER_NOT_IN_WORLD = Message.translation("server.commands.errors.playerNotInWorld");
      @Nonnull
      private final RequiredArg<PlayerRef> playerArg = this.withRequiredArg("player", "server.commands.argtype.player.desc", ArgTypes.PLAYER_REF);

      WhereAmIOtherCommand() {
         super("server.commands.whereami.other.desc");
         this.setPermissionGroup(GameMode.Creative);
         this.requirePermission(HytalePermissions.fromCommand("whereami.other"));
      }

      @Override
      protected void executeSync(@Nonnull CommandContext context) {
         PlayerRef targetPlayerRef = this.playerArg.get(context);
         Ref<EntityStore> ref = targetPlayerRef.getReference();
         if (ref != null && ref.isValid()) {
            Store<EntityStore> store = ref.getStore();
            World world = store.getExternalData().getWorld();
            world.execute(() -> {
               Player playerComponent = store.getComponent(ref, Player.getComponentType());
               if (playerComponent == null) {
                  context.sendMessage(MESSAGE_COMMANDS_ERRORS_PLAYER_NOT_IN_WORLD);
               } else {
                  PlayerRef playerRefComponent = store.getComponent(ref, PlayerRef.getComponentType());

                  assert playerRefComponent != null;

                  WhereAmICommand.sendLocationInfo(context, store, ref, world, playerRefComponent.getUsername());
               }
            });
         } else {
            context.sendMessage(MESSAGE_COMMANDS_ERRORS_PLAYER_NOT_IN_WORLD);
         }
      }
   }
}
