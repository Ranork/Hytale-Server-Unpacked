package com.hypixel.hytale.server.core.discovery.command;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.CommandUtil;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.discovery.DiscoveryModule;
import com.hypixel.hytale.server.core.permissions.HytalePermissions;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class DiscoveryUnlinkCommand extends CommandBase {
   private final DiscoveryModule module;

   public DiscoveryUnlinkCommand(DiscoveryModule module) {
      super("unlink", "server.commands.discovery.unlink.desc");
      this.module = module;
   }

   @Override
   protected void executeSync(@NonNullDecl CommandContext context) {
      CommandUtil.requirePermission(context.sender(), HytalePermissions.fromCommand("discovery.unlink"));
      if (!DiscoveryModule.isDiscoveryEnabled()) {
         context.sendMessage(Message.translation("server.commands.discovery.notEnabled"));
      } else if (!this.module.hasDiscoveryToken()) {
         context.sendMessage(Message.translation("server.commands.discovery.unlink.notLinked"));
      } else {
         this.module.setDiscoveryToken(null);
         context.sendMessage(Message.translation("server.commands.discovery.unlink.success"));
      }
   }
}
