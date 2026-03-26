package com.hypixel.hytale.builtin.hytalegenerator.props;

import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import com.hypixel.hytale.builtin.hytalegenerator.engine.entityfunnel.EntityFunnel;
import com.hypixel.hytale.builtin.hytalegenerator.material.Material;
import com.hypixel.hytale.builtin.hytalegenerator.voxelspace.NullSpace;
import com.hypixel.hytale.builtin.hytalegenerator.voxelspace.VoxelSpace;
import com.hypixel.hytale.math.vector.Vector3i;
import javax.annotation.Nonnull;

public abstract class Prop {
   public abstract boolean generate(@Nonnull Prop.Context var1);

   @Nonnull
   public abstract Bounds3i getReadBounds_voxelGrid();

   @Nonnull
   public abstract Bounds3i getWriteBounds_voxelGrid();

   public static class Context {
      @Nonnull
      public Vector3i position;
      @Nonnull
      public VoxelSpace<Material> materialReadSpace;
      @Nonnull
      public VoxelSpace<Material> materialWriteSpace;
      @Nonnull
      public EntityFunnel entityWriteBuffer;
      public double distanceToBiomeEdge;

      public Context() {
         this.position = new Vector3i();
         this.materialReadSpace = NullSpace.instance();
         this.materialWriteSpace = NullSpace.instance();
         this.entityWriteBuffer = EntityFunnel.NULL;
         this.distanceToBiomeEdge = 0.0;
      }

      public Context(
         @Nonnull Vector3i position,
         @Nonnull VoxelSpace<Material> materialReadSpace,
         @Nonnull VoxelSpace<Material> materialWriteSpace,
         @Nonnull EntityFunnel entityWriteBuffer,
         double distanceToBiomeEdge
      ) {
         this.position = position;
         this.materialReadSpace = materialReadSpace;
         this.materialWriteSpace = materialWriteSpace;
         this.entityWriteBuffer = entityWriteBuffer;
         this.distanceToBiomeEdge = distanceToBiomeEdge;
      }

      public Context(@Nonnull Prop.Context other) {
         this.position = other.position;
         this.materialReadSpace = other.materialReadSpace;
         this.materialWriteSpace = other.materialWriteSpace;
         this.entityWriteBuffer = other.entityWriteBuffer;
         this.distanceToBiomeEdge = other.distanceToBiomeEdge;
      }

      public void assign(@Nonnull Prop.Context other) {
         this.position = other.position;
         this.materialReadSpace = other.materialReadSpace;
         this.materialWriteSpace = other.materialWriteSpace;
         this.entityWriteBuffer = other.entityWriteBuffer;
         this.distanceToBiomeEdge = other.distanceToBiomeEdge;
      }
   }
}
