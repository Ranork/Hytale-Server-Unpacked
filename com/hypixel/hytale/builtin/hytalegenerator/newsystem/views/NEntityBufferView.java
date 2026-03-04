package com.hypixel.hytale.builtin.hytalegenerator.newsystem.views;

import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import com.hypixel.hytale.builtin.hytalegenerator.newsystem.GridUtils;
import com.hypixel.hytale.builtin.hytalegenerator.newsystem.bufferbundle.NBufferBundle;
import com.hypixel.hytale.builtin.hytalegenerator.newsystem.bufferbundle.buffers.NEntityBuffer;
import com.hypixel.hytale.builtin.hytalegenerator.props.entity.EntityPlacementData;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import java.util.function.Consumer;
import javax.annotation.Nonnull;

public class NEntityBufferView implements EntityContainer {
   @Nonnull
   private final NBufferBundle.Access.View access;
   @Nonnull
   private final Bounds3i bounds_voxelGrid;
   @Nonnull
   private final Bounds3i bounds_bufferGrid;

   public NEntityBufferView(@Nonnull NBufferBundle.Access.View bufferAccess) {
      this.access = bufferAccess;
      this.bounds_bufferGrid = bufferAccess.getBounds_bufferGrid();
      this.bounds_voxelGrid = bufferAccess.getBounds_bufferGrid();
      GridUtils.toVoxelGrid_fromBufferGrid(this.bounds_voxelGrid);
   }

   public void forEach(@Nonnull Consumer<EntityPlacementData> consumer) {
      Vector3i position_bufferGrid = this.bounds_voxelGrid.min.clone();
      position_bufferGrid.setX(this.bounds_bufferGrid.min.x);

      while (position_bufferGrid.x < this.bounds_bufferGrid.max.x) {
         position_bufferGrid.setZ(this.bounds_bufferGrid.min.z);

         while (position_bufferGrid.z < this.bounds_bufferGrid.max.z) {
            position_bufferGrid.setY(this.bounds_bufferGrid.min.y);

            while (position_bufferGrid.y < this.bounds_bufferGrid.max.y) {
               NEntityBuffer buffer = this.getBuffer_fromBufferGrid(position_bufferGrid);
               buffer.forEach(consumer);
               position_bufferGrid.setY(position_bufferGrid.y + 1);
            }

            position_bufferGrid.setZ(position_bufferGrid.z + 1);
         }

         position_bufferGrid.setX(position_bufferGrid.x + 1);
      }
   }

   @Nonnull
   private NEntityBuffer getBuffer_fromBufferGrid(@Nonnull Vector3i position_bufferGrid) {
      return (NEntityBuffer)this.access.getBuffer(position_bufferGrid).buffer();
   }

   public void copyFrom(@Nonnull NEntityBufferView source) {
      assert source.bounds_voxelGrid.contains(this.bounds_voxelGrid);

      Bounds3i thisBounds_bufferGrid = this.access.getBounds_bufferGrid();
      Vector3i pos_bufferGrid = new Vector3i();
      pos_bufferGrid.setX(thisBounds_bufferGrid.min.x);

      while (pos_bufferGrid.x < thisBounds_bufferGrid.max.x) {
         pos_bufferGrid.setY(thisBounds_bufferGrid.min.y);

         while (pos_bufferGrid.y < thisBounds_bufferGrid.max.y) {
            pos_bufferGrid.setZ(thisBounds_bufferGrid.min.z);

            while (pos_bufferGrid.z < thisBounds_bufferGrid.max.z) {
               NEntityBuffer sourceBuffer = source.getBuffer_fromBufferGrid(pos_bufferGrid);
               NEntityBuffer destinationBuffer = this.getBuffer_fromBufferGrid(pos_bufferGrid);
               destinationBuffer.copyFrom(sourceBuffer);
               pos_bufferGrid.setZ(pos_bufferGrid.z + 1);
            }

            pos_bufferGrid.setY(pos_bufferGrid.y + 1);
         }

         pos_bufferGrid.setX(pos_bufferGrid.x + 1);
      }
   }

   @Override
   public void addEntity(@Nonnull EntityPlacementData entityPlacementData) {
      Vector3d entityPosition_voxelGrid = entityPlacementData.getOffset().toVector3d();
      TransformComponent transform = entityPlacementData.getEntityHolder().getComponent(TransformComponent.getComponentType());

      assert transform != null;

      Vector3d holderPosition_voxelGrid = transform.getPosition();
      entityPosition_voxelGrid.add(holderPosition_voxelGrid);
      Vector3i position_bufferGrid = GridUtils.toIntegerGrid_fromDecimalGrid(entityPosition_voxelGrid);

      assert this.bounds_voxelGrid.contains(position_bufferGrid);

      GridUtils.toBufferGrid_fromVoxelGrid(position_bufferGrid);
      NEntityBuffer buffer = (NEntityBuffer)this.access.getBuffer(position_bufferGrid).buffer();
      buffer.addEntity(entityPlacementData);
   }

   @Override
   public boolean isInsideBuffer(int x, int y, int z) {
      return this.bounds_voxelGrid.contains(new Vector3i(x, y, z));
   }
}
