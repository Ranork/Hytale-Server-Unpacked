package com.hypixel.hytale.builtin.hytalegenerator.density.nodes;

import com.hypixel.hytale.builtin.hytalegenerator.density.Density;
import com.hypixel.hytale.math.vector.Vector3d;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ScaleDensity extends Density {
   @Nonnull
   private final Vector3d scale;
   private final boolean isInvalid;
   @Nullable
   private Density input;
   @Nonnull
   private final Vector3d rChildPosition;
   @Nonnull
   private final Density.Context rChildContext;

   public ScaleDensity(double scaleX, double scaleY, double scaleZ, Density input) {
      this.scale = new Vector3d(1.0 / scaleX, 1.0 / scaleY, 1.0 / scaleZ);
      this.isInvalid = scaleX == 0.0 || scaleY == 0.0 || scaleZ == 0.0;
      this.input = input;
      this.rChildPosition = new Vector3d();
      this.rChildContext = new Density.Context();
   }

   @Override
   public double process(@Nonnull Density.Context context) {
      if (this.input == null) {
         return 0.0;
      } else if (this.isInvalid) {
         return 0.0;
      } else {
         this.rChildPosition.assign(context.position);
         this.rChildPosition.scale(this.scale);
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
