package com.hypixel.hytale.builtin.hytalegenerator.positionproviders;

import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import javax.annotation.Nonnull;

public class OffsetPositionProvider extends PositionProvider {
   @Nonnull
   private final Vector3i offset3i;
   @Nonnull
   private final Vector3d offset3d;
   @Nonnull
   private final PositionProvider positionProvider;

   public OffsetPositionProvider(@Nonnull Vector3i offset, @Nonnull PositionProvider positionProvider) {
      this.offset3i = offset.clone();
      this.positionProvider = positionProvider;
      this.offset3d = this.offset3i.toVector3d();
   }

   public OffsetPositionProvider(@Nonnull Vector3d offset, @Nonnull PositionProvider positionProvider) {
      this.offset3d = offset.clone();
      this.positionProvider = positionProvider;
      this.offset3i = this.offset3d.toVector3i();
   }

   @Override
   public void positionsIn(@Nonnull PositionProvider.Context context) {
      Vector3d windowMin = context.minInclusive.clone();
      Vector3d windowMax = context.maxExclusive.clone();
      windowMin.subtract(this.offset3d);
      windowMax.subtract(this.offset3d);
      PositionProvider.Context childContext = new PositionProvider.Context();
      childContext.minInclusive = windowMin;
      childContext.maxExclusive = windowMax;
      childContext.consumer = p -> {
         Vector3d offsetP = p.clone();
         offsetP.add(this.offset3d);
         context.consumer.accept(offsetP);
      };
      this.positionProvider.positionsIn(childContext);
   }
}
