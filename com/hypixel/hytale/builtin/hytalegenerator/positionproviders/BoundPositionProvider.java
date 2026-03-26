package com.hypixel.hytale.builtin.hytalegenerator.positionproviders;

import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3d;
import javax.annotation.Nonnull;

public class BoundPositionProvider extends PositionProvider {
   @Nonnull
   private final PositionProvider positionProvider;
   private final Bounds3d bounds;
   @Nonnull
   private final PositionProvider.Context rChildContext;

   public BoundPositionProvider(@Nonnull PositionProvider positionProvider, @Nonnull Bounds3d bounds) {
      this.positionProvider = positionProvider;
      this.bounds = bounds;
      this.rChildContext = new PositionProvider.Context();
   }

   @Override
   public void generate(@Nonnull PositionProvider.Context context) {
      this.rChildContext.assign(context);
      this.rChildContext.bounds.assign(this.bounds);
      this.positionProvider.generate(this.rChildContext);
   }
}
