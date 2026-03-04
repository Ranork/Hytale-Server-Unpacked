package com.hypixel.hytale.builtin.hytalegenerator.props;

import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import com.hypixel.hytale.builtin.hytalegenerator.conveyor.stagedconveyor.ContextDependency;
import com.hypixel.hytale.builtin.hytalegenerator.datastructures.voxelspace.VoxelSpace;
import com.hypixel.hytale.builtin.hytalegenerator.material.Material;
import com.hypixel.hytale.builtin.hytalegenerator.threadindexer.WorkerIndexer;
import com.hypixel.hytale.math.vector.Vector3i;
import javax.annotation.Nonnull;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class OffsetProp extends Prop {
   @Nonnull
   private final Vector3i offset_voxelGrid;
   @Nonnull
   private final Prop childProp;
   @Nonnull
   private final Bounds3i readBounds_voxelGrid;
   @Nonnull
   private final Bounds3i writeBounds_voxelGrid;
   @Nonnull
   private final ContextDependency contextDependency;

   public OffsetProp(@Nonnull Vector3i offset_voxelGrid, @Nonnull Prop childProp) {
      this.offset_voxelGrid = offset_voxelGrid.clone();
      this.childProp = childProp;
      this.readBounds_voxelGrid = childProp.getReadBounds_voxelGrid().clone().offset(offset_voxelGrid);
      this.writeBounds_voxelGrid = childProp.getWriteBounds_voxelGrid().clone().offset(offset_voxelGrid);
      this.contextDependency = ContextDependency.from(this.readBounds_voxelGrid, this.writeBounds_voxelGrid);
   }

   @Override
   public ScanResult scan(@NonNullDecl Vector3i position_voxelGrid, @NonNullDecl VoxelSpace<Material> materialSpace, @NonNullDecl WorkerIndexer.Id id) {
      Vector3i childPosition_voxelGrid = position_voxelGrid.clone().add(this.offset_voxelGrid);
      return this.childProp.scan(childPosition_voxelGrid, materialSpace, id);
   }

   @Override
   public void place(@NonNullDecl Prop.Context context) {
      this.childProp.place(context);
   }

   @Nonnull
   @Override
   public ContextDependency getContextDependency() {
      return this.contextDependency;
   }

   @NonNullDecl
   @Override
   public Bounds3i getReadBounds_voxelGrid() {
      return this.readBounds_voxelGrid;
   }

   @NonNullDecl
   @Override
   public Bounds3i getWriteBounds_voxelGrid() {
      return this.writeBounds_voxelGrid;
   }
}
