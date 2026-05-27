package com.hypixel.hytale.builtin.teleport.commands.teleport;

import com.hypixel.hytale.builtin.teleport.components.TeleportHistory;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.math.vector.Transform;
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
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import org.joml.Vector3d;

public class TeleportWorldCommand extends AbstractPlayerCommand {
   @Nonnull
   private final RequiredArg<World> worldNameArg = this.withRequiredArg("worldName", "server.commands.worldport.worldName.desc", ArgTypes.WORLD);

   public TeleportWorldCommand() {
      super("world", "server.commands.worldport.desc");
      this.setPermissionGroups("hytale:WorldEditor");
      this.requirePermission(HytalePermissions.fromCommand("teleport.world"));
   }

   @Override
   protected void execute(
      @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
   ) {
      World targetWorld = this.worldNameArg.get(context);
      Transform spawnPoint = targetWorld.getWorldConfig().getSpawnProvider().getSpawnPoint(ref, store);
      if (spawnPoint == null) {
         context.sendMessage(Message.translation("server.world.spawn.notSet").param("worldName", targetWorld.getName()));
      } else {
         TransformComponent transformComponent = store.getComponent(ref, TransformComponent.getComponentType());
         HeadRotation headRotationComponent = store.getComponent(ref, HeadRotation.getComponentType());
         if (transformComponent != null && headRotationComponent != null) {
            Vector3d previousPos = new Vector3d(transformComponent.getPosition());
            Rotation3f previousRotation = new Rotation3f(headRotationComponent.getRotation());
            TeleportHistory teleportHistoryComponent = store.ensureAndGetComponent(ref, TeleportHistory.getComponentType());
            teleportHistoryComponent.append(world, previousPos, previousRotation, "World " + targetWorld.getName());
         }

         Teleport teleportComponent = Teleport.createForPlayer(targetWorld, spawnPoint);
         store.addComponent(ref, Teleport.getComponentType(), teleportComponent);
         Vector3d spawnPos = spawnPoint.getPosition();
         context.sendMessage(
            Message.translation("server.commands.teleport.teleportedToWorld")
               .param("worldName", targetWorld.getName())
               .param("x", spawnPos.x())
               .param("y", spawnPos.y())
               .param("z", spawnPos.z())
         );
      }
   }
}
