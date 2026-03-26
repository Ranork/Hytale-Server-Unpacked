package com.hypixel.hytale.builtin.hytalegenerator.patterns;

import com.hypixel.hytale.builtin.hytalegenerator.MaterialSet;
import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import com.hypixel.hytale.builtin.hytalegenerator.material.Material;
import com.hypixel.hytale.math.vector.Vector3i;
import javax.annotation.Nonnull;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class MaterialSetPattern extends Pattern {
   @Nonnull
   private static final Bounds3i BOUNDS_VOXEL_GRID = new Bounds3i(new Vector3i(), Vector3i.ALL_ONES);
   @Nonnull
   private final MaterialSet materialSet;

   public MaterialSetPattern(@Nonnull MaterialSet materialSet) {
      this.materialSet = materialSet;
   }

   @Override
   public boolean matches(@Nonnull Pattern.Context context) {
      if (!context.materialSpace.getBounds().contains(context.position)) {
         return false;
      } else {
         Material material = context.materialSpace.get(context.position);
         int hash = material.hashMaterialIds();
         return this.materialSet.test(hash);
      }
   }

   @NonNullDecl
   @Override
   public Bounds3i getBounds_voxelGrid() {
      return BOUNDS_VOXEL_GRID;
   }
}
