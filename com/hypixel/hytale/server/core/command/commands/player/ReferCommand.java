package com.hypixel.hytale.server.core.command.commands.player;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.CommandUtil;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractTargetPlayerCommand;
import com.hypixel.hytale.server.core.permissions.HytalePermissions;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ReferCommand extends AbstractTargetPlayerCommand {
   @Nonnull
   private final RequiredArg<String> hostArg = this.withRequiredArg("host", "server.commands.refer.host.desc", ArgTypes.STRING);
   @Nonnull
   private final RequiredArg<Integer> portArg = this.withRequiredArg("port", "server.commands.refer.port.desc", ArgTypes.INTEGER);

   public ReferCommand() {
      super("refer", "server.commands.refer.desc");
      this.addAliases("transfer");
   }

   @Override
   protected void execute(
      @Nonnull CommandContext context,
      @Nullable Ref<EntityStore> sourceRef,
      @Nonnull Ref<EntityStore> ref,
      @Nonnull PlayerRef playerRef,
      @Nonnull World world,
      @Nonnull Store<EntityStore> store
   ) {
      String host = this.hostArg.get(context);
      int port = this.portArg.get(context);
      boolean isTargetingOther = !ref.equals(sourceRef);
      if (isTargetingOther) {
         CommandUtil.requirePermission(context.sender(), HytalePermissions.fromCommand("refer.other"));
      } else {
         CommandUtil.requirePermission(context.sender(), HytalePermissions.fromCommand("refer.self"));
      }

      if (port > 0 && port <= 65535) {
         try {
            playerRef.referToServer(host, port);
            if (isTargetingOther) {
               context.sendMessage(
                  Message.translation("server.commands.refer.success.other").param("username", playerRef.getUsername()).param("host", host).param("port", port)
               );
            } else {
               context.sendMessage(Message.translation("server.commands.refer.success.self").param("host", host).param("port", port));
            }
         } catch (Exception var11) {
            context.sendMessage(Message.translation("server.commands.refer.failed").param("error", var11.getMessage()));
         }
      } else {
         context.sendMessage(Message.translation("server.commands.refer.invalidPort"));
      }
   }
}
