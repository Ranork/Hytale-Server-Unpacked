package com.hypixel.hytale.server.core.command.system.arguments.types;

import com.hypixel.hytale.server.core.command.system.ParseResult;
import javax.annotation.Nonnull;

public class BooleanFlagArgumentType extends ArgumentType<Boolean> {
   public BooleanFlagArgumentType() {
      super("server.commands.parsing.argtype.flag.name", "server.commands.parsing.argtype.flag.usage", 0, "None, just specify the argument for true");
   }

   @Nonnull
   public Boolean parse(@Nonnull String[] input, @Nonnull ParseResult parseResult) {
      return Boolean.TRUE;
   }
}
