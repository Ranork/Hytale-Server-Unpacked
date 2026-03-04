package com.hypixel.hytale.sneakythrow.consumer;

import com.hypixel.hytale.sneakythrow.SneakyThrow;
import java.util.function.Consumer;

@FunctionalInterface
public interface ThrowableConsumer<T, E extends Throwable> extends Consumer<T> {
   @Override
   default void accept(T t) {
      try {
         this.acceptNow(t);
      } catch (Throwable var3) {
         throw SneakyThrow.sneakyThrow(var3);
      }
   }

   void acceptNow(T var1) throws E;
}
