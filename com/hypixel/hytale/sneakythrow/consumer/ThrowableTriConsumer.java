package com.hypixel.hytale.sneakythrow.consumer;

import com.hypixel.hytale.function.consumer.TriConsumer;
import com.hypixel.hytale.sneakythrow.SneakyThrow;

@FunctionalInterface
public interface ThrowableTriConsumer<T, U, V, E extends Throwable> extends TriConsumer<T, U, V> {
   @Override
   default void accept(T t, U u, V v) {
      try {
         this.acceptNow(t, u, v);
      } catch (Throwable var5) {
         throw SneakyThrow.sneakyThrow(var5);
      }
   }

   void acceptNow(T var1, U var2, V var3) throws E;
}
