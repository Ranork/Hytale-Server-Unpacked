package com.hypixel.hytale.server.core.modules.accesscontrol.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.modules.accesscontrol.provider.HytaleWhitelistProvider;
import javax.annotation.Nonnull;

public class WhitelistEnableCommand extends CommandBase {
   @Nonnull
   private static final Message MESSAGE_MODULES_WHITELIST_ALREADY_ENABLED = Message.translation("server.modules.whitelist.alreadyEnabled");
   @Nonnull
   private static final Message MESSAGE_MODULES_WHITELIST_ENABLED = Message.translation("server.modules.whitelist.enabled");
   @Nonnull
   private final HytaleWhitelistProvider whitelistProvider;

   public WhitelistEnableCommand(@Nonnull HytaleWhitelistProvider whitelistProvider) {
      super("enable", "server.commands.whitelist.enable.desc");
      this.addAliases("on");
      this.setUnavailableInSingleplayer(true);
      this.whitelistProvider = whitelistProvider;
   }

   @Override
   protected void executeSync(@Nonnull CommandContext context) {
      if (this.whitelistProvider.isEnabled()) {
         context.sendMessage(MESSAGE_MODULES_WHITELIST_ALREADY_ENABLED);
      } else {
         this.whitelistProvider.setEnabled(true);
         context.sendMessage(MESSAGE_MODULES_WHITELIST_ENABLED);
      }
   }
}
