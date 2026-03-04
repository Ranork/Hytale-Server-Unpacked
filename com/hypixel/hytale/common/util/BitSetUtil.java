package com.hypixel.hytale.common.util;

import com.hypixel.hytale.sneakythrow.SneakyThrow;
import com.hypixel.hytale.unsafe.UnsafeUtil;
import java.util.BitSet;
import javax.annotation.Nonnull;

public class BitSetUtil {
   public static final long WORDS_OFFSET;
   public static final long WORDS_IN_USE_OFFSET;

   public static void copyValues(@Nonnull BitSet from, @Nonnull BitSet to) {
      if (UnsafeUtil.UNSAFE == null) {
         copyValuesSlow(from, to);
      } else {
         int wordsInUse = UnsafeUtil.UNSAFE.getInt(from, WORDS_IN_USE_OFFSET);
         UnsafeUtil.UNSAFE.putInt(to, WORDS_IN_USE_OFFSET, wordsInUse);
         long[] fromWords = (long[])UnsafeUtil.UNSAFE.getObject(from, WORDS_OFFSET);
         long[] toWords = (long[])UnsafeUtil.UNSAFE.getObject(to, WORDS_OFFSET);
         if (wordsInUse > toWords.length) {
            toWords = new long[wordsInUse];
            UnsafeUtil.UNSAFE.putObject(to, WORDS_OFFSET, toWords);
         }

         System.arraycopy(fromWords, 0, toWords, 0, wordsInUse);
      }
   }

   public static void copyValuesSlow(@Nonnull BitSet from, @Nonnull BitSet to) {
      to.clear();
      to.or(from);
   }

   static {
      try {
         if (UnsafeUtil.UNSAFE != null) {
            WORDS_OFFSET = UnsafeUtil.UNSAFE.objectFieldOffset(BitSet.class.getDeclaredField("words"));
            WORDS_IN_USE_OFFSET = UnsafeUtil.UNSAFE.objectFieldOffset(BitSet.class.getDeclaredField("wordsInUse"));
         } else {
            WORDS_OFFSET = 0L;
            WORDS_IN_USE_OFFSET = 0L;
         }
      } catch (NoSuchFieldException var1) {
         throw SneakyThrow.sneakyThrow(var1);
      }
   }
}
