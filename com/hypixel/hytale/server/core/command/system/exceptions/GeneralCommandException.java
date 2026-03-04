package com.hypixel.hytale.server.core.command.system.exceptions;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import java.awt.Color;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class GeneralCommandException extends CommandException {
   @Nonnull
   private final Message message;

   public GeneralCommandException(@Nonnull Message message) {
      this.message = message.color(Color.RED);
   }

   @Override
   public void sendTranslatedMessage(@Nonnull CommandSender sender) {
      sender.sendMessage(this.message);
   }

   @Nullable
   public String getMessageText() {
      try {
         return this.message.getAnsiMessage();
      } catch (Exception var3) {
         String rawText = this.message.getRawText();
         return rawText != null ? rawText : this.message.getMessageId();
      }
   }
}
