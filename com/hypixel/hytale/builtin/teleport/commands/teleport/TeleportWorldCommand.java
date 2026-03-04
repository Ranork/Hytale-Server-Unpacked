package com.hypixel.hytale.builtin.teleport.commands.teleport;

import com.hypixel.hytale.builtin.teleport.components.TeleportHistory;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.permissions.HytalePermissions;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class TeleportWorldCommand extends AbstractPlayerCommand {
   @Nonnull
   private final RequiredArg<String> worldNameArg = this.withRequiredArg("worldName", "server.commands.worldport.worldName.desc", ArgTypes.STRING);

   public TeleportWorldCommand() {
      super("world", "server.commands.worldport.desc");
      this.setPermissionGroup(null);
      this.requirePermission(HytalePermissions.fromCommand("teleport.world"));
   }

   @Override
   protected void execute(
      @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
   ) {
      String worldName = this.worldNameArg.get(context);
      World targetWorld = Universe.get().getWorld(worldName);
      if (targetWorld == null) {
         context.sendMessage(Message.translation("server.world.notFound").param("worldName", worldName));
      } else {
         Transform spawnPoint = targetWorld.getWorldConfig().getSpawnProvider().getSpawnPoint(ref, store);
         if (spawnPoint == null) {
            context.sendMessage(Message.translation("server.world.spawn.notSet").param("worldName", worldName));
         } else {
            TransformComponent transformComponent = store.getComponent(ref, TransformComponent.getComponentType());
            HeadRotation headRotationComponent = store.getComponent(ref, HeadRotation.getComponentType());
            if (transformComponent != null && headRotationComponent != null) {
               Vector3d previousPos = transformComponent.getPosition().clone();
               Vector3f previousRotation = headRotationComponent.getRotation().clone();
               TeleportHistory teleportHistoryComponent = store.ensureAndGetComponent(ref, TeleportHistory.getComponentType());
               teleportHistoryComponent.append(world, previousPos, previousRotation, "World " + targetWorld.getName());
            }

            Teleport teleportComponent = Teleport.createForPlayer(targetWorld, spawnPoint);
            store.addComponent(ref, Teleport.getComponentType(), teleportComponent);
            Vector3d spawnPos = spawnPoint.getPosition();
            context.sendMessage(
               Message.translation("server.commands.teleport.teleportedToWorld")
                  .param("worldName", worldName)
                  .param("x", spawnPos.getX())
                  .param("y", spawnPos.getY())
                  .param("z", spawnPos.getZ())
            );
         }
      }
   }
}
