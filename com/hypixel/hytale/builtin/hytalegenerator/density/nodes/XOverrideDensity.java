package com.hypixel.hytale.builtin.hytalegenerator.density.nodes;

import com.hypixel.hytale.builtin.hytalegenerator.density.Density;
import com.hypixel.hytale.math.vector.Vector3d;
import javax.annotation.Nonnull;

public class XOverrideDensity extends Density {
   @Nonnull
   private Density input;
   private final double value;
   @Nonnull
   private final Density.Context rChildContext;
   @Nonnull
   private final Vector3d rChildPosition;

   public XOverrideDensity(@Nonnull Density input, double value) {
      this.input = input;
      this.value = value;
      this.rChildContext = new Density.Context();
      this.rChildPosition = new Vector3d();
   }

   @Override
   public double process(@Nonnull Density.Context context) {
      this.rChildPosition.assign(this.value, context.position.y, context.position.z);
      this.rChildContext.assign(context);
      this.rChildContext.position = this.rChildPosition;
      return this.input.process(this.rChildContext);
   }

   @Override
   public void setInputs(@Nonnull Density[] inputs) {
      assert inputs.length == 1;

      assert inputs[0] != null;

      this.input = inputs[0];
   }
}
