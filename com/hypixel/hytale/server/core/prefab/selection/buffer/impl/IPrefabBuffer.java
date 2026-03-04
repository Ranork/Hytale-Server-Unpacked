package com.hypixel.hytale.server.core.prefab.selection.buffer.impl;

import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.server.core.prefab.PrefabRotation;
import com.hypixel.hytale.server.core.prefab.PrefabWeights;
import com.hypixel.hytale.server.core.prefab.selection.buffer.PrefabBufferCall;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface IPrefabBuffer {
   IPrefabBuffer.ColumnPredicate<?> ALL_COLUMNS = (x, z, blocks, o) -> true;

   int getAnchorX();

   int getAnchorY();

   int getAnchorZ();

   int getMinX(@Nonnull PrefabRotation var1);

   int getMinY();

   int getMinZ(@Nonnull PrefabRotation var1);

   int getMaxX(@Nonnull PrefabRotation var1);

   int getMaxY();

   int getMaxZ(@Nonnull PrefabRotation var1);

   default int getMinX() {
      return this.getMinX(PrefabRotation.ROTATION_0);
   }

   default int getMinZ() {
      return this.getMinZ(PrefabRotation.ROTATION_0);
   }

   default int getMaxX() {
      return this.getMaxX(PrefabRotation.ROTATION_0);
   }

   default int getMaxZ() {
      return this.getMaxZ(PrefabRotation.ROTATION_0);
   }

   int getMinYAt(@Nonnull PrefabRotation var1, int var2, int var3);

   int getMaxYAt(@Nonnull PrefabRotation var1, int var2, int var3);

   int getColumnCount();

   @Nonnull
   PrefabBuffer.ChildPrefab[] getChildPrefabs();

   default int getMaximumExtend() {
      int max = 0;

      for (PrefabRotation rotation : PrefabRotation.VALUES) {
         int x = this.getMaxX(rotation) - this.getMinX(rotation);
         if (x > max) {
            max = x;
         }

         int z = this.getMaxZ(rotation) - this.getMinZ(rotation);
         if (z > max) {
            max = z;
         }
      }

      return max;
   }

   <T extends PrefabBufferCall> void forEach(
      @Nonnull IPrefabBuffer.ColumnPredicate<T> var1,
      @Nonnull IPrefabBuffer.BlockConsumer<T> var2,
      @Nullable IPrefabBuffer.EntityConsumer<T> var3,
      @Nullable IPrefabBuffer.ChildConsumer<T> var4,
      @Nonnull T var5
   );

   <T> void forEachRaw(
      @Nonnull IPrefabBuffer.ColumnPredicate<T> var1,
      @Nonnull IPrefabBuffer.RawBlockConsumer<T> var2,
      @Nonnull IPrefabBuffer.FluidConsumer<T> var3,
      @Nullable IPrefabBuffer.EntityConsumer<T> var4,
      @Nullable T var5
   );

   <T> boolean forEachRaw(
      @Nonnull IPrefabBuffer.ColumnPredicate<T> var1,
      @Nonnull IPrefabBuffer.RawBlockPredicate<T> var2,
      @Nonnull IPrefabBuffer.FluidPredicate<T> var3,
      @Nullable IPrefabBuffer.EntityPredicate<T> var4,
      @Nullable T var5
   );

   void release();

   default <T extends PrefabBufferCall> boolean compare(@Nonnull IPrefabBuffer.BlockComparingPredicate<T> blockComparingPredicate, @Nonnull T t) {
      return this.forEachRaw(
         iterateAllColumns(),
         (x, y, z, blockId, chance, holder, support, rotation, filler, o) -> blockComparingPredicate.test(x, y, z, blockId, rotation, holder, (T)o),
         (x, y, z, fluidId, level, o) -> true,
         (x, z, entityWrappers, o) -> true,
         t
      );
   }

   default <T extends PrefabBufferCall> boolean compare(
      @Nonnull IPrefabBuffer.BlockComparingPrefabPredicate<T> blockComparingIterator, @Nonnull T t, @Nonnull IPrefabBuffer secondPrefab
   ) {
      throw new UnsupportedOperationException("Not implemented! Please implement some inefficient default here!");
   }

   int getBlockId(int var1, int var2, int var3);

   int getFiller(int var1, int var2, int var3);

   int getRotationIndex(int var1, int var2, int var3);

   @Nonnull
   static <T> IPrefabBuffer.ColumnPredicate<T> iterateAllColumns() {
      return (IPrefabBuffer.ColumnPredicate<T>)ALL_COLUMNS;
   }

   @FunctionalInterface
   public interface BlockComparingPredicate<T> {
      boolean test(int var1, int var2, int var3, int var4, int var5, Holder<ChunkStore> var6, T var7);
   }

   @FunctionalInterface
   public interface BlockComparingPrefabPredicate<T> {
      boolean test(
         int var1,
         int var2,
         int var3,
         int var4,
         Holder<ChunkStore> var5,
         float var6,
         int var7,
         int var8,
         int var9,
         Holder<ChunkStore> var10,
         float var11,
         int var12,
         int var13,
         T var14
      );
   }

   @FunctionalInterface
   public interface BlockConsumer<T> {
      void accept(int var1, int var2, int var3, int var4, @Nullable Holder<ChunkStore> var5, int var6, int var7, int var8, T var9, int var10, int var11);
   }

   @FunctionalInterface
   public interface ChildConsumer<T> {
      void accept(int var1, int var2, int var3, String var4, boolean var5, boolean var6, boolean var7, PrefabWeights var8, PrefabRotation var9, T var10);
   }

   @FunctionalInterface
   public interface ColumnPredicate<T> {
      boolean test(int var1, int var2, int var3, T var4);
   }

   @FunctionalInterface
   public interface EntityConsumer<T> {
      void accept(int var1, int var2, @Nullable Holder<EntityStore>[] var3, T var4);
   }

   @FunctionalInterface
   public interface EntityPredicate<T> {
      boolean test(int var1, int var2, @Nonnull Holder<EntityStore>[] var3, T var4);
   }

   @FunctionalInterface
   public interface FluidConsumer<T> {
      void accept(int var1, int var2, int var3, int var4, byte var5, T var6);
   }

   @FunctionalInterface
   public interface FluidPredicate<T> {
      boolean test(int var1, int var2, int var3, int var4, byte var5, T var6);
   }

   @FunctionalInterface
   public interface RawBlockConsumer<T> {
      void accept(int var1, int var2, int var3, int var4, int var5, float var6, Holder<ChunkStore> var7, int var8, int var9, int var10, T var11);
   }

   @FunctionalInterface
   public interface RawBlockPredicate<T> {
      boolean test(int var1, int var2, int var3, int var4, float var5, Holder<ChunkStore> var6, int var7, int var8, int var9, T var10);
   }
}
