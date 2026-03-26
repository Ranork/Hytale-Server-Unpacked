package com.hypixel.hytale.builtin.hytalegenerator.patterns;

import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import com.hypixel.hytale.builtin.hytalegenerator.material.Material;
import com.hypixel.hytale.math.vector.Vector3i;
import javax.annotation.Nonnull;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class MaterialPattern extends Pattern {
   @Nonnull
   private static final Bounds3i BOUNDS = new Bounds3i(Vector3i.ZERO, Vector3i.ALL_ONES);
   @Nonnull
   private final Material material;

   public MaterialPattern(@Nonnull Material material) {
      this.material = material;
   }

   @Override
   public boolean matches(@Nonnull Pattern.Context context) {
      if (!context.materialSpace.getBounds().contains(context.position)) {
         return false;
      } else {
         Material material = context.materialSpace.get(context.position);
         return this.material.solid().blockId == material.solid().blockId && this.material.fluid().fluidId == material.fluid().fluidId;
      }
   }

   @NonNullDecl
   @Override
   public Bounds3i getBounds_voxelGrid() {
      return BOUNDS;
   }
}
