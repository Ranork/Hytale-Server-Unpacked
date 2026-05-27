package com.hypixel.hytale.server.core.command.system.arguments.system;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgumentType;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class RequiredArg<DataType> extends Argument<RequiredArg<DataType>, DataType> {
   @Nullable
   private ArgumentType<?> suggestionOverrideType;

   public RequiredArg(
      @Nonnull AbstractCommand commandRegisteredTo, @Nonnull String name, @Nonnull String description, @Nonnull ArgumentType<DataType> argumentType
   ) {
      super(commandRegisteredTo, name, description, argumentType);
      if (argumentType.getNumberOfParameters() < 1) {
         throw new IllegalArgumentException("Cannot create a Required Argument with 0 parameters.");
      }
   }

   @Nonnull
   public RequiredArg<DataType> withSuggestionOverride(@Nonnull ArgumentType<?> overrideType) {
      this.suggestionOverrideType = overrideType;
      return this;
   }

   @Nullable
   public ArgumentType<?> getSuggestionOverrideType() {
      return this.suggestionOverrideType;
   }

   @Nonnull
   public Message getUsageMessageWithoutDescription() {
      return Message.raw("<").insert(this.getName()).insert(":").insert(this.getArgumentType().getName()).insert(">");
   }

   @Nonnull
   @Override
   public Message getUsageMessage() {
      return Message.raw(this.getName())
         .insert(" (")
         .insert(this.getArgumentType().getName())
         .insert(") -> \"")
         .insert(Message.translation(this.getDescription()))
         .insert("\"");
   }

   @Nonnull
   @Override
   public Message getUsageOneLiner() {
      return Message.raw("<").insert(this.getName()).insert(">");
   }

   @Nonnull
   protected RequiredArg<DataType> getThis() {
      return this;
   }
}
