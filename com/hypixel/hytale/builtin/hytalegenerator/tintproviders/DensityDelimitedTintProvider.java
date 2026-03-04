package com.hypixel.hytale.builtin.hytalegenerator.tintproviders;

import com.hypixel.hytale.builtin.hytalegenerator.delimiters.DelimiterDouble;
import com.hypixel.hytale.builtin.hytalegenerator.delimiters.RangeDouble;
import com.hypixel.hytale.builtin.hytalegenerator.density.Density;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

public class DensityDelimitedTintProvider extends TintProvider {
   @Nonnull
   private final List<DelimiterDouble<TintProvider>> delimiters = new ArrayList<>();
   @Nonnull
   private final Density density;

   public DensityDelimitedTintProvider(@Nonnull List<DelimiterDouble<TintProvider>> delimiters, @Nonnull Density density) {
      for (DelimiterDouble<TintProvider> delimiter : delimiters) {
         RangeDouble range = delimiter.getRange();
         if (!(range.min() >= range.max())) {
            this.delimiters.add(delimiter);
         }
      }

      this.density = density;
   }

   @Override
   public TintProvider.Result getValue(@Nonnull TintProvider.Context context) {
      double densityValue = this.density.process(new Density.Context(context));

      for (DelimiterDouble<TintProvider> delimiter : this.delimiters) {
         if (delimiter.getRange().contains(densityValue)) {
            return delimiter.getValue().getValue(context);
         }
      }

      return TintProvider.Result.WITHOUT_VALUE;
   }
}
