package com.hypixel.hytale.builtin.teleport.commands.teleport;

import com.hypixel.hytale.builtin.teleport.components.TeleportHistory;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.command.system.exceptions.GeneralCommandException;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.permissions.HytalePermissions;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.spawn.ISpawnProvider;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class SpawnCommand extends AbstractPlayerCommand {
   @Nonnull
   private final OptionalArg<Integer> spawnIndexArg = this.withOptionalArg("spawnIndex", "server.commands.spawn.index.desc", ArgTypes.INTEGER);

   public SpawnCommand() {
      super("spawn", "server.commands.spawn.desc");
      this.requirePermission(HytalePermissions.fromCommand("spawn.self"));
      this.addUsageVariant(new SpawnCommand.SpawnOtherCommand());
      this.addSubCommand(new SpawnSetCommand());
      this.addSubCommand(new SpawnSetDefaultCommand());
   }

   @Override
   protected void execute(
      @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
   ) {
      Transform spawnTransform = resolveSpawn(context, world, playerRef, this.spawnIndexArg);
      TransformComponent transformComponent = store.getComponent(ref, TransformComponent.getComponentType());

      assert transformComponent != null;

      HeadRotation headRotationComponent = store.getComponent(ref, HeadRotation.getComponentType());

      assert headRotationComponent != null;

      Vector3d previousPos = transformComponent.getPosition().clone();
      Vector3f previousRotation = headRotationComponent.getRotation().clone();
      TeleportHistory teleportHistoryComponent = store.ensureAndGetComponent(ref, TeleportHistory.getComponentType());
      teleportHistoryComponent.append(world, previousPos, previousRotation, "World " + world.getName() + "'s spawn");
      Teleport teleportComponent = Teleport.createForPlayer(world, spawnTransform);
      store.addComponent(ref, Teleport.getComponentType(), teleportComponent);
      Vector3d position = spawnTransform.getPosition();
      context.sendMessage(
         Message.translation("server.commands.spawn.teleported").param("x", position.getX()).param("y", position.getY()).param("z", position.getZ())
      );
   }

   private static Transform resolveSpawn(
      @Nonnull CommandContext context, @Nonnull World world, @Nonnull PlayerRef playerRef, @Nonnull OptionalArg<Integer> spawnIndexArg
   ) {
      ISpawnProvider spawnProvider = world.getWorldConfig().getSpawnProvider();
      if (spawnIndexArg.provided(context)) {
         int spawnIndex = spawnIndexArg.get(context);
         Transform[] spawnPoints = spawnProvider.getSpawnPoints();
         if (spawnIndex >= 0 && spawnIndex < spawnPoints.length) {
            return spawnPoints[spawnIndex];
         } else {
            int maxIndex = spawnPoints.length - 1;
            context.sendMessage(Message.translation("server.commands.spawn.indexNotFound").param("maxIndex", maxIndex));
            throw new GeneralCommandException(
               Message.translation("server.commands.errors.spawnIndexOutOfRange").param("index", spawnIndex).param("maxIndex", maxIndex)
            );
         }
      } else {
         return spawnProvider.getSpawnPoint(world, playerRef.getUuid());
      }
   }

   private static class SpawnOtherCommand extends CommandBase {
      private static final Message MESSAGE_COMMANDS_ERRORS_PLAYER_NOT_IN_WORLD = Message.translation("server.commands.errors.playerNotInWorld");
      @Nonnull
      private final RequiredArg<PlayerRef> playerArg = this.withRequiredArg("player", "server.commands.argtype.player.desc", ArgTypes.PLAYER_REF);
      @Nonnull
      private final OptionalArg<Integer> spawnIndexArg = this.withOptionalArg("spawnIndex", "server.commands.spawn.index.desc", ArgTypes.INTEGER);

      SpawnOtherCommand() {
         super("server.commands.spawn.other.desc");
         this.requirePermission(HytalePermissions.fromCommand("spawn.other"));
      }

      @Override
      protected void executeSync(@Nonnull CommandContext context) {
         PlayerRef targetPlayerRef = this.playerArg.get(context);
         Ref<EntityStore> ref = targetPlayerRef.getReference();
         if (ref != null && ref.isValid()) {
            Store<EntityStore> store = ref.getStore();
            World world = store.getExternalData().getWorld();
            world.execute(
               () -> {
                  Player playerComponent = store.getComponent(ref, Player.getComponentType());
                  if (playerComponent == null) {
                     context.sendMessage(MESSAGE_COMMANDS_ERRORS_PLAYER_NOT_IN_WORLD);
                  } else {
                     PlayerRef playerRefComponent = store.getComponent(ref, PlayerRef.getComponentType());

                     assert playerRefComponent != null;

                     Transform spawn = SpawnCommand.resolveSpawn(context, world, targetPlayerRef, this.spawnIndexArg);
                     TransformComponent transformComponent = store.getComponent(ref, TransformComponent.getComponentType());

                     assert transformComponent != null;

                     HeadRotation headRotationComponent = store.getComponent(ref, HeadRotation.getComponentType());

                     assert headRotationComponent != null;

                     Vector3d previousPos = transformComponent.getPosition().clone();
                     Vector3f previousRotation = headRotationComponent.getRotation().clone();
                     TeleportHistory teleportHistoryComponent = store.ensureAndGetComponent(ref, TeleportHistory.getComponentType());
                     teleportHistoryComponent.append(world, previousPos, previousRotation, "World " + world.getName() + "'s spawn");
                     Teleport teleportComponent = Teleport.createForPlayer(world, spawn);
                     store.addComponent(ref, Teleport.getComponentType(), teleportComponent);
                     Vector3d position = spawn.getPosition();
                     context.sendMessage(
                        Message.translation("server.commands.spawn.teleportedOther")
                           .param("username", targetPlayerRef.getUsername())
                           .param("x", position.getX())
                           .param("y", position.getY())
                           .param("z", position.getZ())
                     );
                  }
               }
            );
         } else {
            context.sendMessage(MESSAGE_COMMANDS_ERRORS_PLAYER_NOT_IN_WORLD);
         }
      }
   }
}
