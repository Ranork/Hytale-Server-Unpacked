package com.hypixel.hytale.builtin.hytalegenerator.density.nodes;

import com.hypixel.hytale.builtin.hytalegenerator.density.Density;
import com.hypixel.hytale.builtin.hytalegenerator.fields.FastNoiseLite;
import com.hypixel.hytale.math.vector.Vector3d;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class FastGradientWarpDensity extends Density {
   private static final double HALF_PI = Math.PI / 2;
   @Nullable
   private Density input;
   private final double warpScale;
   @Nonnull
   private final FastNoiseLite warper;
   @Nonnull
   private final FastNoiseLite.Vector3 rWarpedPosition;
   @Nonnull
   private final Density.Context rChildContext;
   @Nonnull
   private final Vector3d rPosition;

   public FastGradientWarpDensity(
      @Nonnull Density input, float warpLacunarity, float warpPersistence, int warpOctaves, float warpScale, float warpFactor, int seed
   ) {
      if (warpOctaves < 0.0) {
         throw new IllegalArgumentException();
      } else {
         this.warpScale = warpScale;
         this.input = input;
         this.warper = new FastNoiseLite();
         this.warper.setSeed(seed);
         this.warper.SetFractalGain(warpPersistence);
         this.warper.SetFractalLacunarity(warpLacunarity);
         this.warper.setDomainWarpType(FastNoiseLite.DomainWarpType.OpenSimplex2);
         this.warper.setFractalOctaves(warpOctaves);
         this.warper.setDomainWarpAmp(warpFactor);
         this.warper.setDomainWarpFreq(warpScale);
         this.rWarpedPosition = new FastNoiseLite.Vector3(0.0, 0.0, 0.0);
         this.rChildContext = new Density.Context();
         this.rPosition = new Vector3d();
      }
   }

   @Override
   public double process(@Nonnull Density.Context context) {
      if (this.input == null) {
         return 0.0;
      } else {
         this.rWarpedPosition.x = context.position.x;
         this.rWarpedPosition.y = context.position.y;
         this.rWarpedPosition.z = context.position.z;
         this.warper.DomainWarpFractalProgressive(this.rWarpedPosition);
         this.rPosition.assign(context.position);
         this.rChildContext.assign(context);
         this.rChildContext.position = this.rPosition;
         this.rChildContext.position.assign(this.rWarpedPosition.x, this.rWarpedPosition.y, this.rWarpedPosition.z);
         return this.input.process(this.rChildContext);
      }
   }

   @Override
   public void setInputs(@Nonnull Density[] inputs) {
      if (inputs.length == 1) {
         this.input = inputs[0];
      } else {
         this.input = null;
      }
   }
}
