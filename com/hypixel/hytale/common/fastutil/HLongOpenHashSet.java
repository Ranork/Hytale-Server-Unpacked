package com.hypixel.hytale.common.fastutil;

import com.hypixel.hytale.function.predicate.LongTriIntBiObjPredicate;
import it.unimi.dsi.fastutil.HashCommon;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import javax.annotation.Nonnull;

public class HLongOpenHashSet extends LongOpenHashSet implements HLongSet {
   @Override
   public <T, V> void removeIf(@Nonnull LongTriIntBiObjPredicate<T, V> predicate, int ia, int ib, int ic, T obj1, V obj2) {
      int pos = super.n;
      int last = -1;
      int c = super.size;
      boolean mustReturnNull = super.containsNull;
      LongArrayList wrapped = null;

      while (c != 0) {
         long value = 0L;
         c--;
         if (mustReturnNull) {
            mustReturnNull = false;
            last = super.n;
            value = super.key[super.n];
         } else {
            long[] key1 = super.key;

            while (--pos >= 0) {
               if (key1[pos] != 0L) {
                  last = pos;
                  value = key1[pos];
                  break;
               }
            }

            if (pos < 0) {
               last = Integer.MIN_VALUE;
               value = wrapped.getLong(-pos - 1);
            }
         }

         if (predicate.test(value, ia, ib, ic, obj1, obj2)) {
            if (last == super.n) {
               super.containsNull = false;
               super.key[super.n] = 0L;
               super.size--;
               last = -1;
            } else if (pos < 0) {
               super.remove(wrapped.getLong(-pos - 1));
               last = -1;
            } else {
               int pos1 = last;
               long[] key1 = super.key;

               label80:
               while (true) {
                  int last1 = pos1;

                  long curr;
                  for (pos1 = pos1 + 1 & super.mask; (curr = key1[pos1]) != 0L; pos1 = pos1 + 1 & super.mask) {
                     int slot = (int)HashCommon.mix(curr) & super.mask;
                     if (last1 <= pos1 ? last1 >= slot || slot > pos1 : last1 >= slot && slot > pos1) {
                        if (pos1 < last1) {
                           if (wrapped == null) {
                              wrapped = new LongArrayList(2);
                           }

                           wrapped.add(key1[pos1]);
                        }

                        key1[last1] = curr;
                        continue label80;
                     }
                  }

                  key1[last1] = 0L;
                  super.size--;
                  last = -1;
                  break;
               }
            }
         }
      }
   }
}
