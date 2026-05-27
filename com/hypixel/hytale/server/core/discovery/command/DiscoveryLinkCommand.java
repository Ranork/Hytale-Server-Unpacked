package com.hypixel.hytale.server.core.discovery.command;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.auth.ServerAuthManager;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.CommandUtil;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.discovery.DiscoveryModule;
import com.hypixel.hytale.server.core.permissions.HytalePermissions;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class DiscoveryLinkCommand extends CommandBase {
   @Nonnull
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   @Nonnull
   private final RequiredArg<String> discoveryTokenArg = this.withRequiredArg("token", "server.commands.discovery.link.token.desc", ArgTypes.STRING);
   private final DiscoveryModule module;

   public DiscoveryLinkCommand(DiscoveryModule module) {
      super("link", "server.commands.discovery.link.desc");
      this.module = module;
   }

   @Override
   protected void executeSync(@NonNullDecl CommandContext context) {
      CommandUtil.requirePermission(context.sender(), HytalePermissions.fromCommand("discovery.link"));
      if (!DiscoveryModule.isDiscoveryEnabled()) {
         context.sendMessage(Message.translation("server.commands.discovery.notEnabled"));
      } else if (!ServerAuthManager.getInstance().hasSessionToken()) {
         context.sendMessage(Message.translation("server.commands.discovery.link.noAuth"));
      } else if (this.module.hasDiscoveryToken()) {
         context.sendMessage(Message.translation("server.commands.discovery.link.alreadyLinked"));
      } else {
         String discoveryToken = this.discoveryTokenArg.get(context);
         CompletableFuture.runAsync(() -> {
            boolean authFailure = this.module.getService().sendHeartbeat(discoveryToken);
            if (authFailure) {
               context.sendMessage(Message.translation("server.commands.discovery.link.tokenNotValid"));
            } else {
               this.module.setDiscoveryToken(discoveryToken);
               context.sendMessage(Message.translation("server.commands.discovery.link.success"));
            }
         }).exceptionally(t -> {
            ((HytaleLogger.Api)LOGGER.at(Level.WARNING).withCause(t)).log("Failed to link server discovery server");
            return null;
         });
      }
   }
}
