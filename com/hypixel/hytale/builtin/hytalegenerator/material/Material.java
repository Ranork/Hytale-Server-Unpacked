package com.hypixel.hytale.builtin.hytalegenerator.material;

import java.util.Objects;
import javax.annotation.Nonnull;

public final class Material {
   @Nonnull
   private final SolidMaterial solid;
   @Nonnull
   private final FluidMaterial fluid;
   private Material.Hash hashCode;
   private Material.Hash materialIdsHash;

   public Material(@Nonnull SolidMaterial solid, @Nonnull FluidMaterial fluid) {
      this.solid = solid;
      this.fluid = fluid;
      this.hashCode = new Material.Hash();
      this.materialIdsHash = new Material.Hash();
   }

   @Override
   public boolean equals(Object o) {
      return !(o instanceof Material material) ? false : Objects.equals(this.solid, material.solid) && Objects.equals(this.fluid, material.fluid);
   }

   @Override
   public int hashCode() {
      if (this.hashCode.isCalculated) {
         return this.hashCode.value;
      } else {
         this.hashCode.value = hashCode(this.solid, this.fluid);
         this.hashCode.isCalculated = true;
         return this.hashCode.value;
      }
   }

   public int hashMaterialIds() {
      if (this.materialIdsHash.isCalculated) {
         return this.materialIdsHash.value;
      } else {
         this.materialIdsHash.value = hashMaterialIds(this.solid, this.fluid);
         this.materialIdsHash.isCalculated = true;
         return this.materialIdsHash.value;
      }
   }

   public static int hashCode(@Nonnull SolidMaterial solid, @Nonnull FluidMaterial fluid) {
      int result = solid.hashCode();
      return 31 * result + fluid.hashCode();
   }

   public static int hashMaterialIds(@Nonnull SolidMaterial solid, @Nonnull FluidMaterial fluid) {
      return Objects.hash(solid.blockId, fluid.fluidId);
   }

   @Nonnull
   public SolidMaterial solid() {
      return this.solid;
   }

   @Nonnull
   public FluidMaterial fluid() {
      return this.fluid;
   }

   @Nonnull
   @Override
   public String toString() {
      return "Material[solid=" + this.solid + ", fluid=" + this.fluid + "]";
   }

   private class Hash {
      int value = 0;
      boolean isCalculated = false;
   }
}
