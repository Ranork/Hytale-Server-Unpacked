package com.hypixel.hytale.builtin.hytalegenerator.voxelspace;

import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import com.hypixel.hytale.math.vector.Vector3i;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface VoxelSpace<T> {
   void set(@Nullable T var1, int var2, int var3, int var4);

   void set(@Nullable T var1, @Nonnull Vector3i var2);

   void setAll(@Nullable T var1);

   @Nullable
   T get(int var1, int var2, int var3);

   @Nullable
   T get(@Nonnull Vector3i var1);

   @Nonnull
   Bounds3i getBounds();
}
