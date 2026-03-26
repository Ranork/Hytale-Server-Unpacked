package com.hypixel.hytale.builtin.hytalegenerator.engine.views;

import com.hypixel.hytale.builtin.hytalegenerator.GridUtils;
import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import com.hypixel.hytale.builtin.hytalegenerator.engine.bufferbundle.BufferBundle;
import com.hypixel.hytale.builtin.hytalegenerator.engine.bufferbundle.buffers.PixelBuffer;
import com.hypixel.hytale.builtin.hytalegenerator.voxelspace.VoxelSpace;
import com.hypixel.hytale.math.vector.Vector3i;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class PixelBufferView<T> implements VoxelSpace<T> {
   public static final int Y_LEVEL_BUFFER_GRID = 0;
   public static final int Y_LEVEL_VOXEL_GRID = 0;
   @Nonnull
   private final Class<T> voxelType;
   @Nonnull
   private final BufferBundle.Access.View bufferAccess;
   @Nonnull
   private final Bounds3i bounds_voxelGrid;
   @Nonnull
   private final Vector3i size_voxelGrid;

   public PixelBufferView(@Nonnull BufferBundle.Access.View bufferAccess, @Nonnull Class<T> pixelType) {
      assert bufferAccess.getBounds_bufferGrid().min.y <= 0 && bufferAccess.getBounds_bufferGrid().max.y > 0;

      this.bufferAccess = bufferAccess;
      this.voxelType = pixelType;
      this.bounds_voxelGrid = bufferAccess.getBounds_bufferGrid();
      GridUtils.toVoxelGrid_fromBufferGrid(this.bounds_voxelGrid);
      this.bounds_voxelGrid.min.y = 0;
      this.bounds_voxelGrid.max.y = 1;
      this.size_voxelGrid = this.bounds_voxelGrid.getSize();
   }

   @Override
   public void set(T content, int x, int y, int z) {
      this.set(content, new Vector3i(x, y, z));
   }

   @Override
   public void set(T value, @Nonnull Vector3i position_voxelGrid) {
      assert this.bounds_voxelGrid.contains(position_voxelGrid);

      PixelBuffer<T> buffer = this.getBuffer(position_voxelGrid);
      Vector3i positionInBuffer_voxelGrid = position_voxelGrid.clone();
      GridUtils.toVoxelGridInsideBuffer_fromWorldGrid(positionInBuffer_voxelGrid);
      buffer.setPixelContent(positionInBuffer_voxelGrid, value);
   }

   @Override
   public void setAll(T content) {
      throw new UnsupportedOperationException();
   }

   @Nullable
   @Override
   public T get(int x, int y, int z) {
      return this.get(new Vector3i(x, y, z));
   }

   @Nullable
   @Override
   public T get(@Nonnull Vector3i position_voxelGrid) {
      assert this.bounds_voxelGrid.contains(position_voxelGrid);

      PixelBuffer<T> buffer = this.getBuffer(position_voxelGrid);
      Vector3i positionInBuffer_voxelGrid = position_voxelGrid.clone();
      GridUtils.toVoxelGridInsideBuffer_fromWorldGrid(positionInBuffer_voxelGrid);
      return buffer.getPixelContent(positionInBuffer_voxelGrid);
   }

   @Nonnull
   private PixelBuffer<T> getBuffer(@Nonnull Vector3i position_voxelGrid) {
      assert this.bounds_voxelGrid.contains(position_voxelGrid);

      Vector3i localBufferPosition_bufferGrid = position_voxelGrid.clone();
      GridUtils.toBufferGrid_fromVoxelGrid(localBufferPosition_bufferGrid);
      return (PixelBuffer<T>)this.bufferAccess.getBuffer(localBufferPosition_bufferGrid).buffer();
   }

   @NonNullDecl
   @Override
   public Bounds3i getBounds() {
      return this.bounds_voxelGrid;
   }
}
