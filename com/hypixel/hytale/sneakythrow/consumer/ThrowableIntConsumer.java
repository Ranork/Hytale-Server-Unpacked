package com.hypixel.hytale.sneakythrow.consumer;

import com.hypixel.hytale.sneakythrow.SneakyThrow;
import java.util.function.IntConsumer;

@FunctionalInterface
public interface ThrowableIntConsumer<E extends Throwable> extends IntConsumer {
   @Override
   default void accept(int t) {
      try {
         this.acceptNow(t);
      } catch (Throwable var3) {
         throw SneakyThrow.sneakyThrow(var3);
      }
   }

   void acceptNow(int var1) throws E;
}
