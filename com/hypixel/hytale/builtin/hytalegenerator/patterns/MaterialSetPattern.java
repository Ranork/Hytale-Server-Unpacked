package com.hypixel.hytale.builtin.hytalegenerator.patterns;

import com.hypixel.hytale.builtin.hytalegenerator.MaterialSet;
import com.hypixel.hytale.builtin.hytalegenerator.bounds.SpaceSize;
import com.hypixel.hytale.builtin.hytalegenerator.material.Material;
import com.hypixel.hytale.math.vector.Vector3i;
import javax.annotation.Nonnull;

public class MaterialSetPattern extends Pattern {
   @Nonnull
   private static final SpaceSize READ_SPACE_SIZE = new SpaceSize(new Vector3i(0, 0, 0), new Vector3i(1, 0, 1));
   @Nonnull
   private final MaterialSet materialSet;

   public MaterialSetPattern(@Nonnull MaterialSet materialSet) {
      this.materialSet = materialSet;
   }

   @Override
   public boolean matches(@Nonnull Pattern.Context context) {
      if (!context.materialSpace.isInsideSpace(context.position)) {
         return false;
      } else {
         Material material = context.materialSpace.getContent(context.position);
         int hash = material.hashMaterialIds();
         return this.materialSet.test(hash);
      }
   }

   @Nonnull
   @Override
   public SpaceSize readSpace() {
      return READ_SPACE_SIZE.clone();
   }
}
