package com.hypixel.hytale.codec.util;

import it.unimi.dsi.fastutil.chars.CharArrayList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import javax.annotation.Nullable;

public class Documentation {
   public static String stripMarkdown(@Nullable String markdown) {
      if (markdown == null) {
         return null;
      } else {
         StringBuilder output = new StringBuilder();
         IntArrayList counts = new IntArrayList();
         CharArrayList expectedChars = new CharArrayList();

         for (int i = 0; i < markdown.length(); i++) {
            char c = markdown.charAt(i);
            switch (c) {
               case '*':
               case '_':
                  int start = i;
                  boolean isEnding = i >= 1 && !Character.isWhitespace(markdown.charAt(i - 1));
                  if (!isEnding || !expectedChars.isEmpty() && expectedChars.getChar(expectedChars.size() - 1) == c) {
                     int targetCount = !counts.isEmpty() && isEnding ? counts.getInt(counts.size() - 1) : -1;

                     while (i < markdown.length() && markdown.charAt(i) == c && i - start != targetCount) {
                        i++;
                     }

                     int matchingCount = i - start;
                     if (!counts.isEmpty() && counts.getInt(counts.size() - 1) == matchingCount) {
                        if (!isEnding) {
                           output.append(String.valueOf(c).repeat(matchingCount));
                           continue;
                        }

                        counts.removeInt(counts.size() - 1);
                        expectedChars.removeChar(expectedChars.size() - 1);
                     } else {
                        if (i < markdown.length() && Character.isWhitespace(markdown.charAt(i))) {
                           output.append(String.valueOf(c).repeat(matchingCount));
                           output.append(markdown.charAt(i));
                           continue;
                        }

                        counts.add(matchingCount);
                        expectedChars.add(c);
                     }

                     i--;
                  } else {
                     output.append(markdown.charAt(i));
                  }
                  break;
               default:
                  output.append(c);
            }
         }

         if (!counts.isEmpty()) {
            throw new IllegalArgumentException("Unbalanced markdown formatting");
         } else {
            return output.toString();
         }
      }
   }
}
