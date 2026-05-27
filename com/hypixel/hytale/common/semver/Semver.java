package com.hypixel.hytale.common.semver;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.common.util.StringUtil;
import java.util.Arrays;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Semver implements Comparable<Semver> {
   public static final Codec<Semver> CODEC = SemverCodec.INSTANCE;
   private final long major;
   private final long minor;
   private final long patch;
   private final String[] preRelease;
   private final String build;

   public Semver(long major, long minor, long patch) {
      this(major, minor, patch, null, null);
   }

   public Semver(long major, long minor, long patch, String[] preRelease, String build) {
      if (major < 0L) {
         throw new IllegalArgumentException("Major must be a non-negative integer");
      } else if (minor < 0L) {
         throw new IllegalArgumentException("Minor must be a non-negative integer");
      } else if (patch < 0L) {
         throw new IllegalArgumentException("Patch must be a non-negative integer");
      } else {
         validatePreRelease(preRelease);
         validateBuild(build);
         this.major = major;
         this.minor = minor;
         this.patch = patch;
         this.preRelease = preRelease;
         this.build = build;
      }
   }

   public long getMajor() {
      return this.major;
   }

   public long getMinor() {
      return this.minor;
   }

   public long getPatch() {
      return this.patch;
   }

   public String[] getPreRelease() {
      return this.preRelease == null ? new String[0] : (String[])this.preRelease.clone();
   }

   public boolean hasPreRelease() {
      return this.preRelease != null && this.preRelease.length > 0;
   }

   public String getBuild() {
      return this.build;
   }

   public boolean satisfies(@Nonnull SemverRange range) {
      return range.satisfies(this);
   }

   public int compareTo(@Nonnull Semver other) {
      if (this.major != other.major) {
         return Long.compare(this.major, other.major);
      } else if (this.minor != other.minor) {
         return Long.compare(this.minor, other.minor);
      } else if (this.patch != other.patch) {
         return Long.compare(this.patch, other.patch);
      } else {
         boolean thisHasPre = this.hasPreRelease();
         boolean otherHasPre = other.hasPreRelease();
         if (thisHasPre && !otherHasPre) {
            return -1;
         } else if (!thisHasPre && otherHasPre) {
            return 1;
         } else if (!thisHasPre) {
            return 0;
         } else {
            int i;
            for (i = 0; i < this.preRelease.length && i < other.preRelease.length; i++) {
               String pre = this.preRelease[i];
               String otherPre = other.preRelease[i];
               if (StringUtil.isNumericString(pre) && StringUtil.isNumericString(otherPre)) {
                  int compare = Integer.compare(Integer.parseInt(pre), Integer.parseInt(otherPre));
                  if (compare != 0) {
                     return compare;
                  }
               } else {
                  int compare = pre.compareTo(otherPre);
                  if (compare != 0) {
                     return compare;
                  }
               }
            }

            if (this.preRelease.length > i) {
               return 1;
            } else {
               return other.preRelease.length > i ? -1 : 0;
            }
         }
      }
   }

   @Nonnull
   @Override
   public String toString() {
      StringBuilder ver = new StringBuilder().append(this.major).append('.').append(this.minor).append('.').append(this.patch);
      if (this.preRelease != null && this.preRelease.length > 0) {
         ver.append('-').append(String.join(".", this.preRelease));
      }

      if (this.build != null && !this.build.isEmpty()) {
         ver.append('+').append(this.build);
      }

      return ver.toString();
   }

   @Nonnull
   public static Semver fromString(String str) {
      return fromString(str, false);
   }

   @Nonnull
   public static Semver fromString(String str, boolean strict) {
      Objects.requireNonNull(str, "Semver string can't be null!");
      str = str.trim();
      if (str.isEmpty()) {
         throw new IllegalArgumentException("Semver string is empty (input: '" + str + "')");
      } else {
         if (str.charAt(0) == '=' || str.charAt(0) == 'v') {
            str = str.substring(1);
         }

         if (!str.isEmpty() && (str.charAt(0) == '=' || str.charAt(0) == 'v')) {
            str = str.substring(1);
         }

         str = str.trim();
         if (str.isEmpty()) {
            throw new IllegalArgumentException("Semver string is empty after stripping leading '='/'v' (input: '" + str + "')");
         } else {
            String build = null;
            if (str.contains("+")) {
               String[] buildSplit = str.split("\\+", 2);
               str = buildSplit[0];
               build = buildSplit[1];
               validateBuild(build);
            }

            String[] preRelease = null;
            if (str.contains("-")) {
               String[] preReleaseSplit = str.split("-", 2);
               str = preReleaseSplit[0];
               preRelease = preReleaseSplit[1].split("\\.");
               validatePreRelease(preRelease);
            }

            if (str.isEmpty() || str.charAt(0) != '.' && str.charAt(str.length() - 1) != '.') {
               String[] split = str.split("\\.");
               if (split.length < 1) {
                  throw new IllegalArgumentException("String doesn't match <major>.<minor>.<patch> (" + str + ")");
               } else {
                  long major = parseComponent(split[0], "Major", str);
                  if (major < 0L) {
                     throw new IllegalArgumentException("Major must be a non-negative integers (" + str + ")");
                  } else if (!strict && split.length == 1) {
                     return new Semver(major, 0L, 0L, preRelease, build);
                  } else if (split.length < 2) {
                     throw new IllegalArgumentException("String doesn't match <major>.<minor>.<patch> (" + str + ")");
                  } else {
                     long minor = parseComponent(split[1], "Minor", str);
                     if (minor < 0L) {
                        throw new IllegalArgumentException("Minor must be a non-negative integers (" + str + ")");
                     } else if (!strict && split.length == 2) {
                        return new Semver(major, minor, 0L, preRelease, build);
                     } else if (split.length != 3) {
                        throw new IllegalArgumentException("String doesn't match <major>.<minor>.<patch> (" + str + ")");
                     } else {
                        long patch = parseComponent(split[2], "Patch", str);
                        if (patch < 0L) {
                           throw new IllegalArgumentException("Patch must be a non-negative integers (" + str + ")");
                        } else {
                           return new Semver(major, minor, patch, preRelease, build);
                        }
                     }
                  }
               }
            } else {
               throw new IllegalArgumentException("Failed to parse digits (" + str + ")");
            }
         }
      }
   }

   private static long parseComponent(@Nonnull String value, @Nonnull String component, @Nonnull String original) {
      try {
         return Long.parseLong(value);
      } catch (NumberFormatException var4) {
         throw new IllegalArgumentException(
            "Invalid version '"
               + original
               + "': "
               + component.toLowerCase()
               + " must be a non-negative integer, got '"
               + value
               + "'. Expected format: <major>.<minor>.<patch>.",
            var4
         );
      }
   }

   private static void validateBuild(@Nullable String build) {
      if (build != null) {
         if (!build.isEmpty() && build.charAt(0) != '.' && build.charAt(build.length() - 1) != '.') {
            for (int i = 0; i < build.length(); i++) {
               char c = build.charAt(i);
               if (c == '.') {
                  if (build.charAt(i - 1) == '.') {
                     throw new IllegalArgumentException("Build identifiers must only contain ASCII alphanumerics and hyphens [0-9A-Za-z-] (" + build + ")");
                  }
               } else if ((c < '0' || c > 'z' || c > '9' && c < 'A' || c > 'Z' && c < 'a') && c != '-') {
                  throw new IllegalArgumentException("Build identifiers must only contain ASCII alphanumerics and hyphens [0-9A-Za-z-] (" + build + ")");
               }
            }
         } else {
            throw new IllegalArgumentException("Build identifiers must only contain ASCII alphanumerics and hyphens [0-9A-Za-z-] (" + build + ")");
         }
      }
   }

   private static void validatePreRelease(@Nullable String[] preRelease) {
      if (preRelease != null) {
         for (String preReleasePart : preRelease) {
            if (preReleasePart.isEmpty() || !StringUtil.isAlphaNumericHyphenString(preReleasePart)) {
               throw new IllegalArgumentException("Pre-release must only be alphanumeric (" + Arrays.toString((Object[])preRelease) + ")");
            }
         }
      }
   }
}
