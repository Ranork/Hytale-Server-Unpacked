package com.hypixel.hytale.builtin.hytalegenerator.datastructures.voxelspace;

import com.hypixel.hytale.math.vector.Vector3i;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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
   public boolean set(V content, int x, int y, int z) {
      return false;
   }

   @Override
   public boolean set(V content, @Nonnull Vector3i position) {
      return this.set(content, position.x, position.y, position.z);
   }

   @Override
   public void set(V content) {
   }

   @Override
   public void setOrigin(int x, int y, int z) {
   }

   @Nullable
   @Override
   public V getContent(int x, int y, int z) {
      return null;
   }

   @Nullable
   @Override
   public V getContent(@Nonnull Vector3i position) {
      return this.getContent(position.x, position.y, position.z);
   }

   @Override
   public boolean replace(V replacement, int x, int y, int z, @Nonnull Predicate<V> mask) {
      return false;
   }

   @Override
   public void pasteFrom(@Nonnull VoxelSpace<V> source) {
   }

   @Override
   public int getOriginX() {
      return 0;
   }

   @Override
   public int getOriginY() {
      return 0;
   }

   @Override
   public int getOriginZ() {
      return 0;
   }

   @Nonnull
   @Override
   public String getName() {
      return "null_space";
   }

   @Override
   public boolean isInsideSpace(int x, int y, int z) {
      return false;
   }

   @Override
   public boolean isInsideSpace(@Nonnull Vector3i position) {
      return this.isInsideSpace(position.x, position.y, position.z);
   }

   @Override
   public void forEach(VoxelConsumer<? super V> action) {
   }

   @Override
   public int minX() {
      return 0;
   }

   @Override
   public int maxX() {
      return 0;
   }

   @Override
   public int minY() {
      return 0;
   }

   @Override
   public int maxY() {
      return 0;
   }

   @Override
   public int minZ() {
      return 0;
   }

   @Override
   public int maxZ() {
      return 0;
   }

   @Override
   public int sizeX() {
      return 0;
   }

   @Override
   public int sizeY() {
      return 0;
   }

   @Override
   public int sizeZ() {
      return 0;
   }
}
