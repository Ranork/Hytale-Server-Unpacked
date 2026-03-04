package com.hypixel.hytale.server.core.command.system.arguments.system;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgumentType;
import javax.annotation.Nonnull;

public class OptionalArg<DataType> extends AbstractOptionalArg<OptionalArg<DataType>, DataType> {
   public OptionalArg(
      @Nonnull AbstractCommand commandRegisteredTo, @Nonnull String name, @Nonnull String description, @Nonnull ArgumentType<DataType> argumentType
   ) {
      super(commandRegisteredTo, name, description, argumentType);
      if (argumentType.getNumberOfParameters() < 1) {
         throw new IllegalArgumentException("Cannot create an Optional Argument with 0 parameters. If you want to have 0 parameters, use Flag Arguments");
      }
   }

   @Nonnull
   protected OptionalArg<DataType> getThis() {
      return this;
   }

   @Nonnull
   @Override
   public Message getUsageMessage() {
      return Message.raw("--")
         .insert(this.getName())
         .insert(" ")
         .insert(this.getArgumentType().getName())
         .insert(" -> \"")
         .insert(Message.translation(this.getDescription()))
         .insert("\"");
   }

   @Nonnull
   @Override
   public Message getUsageOneLiner() {
      return Message.raw("[--").insert(this.getName()).insert("=?]");
   }
}
