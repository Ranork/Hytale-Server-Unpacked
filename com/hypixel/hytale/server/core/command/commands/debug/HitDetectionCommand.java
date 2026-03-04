package com.hypixel.hytale.server.core.command.commands.debug;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.none.SelectInteraction;
import javax.annotation.Nonnull;

public class HitDetectionCommand extends CommandBase {
   public HitDetectionCommand() {
      super("hitdetection", "server.commands.hitdetection.desc");
   }

   @Override
   protected void executeSync(@Nonnull CommandContext context) {
      SelectInteraction.SHOW_VISUAL_DEBUG = !SelectInteraction.SHOW_VISUAL_DEBUG;
      context.sendMessage(Message.translation("server.commands.hitdetection.toggled").param("debug", SelectInteraction.SHOW_VISUAL_DEBUG));
   }
}
