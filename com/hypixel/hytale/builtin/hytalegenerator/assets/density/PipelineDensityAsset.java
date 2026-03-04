package com.hypixel.hytale.builtin.hytalegenerator.assets.density;

import com.hypixel.hytale.builtin.hytalegenerator.density.Density;
import com.hypixel.hytale.builtin.hytalegenerator.density.nodes.ConstantValueDensity;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import javax.annotation.Nonnull;

public class PipelineDensityAsset extends DensityAsset {
   @Nonnull
   private static final DensityAsset[] EMPTY_INPUTS = new DensityAsset[0];
   @Nonnull
   public static final BuilderCodec<PipelineDensityAsset> CODEC = BuilderCodec.builder(
         PipelineDensityAsset.class, PipelineDensityAsset::new, DensityAsset.ABSTRACT_CODEC
      )
      .append(new KeyedCodec<>("Pipeline", new ArrayCodec<>(DensityAsset.CODEC, DensityAsset[]::new)), (t, k) -> t.pipeline = k, t -> t.pipeline)
      .add()
      .build();
   private DensityAsset[] pipeline = new DensityAsset[0];

   @Nonnull
   @Override
   public Density build(@Nonnull DensityAsset.Argument argument) {
      if (this.isSkipped()) {
         return new ConstantValueDensity(0.0);
      } else if (this.pipeline.length == 0) {
         return this.buildFirstInput(argument);
      } else {
         Density[] nextInputs = new Density[]{this.pipeline[0].build(argument)};
         nextInputs[0].setInputs(this.buildInputsArray(argument));

         for (int i = 1; i < this.pipeline.length; i++) {
            Density node = this.pipeline[i].buildWithInputs(argument, nextInputs);
            nextInputs[0] = node;
         }

         return nextInputs[0];
      }
   }

   @Override
   public void cleanUp() {
      this.cleanUpInputs();

      for (DensityAsset densityAsset : this.pipeline) {
         densityAsset.cleanUp();
      }
   }
}
