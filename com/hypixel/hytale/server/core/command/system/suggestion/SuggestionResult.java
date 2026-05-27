package com.hypixel.hytale.server.core.command.system.suggestion;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.function.Function;
import javax.annotation.Nonnull;

public class SuggestionResult {
   private final List<String> suggestions = new ObjectArrayList();
   private final List<Boolean> continuations = new ObjectArrayList();

   @Nonnull
   public SuggestionResult suggest(@Nonnull String suggestion) {
      this.suggestions.add(suggestion);
      this.continuations.add(false);
      return this;
   }

   @Nonnull
   public SuggestionResult suggestContinuation(@Nonnull String suggestion) {
      this.suggestions.add(suggestion);
      this.continuations.add(true);
      return this;
   }

   @Nonnull
   public <DataType> SuggestionResult suggest(@Nonnull Function<DataType, String> toStringFunction, @Nonnull DataType suggestion) {
      return this.suggest(toStringFunction.apply(suggestion));
   }

   @Nonnull
   public SuggestionResult suggest(@Nonnull Object objectToString) {
      return this.suggest(objectToString.toString());
   }

   @Nonnull
   public List<String> getSuggestions() {
      return this.suggestions;
   }

   @Nonnull
   public List<Boolean> getContinuations() {
      return this.continuations;
   }
}
