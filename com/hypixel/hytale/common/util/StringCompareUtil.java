package com.hypixel.hytale.common.util;

import java.util.Locale;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class StringCompareUtil {
   public static int indexOfDifference(@Nullable CharSequence cs1, @Nullable CharSequence cs2) {
      if (cs1 == cs2) {
         return -1;
      } else if (cs1 != null && cs2 != null) {
         int i = 0;

         while (i < cs1.length() && i < cs2.length() && cs1.charAt(i) == cs2.charAt(i)) {
            i++;
         }

         return i >= cs2.length() && i >= cs1.length() ? -1 : i;
      } else {
         return 0;
      }
   }

   public static int getFuzzyDistance(@Nonnull CharSequence term, @Nonnull CharSequence query, @Nonnull Locale locale) {
      if (term != null && query != null) {
         if (locale == null) {
            throw new IllegalArgumentException("Locale must not be null");
         } else {
            String termLowerCase = term.toString().toLowerCase(locale);
            String queryLowerCase = query.toString().toLowerCase(locale);
            int score = 0;
            int termIndex = 0;
            int previousMatchingCharacterIndex = Integer.MIN_VALUE;

            for (int queryIndex = 0; queryIndex < queryLowerCase.length(); queryIndex++) {
               char queryChar = queryLowerCase.charAt(queryIndex);

               for (boolean termCharacterMatchFound = false; termIndex < termLowerCase.length() && !termCharacterMatchFound; termIndex++) {
                  char termChar = termLowerCase.charAt(termIndex);
                  if (queryChar == termChar) {
                     score++;
                     if (previousMatchingCharacterIndex + 1 == termIndex) {
                        score += 2;
                     }

                     previousMatchingCharacterIndex = termIndex;
                     termCharacterMatchFound = true;
                  }
               }
            }

            return score;
         }
      } else {
         throw new IllegalArgumentException("Strings must not be null");
      }
   }

   public static int getLevenshteinDistance(@Nonnull CharSequence s, @Nonnull CharSequence t) {
      if (s != null && t != null) {
         int n = s.length();
         int m = t.length();
         if (n == 0) {
            return m;
         } else if (m == 0) {
            return n;
         } else {
            if (n > m) {
               CharSequence tmp = s;
               s = t;
               t = tmp;
               n = m;
               m = tmp.length();
            }

            int[] p = new int[n + 1];
            int i = 0;

            while (i <= n) {
               p[i] = i++;
            }

            for (int j = 1; j <= m; j++) {
               int upper_left = p[0];
               char t_j = t.charAt(j - 1);
               p[0] = j;

               for (int var12 = 1; var12 <= n; var12++) {
                  int upper = p[var12];
                  int cost = s.charAt(var12 - 1) == t_j ? 0 : 1;
                  p[var12] = Math.min(Math.min(p[var12 - 1] + 1, p[var12] + 1), upper_left + cost);
                  upper_left = upper;
               }
            }

            return p[n];
         }
      } else {
         throw new IllegalArgumentException("Strings must not be null");
      }
   }
}
