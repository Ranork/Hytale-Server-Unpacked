package com.hypixel.hytale.function.consumer;

@FunctionalInterface
public interface QuadConsumer<T, U, R, V> {
   void accept(T var1, U var2, R var3, V var4);
}
