package com.hypixel.hytale.function.consumer;

@FunctionalInterface
public interface IntTriObjectConsumer<T, J, K> {
   void accept(int var1, T var2, J var3, K var4);
}
