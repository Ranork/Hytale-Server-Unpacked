package com.hypixel.hytale.common.util;

import com.hypixel.hytale.function.predicate.UnaryBiPredicate;
import com.hypixel.hytale.math.util.MathUtil;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Objects;
import java.util.Random;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ArrayUtil {
   public static final String[] EMPTY_STRING_ARRAY = new String[0];
   public static final double[] EMPTY_DOUBLE_ARRAY = new double[0];
   public static final int[] EMPTY_INT_ARRAY = new int[0];
   public static final long[] EMPTY_LONG_ARRAY = new long[0];
   public static final boolean[] EMPTY_BOOLEAN_ARRAY = new boolean[0];
   public static final Integer[] EMPTY_INTEGER_ARRAY = new Integer[0];
   public static final byte[] EMPTY_BYTE_ARRAY = new byte[0];
   public static final BitSet[] EMPTY_BITSET_ARRAY = new BitSet[0];
   public static final float[] EMPTY_FLOAT_ARRAY = new float[0];
   private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];
   private static final Supplier[] EMPTY_SUPPLIER_ARRAY = new Supplier[0];
   private static final Entry[] EMPTY_ENTRY_ARRAY = new Entry[0];

   @Nonnull
   public static <T> T[] emptyArray() {
      return (T[])EMPTY_OBJECT_ARRAY;
   }

   @Nonnull
   public static <T> Supplier<T>[] emptySupplierArray() {
      return EMPTY_SUPPLIER_ARRAY;
   }

   @Nonnull
   public static <K, V> Entry<K, V>[] emptyEntryArray() {
      return EMPTY_ENTRY_ARRAY;
   }

   public static int grow(int oldSize) {
      return (int)MathUtil.clamp((long)oldSize + (oldSize >> 1), 2L, 2147483639L);
   }

   public static <StartType, EndType> EndType[] copyAndMutate(
      @Nullable StartType[] array, @Nonnull Function<StartType, EndType> adapter, @Nonnull IntFunction<EndType[]> arrayProvider
   ) {
      if (array == null) {
         return null;
      } else {
         EndType[] endArray = (EndType[])arrayProvider.apply(array.length);

         for (int i = 0; i < endArray.length; i++) {
            endArray[i] = adapter.apply(array[i]);
         }

         return endArray;
      }
   }

   @Nullable
   public static <T> T[] combine(@Nullable T[] a1, @Nullable T[] a2) {
      if (a1 == null || a1.length == 0) {
         return a2;
      } else if (a2 != null && a2.length != 0) {
         T[] newArray = (T[])Arrays.copyOf(a1, a1.length + a2.length);
         System.arraycopy(a2, 0, newArray, a1.length, a2.length);
         return newArray;
      } else {
         return a1;
      }
   }

   @Nonnull
   public static <T> T[] append(@Nullable T[] arr, @Nonnull T t) {
      if (arr == null) {
         T[] newArray = (T[])Array.newInstance(t.getClass(), 1);
         newArray[0] = t;
         return newArray;
      } else {
         T[] newArray = (T[])Arrays.copyOf(arr, arr.length + 1);
         newArray[arr.length] = t;
         return newArray;
      }
   }

   @Nonnull
   public static <T> T[] remove(@Nonnull T[] arr, int index) {
      int newLength = arr.length - 1;
      T[] newArray = (T[])Array.newInstance(arr.getClass().getComponentType(), newLength);
      System.arraycopy(arr, 0, newArray, 0, index);
      if (index < newLength) {
         System.arraycopy(arr, index + 1, newArray, index, newLength - index);
      }

      return newArray;
   }

   public static boolean startsWith(@Nonnull byte[] array, @Nonnull byte[] start) {
      if (start.length > array.length) {
         return false;
      } else {
         for (int i = 0; i < start.length; i++) {
            if (array[i] != start[i]) {
               return false;
            }
         }

         return true;
      }
   }

   public static <T> boolean equals(@Nullable T[] a, @Nullable T[] a2, @Nonnull UnaryBiPredicate<T> predicate) {
      if (a == a2) {
         return true;
      } else if (a != null && a2 != null) {
         int length = a.length;
         if (a2.length != length) {
            return false;
         } else {
            for (int i = 0; i < length; i++) {
               T o1 = a[i];
               T o2 = a2[i];
               if (o1 == null ? o2 != null : !predicate.test(o1, o2)) {
                  return false;
               }
            }

            return true;
         }
      } else {
         return false;
      }
   }

   @Nonnull
   public static <T> T[][] split(@Nonnull T[] data, int size) {
      Class<? extends T[]> aClass = (Class<? extends T[]>)data.getClass();
      T[][] ret = (T[][])Array.newInstance(aClass.getComponentType(), MathUtil.ceil((double)data.length / size), 0);
      int start = 0;

      for (int i = 0; i < ret.length; i++) {
         ret[i] = (T[])Arrays.copyOfRange(data, start, Math.min(start + size, data.length));
         start += size;
      }

      return ret;
   }

   public static byte[][] split(@Nonnull byte[] data, int size) {
      byte[][] ret = new byte[MathUtil.ceil((double)data.length / size)][];
      int start = 0;

      for (int i = 0; i < ret.length; i++) {
         ret[i] = Arrays.copyOfRange(data, start, Math.min(start + size, data.length));
         start += size;
      }

      return ret;
   }

   public static void shuffleArray(@Nonnull int[] ar, int from, int to, @Nonnull Random rnd) {
      Objects.checkFromToIndex(from, to, ar.length);

      for (int i = to - 1; i > from; i--) {
         int index = rnd.nextInt(i + 1 - from) + from;
         int a = ar[index];
         ar[index] = ar[i];
         ar[i] = a;
      }
   }

   public static void shuffleArray(@Nonnull byte[] ar, int from, int to, @Nonnull Random rnd) {
      Objects.checkFromToIndex(from, to, ar.length);

      for (int i = to - 1; i > from; i--) {
         int index = rnd.nextInt(i + 1 - from) + from;
         byte a = ar[index];
         ar[index] = ar[i];
         ar[i] = a;
      }
   }

   public static <T> boolean contains(@Nonnull T[] array, @Nullable T obj) {
      return indexOf(array, obj) >= 0;
   }

   public static <T> boolean contains(@Nonnull T[] array, @Nullable T obj, int start, int end) {
      return indexOf(array, obj, start, end) >= 0;
   }

   public static <T> int indexOf(@Nonnull T[] array, @Nullable T obj) {
      return indexOf(array, obj, 0, array.length);
   }

   public static <T> int indexOf(@Nonnull T[] array, @Nullable T obj, int start, int end) {
      if (obj == null) {
         for (int i = start; i < end; i++) {
            if (array[i] == null) {
               return i;
            }
         }
      } else {
         for (int ix = start; ix < end; ix++) {
            if (obj.equals(array[ix])) {
               return ix;
            }
         }
      }

      return -1;
   }
}
