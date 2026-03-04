package com.hypixel.hytale.function.function;

@FunctionalInterface
public interface TriToIntFunction<T, U, V> {
   int apply(T var1, U var2, V var3);
}
