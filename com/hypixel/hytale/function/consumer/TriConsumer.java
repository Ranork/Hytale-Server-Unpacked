package com.hypixel.hytale.function.consumer;

@FunctionalInterface
public interface TriConsumer<T, U, R> {
   void accept(T var1, U var2, R var3);
}
