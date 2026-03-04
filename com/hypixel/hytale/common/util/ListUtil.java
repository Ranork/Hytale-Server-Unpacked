package com.hypixel.hytale.common.util;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import javax.annotation.Nonnull;

public class ListUtil {
   @Nonnull
   public static <T> List<List<T>> partition(@Nonnull List<T> list, int sectionSize) {
      List<List<T>> sections = new ObjectArrayList();
      int i = 0;

      while (i < list.size()) {
         int endIndex = Math.min(list.size(), i + sectionSize);
         sections.add(list.subList(i, endIndex));
         i += sectionSize;
      }

      return sections;
   }

   public static <T> void removeIf(@Nonnull List<T> list, @Nonnull Predicate<T> predicate) {
      for (int i = list.size() - 1; i >= 0; i--) {
         if (predicate.test(list.get(i))) {
            list.remove(i);
         }
      }
   }

   public static <T, U> void removeIf(@Nonnull List<T> list, @Nonnull BiPredicate<T, U> predicate, U obj) {
      for (int i = list.size() - 1; i >= 0; i--) {
         if (predicate.test(list.get(i), obj)) {
            list.remove(i);
         }
      }
   }

   public static <T> boolean emptyOrAllNull(@Nonnull List<T> list) {
      for (int i = 0; i < list.size(); i++) {
         T e = list.get(i);
         if (e != null) {
            return false;
         }
      }

      return true;
   }

   public static <T, V> int binarySearch(@Nonnull List<? extends T> l, @Nonnull Function<T, V> func, V key, @Nonnull Comparator<? super V> c) {
      int low = 0;
      int high = l.size() - 1;

      while (low <= high) {
         int mid = low + high >>> 1;
         T midVal = (T)l.get(mid);
         int cmp = c.compare(func.apply(midVal), key);
         if (cmp < 0) {
            low = mid + 1;
         } else {
            if (cmp <= 0) {
               return mid;
            }

            high = mid - 1;
         }
      }

      return -(low + 1);
   }
}
