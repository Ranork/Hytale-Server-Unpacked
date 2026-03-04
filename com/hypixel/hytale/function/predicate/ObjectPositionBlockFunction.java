package com.hypixel.hytale.function.predicate;

@FunctionalInterface
public interface ObjectPositionBlockFunction<T, V, K> {
   K accept(T var1, V var2, int var3, int var4, int var5, int var6);
}
