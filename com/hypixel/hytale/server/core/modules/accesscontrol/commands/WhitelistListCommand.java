package com.hypixel.hytale.server.core.modules.accesscontrol.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.modules.accesscontrol.provider.HytaleWhitelistProvider;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nonnull;

public class WhitelistListCommand extends CommandBase {
   @Nonnull
   private final HytaleWhitelistProvider whitelistProvider;

   public WhitelistListCommand(@Nonnull HytaleWhitelistProvider whitelistProvider) {
      super("list", "server.commands.whitelist.list.desc");
      this.whitelistProvider = whitelistProvider;
   }

   @Override
   protected void executeSync(@Nonnull CommandContext context) {
      Set<UUID> whitelist = this.whitelistProvider.getList();
      if (!whitelist.isEmpty() && whitelist.size() <= 10) {
         context.sendMessage(Message.translation("server.modules.whitelist.list").param("whitelist", whitelist.toString()));
      } else {
         context.sendMessage(Message.translation("server.modules.whitelist.size").param("size", whitelist.size()));
      }
   }
}
