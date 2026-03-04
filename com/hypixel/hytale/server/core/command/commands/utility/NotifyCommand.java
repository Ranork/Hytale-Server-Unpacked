package com.hypixel.hytale.server.core.command.commands.utility;

import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.CommandUtil;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.util.NotificationUtil;
import javax.annotation.Nonnull;

public class NotifyCommand extends CommandBase {
   public NotifyCommand() {
      super("notify", "server.commands.notify.desc");
      this.setAllowsExtraArguments(true);
   }

   @Override
   protected void executeSync(@Nonnull CommandContext context) {
      String inputString = context.getInputString();
      String rawArgs = CommandUtil.stripCommandName(inputString).trim();
      if (rawArgs.isEmpty()) {
         context.sendMessage(Message.translation("server.commands.parsing.error.wrongNumberRequiredParameters").param("expected", 1).param("actual", 0));
      } else {
         String[] args = rawArgs.split("\\s+");
         if (args.length == 0) {
            context.sendMessage(Message.translation("server.commands.parsing.error.wrongNumberRequiredParameters").param("expected", 1).param("actual", 0));
         } else {
            NotificationStyle style = NotificationStyle.Default;
            int messageStartIndex = 0;
            if (args.length >= 2) {
               String firstArg = args[0];
               if (!firstArg.startsWith("{")) {
                  try {
                     style = NotificationStyle.valueOf(firstArg.toUpperCase());
                     messageStartIndex = 1;
                  } catch (IllegalArgumentException var12) {
                  }
               }
            }

            StringBuilder messageBuilder = new StringBuilder();

            for (int i = messageStartIndex; i < args.length; i++) {
               if (i > messageStartIndex) {
                  messageBuilder.append(' ');
               }

               messageBuilder.append(args[i]);
            }

            String messageString = messageBuilder.toString();
            Message message;
            if (messageString.startsWith("{")) {
               try {
                  message = Message.parse(messageString);
               } catch (IllegalArgumentException var11) {
                  context.sendMessage(Message.raw("Invalid formatted message: " + var11.getMessage()));
                  return;
               }
            } else {
               message = Message.raw(messageString);
            }

            Message senderName = Message.raw(context.sender().getDisplayName());
            NotificationUtil.sendNotificationToUniverse(message, senderName, "announcement", null, style);
         }
      }
   }
}
