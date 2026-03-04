package com.hypixel.hytale.builtin.hytalegenerator.positionproviders;

import com.hypixel.hytale.builtin.hytalegenerator.density.Density;
import com.hypixel.hytale.builtin.hytalegenerator.framework.math.SeedGenerator;
import com.hypixel.hytale.math.util.FastRandom;
import javax.annotation.Nonnull;

public class FieldFunctionOccurrencePositionProvider extends PositionProvider {
   public static final double FP_RESOLUTION = 100.0;
   @Nonnull
   private final Density field;
   @Nonnull
   private final PositionProvider positionProvider;
   @Nonnull
   private final SeedGenerator seedGenerator;

   public FieldFunctionOccurrencePositionProvider(@Nonnull Density field, @Nonnull PositionProvider positionProvider, int seed) {
      this.field = field;
      this.positionProvider = positionProvider;
      this.seedGenerator = new SeedGenerator(seed);
   }

   @Override
   public void positionsIn(@Nonnull PositionProvider.Context context) {
      PositionProvider.Context childContext = new PositionProvider.Context(context);
      childContext.consumer = position -> {
         Density.Context densityContext = new Density.Context();
         densityContext.position = position;
         densityContext.positionsAnchor = context.anchor;
         double discardChance = 1.0 - this.field.process(densityContext);
         FastRandom random = new FastRandom(this.seedGenerator.seedAt(position.x, position.y, position.z, 100.0));
         if (!(discardChance > random.nextDouble())) {
            context.consumer.accept(position);
         }
      };
      this.positionProvider.positionsIn(childContext);
   }
}
