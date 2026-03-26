package com.hypixel.hytale.builtin.hytalegenerator.positionproviders;

import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3d;
import com.hypixel.hytale.builtin.hytalegenerator.pipe.Control;
import com.hypixel.hytale.builtin.hytalegenerator.pipe.Pipe;
import com.hypixel.hytale.math.vector.Vector3d;
import javax.annotation.Nonnull;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class ScalerPositionProvider extends PositionProvider {
   @Nonnull
   private final Vector3d scale;
   @Nonnull
   private final Vector3d inverseScale;
   @Nonnull
   private final PositionProvider positionProvider;
   @Nonnull
   private final PositionProvider.Context rChildContext;
   @Nonnull
   private final Bounds3d rChildBounds;
   @Nonnull
   private PositionProvider.Context rContext;
   @Nonnull
   private final Pipe.One<Vector3d> rChildPipe = new Pipe.One<Vector3d>() {
      public void accept(@NonNullDecl Vector3d position, @NonNullDecl Control control) {
         position.scale(ScalerPositionProvider.this.scale);
         ScalerPositionProvider.this.rContext.pipe.accept(position, control);
      }
   };

   public ScalerPositionProvider(@Nonnull Vector3d scale, @Nonnull PositionProvider positionProvider) {
      this.scale = scale.clone();
      this.inverseScale = new Vector3d(1.0 / scale.x, 1.0 / scale.y, 1.0 / scale.z);
      this.positionProvider = positionProvider;
      this.rChildContext = new PositionProvider.Context();
      this.rChildBounds = new Bounds3d();
      this.rContext = new PositionProvider.Context();
   }

   @Override
   public void generate(@NonNullDecl PositionProvider.Context context) {
      this.rContext = context;
      this.rChildBounds.assign(context.bounds);
      this.rChildBounds.min.scale(this.inverseScale);
      this.rChildBounds.max.scale(this.inverseScale);
      this.rChildContext.assign(context);
      this.rChildContext.bounds = this.rChildBounds;
      this.rChildContext.pipe = this.rChildPipe;
      this.positionProvider.generate(this.rChildContext);
   }
}
