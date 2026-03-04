package com.hypixel.hytale.builtin.teleport.commands.teleport;

import com.hypixel.hytale.builtin.teleport.components.TeleportHistory;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.permissions.HytalePermissions;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class TeleportHomeCommand extends AbstractPlayerCommand {
   @Nonnull
   private static final Message MESSAGE_COMMANDS_TELEPORT_TELEPORTED_SELF_HOME = Message.translation("server.commands.teleport.teleportedSelfHome");

   public TeleportHomeCommand() {
      super("home", "server.commands.home.desc");
      this.requirePermission(HytalePermissions.fromCommand("teleport.home"));
   }

   @Override
   protected void execute(
      @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
   ) {
      TransformComponent transformComponent = store.getComponent(ref, TransformComponent.getComponentType());

      assert transformComponent != null;

      HeadRotation headRotationComponent = store.getComponent(ref, HeadRotation.getComponentType());

      assert headRotationComponent != null;

      Vector3d previousPos = transformComponent.getPosition().clone();
      Vector3f previousHeadRotation = headRotationComponent.getRotation().clone();
      TeleportHistory teleportHistoryComponent = store.ensureAndGetComponent(ref, TeleportHistory.getComponentType());
      teleportHistoryComponent.append(world, previousPos, previousHeadRotation, "Home");
      Player.getRespawnPosition(ref, world.getName(), store).thenAcceptAsync(homeTransform -> {
         Teleport teleportComponent = Teleport.createForPlayer(null, homeTransform);
         store.addComponent(ref, Teleport.getComponentType(), teleportComponent);
         context.sendMessage(MESSAGE_COMMANDS_TELEPORT_TELEPORTED_SELF_HOME);
      }, world);
   }
}
