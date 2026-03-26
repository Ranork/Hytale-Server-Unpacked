package com.hypixel.hytale.server.core.console.command;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.console.ConsoleSender;
import com.hypixel.hytale.server.core.universe.Universe;
import java.awt.Color;
import javax.annotation.Nonnull;

public class SayCommand extends CommandBase {
   @Nonnull
   private static final Color SAY_COMMAND_COLOR = Color.CYAN;
   @Nonnull
   private final RequiredArg<String> messageArg = this.withRequiredArg("message", "server.commands.say.message.desc", ArgTypes.GREEDY_STRING);

   public SayCommand() {
      super("say", "server.commands.say.desc");
      this.addAliases("broadcast");
   }

   @Override
   protected void executeSync(@Nonnull CommandContext context) {
      String rawMessage = this.messageArg.get(context);
      Message result;
      if (rawMessage.charAt(0) == '{') {
         try {
            result = Message.parse(rawMessage).color(SAY_COMMAND_COLOR);
         } catch (IllegalArgumentException var5) {
            context.sendMessage(Message.raw("Failed to parse formatted message: " + var5.getMessage()));
            return;
         }
      } else {
         result = Message.translation("server.chat.broadcastMessage")
            .param("username", context.sender().getDisplayName())
            .param("message", rawMessage)
            .color(SAY_COMMAND_COLOR);
      }

      Universe.get().getWorlds().values().forEach(world -> world.getPlayerRefs().forEach(playerRef -> playerRef.sendMessage(result)));
      ConsoleSender.INSTANCE.sendMessage(result);
   }
}
