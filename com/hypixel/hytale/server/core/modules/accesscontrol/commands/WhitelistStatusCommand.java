package com.hypixel.hytale.server.core.modules.accesscontrol.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.modules.accesscontrol.provider.HytaleWhitelistProvider;
import javax.annotation.Nonnull;

public class WhitelistStatusCommand extends CommandBase {
   @Nonnull
   private final HytaleWhitelistProvider whitelistProvider;

   public WhitelistStatusCommand(@Nonnull HytaleWhitelistProvider whitelistProvider) {
      super("status", "server.commands.whitelist.status.desc");
      this.whitelistProvider = whitelistProvider;
   }

   @Override
   protected void executeSync(@Nonnull CommandContext context) {
      context.sendMessage(Message.translation("server.modules.whitelist.status").param("enabled", this.whitelistProvider.isEnabled() ? "true" : "false"));
   }
}
