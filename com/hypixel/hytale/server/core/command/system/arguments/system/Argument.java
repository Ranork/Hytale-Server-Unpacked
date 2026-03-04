package com.hypixel.hytale.server.core.command.system.arguments.system;

import com.hypixel.hytale.codec.EmptyExtraInfo;
import com.hypixel.hytale.codec.validation.ValidationResults;
import com.hypixel.hytale.codec.validation.Validator;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.command.system.CommandValidationResults;
import com.hypixel.hytale.server.core.command.system.ParseResult;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgumentType;
import com.hypixel.hytale.server.core.command.system.suggestion.SuggestionProvider;
import com.hypixel.hytale.server.core.command.system.suggestion.SuggestionResult;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class Argument<Arg extends Argument<Arg, DataType>, DataType> {
   @Nonnull
   private final String name;
   @Nullable
   private final String description;
   private final ArgumentType<DataType> argumentType;
   @Nullable
   private SuggestionProvider suggestionProvider;
   @Nullable
   private List<Validator<DataType>> validators;
   @Nonnull
   private final AbstractCommand commandRegisteredTo;

   Argument(@Nonnull AbstractCommand commandRegisteredTo, @Nonnull String name, @Nullable String description, @Nonnull ArgumentType<DataType> argumentType) {
      this.commandRegisteredTo = commandRegisteredTo;
      this.name = name;
      this.description = description;
      this.argumentType = argumentType;
   }

   public Arg addValidator(@Nonnull Validator<DataType> validator) {
      if (this.commandRegisteredTo.hasBeenRegistered()) {
         throw new IllegalStateException("Cannot add validators after command has already completed registration");
      } else {
         if (this.validators == null) {
            this.validators = new ObjectArrayList();
         }

         this.validators.add(validator);
         return this.getThis();
      }
   }

   public void validate(@Nonnull DataType data, @Nonnull ParseResult parseResult) {
      if (this.validators != null) {
         CommandValidationResults results = new CommandValidationResults(EmptyExtraInfo.EMPTY);

         for (Validator<DataType> validator : this.validators) {
            validator.accept(data, (ValidationResults)results);
         }

         results.processResults(parseResult);
      }
   }

   public boolean provided(@Nonnull CommandContext context) {
      return context.provided(this);
   }

   public DataType get(@Nonnull CommandContext context) {
      return context.get(this);
   }

   @Nonnull
   protected abstract Arg getThis();

   @Nullable
   public DataType getProcessed(@Nonnull CommandContext context) {
      return this.argumentType.processedGet(context.sender(), context, this);
   }

   public Arg suggest(@Nonnull SuggestionProvider suggestionProvider) {
      if (this.commandRegisteredTo.hasBeenRegistered()) {
         throw new IllegalStateException("Cannot add a SuggestionProvider after command has already completed registration");
      } else {
         this.suggestionProvider = suggestionProvider;
         return this.getThis();
      }
   }

   @Nonnull
   public List<String> getSuggestions(@Nonnull CommandSender sender, @Nonnull String[] textAlreadyEntered) {
      SuggestionResult suggestionResult = new SuggestionResult();
      String textAlreadyEnteredAsSingleString = String.join(" ", textAlreadyEntered);
      if (this.suggestionProvider != null) {
         this.suggestionProvider.suggest(sender, textAlreadyEnteredAsSingleString, textAlreadyEntered.length, suggestionResult);
      }

      this.argumentType.suggest(sender, textAlreadyEnteredAsSingleString, textAlreadyEntered.length, suggestionResult);
      return suggestionResult.getSuggestions();
   }

   @Nonnull
   public abstract Message getUsageMessage();

   @Nonnull
   public abstract Message getUsageOneLiner();

   @Nonnull
   public AbstractCommand getCommandRegisteredTo() {
      return this.commandRegisteredTo;
   }

   @Nonnull
   public String getName() {
      return this.name;
   }

   @Nonnull
   public ArgumentType<DataType> getArgumentType() {
      return this.argumentType;
   }

   @Nullable
   public String getDescription() {
      return this.description;
   }

   @Nonnull
   @Override
   public String toString() {
      return "Argument{name='" + this.name + "', description='" + this.description + "', argumentType=" + this.argumentType + "}";
   }
}
