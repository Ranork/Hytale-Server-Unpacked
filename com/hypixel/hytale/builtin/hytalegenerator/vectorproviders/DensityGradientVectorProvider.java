package com.hypixel.hytale.builtin.hytalegenerator.vectorproviders;

import com.hypixel.hytale.builtin.hytalegenerator.density.Density;
import javax.annotation.Nonnull;
import org.joml.Vector3d;

public class DensityGradientVectorProvider extends VectorProvider {
   @Nonnull
   private final Density density;
   private final double sampleDistance;
   @Nonnull
   private final Density.Context rChildContext;
   @Nonnull
   private final Vector3d rPosition;

   public DensityGradientVectorProvider(@Nonnull Density density, double sampleDistance) {
      assert sampleDistance >= 0.0;

      this.density = density;
      this.sampleDistance = Math.max(0.0, sampleDistance);
      this.rChildContext = new Density.Context();
      this.rPosition = new Vector3d();
   }

   @Override
   public void process(@Nonnull VectorProvider.Context context, @Nonnull Vector3d vector_out) {
      this.rPosition.set(context.position);
      this.rChildContext.assign(context);
      this.rChildContext.position = this.rPosition;
      double valueAtOrigin = this.density.process(this.rChildContext);
      double maxX = context.position.x + this.sampleDistance;
      double maxY = context.position.y + this.sampleDistance;
      double maxZ = context.position.z + this.sampleDistance;
      this.rChildContext.position.set(maxX, context.position.y, context.position.z);
      double deltaX = this.density.process(this.rChildContext) - valueAtOrigin;
      this.rChildContext.position.set(context.position.x, maxY, context.position.z);
      double deltaY = this.density.process(this.rChildContext) - valueAtOrigin;
      this.rChildContext.position.set(context.position.x, context.position.y, maxZ);
      double deltaZ = this.density.process(this.rChildContext) - valueAtOrigin;
      vector_out.set(deltaX, deltaY, deltaZ);
   }
}
