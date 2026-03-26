package com.hypixel.hytale.builtin.hytalegenerator.positionproviders;

import com.hypixel.hytale.builtin.hytalegenerator.density.Density;
import com.hypixel.hytale.builtin.hytalegenerator.pipe.Control;
import com.hypixel.hytale.builtin.hytalegenerator.pipe.Pipe;
import com.hypixel.hytale.math.vector.Vector3d;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class FieldFunctionPositionProvider extends PositionProvider {
   @Nonnull
   private final Density field;
   @Nonnull
   private final List<FieldFunctionPositionProvider.Delimiter> delimiters;
   @Nonnull
   private final PositionProvider positionProvider;
   @Nonnull
   private final PositionProvider.Context rChildContext;
   @Nonnull
   private final Density.Context rDensityContext;
   @Nonnull
   private PositionProvider.Context rContext;
   @Nonnull
   private final Pipe.One<Vector3d> rChildPipe = new Pipe.One<Vector3d>() {
      public void accept(@NonNullDecl Vector3d position, @NonNullDecl Control control) {
         FieldFunctionPositionProvider.this.rDensityContext.position = position;
         FieldFunctionPositionProvider.this.rDensityContext.positionsAnchor = FieldFunctionPositionProvider.this.rContext.anchor;
         double value = FieldFunctionPositionProvider.this.field.process(FieldFunctionPositionProvider.this.rDensityContext);

         for (FieldFunctionPositionProvider.Delimiter delimiter : FieldFunctionPositionProvider.this.delimiters) {
            if (delimiter.isInside(value)) {
               FieldFunctionPositionProvider.this.rContext.pipe.accept(position, control);
               return;
            }
         }
      }
   };

   public FieldFunctionPositionProvider(@Nonnull Density field, @Nonnull PositionProvider positionProvider) {
      this.field = field;
      this.positionProvider = positionProvider;
      this.delimiters = new ArrayList<>();
      this.rChildContext = new PositionProvider.Context();
      this.rDensityContext = new Density.Context();
      this.rContext = new PositionProvider.Context();
   }

   @Override
   public void generate(@Nonnull PositionProvider.Context context) {
      this.rContext = context;
      this.rChildContext.assign(context);
      this.rChildContext.pipe = this.rChildPipe;
      this.positionProvider.generate(this.rChildContext);
   }

   public void addDelimiter(double min, double max) {
      FieldFunctionPositionProvider.Delimiter d = new FieldFunctionPositionProvider.Delimiter();
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
