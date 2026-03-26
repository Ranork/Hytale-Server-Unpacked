package com.hypixel.hytale.server.core.command.commands.utility;

import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.util.NotificationUtil;
import javax.annotation.Nonnull;

public class NotifyCommand extends CommandBase {
   @Nonnull
   private final RequiredArg<String> messageArg = this.withRequiredArg("message", "server.commands.notify.message.desc", ArgTypes.GREEDY_STRING);

   public NotifyCommand() {
      super("notify", "server.commands.notify.desc");
   }

   @Override
   protected void executeSync(@Nonnull CommandContext context) {
      String rawArgs = this.messageArg.get(context);
      NotificationStyle style = NotificationStyle.Default;
      String messageString = rawArgs;
      int firstSpace = rawArgs.indexOf(32);
      if (firstSpace > 0 && !rawArgs.startsWith("{")) {
         String firstWord = rawArgs.substring(0, firstSpace);

         try {
            style = NotificationStyle.valueOf(firstWord.toUpperCase());
            messageString = rawArgs.substring(firstSpace + 1);
         } catch (IllegalArgumentException var9) {
         }
      }

      Message message;
      if (messageString.startsWith("{")) {
         try {
            message = Message.parse(messageString);
         } catch (IllegalArgumentException var8) {
            context.sendMessage(Message.raw("Invalid formatted message: " + var8.getMessage()));
            return;
         }
      } else {
         message = Message.raw(messageString);
      }

      Message senderName = Message.raw(context.sender().getDisplayName());
      NotificationUtil.sendNotificationToUniverse(message, senderName, "announcement", null, style);
   }
}
