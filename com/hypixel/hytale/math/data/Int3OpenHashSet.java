package com.hypixel.hytale.math.data;

import com.hypixel.hytale.function.consumer.TriIntConsumer;
import com.hypixel.hytale.function.predicate.Int3TriIntBiObjPredicate;
import com.hypixel.hytale.function.predicate.TriIntPredicate;
import java.util.Arrays;
import javax.annotation.Nonnull;

public class Int3OpenHashSet {
   private static final int DEFAULT_CAPACITY = 16;
   private static final float DEFAULT_LOAD_FACTOR = 0.75F;
   private static final long PHI = -7046029254386353131L;
   private int[] keys;
   private boolean[] used;
   private int mask;
   private int shift;
   private int size;
   private int maxFill;
   private final float loadFactor;

   public Int3OpenHashSet() {
      this(16);
   }

   public Int3OpenHashSet(int expectedSize) {
      this(expectedSize, 0.75F);
   }

   public Int3OpenHashSet(int expectedSize, float loadFactor) {
      this.loadFactor = loadFactor;
      int capacity = arraySize(expectedSize, loadFactor);
      this.keys = new int[capacity * 3];
      this.used = new boolean[capacity];
      this.mask = capacity - 1;
      this.shift = 64 - Integer.numberOfTrailingZeros(capacity);
      this.maxFill = (int)(capacity * loadFactor);
   }

   private static long mix(int x, int y, int z) {
      long h = x * 7640891576956012808L ^ y * -4942790177534073029L ^ z * 4354685564936845355L;
      h ^= h >>> 33;
      h *= -49064778989728563L;
      h ^= h >>> 33;
      h *= -4265267296055464877L;
      return h ^ h >>> 33;
   }

   private int fibIndex(long hash) {
      return (int)(hash * -7046029254386353131L >>> this.shift);
   }

   public boolean add(int x, int y, int z) {
      int[] k = this.keys;
      boolean[] u = this.used;

      int pos;
      for (pos = this.fibIndex(mix(x, y, z)); u[pos]; pos = pos + 1 & this.mask) {
         int base = pos * 3;
         if (k[base] == x && k[base + 1] == y && k[base + 2] == z) {
            return false;
         }
      }

      int base = pos * 3;
      k[base] = x;
      k[base + 1] = y;
      k[base + 2] = z;
      u[pos] = true;
      if (++this.size > this.maxFill) {
         this.rehash(this.used.length << 1);
      }

      return true;
   }

   public boolean remove(int x, int y, int z) {
      int[] k = this.keys;
      boolean[] u = this.used;

      for (int pos = this.fibIndex(mix(x, y, z)); u[pos]; pos = pos + 1 & this.mask) {
         int base = pos * 3;
         if (k[base] == x && k[base + 1] == y && k[base + 2] == z) {
            this.size--;
            this.shiftKeys(pos);
            int capacity = this.used.length;
            if (capacity > 16 && this.size < this.maxFill >>> 2) {
               this.rehash(capacity >>> 1);
            }

            return true;
         }
      }

      return false;
   }

   public boolean contains(int x, int y, int z) {
      int[] k = this.keys;
      boolean[] u = this.used;

      for (int pos = this.fibIndex(mix(x, y, z)); u[pos]; pos = pos + 1 & this.mask) {
         int base = pos * 3;
         if (k[base] == x && k[base + 1] == y && k[base + 2] == z) {
            return true;
         }
      }

      return false;
   }

   public int size() {
      return this.size;
   }

   public boolean isEmpty() {
      return this.size == 0;
   }

   public void clear() {
      this.size = 0;
      Arrays.fill(this.used, false);
   }

   public void forEach(@Nonnull TriIntConsumer consumer) {
      int[] k = this.keys;
      boolean[] u = this.used;

      for (int i = 0; i < u.length; i++) {
         if (u[i]) {
            int base = i * 3;
            consumer.accept(k[base], k[base + 1], k[base + 2]);
         }
      }
   }

   public void removeIf(@Nonnull TriIntPredicate predicate) {
      int[] k = this.keys;
      boolean[] u = this.used;
      int i = 0;

      while (i < u.length) {
         if (u[i]) {
            int base = i * 3;
            if (predicate.test(k[base], k[base + 1], k[base + 2])) {
               this.size--;
               this.shiftKeys(i);
               continue;
            }
         }

         i++;
      }

      this.maybeShrink();
   }

   public <T, V> void removeIf(@Nonnull Int3TriIntBiObjPredicate<T, V> predicate, int a, int b, int c, T obj1, V obj2) {
      int[] k = this.keys;
      boolean[] u = this.used;
      int i = 0;

      while (i < u.length) {
         if (u[i]) {
            int base = i * 3;
            if (predicate.test(k[base], k[base + 1], k[base + 2], a, b, c, obj1, obj2)) {
               this.size--;
               this.shiftKeys(i);
               continue;
            }
         }

         i++;
      }

      this.maybeShrink();
   }

   private void maybeShrink() {
      int capacity = this.used.length;
      if (capacity > 16 && this.size < this.maxFill >>> 2) {
         this.rehash(capacity >>> 1);
      }
   }

   private void shiftKeys(int pos) {
      int[] k = this.keys;
      boolean[] u = this.used;

      label30:
      while (true) {
         int last = pos;

         for (pos = pos + 1 & this.mask; u[pos]; pos = pos + 1 & this.mask) {
            int base = pos * 3;
            int slot = this.fibIndex(mix(k[base], k[base + 1], k[base + 2]));
            if (last <= pos ? last >= slot || slot > pos : last >= slot && slot > pos) {
               base = pos * 3;
               slot = last * 3;
               k[slot] = k[base];
               k[slot + 1] = k[base + 1];
               k[slot + 2] = k[base + 2];
               u[last] = true;
               continue label30;
            }
         }

         u[last] = false;
         return;
      }
   }

   private void rehash(int newCapacity) {
      int[] oldKeys = this.keys;
      boolean[] oldUsed = this.used;
      this.keys = new int[newCapacity * 3];
      this.used = new boolean[newCapacity];
      this.mask = newCapacity - 1;
      this.shift = 64 - Integer.numberOfTrailingZeros(newCapacity);
      this.maxFill = (int)(newCapacity * this.loadFactor);
      int[] k = this.keys;
      boolean[] u = this.used;

      for (int i = 0; i < oldUsed.length; i++) {
         if (oldUsed[i]) {
            int srcBase = i * 3;
            int ox = oldKeys[srcBase];
            int oy = oldKeys[srcBase + 1];
            int oz = oldKeys[srcBase + 2];
            int pos = this.fibIndex(mix(ox, oy, oz));

            while (u[pos]) {
               pos = pos + 1 & this.mask;
            }

            int dstBase = pos * 3;
            k[dstBase] = ox;
            k[dstBase + 1] = oy;
            k[dstBase + 2] = oz;
            u[pos] = true;
         }
      }
   }

   private static int arraySize(int expected, float loadFactor) {
      if (expected <= 0) {
         return 16;
      } else {
         int needed = (int)Math.ceil(expected / loadFactor);
         int capacity = Integer.highestOneBit(needed - 1) << 1;
         return Math.max(capacity, 16);
      }
   }
}
