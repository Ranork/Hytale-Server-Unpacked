package com.hypixel.hytale.server.core.util;

public final class WildcardMatch {
   private WildcardMatch() {
   }

   public static boolean test(String text, String pattern) {
      return test(text, pattern, false);
   }

   public static boolean test(String text, String pattern, boolean ignoreCase) {
      if (ignoreCase) {
         text = text.toLowerCase();
         pattern = pattern.toLowerCase();
      }

      if (text.equals(pattern)) {
         return true;
      } else {
         int t = 0;
         int p = 0;
         int starIdx = -1;
         int match = 0;

         while (t < text.length()) {
            if (p >= pattern.length() || pattern.charAt(p) != '?' && pattern.charAt(p) != text.charAt(t)) {
               if (p < pattern.length() && pattern.charAt(p) == '*') {
                  starIdx = p++;
                  match = t;
               } else {
                  if (starIdx == -1) {
                     return false;
                  }

                  p = starIdx + 1;
                  t = ++match;
               }
            } else {
               t++;
               p++;
            }
         }

         while (p < pattern.length() && pattern.charAt(p) == '*') {
            p++;
         }

         return p == pattern.length();
      }
   }
}
