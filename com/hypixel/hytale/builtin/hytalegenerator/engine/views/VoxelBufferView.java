package com.hypixel.hytale.builtin.hytalegenerator.engine.views;

import com.hypixel.hytale.builtin.hytalegenerator.GridUtils;
import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import com.hypixel.hytale.builtin.hytalegenerator.engine.bufferbundle.BufferBundle;
import com.hypixel.hytale.builtin.hytalegenerator.engine.bufferbundle.buffers.VoxelBuffer;
import com.hypixel.hytale.builtin.hytalegenerator.voxelspace.VoxelSpace;
import com.hypixel.hytale.math.vector.Vector3i;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class VoxelBufferView<T> implements VoxelSpace<T> {
   @Nonnull
   private final Class<T> voxelType;
   @Nonnull
   private final BufferBundle.Access.View bufferAccess;
   @Nonnull
   private final Bounds3i bounds_voxelGrid;
   @Nonnull
   private final Vector3i size_voxelGrid;

   public VoxelBufferView(@Nonnull BufferBundle.Access.View bufferAccess, @Nonnull Class<T> voxelType) {
      this.bufferAccess = bufferAccess;
      this.voxelType = voxelType;
      this.bounds_voxelGrid = bufferAccess.getBounds_bufferGrid();
      GridUtils.toVoxelGrid_fromBufferGrid(this.bounds_voxelGrid);
      this.size_voxelGrid = this.bounds_voxelGrid.getSize();
   }

   public void copyFrom(@Nonnull VoxelBufferView<T> source) {
      assert source.bounds_voxelGrid.contains(this.bounds_voxelGrid);

      Bounds3i thisBounds_bufferGrid = this.bufferAccess.getBounds_bufferGrid();
      Vector3i pos_bufferGrid = new Vector3i();
      pos_bufferGrid.setX(thisBounds_bufferGrid.min.x);

      while (pos_bufferGrid.x < thisBounds_bufferGrid.max.x) {
         pos_bufferGrid.setY(thisBounds_bufferGrid.min.y);

         while (pos_bufferGrid.y < thisBounds_bufferGrid.max.y) {
            pos_bufferGrid.setZ(thisBounds_bufferGrid.min.z);

            while (pos_bufferGrid.z < thisBounds_bufferGrid.max.z) {
               VoxelBuffer<T> sourceBuffer = source.getBuffer_fromBufferGrid(pos_bufferGrid);
               VoxelBuffer<T> destinationBuffer = this.getBuffer_fromBufferGrid(pos_bufferGrid);
               destinationBuffer.reference(sourceBuffer);
               pos_bufferGrid.setZ(pos_bufferGrid.z + 1);
            }

            pos_bufferGrid.setY(pos_bufferGrid.y + 1);
         }

         pos_bufferGrid.setX(pos_bufferGrid.x + 1);
      }
   }

   @Override
   public void set(T content, int x, int y, int z) {
      assert this.bounds_voxelGrid.contains(x, y, z);

      VoxelBuffer<T> buffer = this.getBuffer_fromVoxelGrid(x, y, z);
      int x_internal = GridUtils.toXVoxelGridInsideBuffer_fromWorldGrid(x);
      int y_internal = GridUtils.toYVoxelGridInsideBuffer_fromWorldGrid(y);
      int z_internal = GridUtils.toZVoxelGridInsideBuffer_fromWorldGrid(z);
      buffer.setVoxelContent(x_internal, y_internal, z_internal, content);
   }

   @Override
   public void set(T content, @Nonnull Vector3i position_voxelGrid) {
      assert this.bounds_voxelGrid.contains(position_voxelGrid);

      int initialX = position_voxelGrid.x;
      int initialY = position_voxelGrid.y;
      int initialZ = position_voxelGrid.z;
      VoxelBuffer<T> buffer = this.getBuffer_fromVoxelGrid(position_voxelGrid);
      GridUtils.toVoxelGridInsideBuffer_fromWorldGrid(position_voxelGrid);
      buffer.setVoxelContent(position_voxelGrid, content);
      position_voxelGrid.assign(initialX, initialY, initialZ);
   }

   @Override
   public void setAll(T content) {
      throw new UnsupportedOperationException();
   }

   @Nullable
   @Override
   public T get(int x, int y, int z) {
      assert this.bounds_voxelGrid.contains(x, y, z);

      VoxelBuffer<T> buffer = this.getBuffer_fromVoxelGrid(x, y, z);
      int x_internal = GridUtils.toXVoxelGridInsideBuffer_fromWorldGrid(x);
      int y_internal = GridUtils.toYVoxelGridInsideBuffer_fromWorldGrid(y);
      int z_internal = GridUtils.toZVoxelGridInsideBuffer_fromWorldGrid(z);
      return buffer.getVoxelContent(x_internal, y_internal, z_internal);
   }

   @Nullable
   @Override
   public T get(@Nonnull Vector3i position_voxelGrid) {
      assert this.bounds_voxelGrid.contains(position_voxelGrid);

      int initialX = position_voxelGrid.x;
      int initialY = position_voxelGrid.y;
      int initialZ = position_voxelGrid.z;
      VoxelBuffer<T> buffer = this.getBuffer_fromVoxelGrid(position_voxelGrid);
      GridUtils.toVoxelGridInsideBuffer_fromWorldGrid(position_voxelGrid);
      T content = buffer.getVoxelContent(position_voxelGrid);
      position_voxelGrid.assign(initialX, initialY, initialZ);
      return content;
   }

   @NonNullDecl
   @Override
   public Bounds3i getBounds() {
      return this.bounds_voxelGrid;
   }

   @Nonnull
   private VoxelBuffer<T> getBuffer_fromVoxelGrid(int x_voxelGrid, int y_voxelGrid, int z_voxelGrid) {
      int x_bufferGrid = GridUtils.toBufferGrid_fromVoxelGrid(x_voxelGrid);
      int y_bufferGrid = GridUtils.toBufferGrid_fromVoxelGrid(y_voxelGrid);
      int z_bufferGrid = GridUtils.toBufferGrid_fromVoxelGrid(z_voxelGrid);
      return this.getBuffer_fromBufferGrid(x_bufferGrid, y_bufferGrid, z_bufferGrid);
   }

   @Nonnull
   private VoxelBuffer<T> getBuffer_fromVoxelGrid(@Nonnull Vector3i position_voxelGrid) {
      Vector3i localBufferPosition_bufferGrid = position_voxelGrid.clone();
      GridUtils.toBufferGrid_fromVoxelGrid(localBufferPosition_bufferGrid);
      return this.getBuffer_fromBufferGrid(localBufferPosition_bufferGrid);
   }

   @Nonnull
   private VoxelBuffer<T> getBuffer_fromBufferGrid(int x_bufferGrid, int y_bufferGrid, int z_bufferGrid) {
      return (VoxelBuffer<T>)this.bufferAccess.getBuffer(x_bufferGrid, y_bufferGrid, z_bufferGrid).buffer();
   }

   @Nonnull
   private VoxelBuffer<T> getBuffer_fromBufferGrid(@Nonnull Vector3i position_bufferGrid) {
      return (VoxelBuffer<T>)this.bufferAccess.getBuffer(position_bufferGrid).buffer();
   }
}
