package com.hypixel.hytale.builtin.teleport.commands.warp;

import com.hypixel.hytale.builtin.teleport.TeleportPlugin;
import com.hypixel.hytale.builtin.teleport.Warp;
import com.hypixel.hytale.builtin.teleport.components.TeleportHistory;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class WarpCommand extends AbstractCommandCollection {
   @Nonnull
   private static final Message MESSAGE_COMMANDS_TELEPORT_WARP_NOT_LOADED = Message.translation("server.commands.teleport.warp.notLoaded");

   public WarpCommand() {
      super("warp", "server.commands.warp.desc");
      this.addUsageVariant(new WarpGoVariantCommand());
      this.addSubCommand(new WarpGoCommand());
      this.addSubCommand(new WarpSetCommand());
      this.addSubCommand(new WarpListCommand());
      this.addSubCommand(new WarpRemoveCommand());
      this.addSubCommand(new WarpReloadCommand());
   }

   static void tryGo(@Nonnull CommandContext context, @Nonnull String warp, @Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store) {
      if (!TeleportPlugin.get().isWarpsLoaded()) {
         context.sendMessage(MESSAGE_COMMANDS_TELEPORT_WARP_NOT_LOADED);
      } else {
         Warp targetWarp = TeleportPlugin.get().getWarps().get(warp.toLowerCase());
         if (targetWarp == null) {
            context.sendMessage(Message.translation("server.commands.teleport.warp.unknownWarp").param("name", warp));
         } else {
            String worldName = targetWarp.getWorld();
            World world = Universe.get().getWorld(worldName);
            Teleport teleportComponent = targetWarp.toTeleport();
            if (world != null && teleportComponent != null) {
               TransformComponent transformComponent = store.getComponent(ref, TransformComponent.getComponentType());

               assert transformComponent != null;

               HeadRotation headRotationComponent = store.getComponent(ref, HeadRotation.getComponentType());

               assert headRotationComponent != null;

               Vector3d playerPosition = transformComponent.getPosition();
               Vector3f playerHeadRotation = headRotationComponent.getRotation();
               store.ensureAndGetComponent(ref, TeleportHistory.getComponentType())
                  .append(world, playerPosition.clone(), playerHeadRotation.clone(), "Warp '" + warp + "'");
               store.addComponent(ref, Teleport.getComponentType(), teleportComponent);
               context.sendMessage(Message.translation("server.commands.teleport.warp.warpedTo").param("name", warp));
            } else {
               context.sendMessage(
                  Message.translation("server.commands.teleport.warp.worldNameForWarpNotFound").param("worldName", worldName).param("warp", warp)
               );
            }
         }
      }
   }
}
