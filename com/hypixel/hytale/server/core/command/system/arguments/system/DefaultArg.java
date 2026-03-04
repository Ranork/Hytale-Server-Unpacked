package com.hypixel.hytale.server.core.command.system.arguments.system;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.ParseResult;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgumentType;
import javax.annotation.Nonnull;

public class DefaultArg<DataType> extends AbstractOptionalArg<DefaultArg<DataType>, DataType> implements AbstractOptionalArg.DefaultValueArgument<DataType> {
   @Nonnull
   private final DataType defaultValue;
   @Nonnull
   private final String defaultValueDescription;

   public DefaultArg(
      @Nonnull AbstractCommand commandRegisteredTo,
      @Nonnull String name,
      @Nonnull String description,
      @Nonnull ArgumentType<DataType> argumentType,
      @Nonnull DataType defaultValue,
      @Nonnull String defaultValueDescription
   ) {
      super(commandRegisteredTo, name, description, argumentType);
      this.defaultValue = defaultValue;
      this.defaultValueDescription = defaultValueDescription;
   }

   @Nonnull
   protected DefaultArg<DataType> getThis() {
      return this;
   }

   @Override
   public final DataType getDefaultValue() {
      return this.defaultValue;
   }

   public void validateDefaultValue(@Nonnull ParseResult parseResult) {
      this.validate(this.getDefaultValue(), parseResult);
   }

   @Nonnull
   @Override
   public Message getUsageMessage() {
      return Message.raw("--")
         .insert(this.getName())
         .insert(" (")
         .insert(this.getArgumentType().getName())
         .insert(":default=")
         .insert(Message.translation(this.getDefaultValueDescription()))
         .insert(") -> \"")
         .insert(Message.translation(this.getDescription()))
         .insert("\"");
   }

   @Nonnull
   @Override
   public Message getUsageOneLiner() {
      String defaultValueStr = this.defaultValue == null ? "?" : this.defaultValue.toString();
      return Message.raw("[--").insert(this.getName()).insert("=").insert(defaultValueStr).insert("]");
   }

   @Nonnull
   public String getDefaultValueDescription() {
      return this.defaultValueDescription;
   }
}
