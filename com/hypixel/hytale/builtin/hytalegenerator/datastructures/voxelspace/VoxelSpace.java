package com.hypixel.hytale.builtin.hytalegenerator.datastructures.voxelspace;

import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import com.hypixel.hytale.math.vector.Vector3i;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface VoxelSpace<T> {
   boolean set(T var1, int var2, int var3, int var4);

   boolean set(T var1, @Nonnull Vector3i var2);

   void set(T var1);

   void setOrigin(int var1, int var2, int var3);

   @Nullable
   T getContent(int var1, int var2, int var3);

   @Nullable
   T getContent(@Nonnull Vector3i var1);

   boolean replace(T var1, int var2, int var3, int var4, @Nonnull Predicate<T> var5);

   void pasteFrom(@Nonnull VoxelSpace<T> var1);

   int getOriginX();

   int getOriginY();

   int getOriginZ();

   String getName();

   boolean isInsideSpace(int var1, int var2, int var3);

   boolean isInsideSpace(@Nonnull Vector3i var1);

   void forEach(VoxelConsumer<? super T> var1);

   @Nonnull
   default Bounds3i getBounds() {
      return new Bounds3i(new Vector3i(this.minX(), this.minY(), this.minZ()), new Vector3i(this.maxX(), this.maxY(), this.maxZ()));
   }

   int minX();

   int maxX();

   int minY();

   int maxY();

   int minZ();

   int maxZ();

   int sizeX();

   int sizeY();

   int sizeZ();
}
