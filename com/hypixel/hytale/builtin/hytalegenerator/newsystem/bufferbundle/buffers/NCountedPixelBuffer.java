package com.hypixel.hytale.builtin.hytalegenerator.newsystem.bufferbundle.buffers;

import com.hypixel.hytale.builtin.hytalegenerator.ArrayUtil;
import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import com.hypixel.hytale.builtin.hytalegenerator.newsystem.performanceinstruments.MemInstrument;
import com.hypixel.hytale.math.vector.Vector3i;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class NCountedPixelBuffer<T> extends NPixelBuffer<T> {
   public static final int BUFFER_SIZE_BITS = 3;
   @Nonnull
   public static final Vector3i SIZE_VOXEL_GRID = new Vector3i(8, 1, 8);
   @Nonnull
   public static final Bounds3i BOUNDS_VOXEL_GRID = new Bounds3i(Vector3i.ZERO, SIZE_VOXEL_GRID);
   @Nonnull
   private final Class<T> pixelType;
   @Nonnull
   private NCountedPixelBuffer.State state;
   @Nullable
   private NCountedPixelBuffer.CountedArrayContents<T> countedArrayContents;
   @Nullable
   private T singleValue;

   public NCountedPixelBuffer(@Nonnull Class<T> voxelType) {
      this.pixelType = voxelType;
      this.state = NCountedPixelBuffer.State.EMPTY;
      this.countedArrayContents = null;
      this.singleValue = null;
   }

   @Nullable
   @Override
   public T getPixelContent(@Nonnull Vector3i position) {
      assert BOUNDS_VOXEL_GRID.contains(position);

      return (T)(switch (this.state) {
         case SINGLE_VALUE -> this.singleValue;
         case ARRAY -> this.countedArrayContents.array[index(position)];
         default -> null;
      });
   }

   @Override
   public void setPixelContent(@Nonnull Vector3i position, @Nullable T value) {
      assert BOUNDS_VOXEL_GRID.contains(position);

      switch (this.state) {
         case SINGLE_VALUE:
            if (this.singleValue == value) {
               return;
            }

            this.switchFromSingleValueToArray();
            this.setPixelContent(position, value);
            break;
         case ARRAY:
            this.countedArrayContents.array[index(position)] = value;
            if (!this.countedArrayContents.allBiomes.contains(value)) {
               this.countedArrayContents.allBiomes.add(value);
            }
            break;
         default:
            this.state = NCountedPixelBuffer.State.SINGLE_VALUE;
            this.singleValue = value;
      }
   }

   @Nonnull
   @Override
   public Class<T> getPixelType() {
      return this.pixelType;
   }

   @Nonnull
   public List<T> getUniqueEntries() {
      switch (this.state) {
         case SINGLE_VALUE:
            return List.of(this.singleValue);
         case ARRAY:
            assert this.countedArrayContents != null;

            return this.countedArrayContents.allBiomes;
         default:
            return List.of();
      }
   }

   public void copyFrom(@Nonnull NCountedPixelBuffer<T> sourceBuffer) {
      this.state = sourceBuffer.state;
      switch (this.state) {
         case SINGLE_VALUE:
            this.singleValue = sourceBuffer.singleValue;
            break;
         case ARRAY:
            this.countedArrayContents = new NCountedPixelBuffer.CountedArrayContents<>();
            this.countedArrayContents.copyFrom(sourceBuffer.countedArrayContents);
            break;
         default:
            return;
      }
   }

   @Nonnull
   @Override
   public MemInstrument.Report getMemoryUsage() {
      long size_bytes = 128L;
      if (this.countedArrayContents != null) {
         size_bytes += this.countedArrayContents.getMemoryUsage().size_bytes();
      }

      return new MemInstrument.Report(size_bytes);
   }

   private void switchFromSingleValueToArray() {
      assert this.state == NCountedPixelBuffer.State.SINGLE_VALUE;

      this.state = NCountedPixelBuffer.State.ARRAY;
      this.countedArrayContents = new NCountedPixelBuffer.CountedArrayContents<>();
      Arrays.fill(this.countedArrayContents.array, this.singleValue);
      this.countedArrayContents.allBiomes.add(this.singleValue);
      this.singleValue = null;
   }

   private static int index(@Nonnull Vector3i position) {
      return position.y + position.x * SIZE_VOXEL_GRID.y + position.z * SIZE_VOXEL_GRID.y * SIZE_VOXEL_GRID.x;
   }

   public static class CountedArrayContents<T> implements MemInstrument {
      @Nonnull
      private final T[] array = (T[])(new Object[NCountedPixelBuffer.SIZE_VOXEL_GRID.x
         * NCountedPixelBuffer.SIZE_VOXEL_GRID.y
         * NCountedPixelBuffer.SIZE_VOXEL_GRID.z]);
      @Nonnull
      private final List<T> allBiomes = new ArrayList<>(1);

      public void copyFrom(@Nonnull NCountedPixelBuffer.CountedArrayContents<T> countedArrayContents) {
         ArrayUtil.copy(countedArrayContents.array, this.array);
         this.allBiomes.clear();
         this.allBiomes.addAll(countedArrayContents.allBiomes);
      }

      @Nonnull
      @Override
      public MemInstrument.Report getMemoryUsage() {
         long size_bytes = 16L + 8L * this.array.length;
         size_bytes += 32L;
         size_bytes += 8L * this.allBiomes.size();
         return new MemInstrument.Report(size_bytes);
      }
   }

   private static enum State {
      EMPTY,
      SINGLE_VALUE,
      ARRAY;
   }
}
