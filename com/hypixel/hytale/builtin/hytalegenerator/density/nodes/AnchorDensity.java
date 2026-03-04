package com.hypixel.hytale.builtin.hytalegenerator.density.nodes;

import com.hypixel.hytale.builtin.hytalegenerator.density.Density;
import com.hypixel.hytale.math.vector.Vector3d;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AnchorDensity extends Density {
   @Nullable
   private Density input;
   private final boolean isReversed;
   @Nonnull
   private final Vector3d rChildPosition;
   @Nonnull
   private final Density.Context rChildContext;

   public AnchorDensity(Density input, boolean isReversed) {
      this.input = input;
      this.isReversed = isReversed;
      this.rChildPosition = new Vector3d();
      this.rChildContext = new Density.Context();
   }

   @Override
   public double process(@Nonnull Density.Context context) {
      if (context.densityAnchor == null) {
         return this.input.process(context);
      } else {
         if (this.isReversed) {
            this.rChildPosition
               .assign(context.position.x + context.densityAnchor.x, context.position.y + context.densityAnchor.y, context.position.z + context.densityAnchor.z);
         } else {
            this.rChildPosition
               .assign(context.position.x - context.densityAnchor.x, context.position.y - context.densityAnchor.y, context.position.z - context.densityAnchor.z);
         }

         this.rChildContext.assign(context);
         this.rChildContext.position = this.rChildPosition;
         return this.input.process(this.rChildContext);
      }
   }

   @Override
   public void setInputs(@Nonnull Density[] inputs) {
      if (inputs.length == 0) {
         this.input = null;
      }

      this.input = inputs[0];
   }
}
