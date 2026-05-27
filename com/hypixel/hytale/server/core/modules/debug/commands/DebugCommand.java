package com.hypixel.hytale.server.core.modules.debug.commands;

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;

public class DebugCommand extends AbstractCommandCollection {
   public DebugCommand() {
      super("debug", "server.commands.debug.desc");
      this.setPermissionGroups("hytale:WorldEditor");
      this.addSubCommand(new DebugShapeSubCommand());
   }
}
