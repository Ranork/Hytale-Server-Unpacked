package com.hypixel.hytale.server.core.command.system.exceptions;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import java.awt.Color;
import javax.annotation.Nonnull;

public class SenderTypeException extends CommandException {
   @Nonnull
   private final Class<?> senderType;

   public SenderTypeException(@Nonnull Class<?> senderType) {
      this.senderType = senderType;
   }

   @Override
   public void sendTranslatedMessage(@Nonnull CommandSender sender) {
      sender.sendMessage(Message.translation("server.commands.errors.sender").param("sender", this.senderType.getSimpleName()).color(Color.RED));
   }
}
