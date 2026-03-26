package com.hypixel.hytale.builtin.hytalegenerator.positionproviders;

import com.hypixel.hytale.builtin.hytalegenerator.density.Density;
import com.hypixel.hytale.builtin.hytalegenerator.pipe.Control;
import com.hypixel.hytale.builtin.hytalegenerator.pipe.Pipe;
import com.hypixel.hytale.builtin.hytalegenerator.rng.RngField;
import com.hypixel.hytale.math.util.FastRandom;
import com.hypixel.hytale.math.vector.Vector3d;
import javax.annotation.Nonnull;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class FieldFunctionOccurrencePositionProvider extends PositionProvider {
   public static final double FP_RESOLUTION = 100.0;
   @Nonnull
   private final Density field;
   @Nonnull
   private final PositionProvider positionProvider;
   @Nonnull
   private final RngField rngField;
   @Nonnull
   private final FastRandom rRandom;
   @Nonnull
   private PositionProvider.Context rContext;
   @Nonnull
   private final PositionProvider.Context rChildContext;
   @Nonnull
   private final Density.Context rDensityContext;
   @Nonnull
   private final Pipe.One<Vector3d> rChildPipe = new Pipe.One<Vector3d>() {
      public void accept(@NonNullDecl Vector3d position, @NonNullDecl Control control) {
         FieldFunctionOccurrencePositionProvider.this.rDensityContext.position = position;
         FieldFunctionOccurrencePositionProvider.this.rDensityContext.positionsAnchor = FieldFunctionOccurrencePositionProvider.this.rContext.anchor;
         FieldFunctionOccurrencePositionProvider.this.rDensityContext.densityAnchor = FieldFunctionOccurrencePositionProvider.this.rContext.anchor;
         double discardChance = 1.0 - FieldFunctionOccurrencePositionProvider.this.field.process(FieldFunctionOccurrencePositionProvider.this.rDensityContext);
         FieldFunctionOccurrencePositionProvider.this.rRandom
            .setSeed(FieldFunctionOccurrencePositionProvider.this.rngField.get(position.x, position.y, position.z));
         if (!(discardChance > FieldFunctionOccurrencePositionProvider.this.rRandom.nextDouble())) {
            FieldFunctionOccurrencePositionProvider.this.rContext.pipe.accept(position, control);
         }
      }
   };

   public FieldFunctionOccurrencePositionProvider(@Nonnull Density field, @Nonnull PositionProvider positionProvider, int seed) {
      this.field = field;
      this.positionProvider = positionProvider;
      this.rngField = new RngField(seed);
      this.rChildContext = new PositionProvider.Context();
      this.rRandom = new FastRandom();
      this.rContext = new PositionProvider.Context();
      this.rDensityContext = new Density.Context();
   }

   @Override
   public void generate(@Nonnull PositionProvider.Context context) {
      this.rContext = context;
      this.rChildContext.assign(context);
      this.rChildContext.pipe = this.rChildPipe;
      this.positionProvider.generate(this.rChildContext);
   }
}
