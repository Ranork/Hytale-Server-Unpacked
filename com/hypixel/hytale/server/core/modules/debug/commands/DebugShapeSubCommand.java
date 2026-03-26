package com.hypixel.hytale.server.core.modules.debug.commands;

import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.FlagArg;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import com.hypixel.hytale.server.core.modules.debug.DebugUtils;
import javax.annotation.Nonnull;

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

   static int buildFlags(@Nonnull CommandContext context, @Nonnull FlagArg fadeFlag, @Nonnull FlagArg noWireframeFlag, @Nonnull FlagArg noSolidFlag) {
      int flags = 0;
      if (context.get(fadeFlag)) {
         flags |= DebugUtils.FLAG_FADE;
      }

      if (context.get(noWireframeFlag)) {
         flags |= DebugUtils.FLAG_NO_WIREFRAME;
      }

      if (context.get(noSolidFlag)) {
         flags |= DebugUtils.FLAG_NO_SOLID;
      }

      return flags;
   }
}
