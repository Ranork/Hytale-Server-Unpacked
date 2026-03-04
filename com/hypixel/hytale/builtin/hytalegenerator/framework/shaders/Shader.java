package com.hypixel.hytale.builtin.hytalegenerator.framework.shaders;

public interface Shader<T> {
   T shade(T var1, long var2);

   T shade(T var1, long var2, long var4);

   T shade(T var1, long var2, long var4, long var6);
}
