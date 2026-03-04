package com.hypixel.hytale.server.core.modules.debug.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.modules.debug.DebugUtils;
import javax.annotation.Nonnull;

public class DebugShapeShowForceCommand extends CommandBase {
   public DebugShapeShowForceCommand() {
      super("showforce", "server.commands.debug.shape.showforce.desc");
   }

   @Override
   protected void executeSync(@Nonnull CommandContext context) {
      DebugUtils.DISPLAY_FORCES = !DebugUtils.DISPLAY_FORCES;
      context.sendMessage(Message.raw("Display forces: " + (DebugUtils.DISPLAY_FORCES ? "enabled" : "disabled")));
   }
}
