package com.hypixel.hytale.common.map;

import com.hypixel.hytale.common.util.ArrayUtil;
import com.hypixel.hytale.function.function.BiDoubleToDoubleFunction;
import com.hypixel.hytale.function.function.BiIntToDoubleFunction;
import com.hypixel.hytale.function.function.BiLongToDoubleFunction;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.DoubleSupplier;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.ObjDoubleConsumer;
import java.util.function.ToDoubleFunction;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class WeightedMap<T> implements IWeightedMap<T> {
   public static final double EPSILON = 0.99999;
   public static final double ONE_MINUS_EPSILON = 9.99999999995449E-6;
   @Nonnull
   private final Set<T> keySet = new HashSet<>();
   @Nonnull
   private final T[] keys;
   private final double[] values;
   private final double sum;

   @Nonnull
   public static <T> WeightedMap.Builder<T> builder(T[] emptyKeys) {
      return new WeightedMap.Builder<>(emptyKeys);
   }

   private WeightedMap(@Nonnull T[] keys, double[] values, double sum) {
      Collections.addAll(this.keySet, keys);
      this.keys = keys;
      this.values = values;
      this.sum = sum;
   }

   @Nullable
   @Override
   public T get(double value) {
      double weightPercentSum = Math.min(value, 0.99999) * this.sum;

      for (int i = 0; i < this.keys.length; i++) {
         if ((weightPercentSum -= this.values[i]) <= 9.99999999995449E-6) {
            return this.keys[i];
         }
      }

      return null;
   }

   @Nullable
   @Override
   public T get(@Nonnull DoubleSupplier supplier) {
      return this.get(supplier.getAsDouble());
   }

   @Nullable
   @Override
   public T get(@Nonnull Random random) {
      return this.get(random.nextDouble());
   }

   @Nullable
   @Override
   public T get(int x, int z, @Nonnull BiIntToDoubleFunction supplier) {
      return this.get(supplier.apply(x, z));
   }

   @Nullable
   @Override
   public T get(long x, long z, @Nonnull BiLongToDoubleFunction supplier) {
      return this.get(supplier.apply(x, z));
   }

   @Nullable
   @Override
   public T get(double x, double z, @Nonnull BiDoubleToDoubleFunction supplier) {
      return this.get(supplier.apply(x, z));
   }

   @Nullable
   @Override
   public <K> T get(int seed, int x, int z, @Nonnull IWeightedMap.SeedCoordinateFunction<K> supplier, K k) {
      return this.get(supplier.apply(seed, x, z, k));
   }

   @Override
   public int size() {
      return this.keys.length;
   }

   @Override
   public boolean contains(T obj) {
      return this.keySet.contains(obj);
   }

   @Override
   public void forEach(@Nonnull Consumer<T> consumer) {
      for (T o : this.keys) {
         consumer.accept(o);
      }
   }

   @Override
   public void forEachEntry(@Nonnull ObjDoubleConsumer<T> consumer) {
      for (int i = 0; i < this.keys.length; i++) {
         consumer.accept(this.keys[i], this.values[i]);
      }
   }

   @Nonnull
   @Override
   public T[] internalKeys() {
      return this.keys;
   }

   @Nonnull
   @Override
   public T[] toArray() {
      return (T[])Arrays.copyOf(this.keys, this.keys.length);
   }

   @Nonnull
   @Override
   public <K> IWeightedMap<K> resolveKeys(@Nonnull Function<T, K> mapper, @Nonnull IntFunction<K[]> arraySupplier) {
      K[] array = (K[])arraySupplier.apply(this.keys.length);

      for (int i = 0; i < this.keys.length; i++) {
         array[i] = mapper.apply(this.keys[i]);
      }

      return new WeightedMap<>(array, this.values, this.sum);
   }

   @Nonnull
   @Override
   public String toString() {
      return "WeightedMap{keySet="
         + this.keySet
         + ", sum="
         + this.sum
         + ", keys="
         + Arrays.toString(this.keys)
         + ", values="
         + Arrays.toString(this.values)
         + "}";
   }

   public static class Builder<T> {
      private final T[] emptyKeys;
      private T[] keys;
      private double[] values;
      private int size;

      private Builder(T[] emptyKeys) {
         this.emptyKeys = emptyKeys;
         this.keys = emptyKeys;
         this.values = ArrayUtil.EMPTY_DOUBLE_ARRAY;
      }

      @Nonnull
      public WeightedMap.Builder<T> putAll(@Nullable IWeightedMap<T> map) {
         if (map != null) {
            this.ensureCapacity(map.size());
            map.forEachEntry(this::insert);
         }

         return this;
      }

      @Nonnull
      public WeightedMap.Builder<T> putAll(@Nullable T[] arr, @Nonnull ToDoubleFunction<T> weight) {
         if (arr != null && arr.length != 0) {
            this.ensureCapacity(arr.length);

            for (T t : arr) {
               this.insert(t, weight.applyAsDouble(t));
            }

            return this;
         } else {
            return this;
         }
      }

      @Nonnull
      public WeightedMap.Builder<T> put(T obj, double weight) {
         this.ensureCapacity(1);
         this.insert(obj, weight);
         return this;
      }

      public void ensureCapacity(int toAdd) {
         int minCapacity = this.size + toAdd;
         int allocated = this.allocated();
         if (minCapacity > allocated) {
            int newLength = Math.max(allocated + (allocated >> 1), minCapacity);
            this.resize(newLength);
         }
      }

      private void resize(int newLength) {
         this.keys = (T[])Arrays.copyOf(this.keys, newLength);
         this.values = Arrays.copyOf(this.values, newLength);
      }

      private void insert(T key, double value) {
         this.keys[this.size] = key;
         this.values[this.size] = value;
         this.size++;
      }

      public int size() {
         return this.size;
      }

      private int allocated() {
         return this.keys.length;
      }

      public void clear() {
         this.keys = this.emptyKeys;
         this.values = ArrayUtil.EMPTY_DOUBLE_ARRAY;
         this.size = 0;
      }

      @Nonnull
      public IWeightedMap<T> build() {
         if (this.size < this.allocated()) {
            this.resize(this.size);
         }

         if (this.keys.length != 0 && this.keys.length != 1) {
            double sum = 0.0;

            for (double value : this.values) {
               sum += value;
            }

            return new WeightedMap<>(this.keys, this.values, sum);
         } else {
            return new WeightedMap.SingletonWeightedMap<>(this.keys);
         }
      }
   }

   private static class SingletonWeightedMap<T> implements IWeightedMap<T> {
      @Nonnull
      protected final T[] keys;
      protected final T key;

      private SingletonWeightedMap(@Nonnull T[] keys) {
         this.keys = keys;
         this.key = keys.length > 0 ? keys[0] : null;
      }

      @Override
      public T get(double value) {
         return this.key;
      }

      @Override
      public T get(DoubleSupplier supplier) {
         return this.key;
      }

      @Override
      public T get(Random random) {
         return this.key;
      }

      @Override
      public T get(int x, int z, BiIntToDoubleFunction supplier) {
         return this.key;
      }

      @Override
      public T get(long x, long z, BiLongToDoubleFunction supplier) {
         return this.key;
      }

      @Override
      public T get(double x, double z, BiDoubleToDoubleFunction supplier) {
         return this.key;
      }

      @Override
      public <K> T get(int seed, int x, int z, IWeightedMap.SeedCoordinateFunction<K> supplier, K k) {
         return this.key;
      }

      @Override
      public int size() {
         return this.keys.length;
      }

      @Override
      public boolean contains(@Nullable T obj) {
         return obj != null && (obj == this.key || obj.equals(this.key));
      }

      @Override
      public void forEach(@Nonnull Consumer<T> consumer) {
         if (this.key != null) {
            consumer.accept(this.key);
         }
      }

      @Override
      public void forEachEntry(@Nonnull ObjDoubleConsumer<T> consumer) {
         if (this.key != null) {
            consumer.accept(this.key, 1.0);
         }
      }

      @Nonnull
      @Override
      public T[] internalKeys() {
         return this.keys;
      }

      @Nonnull
      @Override
      public T[] toArray() {
         return (T[])Arrays.copyOf(this.keys, this.keys.length);
      }

      @Nonnull
      @Override
      public <K> IWeightedMap<K> resolveKeys(@Nonnull Function<T, K> mapper, @Nonnull IntFunction<K[]> arraySupplier) {
         K[] array = (K[])arraySupplier.apply(1);
         array[0] = mapper.apply(this.key);
         return new WeightedMap.SingletonWeightedMap<>(array);
      }

      @Nonnull
      @Override
      public String toString() {
         return "SingletonWeightedMap{key=" + this.key + "}";
      }
   }
}
