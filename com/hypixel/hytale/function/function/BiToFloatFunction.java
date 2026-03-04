package com.hypixel.hytale.function.function;

@FunctionalInterface
public interface BiToFloatFunction<T, V> {
   float applyAsFloat(T var1, V var2);
}
