package com.hypixel.hytale.server.core.command.system.exceptions;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import java.awt.Color;
import javax.annotation.Nonnull;

public class NoPermissionException extends CommandException {
   @Nonnull
   private final String permission;

   public NoPermissionException(@Nonnull String permission) {
      this.permission = permission;
   }

   @Override
   public void sendTranslatedMessage(@Nonnull CommandSender sender) {
      sender.sendMessage(Message.translation("server.commands.errors.permission").param("permission", this.permission).color(Color.RED));
   }
}
