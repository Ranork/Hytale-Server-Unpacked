package com.hypixel.hytale.builtin.hytalegenerator.patterns;

import com.hypixel.hytale.builtin.hytalegenerator.bounds.SpaceSize;
import com.hypixel.hytale.builtin.hytalegenerator.density.Density;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

public class FieldFunctionPattern extends Pattern {
   @Nonnull
   private final Density field;
   @Nonnull
   private final SpaceSize readSpaceSize;
   @Nonnull
   private final List<FieldFunctionPattern.Delimiter> delimiters;
   @Nonnull
   private final Density.Context rDensityContext;

   public FieldFunctionPattern(@Nonnull Density field) {
      this.field = field;
      this.readSpaceSize = SpaceSize.empty();
      this.delimiters = new ArrayList<>(1);
      this.rDensityContext = new Density.Context();
   }

   @Override
   public boolean matches(@Nonnull Pattern.Context context) {
      this.rDensityContext.assign(context);
      double density = this.field.process(this.rDensityContext);

      for (FieldFunctionPattern.Delimiter d : this.delimiters) {
         if (d.isInside(density)) {
            return true;
         }
      }

      return false;
   }

   @Nonnull
   @Override
   public SpaceSize readSpace() {
      return this.readSpaceSize.clone();
   }

   public void addDelimiter(double min, double max) {
      FieldFunctionPattern.Delimiter d = new FieldFunctionPattern.Delimiter();
      d.min = min;
      d.max = max;
      this.delimiters.add(d);
   }

   private static class Delimiter {
      double min;
      double max;

      boolean isInside(double v) {
         return v >= this.min && v < this.max;
      }
   }
}
