package com.hypixel.hytale.builtin.hytalegenerator.vectorproviders;

import com.hypixel.hytale.math.vector.Vector3d;
import javax.annotation.Nonnull;

public class ConstantVectorProvider extends VectorProvider {
   @Nonnull
   private final Vector3d value;

   public ConstantVectorProvider(@Nonnull Vector3d value) {
      this.value = value.clone();
   }

   @Override
   public void process(@Nonnull VectorProvider.Context context, @Nonnull Vector3d vector_out) {
      vector_out.assign(this.value);
   }
}
