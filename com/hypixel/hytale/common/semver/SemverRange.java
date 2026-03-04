package com.hypixel.hytale.common.semver;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.function.FunctionCodec;
import java.util.Objects;
import java.util.StringJoiner;
import javax.annotation.Nonnull;

public class SemverRange implements SemverSatisfies {
   public static final Codec<SemverRange> CODEC = new FunctionCodec<>(Codec.STRING, SemverRange::fromString, SemverRange::toString);
   public static final SemverRange WILDCARD = new SemverRange(new SemverSatisfies[0], true);
   private final SemverSatisfies[] comparators;
   private final boolean and;

   public SemverRange(SemverSatisfies[] comparators, boolean and) {
      this.comparators = comparators;
      this.and = and;
   }

   @Override
   public boolean satisfies(Semver semver) {
      if (this.and) {
         for (SemverSatisfies comparator : this.comparators) {
            if (!comparator.satisfies(semver)) {
               return false;
            }
         }

         return true;
      } else {
         for (SemverSatisfies comparatorx : this.comparators) {
            if (comparatorx.satisfies(semver)) {
               return true;
            }
         }

         return false;
      }
   }

   @Override
   public String toString() {
      StringJoiner joiner = new StringJoiner(" || ");

      for (SemverSatisfies comparator : this.comparators) {
         joiner.add(comparator.toString());
      }

      return joiner.toString();
   }

   @Nonnull
   public static SemverRange fromString(String str) {
      return fromString(str, false);
   }

   @Nonnull
   public static SemverRange fromString(String str, boolean strict) {
      Objects.requireNonNull(str, "String can't be null!");
      str = str.trim();
      if (!str.isBlank() && !"*".equals(str)) {
         String[] split = str.split("\\|\\|");
         SemverSatisfies[] comparators = new SemverSatisfies[split.length];

         for (int i = 0; i < split.length; i++) {
            String subRange = split[i].trim();
            if (subRange.contains(" - ")) {
               String[] range = subRange.split(" - ");
               if (range.length != 2) {
                  throw new IllegalArgumentException("Range has an invalid number of arguments!");
               }

               comparators[i] = new SemverRange(
                  new SemverSatisfies[]{
                     new SemverComparator(SemverComparator.ComparisonType.GTE, Semver.fromString(range[0], strict)),
                     new SemverComparator(SemverComparator.ComparisonType.LTE, Semver.fromString(range[1], strict))
                  },
                  true
               );
            } else if (subRange.charAt(0) == '~') {
               Semver semver = Semver.fromString(subRange.substring(1), strict);
               if (semver.getMinor() > 0L) {
                  comparators[i] = new SemverRange(
                     new SemverSatisfies[]{
                        new SemverComparator(SemverComparator.ComparisonType.GTE, semver),
                        new SemverComparator(SemverComparator.ComparisonType.LT, new Semver(semver.getMajor(), semver.getMinor() + 1L, 0L, null, null))
                     },
                     true
                  );
               } else {
                  comparators[i] = new SemverRange(
                     new SemverSatisfies[]{
                        new SemverComparator(SemverComparator.ComparisonType.GTE, semver),
                        new SemverComparator(SemverComparator.ComparisonType.LT, new Semver(semver.getMajor() + 1L, 0L, 0L, null, null))
                     },
                     true
                  );
               }
            } else if (subRange.charAt(0) == '^') {
               Semver semver = Semver.fromString(subRange.substring(1), strict);
               if (semver.getMajor() > 0L) {
                  comparators[i] = new SemverRange(
                     new SemverSatisfies[]{
                        new SemverComparator(SemverComparator.ComparisonType.GTE, semver),
                        new SemverComparator(SemverComparator.ComparisonType.LT, new Semver(semver.getMajor() + 1L, 0L, 0L, null, null))
                     },
                     true
                  );
               } else if (semver.getMinor() > 0L) {
                  comparators[i] = new SemverRange(
                     new SemverSatisfies[]{
                        new SemverComparator(SemverComparator.ComparisonType.GTE, semver),
                        new SemverComparator(SemverComparator.ComparisonType.LT, new Semver(0L, semver.getMinor() + 1L, 0L, null, null))
                     },
                     true
                  );
               } else {
                  comparators[i] = new SemverRange(
                     new SemverSatisfies[]{
                        new SemverComparator(SemverComparator.ComparisonType.GTE, semver),
                        new SemverComparator(SemverComparator.ComparisonType.LT, new Semver(0L, 0L, semver.getPatch() + 1L, null, null))
                     },
                     true
                  );
               }
            } else if (SemverComparator.ComparisonType.hasAPrefix(subRange)) {
               comparators[i] = SemverComparator.fromString(subRange);
            } else if (!subRange.contains(" ")) {
               Semver semver = Semver.fromString(subRange.replace("x", "0").replace("*", "0"), strict);
               if (semver.getPatch() == 0L && semver.getMinor() == 0L && semver.getMajor() == 0L) {
                  comparators[i] = new SemverComparator(SemverComparator.ComparisonType.GTE, new Semver(0L, 0L, 0L));
               } else if (semver.getPatch() == 0L && semver.getMinor() == 0L) {
                  comparators[i] = new SemverRange(
                     new SemverSatisfies[]{
                        new SemverComparator(SemverComparator.ComparisonType.GTE, semver),
                        new SemverComparator(SemverComparator.ComparisonType.LT, new Semver(semver.getMajor() + 1L, 0L, 0L, null, null))
                     },
                     true
                  );
               } else {
                  if (semver.getPatch() != 0L) {
                     throw new IllegalArgumentException("Invalid X-Range! " + subRange);
                  }

                  comparators[i] = new SemverRange(
                     new SemverSatisfies[]{
                        new SemverComparator(SemverComparator.ComparisonType.GTE, semver),
                        new SemverComparator(SemverComparator.ComparisonType.LT, new Semver(semver.getMajor(), semver.getMinor() + 1L, 0L, null, null))
                     },
                     true
                  );
               }
            } else {
               String[] comparatorStrings = subRange.split(" ");
               SemverSatisfies[] comparatorsAnd = new SemverSatisfies[comparatorStrings.length];

               for (int y = 0; y < comparatorStrings.length; y++) {
                  comparatorsAnd[i] = SemverComparator.fromString(comparatorStrings[i]);
               }

               comparators[i] = new SemverRange(comparatorsAnd, true);
            }
         }

         return new SemverRange(comparators, false);
      } else {
         return WILDCARD;
      }
   }
}
