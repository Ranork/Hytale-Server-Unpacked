package com.hypixel.hytale.common.semver;

import com.hypixel.hytale.codec.Codec;
import java.util.Objects;
import java.util.StringJoiner;
import javax.annotation.Nonnull;

public class SemverRange implements SemverSatisfies {
   public static final Codec<SemverRange> CODEC = SemverRangeCodec.INSTANCE;
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

         return this.comparators.length == 0 ? true : matchesPreReleaseRule(semver, this.comparators);
      } else {
         for (SemverSatisfies child : this.comparators) {
            if (child instanceof SemverComparator sc) {
               if (sc.satisfies(semver) && matchesPreReleaseRule(semver, new SemverSatisfies[]{sc})) {
                  return true;
               }
            } else if (child.satisfies(semver)) {
               return true;
            }
         }

         return false;
      }
   }

   private static boolean matchesPreReleaseRule(@Nonnull Semver version, @Nonnull SemverSatisfies[] set) {
      if (!version.hasPreRelease()) {
         return true;
      } else {
         for (SemverSatisfies s : set) {
            if (s instanceof SemverComparator sc) {
               Semver target = sc.getCompareTo();
               if (target.hasPreRelease()
                  && target.getMajor() == version.getMajor()
                  && target.getMinor() == version.getMinor()
                  && target.getPatch() == version.getPatch()) {
                  return true;
               }
            }
         }

         return false;
      }
   }

   @Override
   public String toString() {
      if (this.comparators.length == 0) {
         return "*";
      } else {
         StringJoiner joiner = new StringJoiner(this.and ? " " : " || ");

         for (SemverSatisfies comparator : this.comparators) {
            joiner.add(comparator.toString());
         }

         return joiner.toString();
      }
   }

   @Nonnull
   public static SemverRange fromString(String str) {
      return fromString(str, false);
   }

   @Nonnull
   public static SemverRange fromString(String str, boolean strict) {
      Objects.requireNonNull(str, "String can't be null!");
      str = str.trim();
      if (!str.isEmpty() && !"*".equals(str)) {
         String[] split = str.split("\\|\\|", -1);
         SemverSatisfies[] comparators = new SemverSatisfies[split.length];

         for (int i = 0; i < split.length; i++) {
            String subRange = split[i].trim();
            if (subRange.isEmpty()) {
               throw new IllegalArgumentException("Sub-range is empty (input: '" + str + "')");
            }

            if (subRange.contains(" - ")) {
               String[] range = subRange.split(" - ");
               if (range.length != 2) {
                  throw new IllegalArgumentException("Hyphen range '" + subRange + "' must have exactly one ' - ' separator");
               }

               comparators[i] = new SemverRange(
                  new SemverSatisfies[]{
                     new SemverComparator(SemverComparator.ComparisonType.GTE, Semver.fromString(range[0].trim(), strict)),
                     new SemverComparator(SemverComparator.ComparisonType.LTE, Semver.fromString(range[1].trim(), strict))
                  },
                  true
               );
            } else if (subRange.charAt(0) == '~') {
               String rest = subRange.substring(1).trim();
               if (rest.isEmpty()) {
                  throw new IllegalArgumentException("Tilde range '~' has no version (input: '" + subRange + "')");
               }

               Semver semver = Semver.fromString(rest, strict);
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
               String restx = subRange.substring(1).trim();
               if (restx.isEmpty()) {
                  throw new IllegalArgumentException("Caret range '^' has no version (input: '" + subRange + "')");
               }

               Semver semver = Semver.fromString(restx, strict);
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
            } else if (!subRange.contains(" ")) {
               if (SemverComparator.ComparisonType.hasAPrefix(subRange)) {
                  comparators[i] = SemverComparator.fromString(subRange);
               } else {
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
                        throw new IllegalArgumentException(
                           "Bare version '"
                              + subRange
                              + "' is not a valid range. Use '="
                              + subRange
                              + "' for an exact match, or '^"
                              + subRange
                              + "' / '~"
                              + subRange
                              + "' for a range. Bare ranges only work when the patch is zero (e.g. '1.2.0' or '1.x')."
                        );
                     }

                     comparators[i] = new SemverRange(
                        new SemverSatisfies[]{
                           new SemverComparator(SemverComparator.ComparisonType.GTE, semver),
                           new SemverComparator(SemverComparator.ComparisonType.LT, new Semver(semver.getMajor(), semver.getMinor() + 1L, 0L, null, null))
                        },
                        true
                     );
                  }
               }
            } else {
               String[] comparatorStrings = subRange.split("\\s+");
               SemverSatisfies[] comparatorsAnd = new SemverSatisfies[comparatorStrings.length];

               for (int y = 0; y < comparatorStrings.length; y++) {
                  comparatorsAnd[y] = SemverComparator.fromString(comparatorStrings[y]);
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
