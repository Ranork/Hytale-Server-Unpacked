package com.hypixel.hytale.builtin.hytalegenerator.assets.density;

import com.hypixel.hytale.builtin.hytalegenerator.density.Density;
import com.hypixel.hytale.builtin.hytalegenerator.density.nodes.ConstantValueDensity;
import com.hypixel.hytale.builtin.hytalegenerator.density.nodes.MultiCacheDensity;
import com.hypixel.hytale.builtin.hytalegenerator.density.nodes.YOverrideDensity;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import javax.annotation.Nonnull;

public class Cache2dDensityAsset_Deprecated extends DensityAsset {
   @Nonnull
   public static final BuilderCodec<Cache2dDensityAsset_Deprecated> CODEC = BuilderCodec.builder(
         Cache2dDensityAsset_Deprecated.class, Cache2dDensityAsset_Deprecated::new, DensityAsset.ABSTRACT_CODEC
      )
      .append(new KeyedCodec<>("Y", Codec.DOUBLE, false), (t, k) -> t.y = k, t -> t.y)
      .add()
      .build();
   private double y = 0.0;

   @Nonnull
   @Override
   public Density build(@Nonnull DensityAsset.Argument argument) {
      DensityAsset inputAsset = this.firstInput();
      if (inputAsset != null && !this.isSkipped() && !inputAsset.isSkipped()) {
         Density input = this.buildFirstInput(argument);
         if (input == null) {
            return new ConstantValueDensity(0.0);
         } else {
            Density cacheDensity = new MultiCacheDensity(input, CacheDensityAsset.DEFAULT_CAPACITY);
            return new YOverrideDensity(cacheDensity, this.y);
         }
      } else {
         return new ConstantValueDensity(0.0);
      }
   }

   @Override
   public void cleanUp() {
      this.cleanUpInputs();
   }
}
