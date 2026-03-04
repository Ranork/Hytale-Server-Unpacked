package com.hypixel.hytale.builtin.hytalegenerator.datastructures.bicoordinatecache;

public interface BiCoordinateCache<T> {
   T get(int var1, int var2);

   boolean isCached(int var1, int var2);

   T save(int var1, int var2, T var3);

   void flush(int var1, int var2);

   void flush();

   int size();
}
