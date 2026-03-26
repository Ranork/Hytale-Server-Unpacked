package com.hypixel.hytale.builtin.hytalegenerator.positionproviders;

import com.hypixel.hytale.builtin.hytalegenerator.delimiters.RangeDouble;
import com.hypixel.hytale.builtin.hytalegenerator.pipe.Control;
import com.hypixel.hytale.builtin.hytalegenerator.pipe.Pipe;
import com.hypixel.hytale.math.vector.Vector3d;
import javax.annotation.Nonnull;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class SimpleHorizontalPositionProvider extends PositionProvider {
   @Nonnull
   private final RangeDouble rangeY;
   @Nonnull
   private final PositionProvider positionProvider;
   @Nonnull
   private final PositionProvider.Context rChildContext;
   @Nonnull
   private PositionProvider.Context rContext;
   @Nonnull
   private final Pipe.One<Vector3d> rChildPipe = new Pipe.One<Vector3d>() {
      public void accept(@NonNullDecl Vector3d position, @NonNullDecl Control control) {
         if (SimpleHorizontalPositionProvider.this.rangeY.contains(position.y)) {
            SimpleHorizontalPositionProvider.this.rContext.pipe.accept(position, control);
         }
      }
   };

   public SimpleHorizontalPositionProvider(@Nonnull RangeDouble rangeY, @Nonnull PositionProvider positionProvider) {
      this.rangeY = rangeY;
      this.positionProvider = positionProvider;
      this.rContext = new PositionProvider.Context();
      this.rChildContext = new PositionProvider.Context();
   }

   @Override
   public void generate(@Nonnull PositionProvider.Context context) {
      this.rContext = context;
      this.rChildContext.assign(context);
      this.rChildContext.pipe = this.rChildPipe;
      this.positionProvider.generate(this.rChildContext);
   }
}
