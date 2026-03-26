package com.hypixel.hytale.builtin.hytalegenerator.positionproviders;

import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3d;
import com.hypixel.hytale.builtin.hytalegenerator.pipe.Control;
import com.hypixel.hytale.builtin.hytalegenerator.pipe.Pipe;
import com.hypixel.hytale.math.vector.Vector3d;
import javax.annotation.Nonnull;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class OffsetPositionProvider extends PositionProvider {
   @Nonnull
   private final Vector3d vector;
   @Nonnull
   private final PositionProvider positionProvider;
   @Nonnull
   private final Bounds3d rBounds;
   @Nonnull
   private final PositionProvider.Context rChildContext;
   @Nonnull
   private PositionProvider.Context rContext;
   @Nonnull
   private final Pipe.One<Vector3d> rChildPipe = new Pipe.One<Vector3d>() {
      public void accept(@NonNullDecl Vector3d position, @NonNullDecl Control control) {
         position.add(OffsetPositionProvider.this.vector);
         OffsetPositionProvider.this.rContext.pipe.accept(position, control);
      }
   };

   public OffsetPositionProvider(@Nonnull Vector3d vector, @Nonnull PositionProvider positionProvider) {
      this.vector = vector.clone();
      this.positionProvider = positionProvider;
      this.rBounds = new Bounds3d();
      this.rChildContext = new PositionProvider.Context();
      this.rContext = new PositionProvider.Context();
   }

   @Override
   public void generate(@Nonnull PositionProvider.Context context) {
      this.rContext = context;
      this.rBounds.assign(context.bounds);
      this.rBounds.offsetOpposite(this.vector);
      this.rChildContext.assign(context);
      this.rChildContext.bounds = this.rBounds;
      this.rChildContext.pipe = this.rChildPipe;
      this.positionProvider.generate(this.rChildContext);
   }
}
