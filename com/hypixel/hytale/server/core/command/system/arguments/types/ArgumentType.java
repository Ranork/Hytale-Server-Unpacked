package com.hypixel.hytale.server.core.command.system.arguments.types;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.command.system.ParseResult;
import com.hypixel.hytale.server.core.command.system.arguments.system.Argument;
import com.hypixel.hytale.server.core.command.system.suggestion.SuggestionProvider;
import com.hypixel.hytale.server.core.command.system.suggestion.SuggestionResult;
import java.util.Arrays;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class ArgumentType<DataType> implements SuggestionProvider {
   @Nonnull
   public static final String[] EMPTY_EXAMPLES = new String[0];
   private final Message name;
   @Nonnull
   private final Message argumentUsage;
   @Nonnull
   protected final String[] examples;
   protected int numberOfParameters;

   protected ArgumentType(@Nonnull Message name, @Nonnull Message argumentUsage, int numberOfParameters, @Nullable String... examples) {
      this.name = name;
      this.argumentUsage = argumentUsage;
      this.numberOfParameters = numberOfParameters;
      if (numberOfParameters < 0) {
         throw new IllegalArgumentException("You cannot have less than 0 parameters for a argument type");
      } else {
         this.examples = examples == null ? EMPTY_EXAMPLES : examples;
      }
   }

   protected ArgumentType(@Nonnull String name, @Nonnull Message argumentUsage, int numberOfParameters, @Nullable String... examples) {
      this(Message.translation(name), argumentUsage, numberOfParameters, examples);
   }

   protected ArgumentType(String name, @Nonnull String argumentUsage, int numberOfParameters, @Nullable String... examples) {
      this(Message.translation(name), Message.translation(argumentUsage), numberOfParameters, examples);
   }

   @Nullable
   public DataType processedGet(CommandSender sender, CommandContext context, Argument<?, DataType> argument) {
      throw new UnsupportedOperationException("This method has not yet been implemented in the subclass, please implement it or do not call it");
   }

   @Override
   public void suggest(@Nonnull CommandSender sender, @Nonnull String textAlreadyEntered, int numParametersTyped, @Nonnull SuggestionResult result) {
   }

   @Nullable
   public abstract DataType parse(@Nonnull String[] var1, @Nonnull ParseResult var2);

   @Nonnull
   public Message getArgumentUsage() {
      return this.argumentUsage;
   }

   public int getNumberOfParameters() {
      return this.numberOfParameters;
   }

   @Nonnull
   public Message getName() {
      return this.name;
   }

   @Nonnull
   public String[] getExamples() {
      return this.examples;
   }

   public boolean isListArgument() {
      return false;
   }

   @Nonnull
   @Override
   public String toString() {
      return "ArgumentType{name='"
         + this.name
         + "', argumentUsage="
         + this.argumentUsage
         + ", examples="
         + Arrays.toString((Object[])this.examples)
         + ", numberOfParameters="
         + this.numberOfParameters
         + "}";
   }
}
