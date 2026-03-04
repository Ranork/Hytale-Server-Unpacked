package com.hypixel.hytale.builtin.hytalegenerator.materialproviders;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TerrainDensityMaterialProvider<V> extends MaterialProvider<V> {
   @Nonnull
   private final TerrainDensityMaterialProvider.FieldDelimiter<V>[] fieldDelimiters;

   public TerrainDensityMaterialProvider(@Nonnull List<TerrainDensityMaterialProvider.FieldDelimiter<V>> delimiters) {
      this.fieldDelimiters = new TerrainDensityMaterialProvider.FieldDelimiter[delimiters.size()];

      for (TerrainDensityMaterialProvider.FieldDelimiter<V> field : delimiters) {
         if (field == null) {
            throw new IllegalArgumentException("delimiters contain null value");
         }
      }

      for (int i = 0; i < delimiters.size(); i++) {
         this.fieldDelimiters[i] = delimiters.get(i);
      }
   }

   @Nullable
   @Override
   public V getVoxelTypeAt(@Nonnull MaterialProvider.Context context) {
      for (TerrainDensityMaterialProvider.FieldDelimiter<V> delimiter : this.fieldDelimiters) {
         if (delimiter.isInside(context.density)) {
            return delimiter.materialProvider.getVoxelTypeAt(context);
         }
      }

      return null;
   }

   public static class FieldDelimiter<V> {
      double top;
      double bottom;
      MaterialProvider<V> materialProvider;

      public FieldDelimiter(@Nonnull MaterialProvider<V> materialProvider, double bottom, double top) {
         this.bottom = bottom;
         this.top = top;
         this.materialProvider = materialProvider;
      }

      boolean isInside(double fieldValue) {
         return fieldValue < this.top && fieldValue >= this.bottom;
      }
   }
}
