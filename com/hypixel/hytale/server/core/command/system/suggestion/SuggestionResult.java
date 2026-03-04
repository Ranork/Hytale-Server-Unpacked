package com.hypixel.hytale.server.core.command.system.suggestion;

import com.hypixel.hytale.common.util.StringCompareUtil;
import it.unimi.dsi.fastutil.ints.IntObjectPair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import javax.annotation.Nonnull;

public class SuggestionResult {
   private static final int FUZZY_SUGGESTION_MAX_RESULTS = 5;
   @Nonnull
   private static final Comparator<IntObjectPair<String>> INTEGER_STRING_PAIR_COMPARATOR = Comparator.comparingInt(IntObjectPair::leftInt);
   private final List<String> suggestions = new ObjectArrayList();

   @Nonnull
   public SuggestionResult suggest(@Nonnull String suggestion) {
      this.suggestions.add(suggestion);
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
   public <DataType> SuggestionResult fuzzySuggest(
      @Nonnull String input, @Nonnull Collection<DataType> items, @Nonnull Function<DataType, String> toStringFunction
   ) {
      List<IntObjectPair<String>> sorting = new ObjectArrayList(5);
      int lowestStoredFuzzyValue = Integer.MIN_VALUE;

      for (DataType item : items) {
         String toString = toStringFunction.apply(item);
         int fuzzyValue = StringCompareUtil.getFuzzyDistance(toString, input, Locale.ENGLISH);
         if (sorting.size() == 5) {
            if (fuzzyValue < lowestStoredFuzzyValue) {
               continue;
            }

            sorting.set(0, IntObjectPair.of(fuzzyValue, toString));
         } else {
            sorting.add(IntObjectPair.of(fuzzyValue, toString));
         }

         sorting.sort(INTEGER_STRING_PAIR_COMPARATOR);
         lowestStoredFuzzyValue = sorting.getFirst().leftInt();
      }

      for (IntObjectPair<String> integerStringPair : sorting) {
         this.suggest((String)integerStringPair.right());
      }

      return this;
   }
}
