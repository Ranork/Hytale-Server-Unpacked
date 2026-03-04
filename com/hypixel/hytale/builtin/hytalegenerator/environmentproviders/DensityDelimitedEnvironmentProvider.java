package com.hypixel.hytale.builtin.hytalegenerator.environmentproviders;

import com.hypixel.hytale.builtin.hytalegenerator.delimiters.DelimiterDouble;
import com.hypixel.hytale.builtin.hytalegenerator.delimiters.RangeDouble;
import com.hypixel.hytale.builtin.hytalegenerator.density.Density;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

public class DensityDelimitedEnvironmentProvider extends EnvironmentProvider {
   @Nonnull
   private final List<DelimiterDouble<EnvironmentProvider>> delimiters = new ArrayList<>();
   @Nonnull
   private final Density density;
   @Nonnull
   private final Density.Context rDensityContext;

   public DensityDelimitedEnvironmentProvider(@Nonnull List<DelimiterDouble<EnvironmentProvider>> delimiters, @Nonnull Density density) {
      for (DelimiterDouble<EnvironmentProvider> delimiter : delimiters) {
         RangeDouble range = delimiter.getRange();
         if (!(range.min() >= range.max())) {
            this.delimiters.add(delimiter);
         }
      }

      this.density = density;
      this.rDensityContext = new Density.Context();
   }

   @Override
   public int getValue(@Nonnull EnvironmentProvider.Context context) {
      this.rDensityContext.assign(context);
      double densityValue = this.density.process(this.rDensityContext);

      for (DelimiterDouble<EnvironmentProvider> delimiter : this.delimiters) {
         if (delimiter.getRange().contains(densityValue)) {
            return delimiter.getValue().getValue(context);
         }
      }

      return 0;
   }
}
