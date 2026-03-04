package com.hypixel.hytale.server.core.modules.debug.commands;

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;

public class DebugShapeSubCommand extends AbstractCommandCollection {
   public DebugShapeSubCommand() {
      super("shape", "server.commands.debug.shape.desc");
      this.addSubCommand(new DebugShapeSphereCommand());
      this.addSubCommand(new DebugShapeCubeCommand());
      this.addSubCommand(new DebugShapeCylinderCommand());
      this.addSubCommand(new DebugShapeConeCommand());
      this.addSubCommand(new DebugShapeArrowCommand());
      this.addSubCommand(new DebugShapeShowForceCommand());
      this.addSubCommand(new DebugShapeClearCommand());
   }
}
