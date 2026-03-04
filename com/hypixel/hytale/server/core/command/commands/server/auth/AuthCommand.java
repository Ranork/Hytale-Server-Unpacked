package com.hypixel.hytale.server.core.command.commands.server.auth;

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;

public class AuthCommand extends AbstractCommandCollection {
   public AuthCommand() {
      super("auth", "server.commands.auth.desc");
      this.addSubCommand(new AuthStatusCommand());
      this.addSubCommand(new AuthLoginCommand());
      this.addSubCommand(new AuthSelectCommand());
      this.addSubCommand(new AuthLogoutCommand());
      this.addSubCommand(new AuthCancelCommand());
      this.addSubCommand(new AuthPersistenceCommand());
   }
}
