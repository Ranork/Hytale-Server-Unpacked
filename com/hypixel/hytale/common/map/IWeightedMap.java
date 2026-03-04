package com.hypixel.hytale.common.map;

import com.hypixel.hytale.function.function.BiDoubleToDoubleFunction;
import com.hypixel.hytale.function.function.BiIntToDoubleFunction;
import com.hypixel.hytale.function.function.BiLongToDoubleFunction;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.DoubleSupplier;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.ObjDoubleConsumer;
import javax.annotation.Nullable;

public interface IWeightedMap<T> {
   @Nullable
   T get(double var1);

   @Nullable
   T get(DoubleSupplier var1);

   @Nullable
   T get(Random var1);

   @Nullable
   T get(int var1, int var2, BiIntToDoubleFunction var3);

   @Nullable
   T get(long var1, long var3, BiLongToDoubleFunction var5);

   @Nullable
   T get(double var1, double var3, BiDoubleToDoubleFunction var5);

   @Nullable
   <K> T get(int var1, int var2, int var3, IWeightedMap.SeedCoordinateFunction<K> var4, K var5);

   int size();

   boolean contains(T var1);

   void forEach(Consumer<T> var1);

   void forEachEntry(ObjDoubleConsumer<T> var1);

   T[] internalKeys();

   T[] toArray();

   <K> IWeightedMap<K> resolveKeys(Function<T, K> var1, IntFunction<K[]> var2);

   @FunctionalInterface
   public interface SeedCoordinateFunction<T> {
      double apply(int var1, int var2, int var3, T var4);
   }
}
