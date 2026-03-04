package com.hypixel.hytale.sneakythrow.function;

import com.hypixel.hytale.sneakythrow.SneakyThrow;
import java.util.function.BiFunction;

@FunctionalInterface
public interface ThrowableBiFunction<T, U, R, E extends Throwable> extends BiFunction<T, U, R> {
   @Override
   default R apply(T t, U u) {
      try {
         return this.applyNow(t, u);
      } catch (Throwable var4) {
         throw SneakyThrow.sneakyThrow(var4);
      }
   }

   R applyNow(T var1, U var2) throws E;
}
