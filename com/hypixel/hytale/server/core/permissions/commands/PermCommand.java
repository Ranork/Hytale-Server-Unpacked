package com.hypixel.hytale.server.core.permissions.commands;

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;

public class PermCommand extends AbstractCommandCollection {
   public PermCommand() {
      super("perm", "server.commands.perm.desc");
      this.addSubCommand(new PermGroupCommand());
      this.addSubCommand(new PermUserCommand());
      this.addSubCommand(new PermTestCommand());
   }
}
