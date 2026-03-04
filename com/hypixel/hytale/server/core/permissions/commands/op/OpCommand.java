package com.hypixel.hytale.server.core.permissions.commands.op;

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;

public class OpCommand extends AbstractCommandCollection {
   public OpCommand() {
      super("op", "server.commands.op.desc");
      this.addSubCommand(new OpSelfCommand());
      this.addSubCommand(new OpAddCommand());
      this.addSubCommand(new OpRemoveCommand());
   }

   @Override
   protected boolean canGeneratePermission() {
      return false;
   }
}
