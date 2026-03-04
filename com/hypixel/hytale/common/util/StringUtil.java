package com.hypixel.hytale.common.util;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.DoubleFunction;
import java.util.function.IntToDoubleFunction;
import java.util.function.IntToLongFunction;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class StringUtil {
   public static final Pattern RAW_ARGS_PATTERN = Pattern.compile(" -- ");
   @Nonnull
   private static final char[] GRAPH_CHARS = new char[]{'_', '▄', '─', '▀', '¯'};

   public static boolean isNumericString(@Nonnull String str) {
      for (int i = 0; i < str.length(); i++) {
         char c = str.charAt(i);
         if (c < '0' || c > '9') {
            return false;
         }
      }

      return true;
   }

   public static boolean isAlphaNumericHyphenString(@Nonnull String str) {
      for (int i = 0; i < str.length(); i++) {
         char c = str.charAt(i);
         if ((c < '0' || c > 'z' || c > '9' && c < 'A' || c > 'Z' && c < 'a') && c != '-') {
            return false;
         }
      }

      return true;
   }

   public static boolean isAlphaNumericHyphenUnderscoreString(@Nonnull String str) {
      for (int i = 0; i < str.length(); i++) {
         char c = str.charAt(i);
         if ((c < '0' || c > 'z' || c > '9' && c < 'A' || c > 'Z' && c < 'a') && c != '-' && c != '_') {
            return false;
         }
      }

      return true;
   }

   public static boolean isCapitalized(@Nonnull String keyStr, char delim) {
      boolean wasDelimOrFirst = true;

      for (int i = 0; i < keyStr.length(); i++) {
         char c = keyStr.charAt(i);
         if (wasDelimOrFirst && c != Character.toUpperCase(c)) {
            return false;
         }

         wasDelimOrFirst = c == delim;
      }

      return true;
   }

   @Nonnull
   public static String capitalize(@Nonnull String keyStr, char delim) {
      boolean wasDelimOrFirst = true;
      StringBuilder sb = new StringBuilder();

      for (int i = 0; i < keyStr.length(); i++) {
         char c = keyStr.charAt(i);
         sb.append(wasDelimOrFirst ? Character.toUpperCase(c) : c);
         wasDelimOrFirst = c == delim;
      }

      return sb.toString();
   }

   @Nullable
   public static <V extends Enum<V>> V parseEnum(@Nonnull V[] enumConstants, String str) {
      return parseEnum(enumConstants, str, StringUtil.MatchType.EQUALS);
   }

   @Nullable
   public static <V extends Enum<V>> V parseEnum(@Nonnull V[] enumConstants, String str, StringUtil.MatchType matchType) {
      if (matchType == StringUtil.MatchType.EQUALS) {
         for (V enumValue : enumConstants) {
            if (enumValue.name().equals(str)) {
               return enumValue;
            }
         }
      } else if (matchType == StringUtil.MatchType.CASE_INSENSITIVE) {
         for (V enumValuex : enumConstants) {
            if (enumValuex.name().equalsIgnoreCase(str)) {
               return enumValuex;
            }
         }
      } else {
         str = str.toLowerCase();
         V closest = null;
         int diff = -2;

         for (V enumValuexx : enumConstants) {
            int index = StringCompareUtil.indexOfDifference(str, enumValuexx.name().toLowerCase());
            if (index > diff && diff != -1 || index == -1 || diff == -2) {
               closest = enumValuexx;
               diff = index;
            }
         }

         if (diff > -2) {
            return closest;
         }
      }

      return null;
   }

   @Nonnull
   @Deprecated(forRemoval = true)
   public static String[] parseArgs(String rawString, @Nonnull Map<String, String> argOptions) {
      String[] rawSplit = RAW_ARGS_PATTERN.split(rawString, 2);
      String argsStr = rawSplit[0];
      boolean hasRaw = rawSplit.length > 1;
      char quote = 0;
      int start = 0;
      List<String> argsList = new ObjectArrayList();

      for (int i = 0; i < argsStr.length(); i++) {
         char c = argsStr.charAt(i);
         switch (c) {
            case ' ':
               if (quote == 0) {
                  if (start != i) {
                     argsList.add(argsStr.substring(start, i));
                  }

                  start = i + 1;
               }
               break;
            case '"':
               switch (quote) {
                  case '\u0000':
                     quote = '"';
                     continue;
                  case '"':
                     quote = 0;
                     argsList.add(argsStr.substring(start, i + 1));
                     start = i + 1;
                  default:
                     continue;
               }
            case '\'':
               switch (quote) {
                  case '\u0000':
                     quote = '\'';
                     continue;
                  case '\'':
                     quote = 0;
                     argsList.add(argsStr.substring(start, i + 1));
                     start = i + 1;
                  default:
                     continue;
               }
            case '\\':
               if (i + 1 >= argsStr.length()) {
                  throw new IllegalStateException("Invalid escape at end of string, index " + (i + 1) + " in: " + rawString);
               }

               char c1 = argsStr.charAt(i + 1);
               switch (c1) {
                  case '"':
                  case '\'':
                  case '\\':
                     argsStr = argsStr.substring(0, i) + argsStr.substring(i + 1);
                     i++;
                     break;
                  default:
                     throw new IllegalStateException("Invalid escape for char " + c1 + " at index " + (i + 1) + " in: " + rawString);
               }
         }
      }

      if (quote != 0) {
         throw new IllegalStateException("Unbalanced quotes!");
      } else {
         if (start != argsStr.length()) {
            argsList.add(argsStr.substring(start));
         }

         argsList.removeIf(arg -> {
            if (arg.startsWith("--")) {
               String[] split = arg.substring(2).split("=", 2);
               String value = "";
               if (split.length > 1) {
                  value = split[1];
                  value = removeQuotes(value);
               }

               argOptions.put(split[0], value);
               return true;
            } else {
               return false;
            }
         });
         argsList.replaceAll(value -> removeQuotes(value.trim()));
         if (hasRaw) {
            String[] strings = new String[argsList.size() + 1];
            argsList.toArray(strings);
            strings[argsList.size()] = rawSplit[1].trim();
            return strings;
         } else {
            return argsList.toArray(String[]::new);
         }
      }
   }

   @Nonnull
   public static String[] parseArgs(String rawString) {
      String[] rawSplit = RAW_ARGS_PATTERN.split(rawString, 2);
      String argsStr = rawSplit[0];
      boolean hasRaw = rawSplit.length > 1;
      char quote = 0;
      int start = 0;
      List<String> argsList = new ObjectArrayList();

      for (int i = 0; i < argsStr.length(); i++) {
         char c = argsStr.charAt(i);
         switch (c) {
            case ' ':
               if (quote == 0) {
                  if (start != i) {
                     argsList.add(argsStr.substring(start, i));
                  }

                  start = i + 1;
               }
               break;
            case '"':
               switch (quote) {
                  case '\u0000':
                     quote = '"';
                     continue;
                  case '"':
                     quote = 0;
                     argsList.add(argsStr.substring(start, i + 1));
                     start = i + 1;
                  default:
                     continue;
               }
            case '\'':
               switch (quote) {
                  case '\u0000':
                     quote = '\'';
                     continue;
                  case '\'':
                     quote = 0;
                     argsList.add(argsStr.substring(start, i + 1));
                     start = i + 1;
                  default:
                     continue;
               }
            case '\\':
               if (i + 1 >= argsStr.length()) {
                  throw new IllegalStateException("Invalid escape at end of string, index " + (i + 1) + " in: " + rawString);
               }

               char c1 = argsStr.charAt(i + 1);
               switch (c1) {
                  case '"':
                  case '\'':
                  case '\\':
                     argsStr = argsStr.substring(0, i) + argsStr.substring(i + 1);
                     i++;
                     break;
                  default:
                     throw new IllegalStateException("Invalid escape for char " + c1 + " at index " + (i + 1) + " in: " + rawString);
               }
         }
      }

      if (quote != 0) {
         throw new IllegalStateException("Unbalanced quotes!");
      } else {
         if (start != argsStr.length()) {
            argsList.add(argsStr.substring(start));
         }

         argsList.replaceAll(value -> removeQuotes(value.trim()));
         if (hasRaw) {
            String[] strings = new String[argsList.size() + 1];
            argsList.toArray(strings);
            strings[argsList.size()] = rawSplit[1].trim();
            return strings;
         } else {
            return argsList.toArray(String[]::new);
         }
      }
   }

   @Nonnull
   public static String removeQuotes(@Nonnull String value) {
      switch (value.charAt(0)) {
         case '"':
         case '\'':
            value = value.substring(1, value.length() - 1);
         default:
            return value;
      }
   }

   @Nonnull
   public static String stripQuotes(@Nonnull String s) {
      if (s.length() >= 2) {
         char first = s.charAt(0);
         char last = s.charAt(s.length() - 1);
         if (first == '"' && last == '"' || first == '\'' && last == '\'') {
            return s.substring(1, s.length() - 1);
         }
      }

      return s;
   }

   public static boolean isGlobMatching(@Nonnull String pattern, @Nonnull String text) {
      return pattern.equals(text) || isGlobMatching(pattern, 0, text, 0);
   }

   public static boolean isGlobMatching(@Nonnull String pattern, int patternPos, @Nonnull String text, int textPos) {
      while (patternPos < pattern.length()) {
         char charAt = pattern.charAt(patternPos);
         if (charAt == '*') {
            patternPos++;

            while (patternPos < pattern.length() && pattern.charAt(patternPos) == '*') {
               patternPos++;
            }

            if (patternPos == pattern.length()) {
               return true;
            }

            for (char matchChar = pattern.charAt(patternPos); textPos < text.length(); textPos++) {
               if (matchChar == text.charAt(textPos) && isGlobMatching(pattern, patternPos + 1, text, textPos + 1)) {
                  return true;
               }
            }

            return false;
         }

         if (textPos == text.length()) {
            return false;
         }

         if (charAt != '?' && charAt != text.charAt(textPos)) {
            return false;
         }

         patternPos++;
         textPos++;
      }

      return textPos == text.length();
   }

   public static boolean isGlobPattern(@Nonnull String text) {
      for (int i = 0; i < text.length(); i++) {
         char c = text.charAt(i);
         if (c == '?' || c == '*') {
            return true;
         }
      }

      return false;
   }

   @Nonnull
   public static String humanizeTime(@Nonnull Duration duration, boolean useSeconds) {
      long length = duration.toMillis();
      long days = length / 86400000L;
      long hours = (length - days * 86400000L) / 3600000L;
      long minutes = (length - (days * 86400000L + hours * 3600000L)) / 60000L;
      String base = days + "d " + hours + "h " + minutes + "m";
      if (useSeconds) {
         long seconds = (length - (days * 86400000L + hours * 3600000L + minutes * 60000L)) / 1000L;
         base = base + " " + seconds + "s";
      }

      return base;
   }

   @Nonnull
   public static String humanizeTime(@Nonnull Duration length) {
      return humanizeTime(length, false);
   }

   @Nonnull
   public static <T> List<T> sortByFuzzyDistance(@Nonnull String str, @Nonnull Collection<T> collection, int length) {
      List<T> list = sortByFuzzyDistance(str, collection);
      return list.size() > length ? list.subList(0, length) : list;
   }

   @Nonnull
   public static <T> List<T> sortByFuzzyDistance(@Nonnull String str, @Nonnull Collection<T> collection) {
      Object2IntMap<T> map = new Object2IntOpenHashMap(collection.size());

      for (T value : collection) {
         map.put(value, StringCompareUtil.getFuzzyDistance(value.toString(), str, Locale.ENGLISH));
      }

      List<T> list = new ObjectArrayList(collection);
      list.sort(Comparator.comparingInt(map::getInt).reversed());
      return list;
   }

   @Nonnull
   public static String toPaddedBinaryString(int val) {
      byte[] buf = new byte[]{48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48};
      int leadingZeros = Integer.numberOfLeadingZeros(val);
      if (leadingZeros == 32) {
         return new String(buf, 0);
      } else {
         int mag = 32 - leadingZeros;
         int pos = Math.max(mag, 1);

         do {
            buf[leadingZeros + --pos] = (byte)(48 + (val & 1));
            val >>>= 1;
         } while (pos > 0);

         return new String(buf, 0);
      }
   }

   @Nonnull
   public static String trimEnd(@Nonnull String str, @Nonnull String end) {
      return !str.endsWith(end) ? str : str.substring(0, str.length() - end.length());
   }

   public static void generateGraph(
      @Nonnull StringBuilder sb,
      int width,
      int height,
      long minX,
      long maxX,
      double minY,
      double maxY,
      @Nonnull DoubleFunction<String> labelFormatFunc,
      int historyLength,
      @Nonnull IntToLongFunction timestampFunc,
      @Nonnull IntToDoubleFunction valueFunc
   ) {
      double lengthY = maxY - minY;
      long lengthX = maxX - minX;
      double rowAggLength = lengthY / height;
      double colAggLength = (double)lengthX / width;
      double[] values = new double[width];
      Arrays.fill(values, -1.0);
      int historyIndex = 0;

      for (int i = 0; i < width; i++) {
         double total = 0.0;
         int count = 0;

         for (long nextAggTimestamp = maxX - (lengthX - (long)(colAggLength * i));
            historyIndex < historyLength && timestampFunc.applyAsLong(historyIndex) < nextAggTimestamp;
            historyIndex++
         ) {
            total += valueFunc.applyAsDouble(historyIndex);
            count++;
         }

         if (count != 0) {
            values[i] = total / count;
         } else if (i > 0) {
            values[i] = values[i - 1];
         }
      }

      double last = -1.0;

      for (int i = values.length - 1; i >= 0; i--) {
         if (values[i] != -1.0) {
            last = values[i];
         } else if (last != -1.0) {
            values[i] = last;
         }
      }

      int yLabelWidth = 0;
      String[] labels = new String[height];

      for (int row = 0; row < height; row++) {
         double rowMaxValue = minY + lengthY - rowAggLength * row;
         String label = labelFormatFunc.apply(rowMaxValue);
         labels[row] = label;
         int length = label.length();
         if (length > yLabelWidth) {
            yLabelWidth = length;
         }
      }

      String bar = " ".repeat(yLabelWidth) + " " + "#".repeat(width + 2);
      sb.append(bar).append('\n');

      for (int rowx = 0; rowx < height; rowx++) {
         sb.append(" ".repeat(Math.max(0, yLabelWidth - labels[rowx].length()))).append(labels[rowx]).append(" #");
         double rowMinValue = minY + lengthY - rowAggLength * (rowx + 1);

         for (int col = 0; col < width; col++) {
            double colRowValue = values[col] - rowMinValue;
            if (!(colRowValue <= 0.0) && !(colRowValue > rowAggLength)) {
               double valuePercent = colRowValue / rowAggLength;
               int charIndex = (int)Math.round(valuePercent * (GRAPH_CHARS.length - 1));
               sb.append(GRAPH_CHARS[Math.max(0, charIndex)]);
            } else {
               sb.append(' ');
            }
         }

         sb.append("#\n");
      }

      sb.append(bar).append('\n');
      sb.append('\n');
   }

   public static enum MatchType {
      INDEX_DIFFERENCE,
      EQUALS,
      CASE_INSENSITIVE;
   }
}
