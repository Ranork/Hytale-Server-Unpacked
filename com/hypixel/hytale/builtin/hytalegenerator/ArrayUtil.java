package com.hypixel.hytale.builtin.hytalegenerator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import javax.annotation.Nonnull;

public class ArrayUtil {
   @Nonnull
   public static <T> T[] brokenCopyOf(@Nonnull T[] a) {
      T[] copy = (T[])(new Object[a.length]);
      System.arraycopy(a, 0, copy, 0, a.length);
      return copy;
   }

   public static <T> void copy(@Nonnull T[] source, @Nonnull T[] destination) {
      if (source.length != destination.length) {
         throw new IllegalArgumentException("arrays must have the same size");
      } else {
         System.arraycopy(source, 0, destination, 0, source.length);
      }
   }

   @Nonnull
   public static <T> T[] append(@Nonnull T[] a, T e) {
      T[] expanded = (T[])(new Object[a.length + 1]);
      System.arraycopy(a, 0, expanded, 0, a.length);
      expanded[a.length] = e;
      return expanded;
   }

   @Nonnull
   public static <T> List<List<T>> split(@Nonnull List<T> list, int partCount) {
      if (partCount < 1) {
         throw new IllegalArgumentException("parts must be greater than 0");
      } else if (partCount == 1) {
         return Collections.singletonList(list);
      } else {
         List<List<T>> out = new ArrayList<>(partCount);
         int listSize = list.size();
         if (listSize <= partCount) {
            for (int i = 0; i < listSize; i++) {
               out.add(List.of(list.get(i)));
            }

            for (int i = listSize; i < partCount; i++) {
               out.add(List.of());
            }

            return out;
         } else {
            int[] partSizes = getPartSizes(listSize, partCount);
            int elementIndex = 0;

            for (int partIndex = 0; partIndex < partCount; partIndex++) {
               int partSize = partSizes[partIndex];
               List<T> partList = new ArrayList<>(partSize);

               for (int i = 0; i < partSize; i++) {
                  partList.add(list.get(elementIndex++));
               }

               out.add(partList);
            }

            return out;
         }
      }
   }

   public static int[] getPartSizes(int total, int partCount) {
      if (total >= 0 && partCount >= 1) {
         if (total == 0) {
            return new int[]{total};
         } else {
            int[] sizes = new int[partCount];
            int baseSize = total / partCount;
            int remainder = total % partCount;

            for (int i = 0; i < partCount; i++) {
               if (i < remainder) {
                  sizes[i] = baseSize + 1;
               } else {
                  sizes[i] = baseSize;
               }
            }

            return sizes;
         }
      } else {
         throw new IllegalArgumentException("total and/or parts must be greater than 0");
      }
   }

   public static <T, G> int sortedSearch(@Nonnull List<T> sortedList, @Nonnull G gauge, @Nonnull BiFunction<G, T, Integer> comparator) {
      int BINARY_SIZE_THRESHOLD = 250;
      if (sortedList.isEmpty()) {
         return -1;
      } else if (sortedList.size() == 1) {
         return comparator.apply(gauge, sortedList.getFirst()) == 0 ? 0 : -1;
      } else if (sortedList.size() <= 250) {
         for (int i = 0; i < sortedList.size(); i++) {
            if (comparator.apply(gauge, sortedList.get(i)) == 0) {
               return i;
            }
         }

         return -1;
      } else {
         return binarySearch(sortedList, gauge, comparator);
      }
   }

   public static <T, G> int binarySearch(@Nonnull List<T> sortedList, @Nonnull G gauge, @Nonnull BiFunction<G, T, Integer> comparator) {
      if (sortedList.isEmpty()) {
         return -1;
      } else {
         int min = 0;
         int max = sortedList.size();

         while (true) {
            int index = (max + min) / 2;
            T item = sortedList.get(index);
            int comparison = comparator.apply(gauge, item);
            if (comparison == 0) {
               return index;
            }

            if (min == max - 1) {
               return -1;
            }

            if (comparison == -1) {
               max = index;
            }

            if (comparison == 1) {
               min = index;
            }
         }
      }
   }
}
