package com.hypixel.hytale.builtin.hytalegenerator.vectorproviders;

import javax.annotation.Nonnull;
import org.joml.Vector3d;

public class ConstantVectorProvider extends VectorProvider {
   @Nonnull
   private final Vector3d value;

   public ConstantVectorProvider(@Nonnull Vector3d value) {
      this.value = new Vector3d(value);
   }

   @Override
   public void process(@Nonnull VectorProvider.Context context, @Nonnull Vector3d vector_out) {
      vector_out.set(this.value);
   }
}
