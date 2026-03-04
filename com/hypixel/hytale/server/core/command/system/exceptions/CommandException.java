package com.hypixel.hytale.server.core.command.system.exceptions;

import com.hypixel.hytale.server.core.command.system.CommandSender;
import javax.annotation.Nonnull;

public abstract class CommandException extends RuntimeException {
   public abstract void sendTranslatedMessage(@Nonnull CommandSender var1);
}
