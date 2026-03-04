package com.hypixel.hytale.function.consumer;

@FunctionalInterface
public interface IntBiObjectConsumer<T, J> {
   void accept(int var1, T var2, J var3);
}
