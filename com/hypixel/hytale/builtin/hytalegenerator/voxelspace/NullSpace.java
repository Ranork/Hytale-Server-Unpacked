package com.hypixel.hytale.builtin.hytalegenerator.voxelspace;

import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import com.hypixel.hytale.math.vector.Vector3i;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class NullSpace<V> implements VoxelSpace<V> {
   @Nonnull
   private static final NullSpace INSTANCE = new NullSpace();

   @Nonnull
   public static <V> NullSpace<V> instance() {
      return INSTANCE;
   }

   @Nonnull
   public static <V> NullSpace<V> instance(@Nonnull Class<V> clazz) {
      return INSTANCE;
   }

   private NullSpace() {
   }

   @Override
   public void set(V content, int x, int y, int z) {
   }

   @Override
   public void set(V content, @Nonnull Vector3i position) {
   }

   @Override
   public void setAll(V content) {
   }

   @Nullable
   @Override
   public V get(int x, int y, int z) {
      return null;
   }

   @Nullable
   @Override
   public V get(@Nonnull Vector3i position) {
      return this.get(position.x, position.y, position.z);
   }

   @NonNullDecl
   @Override
   public Bounds3i getBounds() {
      return Bounds3i.ZERO;
   }
}
