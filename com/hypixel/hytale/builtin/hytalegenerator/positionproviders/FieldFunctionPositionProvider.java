package com.hypixel.hytale.builtin.hytalegenerator.positionproviders;

import com.hypixel.hytale.builtin.hytalegenerator.density.Density;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

public class FieldFunctionPositionProvider extends PositionProvider {
   @Nonnull
   private final Density field;
   @Nonnull
   private final List<FieldFunctionPositionProvider.Delimiter> delimiters;
   @Nonnull
   private final PositionProvider positionProvider;

   public FieldFunctionPositionProvider(@Nonnull Density field, @Nonnull PositionProvider positionProvider) {
      this.field = field;
      this.positionProvider = positionProvider;
      this.delimiters = new ArrayList<>();
   }

   public void addDelimiter(double min, double max) {
      FieldFunctionPositionProvider.Delimiter d = new FieldFunctionPositionProvider.Delimiter();
      d.min = min;
      d.max = max;
      this.delimiters.add(d);
   }

   @Override
   public void positionsIn(@Nonnull PositionProvider.Context context) {
      PositionProvider.Context childContext = new PositionProvider.Context(context);
      childContext.consumer = p -> {
         Density.Context densityContext = new Density.Context();
         densityContext.position = p;
         densityContext.positionsAnchor = context.anchor;
         double value = this.field.process(densityContext);

         for (FieldFunctionPositionProvider.Delimiter d : this.delimiters) {
            if (d.isInside(value)) {
               context.consumer.accept(p);
               return;
            }
         }
      };
      this.positionProvider.positionsIn(childContext);
   }

   private static class Delimiter {
      double min;
      double max;

      boolean isInside(double v) {
         return v >= this.min && v < this.max;
      }
   }
}
