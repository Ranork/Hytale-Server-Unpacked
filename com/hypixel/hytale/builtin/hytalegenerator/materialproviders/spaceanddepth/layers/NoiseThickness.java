package com.hypixel.hytale.builtin.hytalegenerator.materialproviders.spaceanddepth.layers;

import com.hypixel.hytale.builtin.hytalegenerator.density.Density;
import com.hypixel.hytale.builtin.hytalegenerator.materialproviders.MaterialProvider;
import com.hypixel.hytale.builtin.hytalegenerator.materialproviders.spaceanddepth.SpaceAndDepthMaterialProvider;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class NoiseThickness<V> extends SpaceAndDepthMaterialProvider.Layer<V> {
   @Nonnull
   private final Density density;
   @Nullable
   private final MaterialProvider<V> materialProvider;
   @Nonnull
   private final Density.Context rDensityContext;

   public NoiseThickness(@Nonnull Density density, @Nullable MaterialProvider<V> materialProvider) {
      this.density = density;
      this.materialProvider = materialProvider;
      this.rDensityContext = new Density.Context();
   }

   @Override
   public int getThicknessAt(
      int x, int y, int z, int depthIntoFloor, int depthIntoCeiling, int spaceAboveFloor, int spaceBelowCeiling, double distanceToBiomeEdge
   ) {
      this.rDensityContext.position.assign(x, y, z);
      this.rDensityContext.distanceToBiomeEdge = distanceToBiomeEdge;
      return (int)this.density.process(this.rDensityContext);
   }

   @Nullable
   @Override
   public MaterialProvider<V> getMaterialProvider() {
      return this.materialProvider;
   }
}
