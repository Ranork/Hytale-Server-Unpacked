package com.hypixel.hytale.builtin.commandmacro;

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import javax.annotation.Nonnull;

public class MacroCommandCollection extends AbstractCommandCollection {
   @Nonnull
   private static final String DESCRIPTION = "server.commands.macro.collection.desc";

   public MacroCommandCollection(@Nonnull String leafToken) {
      super(leafToken, "server.commands.macro.collection.desc");
   }
}
