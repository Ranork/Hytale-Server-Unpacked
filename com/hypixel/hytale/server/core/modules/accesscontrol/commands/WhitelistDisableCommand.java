package com.hypixel.hytale.server.core.modules.accesscontrol.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.modules.accesscontrol.provider.HytaleWhitelistProvider;
import javax.annotation.Nonnull;

public class WhitelistDisableCommand extends CommandBase {
   @Nonnull
   private static final Message MESSAGE_MODULES_WHITELIST_DISABLED = Message.translation("server.modules.whitelist.disabled");
   @Nonnull
   private static final Message MESSAGE_MODULES_WHITELIST_ALREADY_DISABLED = Message.translation("server.modules.whitelist.alreadyDisabled");
   @Nonnull
   private final HytaleWhitelistProvider whitelistProvider;

   public WhitelistDisableCommand(@Nonnull HytaleWhitelistProvider whitelistProvider) {
      super("disable", "server.commands.whitelist.disable.desc");
      this.addAliases("off");
      this.setUnavailableInSingleplayer(true);
      this.whitelistProvider = whitelistProvider;
   }

   @Override
   protected void executeSync(@Nonnull CommandContext context) {
      if (this.whitelistProvider.isEnabled()) {
         this.whitelistProvider.setEnabled(false);
         context.sendMessage(MESSAGE_MODULES_WHITELIST_DISABLED);
      } else {
         context.sendMessage(MESSAGE_MODULES_WHITELIST_ALREADY_DISABLED);
      }
   }
}
