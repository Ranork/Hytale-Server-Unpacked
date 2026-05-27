package com.hypixel.hytale.server.core.command.system.arguments.types;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.command.system.suggestion.SuggestionResult;
import java.util.Arrays;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class WrappedArgumentType<DataType> extends SingleArgumentType<DataType> {
   protected final ArgumentType<DataType> wrappedArgumentType;

   public WrappedArgumentType(Message name, ArgumentType<DataType> wrappedArgumentType, @Nonnull String argumentUsage, @Nullable String... examples) {
      super(name, argumentUsage, examples);
      this.wrappedArgumentType = wrappedArgumentType;
      this.withSharedSuggestions(wrappedArgumentType);
   }

   @Nonnull
   @Override
   public String[] getExamples() {
      return Arrays.equals((Object[])this.examples, (Object[])EMPTY_EXAMPLES) ? this.wrappedArgumentType.getExamples() : this.examples;
   }

   @Nullable
   public DataType get(@Nonnull MultiArgumentContext context) {
      return context.get(this);
   }

   @Override
   public int getSuggestionValueCount() {
      return this.wrappedArgumentType.getSuggestionValueCount();
   }

   @Override
   public void suggest(@Nonnull CommandSender sender, @Nonnull String textAlreadyEntered, int numParametersTyped, @Nonnull SuggestionResult result) {
      this.wrappedArgumentType.suggest(sender, textAlreadyEntered, numParametersTyped, result);
   }
}
