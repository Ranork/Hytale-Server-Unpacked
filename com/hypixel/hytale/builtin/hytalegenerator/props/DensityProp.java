package com.hypixel.hytale.builtin.hytalegenerator.props;

import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import com.hypixel.hytale.builtin.hytalegenerator.density.Density;
import com.hypixel.hytale.builtin.hytalegenerator.material.Material;
import com.hypixel.hytale.builtin.hytalegenerator.materialproviders.MaterialProvider;
import com.hypixel.hytale.builtin.hytalegenerator.voxelspace.ArrayVoxelSpace;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import javax.annotation.Nonnull;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class DensityProp extends Prop {
   @Nonnull
   private final Density density;
   @Nonnull
   private final MaterialProvider<Material> materialProvider;
   @Nonnull
   private final Bounds3i writeBounds;
   @Nonnull
   private final Bounds3i solidityBufferBounds;
   @Nonnull
   private final Bounds3i rIntersectingWriteBounds;
   @Nonnull
   private final ArrayVoxelSpace<Boolean> rSolidityBuffer;
   @Nonnull
   private final Density.Context rDensityContext;
   @Nonnull
   private final MaterialProvider.Context rMaterialProviderContext;
   @Nonnull
   private final Vector3i rPosition;
   @Nonnull
   private final int[] rDepthIntoCeiling;
   @Nonnull
   private final int[] rDepthIntoFloor;
   @Nonnull
   private final int[] rSpaceBelowCeiling;
   @Nonnull
   private final int[] rSpaceAboveFloor;

   public DensityProp(@Nonnull Density density, @Nonnull MaterialProvider<Material> materialProvider, @Nonnull Bounds3i bounds) {
      this.density = density;
      this.materialProvider = materialProvider;
      this.writeBounds = bounds.clone();
      this.solidityBufferBounds = bounds.clone();
      this.solidityBufferBounds.min.y--;
      this.solidityBufferBounds.max.y++;
      this.rSolidityBuffer = new ArrayVoxelSpace<>(this.solidityBufferBounds);
      this.rIntersectingWriteBounds = new Bounds3i();
      this.rDensityContext = new Density.Context();
      this.rDensityContext.densityAnchor = new Vector3d();
      this.rMaterialProviderContext = new MaterialProvider.Context();
      this.rPosition = new Vector3i();
      int bufferHeight = this.writeBounds.max.y - this.writeBounds.min.y + 2;
      this.rDepthIntoCeiling = new int[bufferHeight + 1];
      this.rDepthIntoFloor = new int[bufferHeight + 1];
      this.rSpaceBelowCeiling = new int[bufferHeight + 1];
      this.rSpaceAboveFloor = new int[bufferHeight + 1];
   }

   @Override
   public boolean generate(@NonNullDecl Prop.Context context) {
      Bounds3i writeSpaceBounds = context.materialWriteSpace.getBounds();
      this.rIntersectingWriteBounds.assign(this.writeBounds);
      this.rIntersectingWriteBounds.offset(context.position);
      Bounds3i localSolidityBufferBounds = this.rSolidityBuffer.getBounds();
      localSolidityBufferBounds.assign(this.solidityBufferBounds);
      localSolidityBufferBounds.offset(context.position);
      this.rIntersectingWriteBounds.min.x = Math.max(this.rIntersectingWriteBounds.min.x, writeSpaceBounds.min.x);
      this.rIntersectingWriteBounds.min.z = Math.max(this.rIntersectingWriteBounds.min.z, writeSpaceBounds.min.z);
      this.rIntersectingWriteBounds.max.x = Math.min(this.rIntersectingWriteBounds.max.x, writeSpaceBounds.max.x);
      this.rIntersectingWriteBounds.max.z = Math.min(this.rIntersectingWriteBounds.max.z, writeSpaceBounds.max.z);
      this.rIntersectingWriteBounds.min.y--;
      this.rIntersectingWriteBounds.max.y++;

      assert this.rDensityContext.densityAnchor != null;

      this.rDensityContext.densityAnchor.assign(context.position);

      for (this.rPosition.x = this.rIntersectingWriteBounds.min.x; this.rPosition.x < this.rIntersectingWriteBounds.max.x; this.rPosition.x++) {
         for (this.rPosition.y = this.rIntersectingWriteBounds.min.y; this.rPosition.y < this.rIntersectingWriteBounds.max.y; this.rPosition.y++) {
            for (this.rPosition.z = this.rIntersectingWriteBounds.min.z; this.rPosition.z < this.rIntersectingWriteBounds.max.z; this.rPosition.z++) {
               this.rDensityContext.position.assign(this.rPosition);
               double densityValue = this.density.process(this.rDensityContext);
               this.rSolidityBuffer.set(densityValue > 0.0 ? Boolean.TRUE : Boolean.FALSE, this.rPosition);
            }
         }
      }

      for (this.rPosition.x = this.rIntersectingWriteBounds.min.x; this.rPosition.x < this.rIntersectingWriteBounds.max.x; this.rPosition.x++) {
         for (this.rPosition.z = this.rIntersectingWriteBounds.min.z; this.rPosition.z < this.rIntersectingWriteBounds.max.z; this.rPosition.z++) {
            for (this.rPosition.y = this.rIntersectingWriteBounds.max.y - 2; this.rPosition.y > this.rIntersectingWriteBounds.min.y; this.rPosition.y--) {
               int i = this.rPosition.y - this.rIntersectingWriteBounds.min.y;
               boolean solidity = this.rSolidityBuffer.get(this.rPosition.x, this.rPosition.y, this.rPosition.z);
               if (this.rPosition.y == this.rIntersectingWriteBounds.max.y - 1) {
                  if (solidity) {
                     this.rDepthIntoFloor[i] = 1;
                  } else {
                     this.rDepthIntoFloor[i] = 0;
                  }

                  this.rSpaceAboveFloor[i] = 1073741823;
               } else if (solidity) {
                  this.rDepthIntoFloor[i] = this.rDepthIntoFloor[i + 1] + 1;
                  this.rSpaceAboveFloor[i] = this.rSpaceAboveFloor[i + 1];
               } else {
                  this.rDepthIntoFloor[i] = 0;
                  if (this.rSolidityBuffer.get(this.rPosition.x, this.rPosition.y + 1, this.rPosition.z)) {
                     this.rSpaceAboveFloor[i] = 0;
                  } else {
                     this.rSpaceAboveFloor[i] = this.rSpaceAboveFloor[i + 1] + 1;
                  }
               }
            }

            for (this.rPosition.y = this.rIntersectingWriteBounds.min.y + 1; this.rPosition.y < this.rIntersectingWriteBounds.max.y - 1; this.rPosition.y++) {
               int i = this.rPosition.y - this.rIntersectingWriteBounds.min.y;
               boolean solidity = this.rSolidityBuffer.get(this.rPosition.x, this.rPosition.y, this.rPosition.z);
               if (this.rPosition.y == this.rIntersectingWriteBounds.min.x) {
                  if (solidity) {
                     this.rDepthIntoCeiling[i] = 1;
                  } else {
                     this.rDepthIntoCeiling[i] = 0;
                  }

                  this.rSpaceBelowCeiling[i] = Integer.MAX_VALUE;
               } else if (solidity) {
                  this.rDepthIntoCeiling[i] = this.rDepthIntoCeiling[i - 1] + 1;
                  this.rSpaceBelowCeiling[i] = this.rSpaceBelowCeiling[i - 1];
               } else {
                  this.rDepthIntoCeiling[i] = 0;
                  if (this.rSolidityBuffer.get(this.rPosition.x, this.rPosition.y - 1, this.rPosition.z)) {
                     this.rSpaceBelowCeiling[i] = 0;
                  } else {
                     this.rSpaceBelowCeiling[i] = this.rSpaceBelowCeiling[i - 1] + 1;
                  }
               }
            }

            for (this.rPosition.y = this.rIntersectingWriteBounds.max.y - 2; this.rPosition.y > this.rIntersectingWriteBounds.min.x; this.rPosition.y--) {
               if (this.rIntersectingWriteBounds.contains(this.rPosition)) {
                  int i = this.rPosition.y - this.rIntersectingWriteBounds.min.y;
                  this.rMaterialProviderContext.position.assign(this.rPosition);
                  this.rMaterialProviderContext.depthIntoFloor = this.rDepthIntoFloor[i];
                  this.rMaterialProviderContext.depthIntoCeiling = this.rDepthIntoCeiling[i];
                  this.rMaterialProviderContext.spaceAboveFloor = this.rSpaceAboveFloor[i];
                  this.rMaterialProviderContext.spaceBelowCeiling = this.rSpaceBelowCeiling[i];
                  this.rMaterialProviderContext.distanceToBiomeEdge = context.distanceToBiomeEdge;
                  Material material = this.materialProvider.getVoxelTypeAt(this.rMaterialProviderContext);
                  if (material != null && context.materialWriteSpace.getBounds().contains(this.rPosition)) {
                     context.materialWriteSpace.set(material, this.rPosition);
                  }
               }
            }
         }
      }

      return true;
   }

   @NonNullDecl
   @Override
   public Bounds3i getReadBounds_voxelGrid() {
      return Bounds3i.ZERO;
   }

   @NonNullDecl
   @Override
   public Bounds3i getWriteBounds_voxelGrid() {
      return this.writeBounds;
   }
}
