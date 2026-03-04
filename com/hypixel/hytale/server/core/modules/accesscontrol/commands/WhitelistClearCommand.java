package com.hypixel.hytale.server.core.modules.accesscontrol.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.modules.accesscontrol.provider.HytaleWhitelistProvider;
import javax.annotation.Nonnull;

public class WhitelistClearCommand extends CommandBase {
   @Nonnull
   private static final Message MESSAGE_MODULES_WHITELIST_CLEARED = Message.translation("server.modules.whitelist.cleared");
   @Nonnull
   private final HytaleWhitelistProvider whitelistProvider;

   public WhitelistClearCommand(@Nonnull HytaleWhitelistProvider whitelistProvider) {
      super("clear", "server.commands.whitelist.clear.desc");
      this.whitelistProvider = whitelistProvider;
   }

   @Override
   protected void executeSync(@Nonnull CommandContext context) {
      this.whitelistProvider.modify(list -> {
         list.clear();
         return true;
      });
      context.sendMessage(MESSAGE_MODULES_WHITELIST_CLEARED);
   }
}
