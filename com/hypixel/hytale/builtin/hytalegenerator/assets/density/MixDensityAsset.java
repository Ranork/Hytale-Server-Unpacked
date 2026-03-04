package com.hypixel.hytale.builtin.hytalegenerator.assets.density;

import com.hypixel.hytale.builtin.hytalegenerator.density.Density;
import com.hypixel.hytale.builtin.hytalegenerator.density.nodes.ConstantValueDensity;
import com.hypixel.hytale.builtin.hytalegenerator.density.nodes.MixDensity;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import java.util.List;
import javax.annotation.Nonnull;

public class MixDensityAsset extends DensityAsset {
   @Nonnull
   public static final BuilderCodec<MixDensityAsset> CODEC = BuilderCodec.builder(MixDensityAsset.class, MixDensityAsset::new, DensityAsset.ABSTRACT_CODEC)
      .build();

   @Nonnull
   @Override
   public Density build(@Nonnull DensityAsset.Argument argument) {
      if (this.isSkipped()) {
         return new ConstantValueDensity(0.0);
      } else {
         List<Density> builtInputs = this.buildInputs(argument, true);
         return (Density)(builtInputs.size() != 3 ? new ConstantValueDensity(0.0) : new MixDensity(builtInputs.get(0), builtInputs.get(1), builtInputs.get(2)));
      }
   }

   @Override
   public void cleanUp() {
      this.cleanUpInputs();
   }
}
