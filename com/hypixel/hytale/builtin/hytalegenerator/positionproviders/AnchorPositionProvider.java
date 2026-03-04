package com.hypixel.hytale.builtin.hytalegenerator.positionproviders;

import com.hypixel.hytale.builtin.hytalegenerator.VectorUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import javax.annotation.Nonnull;

public class AnchorPositionProvider extends PositionProvider {
   @Nonnull
   private final PositionProvider positionProvider;
   private final boolean isReversed;

   public AnchorPositionProvider(@Nonnull PositionProvider positionProvider, boolean isReversed) {
      this.positionProvider = positionProvider;
      this.isReversed = isReversed;
   }

   @Override
   public void positionsIn(@Nonnull PositionProvider.Context context) {
      if (context != null) {
         Vector3d anchor = context.anchor;
         if (anchor != null) {
            Vector3d offsetMin = this.isReversed ? context.minInclusive.clone().add(anchor) : context.minInclusive.clone().addScaled(anchor, -1.0);
            Vector3d offsetMax = this.isReversed ? context.maxExclusive.clone().add(anchor) : context.maxExclusive.clone().addScaled(anchor, -1.0);
            PositionProvider.Context childContext = new PositionProvider.Context(offsetMin, offsetMax, p -> {
               Vector3d newPoint = p.clone();
               if (this.isReversed) {
                  newPoint.addScaled(anchor, -1.0);
               } else {
                  newPoint.add(anchor);
               }

               if (VectorUtil.isInside(newPoint, context.minInclusive, context.maxExclusive)) {
                  context.consumer.accept(newPoint);
               }
            }, context.anchor);
            this.positionProvider.positionsIn(childContext);
         }
      }
   }
}
