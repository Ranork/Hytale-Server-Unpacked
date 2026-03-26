package com.hypixel.hytale.builtin.hytalegenerator.voxelspace;

import com.hypixel.hytale.builtin.hytalegenerator.BlockMask;
import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import com.hypixel.hytale.builtin.hytalegenerator.material.Material;
import com.hypixel.hytale.math.vector.Vector3i;
import javax.annotation.Nonnull;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

public class MaskVoxelSpace implements VoxelSpace<Material> {
   @Nonnull
   private final BlockMask mask;
   @Nonnull
   private VoxelSpace<Material> source;

   public MaskVoxelSpace(@Nonnull BlockMask mask, @Nonnull VoxelSpace<Material> source) {
      this.mask = mask;
      this.source = source;
   }

   public void setSource(@Nonnull VoxelSpace<Material> source) {
      this.source = source;
   }

   public void set(@NullableDecl Material content, int x, int y, int z) {
      assert this.source.getBounds().contains(x, y, z);

      if (this.mask.canPlace(content.hashMaterialIds())) {
         Material existingMaterial = this.source.get(x, y, z);
         if (this.mask.canReplace(content.hashMaterialIds(), existingMaterial.hashMaterialIds())) {
            this.source.set(content, x, y, z);
         }
      }
   }

   public void set(@NullableDecl Material content, @NonNullDecl Vector3i position) {
      this.set(content, position.x, position.y, position.z);
   }

   public void setAll(@NullableDecl Material content) {
      Bounds3i bounds = this.source.getBounds();

      for (int x = bounds.min.x; x < bounds.max.x; x++) {
         for (int z = bounds.min.z; z < bounds.max.z; z++) {
            for (int y = bounds.min.y; y < bounds.max.y; y++) {
               this.set(content, x, y, z);
            }
         }
      }
   }

   @NullableDecl
   public Material get(int x, int y, int z) {
      return this.source.get(x, y, z);
   }

   @NullableDecl
   public Material get(@NonNullDecl Vector3i position) {
      return this.source.get(position);
   }

   @NonNullDecl
   @Override
   public Bounds3i getBounds() {
      return this.source.getBounds();
   }
}
