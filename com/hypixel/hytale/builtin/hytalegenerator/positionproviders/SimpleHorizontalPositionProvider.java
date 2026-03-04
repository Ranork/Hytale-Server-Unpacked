package com.hypixel.hytale.builtin.hytalegenerator.positionproviders;

import com.hypixel.hytale.builtin.hytalegenerator.delimiters.RangeDouble;
import javax.annotation.Nonnull;

public class SimpleHorizontalPositionProvider extends PositionProvider {
   @Nonnull
   private final RangeDouble rangeY;
   @Nonnull
   private final PositionProvider positionProvider;

   public SimpleHorizontalPositionProvider(@Nonnull RangeDouble rangeY, @Nonnull PositionProvider positionProvider) {
      this.rangeY = rangeY;
      this.positionProvider = positionProvider;
   }

   @Override
   public void positionsIn(@Nonnull PositionProvider.Context context) {
      PositionProvider.Context childContext = new PositionProvider.Context(context);
      childContext.consumer = positions -> {
         if (this.rangeY.contains(positions.y)) {
            context.consumer.accept(positions);
         }
      };
      this.positionProvider.positionsIn(childContext);
   }
}
