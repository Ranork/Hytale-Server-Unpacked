package com.hypixel.hytale.server.core;

import java.util.Collection;
import java.util.Comparator;
import java.util.function.BiPredicate;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public enum NameMatching {
   EXACT((s1, s2) -> s1.equals(s2) ? 0 : Integer.MIN_VALUE, String::equals),
   EXACT_IGNORE_CASE((s1, s2) -> s1.equalsIgnoreCase(s2) ? 0 : Integer.MIN_VALUE, String::equalsIgnoreCase),
   STARTS_WITH((s1, s2) -> s1.startsWith(s2) ? s1.length() - s2.length() : Integer.MIN_VALUE, String::equals),
   STARTS_WITH_IGNORE_CASE((s1, s2) -> s1.toLowerCase().startsWith(s2.toLowerCase()) ? s1.length() - s2.length() : Integer.MIN_VALUE, String::equalsIgnoreCase);

   @Nonnull
   public static NameMatching DEFAULT = STARTS_WITH_IGNORE_CASE;
   private final Comparator<String> comparator;
   private final BiPredicate<String, String> equality;

   private NameMatching(Comparator<String> comparator, BiPredicate<String, String> equality) {
      this.comparator = comparator;
      this.equality = equality;
   }

   public Comparator<String> getComparator() {
      return this.comparator;
   }

   @Nullable
   public <T> T find(@Nonnull Collection<T> players, String value, @Nonnull Function<T, String> getter) {
      return find(players, value, getter, this.comparator, this.equality);
   }

   @Nullable
   public static <T> T find(
      @Nonnull Collection<T> players,
      String value,
      @Nonnull Function<T, String> getter,
      @Nonnull Comparator<String> comparator,
      @Nonnull BiPredicate<String, String> equality
   ) {
      T closest = null;
      int highestScore = Integer.MIN_VALUE;

      for (T player : players) {
         String name = getter.apply(player);
         if (equality.test(name, value)) {
            return player;
         }

         int comparison = comparator.compare(name, value);
         if (comparison > highestScore) {
            highestScore = comparison;
            closest = player;
         }
      }

      return highestScore == Integer.MIN_VALUE ? null : closest;
   }
}
