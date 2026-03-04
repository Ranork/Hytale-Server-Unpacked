package com.hypixel.hytale.server.core.modules.accesscontrol.commands;

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import com.hypixel.hytale.server.core.modules.accesscontrol.provider.HytaleWhitelistProvider;
import javax.annotation.Nonnull;

public class WhitelistCommand extends AbstractCommandCollection {
   public WhitelistCommand(@Nonnull HytaleWhitelistProvider whitelistProvider) {
      super("whitelist", "server.commands.whitelist.desc");
      this.addSubCommand(new WhitelistAddCommand(whitelistProvider));
      this.addSubCommand(new WhitelistRemoveCommand(whitelistProvider));
      this.addSubCommand(new WhitelistEnableCommand(whitelistProvider));
      this.addSubCommand(new WhitelistDisableCommand(whitelistProvider));
      this.addSubCommand(new WhitelistClearCommand(whitelistProvider));
      this.addSubCommand(new WhitelistStatusCommand(whitelistProvider));
      this.addSubCommand(new WhitelistListCommand(whitelistProvider));
   }
}
