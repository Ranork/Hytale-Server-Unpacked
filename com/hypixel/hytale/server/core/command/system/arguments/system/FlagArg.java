package com.hypixel.hytale.server.core.command.system.arguments.system;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.arguments.types.BooleanFlagArgumentType;
import javax.annotation.Nonnull;

public class FlagArg extends AbstractOptionalArg<FlagArg, Boolean> implements AbstractOptionalArg.DefaultValueArgument<Boolean> {
   @Nonnull
   private static final BooleanFlagArgumentType BOOLEAN_FLAG_ARGUMENT_TYPE = new BooleanFlagArgumentType();

   public FlagArg(@Nonnull AbstractCommand commandRegisteredTo, @Nonnull String name, @Nonnull String description) {
      super(commandRegisteredTo, name, description, BOOLEAN_FLAG_ARGUMENT_TYPE);
   }

   @Nonnull
   protected FlagArg getThis() {
      return this;
   }

   @Nonnull
   public Boolean getDefaultValue() {
      return Boolean.FALSE;
   }

   @Nonnull
   @Override
   public Message getUsageMessage() {
      return Message.raw("--").insert(this.getName()).insert(" -> \"").insert(Message.translation(this.getDescription())).insert("\"");
   }

   @Nonnull
   @Override
   public Message getUsageOneLiner() {
      return Message.raw("[--").insert(this.getName()).insert("]");
   }
}
