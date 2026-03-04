package com.hypixel.hytale.builtin.hytalegenerator.materialproviders.spaceanddepth.layers;

import com.hypixel.hytale.builtin.hytalegenerator.materialproviders.MaterialProvider;
import com.hypixel.hytale.builtin.hytalegenerator.materialproviders.spaceanddepth.SpaceAndDepthMaterialProvider;
import javax.annotation.Nullable;

public class ConstantThicknessLayer<V> extends SpaceAndDepthMaterialProvider.Layer<V> {
   private final int thickness;
   @Nullable
   private final MaterialProvider<V> materialProvider;

   public ConstantThicknessLayer(int thickness, @Nullable MaterialProvider<V> materialProvider) {
      this.thickness = thickness;
      this.materialProvider = materialProvider;
   }

   @Override
   public int getThicknessAt(
      int x, int y, int z, int depthIntoFloor, int depthIntoCeiling, int spaceAboveFloor, int spaceBelowCeiling, double distanceTOBiomeEdge
   ) {
      return this.thickness;
   }

   @Nullable
   @Override
   public MaterialProvider<V> getMaterialProvider() {
      return this.materialProvider;
   }
}
