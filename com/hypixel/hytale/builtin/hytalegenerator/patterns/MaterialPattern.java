package com.hypixel.hytale.builtin.hytalegenerator.patterns;

import com.hypixel.hytale.builtin.hytalegenerator.bounds.SpaceSize;
import com.hypixel.hytale.builtin.hytalegenerator.material.Material;
import com.hypixel.hytale.math.vector.Vector3i;
import javax.annotation.Nonnull;

public class MaterialPattern extends Pattern {
   @Nonnull
   private static final SpaceSize READ_SPACE_SIZE = new SpaceSize(new Vector3i(0, 0, 0), new Vector3i(1, 0, 1));
   @Nonnull
   private final Material material;

   public MaterialPattern(@Nonnull Material material) {
      this.material = material;
   }

   @Override
   public boolean matches(@Nonnull Pattern.Context context) {
      if (!context.materialSpace.isInsideSpace(context.position)) {
         return false;
      } else {
         Material material = context.materialSpace.getContent(context.position);
         return this.material.solid().blockId == material.solid().blockId && this.material.fluid().fluidId == material.fluid().fluidId;
      }
   }

   @Nonnull
   @Override
   public SpaceSize readSpace() {
      return READ_SPACE_SIZE.clone();
   }
}
