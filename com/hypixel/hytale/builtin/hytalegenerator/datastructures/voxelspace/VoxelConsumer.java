package com.hypixel.hytale.builtin.hytalegenerator.datastructures.voxelspace;

@FunctionalInterface
public interface VoxelConsumer<V> {
   void accept(V var1, int var2, int var3, int var4);
}
