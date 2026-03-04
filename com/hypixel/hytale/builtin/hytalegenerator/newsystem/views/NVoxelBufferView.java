package com.hypixel.hytale.builtin.hytalegenerator.newsystem.views;

import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import com.hypixel.hytale.builtin.hytalegenerator.datastructures.voxelspace.VoxelConsumer;
import com.hypixel.hytale.builtin.hytalegenerator.datastructures.voxelspace.VoxelSpace;
import com.hypixel.hytale.builtin.hytalegenerator.newsystem.GridUtils;
import com.hypixel.hytale.builtin.hytalegenerator.newsystem.bufferbundle.NBufferBundle;
import com.hypixel.hytale.builtin.hytalegenerator.newsystem.bufferbundle.buffers.NVoxelBuffer;
import com.hypixel.hytale.math.vector.Vector3i;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class NVoxelBufferView<T> implements VoxelSpace<T> {
   @Nonnull
   private final Class<T> voxelType;
   @Nonnull
   private final NBufferBundle.Access.View bufferAccess;
   @Nonnull
   private final Bounds3i bounds_voxelGrid;
   @Nonnull
   private final Vector3i size_voxelGrid;

   public NVoxelBufferView(@Nonnull NBufferBundle.Access.View bufferAccess, @Nonnull Class<T> voxelType) {
      this.bufferAccess = bufferAccess;
      this.voxelType = voxelType;
      this.bounds_voxelGrid = bufferAccess.getBounds_bufferGrid();
      GridUtils.toVoxelGrid_fromBufferGrid(this.bounds_voxelGrid);
      this.size_voxelGrid = this.bounds_voxelGrid.getSize();
   }

   public void copyFrom(@Nonnull NVoxelBufferView<T> source) {
      assert source.bounds_voxelGrid.contains(this.bounds_voxelGrid);

      Bounds3i thisBounds_bufferGrid = this.bufferAccess.getBounds_bufferGrid();
      Vector3i pos_bufferGrid = new Vector3i();
      pos_bufferGrid.setX(thisBounds_bufferGrid.min.x);

      while (pos_bufferGrid.x < thisBounds_bufferGrid.max.x) {
         pos_bufferGrid.setY(thisBounds_bufferGrid.min.y);

         while (pos_bufferGrid.y < thisBounds_bufferGrid.max.y) {
            pos_bufferGrid.setZ(thisBounds_bufferGrid.min.z);

            while (pos_bufferGrid.z < thisBounds_bufferGrid.max.z) {
               NVoxelBuffer<T> sourceBuffer = source.getBuffer_fromBufferGrid(pos_bufferGrid);
               NVoxelBuffer<T> destinationBuffer = this.getBuffer_fromBufferGrid(pos_bufferGrid);
               destinationBuffer.reference(sourceBuffer);
               pos_bufferGrid.setZ(pos_bufferGrid.z + 1);
            }

            pos_bufferGrid.setY(pos_bufferGrid.y + 1);
         }

         pos_bufferGrid.setX(pos_bufferGrid.x + 1);
      }
   }

   @Override
   public boolean set(T content, int x, int y, int z) {
      return this.set(content, new Vector3i(x, y, z));
   }

   @Override
   public boolean set(T content, @Nonnull Vector3i position_voxelGrid) {
      assert this.bounds_voxelGrid.contains(position_voxelGrid);

      int initialX = position_voxelGrid.x;
      int initialY = position_voxelGrid.y;
      int initialZ = position_voxelGrid.z;
      NVoxelBuffer<T> buffer = this.getBuffer_fromVoxelGrid(position_voxelGrid);
      GridUtils.toVoxelGridInsideBuffer_fromWorldGrid(position_voxelGrid);
      buffer.setVoxelContent(position_voxelGrid, content);
      position_voxelGrid.assign(initialX, initialY, initialZ);
      return true;
   }

   @Override
   public void set(T content) {
      throw new UnsupportedOperationException();
   }

   @Override
   public void setOrigin(int x, int y, int z) {
      throw new UnsupportedOperationException();
   }

   @Nullable
   @Override
   public T getContent(int x, int y, int z) {
      return this.getContent(new Vector3i(x, y, z));
   }

   @Nullable
   @Override
   public T getContent(@Nonnull Vector3i position_voxelGrid) {
      assert this.bounds_voxelGrid.contains(position_voxelGrid);

      int initialX = position_voxelGrid.x;
      int initialY = position_voxelGrid.y;
      int initialZ = position_voxelGrid.z;
      NVoxelBuffer<T> buffer = this.getBuffer_fromVoxelGrid(position_voxelGrid);
      GridUtils.toVoxelGridInsideBuffer_fromWorldGrid(position_voxelGrid);
      T content = buffer.getVoxelContent(position_voxelGrid);
      position_voxelGrid.assign(initialX, initialY, initialZ);
      return content;
   }

   @Override
   public boolean replace(T replacement, int x, int y, int z, @Nonnull Predicate<T> mask) {
      return false;
   }

   @Override
   public void pasteFrom(@Nonnull VoxelSpace<T> source) {
      throw new UnsupportedOperationException();
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

   @Override
   public String getName() {
      throw new UnsupportedOperationException();
   }

   @Override
   public boolean isInsideSpace(int x, int y, int z) {
      return this.isInsideSpace(new Vector3i(x, y, z));
   }

   @Override
   public boolean isInsideSpace(@Nonnull Vector3i position) {
      return this.bounds_voxelGrid.contains(position);
   }

   @Override
   public void forEach(VoxelConsumer<? super T> action) {
      throw new UnsupportedOperationException();
   }

   @Override
   public int minX() {
      return this.bounds_voxelGrid.min.x;
   }

   @Override
   public int maxX() {
      return this.bounds_voxelGrid.max.x;
   }

   @Override
   public int minY() {
      return this.bounds_voxelGrid.min.y;
   }

   @Override
   public int maxY() {
      return this.bounds_voxelGrid.max.y;
   }

   @Override
   public int minZ() {
      return this.bounds_voxelGrid.min.z;
   }

   @Override
   public int maxZ() {
      return this.bounds_voxelGrid.max.z;
   }

   @Override
   public int sizeX() {
      return this.size_voxelGrid.x;
   }

   @Override
   public int sizeY() {
      return this.size_voxelGrid.y;
   }

   @Override
   public int sizeZ() {
      return this.size_voxelGrid.z;
   }

   @Nonnull
   private NVoxelBuffer<T> getBuffer_fromVoxelGrid(@Nonnull Vector3i position_voxelGrid) {
      Vector3i localBufferPosition_bufferGrid = position_voxelGrid.clone();
      GridUtils.toBufferGrid_fromVoxelGrid(localBufferPosition_bufferGrid);
      return this.getBuffer_fromBufferGrid(localBufferPosition_bufferGrid);
   }

   @Nonnull
   private NVoxelBuffer<T> getBuffer_fromBufferGrid(@Nonnull Vector3i position_bufferGrid) {
      return (NVoxelBuffer<T>)this.bufferAccess.getBuffer(position_bufferGrid).buffer();
   }
}
