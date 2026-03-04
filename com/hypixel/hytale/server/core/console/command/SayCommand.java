package com.hypixel.hytale.server.core.console.command;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.CommandUtil;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.console.ConsoleSender;
import com.hypixel.hytale.server.core.universe.Universe;
import java.awt.Color;
import javax.annotation.Nonnull;

public class SayCommand extends CommandBase {
   @Nonnull
   private static final Color SAY_COMMAND_COLOR = Color.CYAN;

   public SayCommand() {
      super("say", "server.commands.say.desc");
      this.addAliases("broadcast");
      this.setAllowsExtraArguments(true);
   }

   @Override
   protected void executeSync(@Nonnull CommandContext context) {
      String rawArgs = CommandUtil.stripCommandName(context.getInputString()).trim();
      if (rawArgs.isEmpty()) {
         context.sendMessage(Message.translation("server.commands.parsing.error.wrongNumberRequiredParameters").param("expected", 1).param("actual", 0));
      } else {
         Message result;
         if (rawArgs.charAt(0) == '{') {
            try {
               result = Message.parse(rawArgs).color(SAY_COMMAND_COLOR);
            } catch (IllegalArgumentException var5) {
               context.sendMessage(Message.raw("Failed to parse formatted message: " + var5.getMessage()));
               return;
            }
         } else {
            result = Message.translation("server.chat.broadcastMessage")
               .param("username", context.sender().getDisplayName())
               .param("message", rawArgs)
               .color(SAY_COMMAND_COLOR);
         }

         Universe.get().getWorlds().values().forEach(world -> world.getPlayerRefs().forEach(playerRef -> playerRef.sendMessage(result)));
         ConsoleSender.INSTANCE.sendMessage(result);
      }
   }
}
