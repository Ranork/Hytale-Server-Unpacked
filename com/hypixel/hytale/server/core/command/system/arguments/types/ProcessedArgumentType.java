package com.hypixel.hytale.server.core.command.system.arguments.types;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.command.system.ParseResult;
import com.hypixel.hytale.server.core.command.system.suggestion.SuggestionResult;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class ProcessedArgumentType<InputType, OutputType> extends ArgumentType<OutputType> {
   @Nonnull
   private final ArgumentType<InputType> inputTypeArgumentType;

   public ProcessedArgumentType(String name, Message argumentUsage, @Nonnull ArgumentType<InputType> inputTypeArgumentType, @Nullable String... examples) {
      super(name, argumentUsage, inputTypeArgumentType.numberOfParameters, examples);
      this.inputTypeArgumentType = inputTypeArgumentType;
      this.withSharedSuggestions(inputTypeArgumentType);
   }

   @Nonnull
   public ArgumentType<InputType> getInputTypeArgumentType() {
      return this.inputTypeArgumentType;
   }

   @Override
   public boolean isListArgument() {
      return this.getInputTypeArgumentType().isListArgument();
   }

   @Nullable
   @Override
   public OutputType parse(@Nonnull String[] input, @Nonnull ParseResult parseResult) {
      InputType parsedData = this.inputTypeArgumentType.parse(input, parseResult);
      if (parseResult.failed()) {
         return null;
      } else {
         OutputType outputType = this.processInput(parsedData);
         return parseResult.failed() ? null : outputType;
      }
   }

   public abstract OutputType processInput(InputType var1);

   @Override
   public void suggest(@Nonnull CommandSender sender, @Nonnull String textAlreadyEntered, int numParametersTyped, @Nonnull SuggestionResult result) {
      this.inputTypeArgumentType.suggest(sender, textAlreadyEntered, numParametersTyped, result);
   }

   @Override
   public int getSuggestionValueCount() {
      return this.inputTypeArgumentType.getSuggestionValueCount();
   }
}
