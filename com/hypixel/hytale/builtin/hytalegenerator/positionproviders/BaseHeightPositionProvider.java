package com.hypixel.hytale.builtin.hytalegenerator.positionproviders;

import com.hypixel.hytale.builtin.hytalegenerator.pipe.Control;
import com.hypixel.hytale.builtin.hytalegenerator.pipe.Pipe;
import com.hypixel.hytale.math.vector.Vector3d;
import javax.annotation.Nonnull;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class BaseHeightPositionProvider extends PositionProvider {
   @Nonnull
   private final double baseHeight;
   private final double maxYInput;
   private final double minYInput;
   @Nonnull
   private final PositionProvider positionProvider;
   @Nonnull
   private final Vector3d rOffsetPosition;
   @Nonnull
   private PositionProvider.Context rContext;
   @Nonnull
   private final PositionProvider.Context rChildContext;
   @Nonnull
   private final Pipe.One<Vector3d> rChildPipe = new Pipe.One<Vector3d>() {
      public void accept(@NonNullDecl Vector3d position, @NonNullDecl Control control) {
         BaseHeightPositionProvider.this.rOffsetPosition.assign(position);
         BaseHeightPositionProvider.this.rOffsetPosition.y = BaseHeightPositionProvider.this.rOffsetPosition.y + BaseHeightPositionProvider.this.baseHeight;
         if (BaseHeightPositionProvider.this.rContext.bounds.contains(BaseHeightPositionProvider.this.rOffsetPosition)) {
            BaseHeightPositionProvider.this.rContext.pipe.accept(BaseHeightPositionProvider.this.rOffsetPosition, control);
         }
      }
   };

   public BaseHeightPositionProvider(double baseHeight, @Nonnull PositionProvider positionProvider, double minYInput, double maxYInput) {
      maxYInput = Math.max(minYInput, maxYInput);
      this.baseHeight = baseHeight;
      this.positionProvider = positionProvider;
      this.maxYInput = maxYInput;
      this.minYInput = minYInput;
      this.rOffsetPosition = new Vector3d();
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
