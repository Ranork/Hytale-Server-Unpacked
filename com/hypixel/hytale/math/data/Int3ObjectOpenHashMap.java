package com.hypixel.hytale.math.data;

import com.hypixel.hytale.function.consumer.TriIntObjectConsumer;
import java.util.Arrays;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Int3ObjectOpenHashMap<V> {
   private static final int DEFAULT_CAPACITY = 16;
   private static final float DEFAULT_LOAD_FACTOR = 0.75F;
   private static final long PHI = -7046029254386353131L;
   private int[] keys;
   private Object[] values;
   private int mask;
   private int shift;
   private int size;
   private int maxFill;
   private final float loadFactor;

   public Int3ObjectOpenHashMap() {
      this(16);
   }

   public Int3ObjectOpenHashMap(int expectedSize) {
      this(expectedSize, 0.75F);
   }

   public Int3ObjectOpenHashMap(int expectedSize, float loadFactor) {
      this.loadFactor = loadFactor;
      int capacity = arraySize(expectedSize, loadFactor);
      this.keys = new int[capacity * 3];
      this.values = new Object[capacity];
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

   @Nullable
   public V get(int x, int y, int z) {
      int[] k = this.keys;
      int pos = this.fibIndex(mix(x, y, z));

      while (true) {
         Object v = this.values[pos];
         if (v == null) {
            return null;
         }

         int base = pos * 3;
         if (k[base] == x && k[base + 1] == y && k[base + 2] == z) {
            return (V)v;
         }

         pos = pos + 1 & this.mask;
      }
   }

   @Nullable
   public V put(int x, int y, int z, @Nonnull V value) {
      Objects.requireNonNull(value, "value");
      int[] k = this.keys;
      int pos = this.fibIndex(mix(x, y, z));

      while (true) {
         Object existing = this.values[pos];
         if (existing == null) {
            int base = pos * 3;
            k[base] = x;
            k[base + 1] = y;
            k[base + 2] = z;
            this.values[pos] = value;
            if (++this.size > this.maxFill) {
               this.rehash(this.values.length << 1);
            }

            return null;
         }

         int base = pos * 3;
         if (k[base] == x && k[base + 1] == y && k[base + 2] == z) {
            this.values[pos] = value;
            return (V)existing;
         }

         pos = pos + 1 & this.mask;
      }
   }

   @Nullable
   public V remove(int x, int y, int z) {
      int[] k = this.keys;
      int pos = this.fibIndex(mix(x, y, z));

      while (true) {
         Object existing = this.values[pos];
         if (existing == null) {
            return null;
         }

         int base = pos * 3;
         if (k[base] == x && k[base + 1] == y && k[base + 2] == z) {
            this.size--;
            this.shiftKeys(pos);
            int capacity = this.values.length;
            if (capacity > 16 && this.size < this.maxFill >>> 2) {
               this.rehash(capacity >>> 1);
            }

            return (V)existing;
         }

         pos = pos + 1 & this.mask;
      }
   }

   public boolean containsKey(int x, int y, int z) {
      int[] k = this.keys;

      for (int pos = this.fibIndex(mix(x, y, z)); this.values[pos] != null; pos = pos + 1 & this.mask) {
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
      Arrays.fill(this.values, null);
   }

   public void forEach(@Nonnull TriIntObjectConsumer<? super V> consumer) {
      int[] k = this.keys;

      for (int i = 0; i < this.values.length; i++) {
         Object v = this.values[i];
         if (v != null) {
            int base = i * 3;
            consumer.accept(k[base], k[base + 1], k[base + 2], (V)v);
         }
      }
   }

   private void shiftKeys(int pos) {
      int[] k = this.keys;

      label30:
      while (true) {
         int last = pos;

         for (pos = pos + 1 & this.mask; this.values[pos] != null; pos = pos + 1 & this.mask) {
            int base = pos * 3;
            int slot = this.fibIndex(mix(k[base], k[base + 1], k[base + 2]));
            if (last <= pos ? last >= slot || slot > pos : last >= slot && slot > pos) {
               base = pos * 3;
               slot = last * 3;
               k[slot] = k[base];
               k[slot + 1] = k[base + 1];
               k[slot + 2] = k[base + 2];
               this.values[last] = this.values[pos];
               continue label30;
            }
         }

         this.values[last] = null;
         return;
      }
   }

   private void rehash(int newCapacity) {
      int[] oldKeys = this.keys;
      Object[] oldValues = this.values;
      this.keys = new int[newCapacity * 3];
      this.values = new Object[newCapacity];
      this.mask = newCapacity - 1;
      this.shift = 64 - Integer.numberOfTrailingZeros(newCapacity);
      this.maxFill = (int)(newCapacity * this.loadFactor);
      int[] k = this.keys;

      for (int i = 0; i < oldValues.length; i++) {
         if (oldValues[i] != null) {
            int srcBase = i * 3;
            int ox = oldKeys[srcBase];
            int oy = oldKeys[srcBase + 1];
            int oz = oldKeys[srcBase + 2];
            int pos = this.fibIndex(mix(ox, oy, oz));

            while (this.values[pos] != null) {
               pos = pos + 1 & this.mask;
            }

            int dstBase = pos * 3;
            k[dstBase] = ox;
            k[dstBase + 1] = oy;
            k[dstBase + 2] = oz;
            this.values[pos] = oldValues[i];
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
