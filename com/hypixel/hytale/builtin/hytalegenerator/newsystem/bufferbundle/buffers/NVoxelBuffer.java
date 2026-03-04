package com.hypixel.hytale.builtin.hytalegenerator.newsystem.bufferbundle.buffers;

import com.hypixel.hytale.builtin.hytalegenerator.ArrayUtil;
import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import com.hypixel.hytale.builtin.hytalegenerator.newsystem.performanceinstruments.MemInstrument;
import com.hypixel.hytale.math.vector.Vector3i;
import java.util.Arrays;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class NVoxelBuffer<T> extends NBuffer {
   public static final int BUFFER_SIZE_BITS = 3;
   @Nonnull
   public static final Vector3i SIZE = new Vector3i(8, 8, 8);
   @Nonnull
   private static final Bounds3i bounds = new Bounds3i(Vector3i.ZERO, SIZE);
   @Nonnull
   private final Class<T> voxelType;
   @Nonnull
   private NVoxelBuffer.State state;
   @Nullable
   private NVoxelBuffer.ArrayContents<T> arrayContents;
   @Nullable
   private T singleValue;
   @Nullable
   private NVoxelBuffer<T> referenceBuffer;

   public NVoxelBuffer(@Nonnull Class<T> voxelType) {
      this.voxelType = voxelType;
      this.state = NVoxelBuffer.State.EMPTY;
      this.arrayContents = null;
      this.singleValue = null;
      this.referenceBuffer = null;
   }

   @Nullable
   public T getVoxelContent(@Nonnull Vector3i position) {
      assert bounds.contains(position);

      return (T)(switch (this.state) {
         case SINGLE_VALUE -> this.singleValue;
         case ARRAY -> this.arrayContents.array[index(position)];
         case REFERENCE -> this.referenceBuffer.getVoxelContent(position);
         default -> null;
      });
   }

   @Nonnull
   public Class<T> getVoxelType() {
      return this.voxelType;
   }

   public void setVoxelContent(@Nonnull Vector3i position, @Nullable T value) {
      assert bounds.contains(position);

      switch (this.state) {
         case SINGLE_VALUE:
            if (this.singleValue == value) {
               return;
            }

            this.switchFromSingleValueToArray();
            this.setVoxelContent(position, value);
            break;
         case ARRAY:
            this.arrayContents.array[index(position)] = value;
            break;
         case REFERENCE:
            this.dereference();
            this.setVoxelContent(position, value);
            break;
         default:
            this.state = NVoxelBuffer.State.SINGLE_VALUE;
            this.singleValue = value;
      }
   }

   public void reference(@Nonnull NVoxelBuffer<T> sourceBuffer) {
      this.state = NVoxelBuffer.State.REFERENCE;
      this.referenceBuffer = this.lastReference(sourceBuffer);
      this.singleValue = null;
      this.arrayContents = null;
   }

   @Nonnull
   private NVoxelBuffer<T> lastReference(@Nonnull NVoxelBuffer<T> sourceBuffer) {
      while (sourceBuffer.state == NVoxelBuffer.State.REFERENCE) {
         sourceBuffer = sourceBuffer.referenceBuffer;
      }

      return sourceBuffer;
   }

   @Nonnull
   @Override
   public MemInstrument.Report getMemoryUsage() {
      long size_bytes = 128L;
      size_bytes += 40L;
      if (this.state == NVoxelBuffer.State.ARRAY) {
         size_bytes += this.arrayContents.getMemoryUsage().size_bytes();
      }

      return new MemInstrument.Report(size_bytes);
   }

   private void switchFromSingleValueToArray() {
      assert this.state == NVoxelBuffer.State.SINGLE_VALUE;

      this.state = NVoxelBuffer.State.ARRAY;
      this.arrayContents = new NVoxelBuffer.ArrayContents<>();
      Arrays.fill(this.arrayContents.array, this.singleValue);
      this.singleValue = null;
   }

   private void dereference() {
      assert this.state == NVoxelBuffer.State.REFERENCE;

      this.state = this.referenceBuffer.state;
      switch (this.state) {
         case SINGLE_VALUE:
            this.singleValue = this.referenceBuffer.singleValue;
            break;
         case ARRAY:
            this.arrayContents = new NVoxelBuffer.ArrayContents<>();
            ArrayUtil.copy(this.referenceBuffer.arrayContents.array, this.arrayContents.array);
            break;
         case REFERENCE:
            this.referenceBuffer = this.referenceBuffer.referenceBuffer;
            break;
         default:
            return;
      }
   }

   private static int index(@Nonnull Vector3i position) {
      return position.y + position.x * SIZE.y + position.z * SIZE.y * SIZE.x;
   }

   public static class ArrayContents<T> implements MemInstrument {
      @Nonnull
      private final T[] array = (T[])(new Object[NVoxelBuffer.SIZE.x * NVoxelBuffer.SIZE.y * NVoxelBuffer.SIZE.z]);

      @Nonnull
      @Override
      public MemInstrument.Report getMemoryUsage() {
         long size_bytes = 16L + 8L * this.array.length;
         return new MemInstrument.Report(size_bytes);
      }
   }

   private static enum State {
      EMPTY,
      SINGLE_VALUE,
      ARRAY,
      REFERENCE;
   }
}
