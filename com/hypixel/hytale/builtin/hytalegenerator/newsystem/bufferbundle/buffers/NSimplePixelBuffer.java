package com.hypixel.hytale.builtin.hytalegenerator.newsystem.bufferbundle.buffers;

import com.hypixel.hytale.builtin.hytalegenerator.ArrayUtil;
import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import com.hypixel.hytale.builtin.hytalegenerator.newsystem.performanceinstruments.MemInstrument;
import com.hypixel.hytale.math.vector.Vector3i;
import java.util.Arrays;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class NSimplePixelBuffer<T> extends NPixelBuffer<T> {
   @Nonnull
   private static final Bounds3i bounds = new Bounds3i(Vector3i.ZERO, SIZE);
   @Nonnull
   private final Class<T> pixelType;
   @Nonnull
   private NSimplePixelBuffer.State state;
   @Nullable
   private NSimplePixelBuffer.ArrayContents<T> arrayContents;
   @Nullable
   private T singleValue;

   public NSimplePixelBuffer(@Nonnull Class<T> pixelType) {
      this.pixelType = pixelType;
      this.state = NSimplePixelBuffer.State.EMPTY;
      this.arrayContents = null;
      this.singleValue = null;
   }

   @Nullable
   @Override
   public T getPixelContent(@Nonnull Vector3i position) {
      assert bounds.contains(position);

      return (T)(switch (this.state) {
         case SINGLE_VALUE -> this.singleValue;
         case ARRAY -> this.arrayContents.array[index(position)];
         default -> null;
      });
   }

   @Override
   public void setPixelContent(@Nonnull Vector3i position, @Nullable T value) {
      assert bounds.contains(position);

      switch (this.state) {
         case SINGLE_VALUE:
            if (this.singleValue == value) {
               return;
            }

            this.switchFromSingleValueToArray();
            this.setPixelContent(position, value);
            break;
         case ARRAY:
            this.arrayContents.array[index(position)] = value;
            break;
         default:
            this.state = NSimplePixelBuffer.State.SINGLE_VALUE;
            this.singleValue = value;
      }
   }

   @Nonnull
   @Override
   public Class<T> getPixelType() {
      return this.pixelType;
   }

   public void copyFrom(@Nonnull NSimplePixelBuffer<T> sourceBuffer) {
      this.state = sourceBuffer.state;
      switch (this.state) {
         case SINGLE_VALUE:
            this.singleValue = sourceBuffer.singleValue;
            break;
         case ARRAY:
            this.arrayContents = new NSimplePixelBuffer.ArrayContents<>();
            ArrayUtil.copy(sourceBuffer.arrayContents.array, this.arrayContents.array);
            break;
         default:
            return;
      }
   }

   @Nonnull
   @Override
   public MemInstrument.Report getMemoryUsage() {
      long size_bytes = 128L;
      if (this.arrayContents != null) {
         size_bytes += this.arrayContents.getMemoryUsage().size_bytes();
      }

      return new MemInstrument.Report(size_bytes);
   }

   private void ensureContents() {
      if (this.arrayContents == null) {
         this.arrayContents = new NSimplePixelBuffer.ArrayContents<>();
      }
   }

   private void switchFromSingleValueToArray() {
      assert this.state == NSimplePixelBuffer.State.SINGLE_VALUE;

      this.state = NSimplePixelBuffer.State.ARRAY;
      this.arrayContents = new NSimplePixelBuffer.ArrayContents<>();
      Arrays.fill(this.arrayContents.array, this.singleValue);
      this.singleValue = null;
   }

   private static int index(@Nonnull Vector3i position) {
      return position.y + position.x * SIZE.y + position.z * SIZE.y * SIZE.x;
   }

   public static class ArrayContents<T> implements MemInstrument {
      @Nonnull
      private final T[] array = (T[])(new Object[NPixelBuffer.SIZE.x * NPixelBuffer.SIZE.y * NPixelBuffer.SIZE.z]);

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
      ARRAY;
   }
}
