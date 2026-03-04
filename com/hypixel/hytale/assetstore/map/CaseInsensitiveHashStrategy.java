package com.hypixel.hytale.assetstore.map;

import it.unimi.dsi.fastutil.Hash.Strategy;

public class CaseInsensitiveHashStrategy<K> implements Strategy<K> {
   private static final CaseInsensitiveHashStrategy INSTANCE = new CaseInsensitiveHashStrategy();

   public static <K> CaseInsensitiveHashStrategy<K> getInstance() {
      return INSTANCE;
   }

   public int hashCode(K key) {
      if (key == null) {
         return 0;
      } else if (!(key instanceof String s)) {
         return key.hashCode();
      } else {
         int hash = 0;

         for (int i = 0; i < s.length(); i++) {
            hash = 31 * hash + Character.toLowerCase(s.charAt(i));
         }

         return hash;
      }
   }

   public boolean equals(K a, K b) {
      if (a == b) {
         return true;
      } else if (a != null && b != null) {
         return a instanceof String sa && b instanceof String sb ? sa.equalsIgnoreCase(sb) : a.equals(b);
      } else {
         return false;
      }
   }
}
