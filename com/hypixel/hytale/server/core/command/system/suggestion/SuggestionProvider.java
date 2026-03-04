package com.hypixel.hytale.server.core.command.system.suggestion;

import com.hypixel.hytale.server.core.command.system.CommandSender;
import javax.annotation.Nonnull;

@FunctionalInterface
public interface SuggestionProvider {
   void suggest(@Nonnull CommandSender var1, @Nonnull String var2, int var3, @Nonnull SuggestionResult var4);
}
