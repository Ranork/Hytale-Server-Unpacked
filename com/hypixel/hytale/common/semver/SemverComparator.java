package com.hypixel.hytale.common.semver;

import java.util.Objects;
import java.util.function.BiPredicate;
import javax.annotation.Nonnull;

public class SemverComparator implements SemverSatisfies {
   private final SemverComparator.ComparisonType comparisonType;
   private final Semver compareTo;

   public SemverComparator(SemverComparator.ComparisonType comparisonType, Semver compareTo) {
      this.comparisonType = comparisonType;
      this.compareTo = compareTo;
   }

   @Override
   public boolean satisfies(Semver semver) {
      return this.comparisonType.satisfies(this.compareTo, semver);
   }

   @Nonnull
   public Semver getCompareTo() {
      return this.compareTo;
   }

   @Nonnull
   @Override
   public String toString() {
      return this.comparisonType.getPrefix() + this.compareTo;
   }

   @Nonnull
   public static SemverComparator fromString(String str) {
      Objects.requireNonNull(str, "Comparator string can't be null!");
      str = str.trim();
      if (str.isEmpty()) {
         throw new IllegalArgumentException("Comparator string is empty (input: '" + str + "')");
      } else {
         for (SemverComparator.ComparisonType comparisonType : SemverComparator.ComparisonType.values()) {
            if (str.startsWith(comparisonType.getPrefix())) {
               String versionPart = str.substring(comparisonType.getPrefix().length()).trim();
               if (versionPart.isEmpty()) {
                  throw new IllegalArgumentException("Comparator '" + comparisonType.getPrefix() + "' has no version after the operator (input: '" + str + "')");
               }

               Semver semver = Semver.fromString(versionPart);
               return new SemverComparator(comparisonType, semver);
            }
         }

         throw new IllegalArgumentException("Invalid comparator type! " + str);
      }
   }

   public static enum ComparisonType {
      GTE(">=", (ct, s) -> ct.compareTo(s) <= 0),
      GT(">", (ct, s) -> ct.compareTo(s) < 0),
      LTE("<=", (ct, s) -> ct.compareTo(s) >= 0),
      LT("<", (ct, s) -> ct.compareTo(s) > 0),
      EQUAL("=", (ct, s) -> ct.compareTo(s) == 0);

      private final String prefix;
      private final BiPredicate<Semver, Semver> satisfies;

      private ComparisonType(String prefix, BiPredicate<Semver, Semver> satisfies) {
         this.prefix = prefix;
         this.satisfies = satisfies;
      }

      public String getPrefix() {
         return this.prefix;
      }

      public boolean satisfies(Semver compareTo, Semver semver) {
         return this.satisfies.test(compareTo, semver);
      }

      public static boolean hasAPrefix(@Nonnull String range) {
         for (SemverComparator.ComparisonType comparisonType : values()) {
            if (range.startsWith(comparisonType.prefix)) {
               return true;
            }
         }

         return false;
      }
   }
}
