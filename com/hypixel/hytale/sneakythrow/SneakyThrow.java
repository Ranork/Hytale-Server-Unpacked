package com.hypixel.hytale.sneakythrow;

import com.hypixel.hytale.function.consumer.TriConsumer;
import com.hypixel.hytale.sneakythrow.consumer.ThrowableBiConsumer;
import com.hypixel.hytale.sneakythrow.consumer.ThrowableConsumer;
import com.hypixel.hytale.sneakythrow.consumer.ThrowableIntConsumer;
import com.hypixel.hytale.sneakythrow.consumer.ThrowableTriConsumer;
import com.hypixel.hytale.sneakythrow.function.ThrowableBiFunction;
import com.hypixel.hytale.sneakythrow.function.ThrowableFunction;
import com.hypixel.hytale.sneakythrow.supplier.ThrowableIntSupplier;
import com.hypixel.hytale.sneakythrow.supplier.ThrowableSupplier;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import javax.annotation.Nonnull;

public class SneakyThrow {
   @Nonnull
   public static RuntimeException sneakyThrow(@Nonnull Throwable t) {
      if (t == null) {
         throw new NullPointerException("t");
      } else {
         return sneakyThrow0(t);
      }
   }

   private static <T extends Throwable> T sneakyThrow0(Throwable t) throws T {
      throw t;
   }

   public static <E extends Throwable> Runnable sneakyRunnable(ThrowableRunnable<E> runnable) {
      return runnable;
   }

   public static <T, E extends Throwable> Supplier<T> sneakySupplier(ThrowableSupplier<T, E> supplier) {
      return supplier;
   }

   public static <E extends Throwable> IntSupplier sneakySupplier(ThrowableIntSupplier<E> supplier) {
      return supplier;
   }

   public static <T, E extends Throwable> Consumer<T> sneakyConsumer(ThrowableConsumer<T, E> consumer) {
      return consumer;
   }

   public static <T, U, E extends Throwable> BiConsumer<T, U> sneakyConsumer(ThrowableBiConsumer<T, U, E> consumer) {
      return consumer;
   }

   public static <T, U, V, E extends Throwable> TriConsumer<T, U, V> sneakyConsumer(ThrowableTriConsumer<T, U, V, E> consumer) {
      return consumer;
   }

   public static <E extends Throwable> IntConsumer sneakyIntConsumer(ThrowableIntConsumer<E> consumer) {
      return consumer;
   }

   public static <T, R, E extends Throwable> Function<T, R> sneakyFunction(ThrowableFunction<T, R, E> function) {
      return function;
   }

   public static <T, U, R, E extends Throwable> BiFunction<T, U, R> sneakyFunction(ThrowableBiFunction<T, U, R, E> function) {
      return function;
   }
}
