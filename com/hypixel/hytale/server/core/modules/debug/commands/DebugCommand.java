package com.hypixel.hytale.server.core.modules.debug.commands;

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;

public class DebugCommand extends AbstractCommandCollection {
   public DebugCommand() {
      super("debug", "server.commands.debug.desc");
      this.addSubCommand(new DebugShapeSubCommand());
   }
}
