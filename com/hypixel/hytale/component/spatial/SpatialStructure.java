package com.hypixel.hytale.component.spatial;

import com.hypixel.hytale.math.vector.Vector3d;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface SpatialStructure<T> {
   int size();

   void rebuild(@Nonnull SpatialData<T> var1);

   @Nullable
   T closest(@Nonnull Vector3d var1);

   void collect(@Nonnull Vector3d var1, double var2, @Nonnull List<T> var4);

   void collectCylinder(@Nonnull Vector3d var1, double var2, double var4, @Nonnull List<T> var6);

   void collectBox(@Nonnull Vector3d var1, @Nonnull Vector3d var2, @Nonnull List<T> var3);

   void ordered(@Nonnull Vector3d var1, double var2, @Nonnull List<T> var4);

   void ordered3DAxis(@Nonnull Vector3d var1, double var2, double var4, double var6, @Nonnull List<T> var8);

   @Nonnull
   String dump();
}
