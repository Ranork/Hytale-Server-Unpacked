package com.hypixel.hytale.builtin.hytalegenerator.material;

import java.util.Objects;
import javax.annotation.Nonnull;

public class FluidMaterial {
   @Nonnull
   private final MaterialCache materialCache;
   public final int fluidId;
   public final byte fluidLevel;

   FluidMaterial(@Nonnull MaterialCache materialCache, int fluidId, byte fluidLevel) {
      this.materialCache = materialCache;
      this.fluidId = fluidId;
      this.fluidLevel = fluidLevel;
   }

   @Nonnull
   public MaterialCache getVoxelCache() {
      return this.materialCache;
   }

   @Override
   public final boolean equals(Object o) {
      return !(o instanceof FluidMaterial that)
         ? false
         : this.fluidId == that.fluidId && this.fluidLevel == that.fluidLevel && this.materialCache.equals(that.materialCache);
   }

   @Override
   public int hashCode() {
      return contentHash(this.fluidId, this.fluidLevel);
   }

   public static int contentHash(int blockId, byte fluidLevel) {
      return Objects.hash(blockId, fluidLevel);
   }

   @Nonnull
   @Override
   public String toString() {
      return "FluidMaterial{materialCache=" + this.materialCache + ", fluidId=" + this.fluidId + ", fluidLevel=" + this.fluidLevel + "}";
   }
}
