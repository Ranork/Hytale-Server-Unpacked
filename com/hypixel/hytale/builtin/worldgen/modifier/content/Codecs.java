package com.hypixel.hytale.builtin.worldgen.modifier.content;

import com.hypixel.hytale.assetstore.codec.ContainedAssetCodec;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.math.range.FloatRange;
import com.hypixel.hytale.math.range.IntRange;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.fluid.Fluid;

public interface Codecs {
   BuilderCodec<IntRange> INT_RANGE = BuilderCodec.builder(IntRange.class, IntRange::new)
      .append(new KeyedCodec<>("Min", Codec.INTEGER), IntRange::setInclusiveMin, IntRange::getInclusiveMin)
      .documentation("The inclusive minimum value of the range")
      .add()
      .append(new KeyedCodec<>("Max", Codec.INTEGER), IntRange::setInclusiveMax, IntRange::getInclusiveMax)
      .documentation("The inclusive maximum value of the range")
      .add()
      .build();
   BuilderCodec<FloatRange> DENSITY_RANGE = BuilderCodec.builder(FloatRange.class, FloatRange::new)
      .append(new KeyedCodec<>("Min", Codec.FLOAT), FloatRange::setInclusiveMin, FloatRange::getInclusiveMin)
      .addValidator(Validators.range(0.0F, 1.0F))
      .documentation("The inclusive minimum value of the range")
      .add()
      .append(new KeyedCodec<>("Max", Codec.FLOAT), FloatRange::setInclusiveMax, FloatRange::getInclusiveMax)
      .addValidator(Validators.range(0.0F, 1.0F))
      .documentation("The inclusive maximum value of the range")
      .add()
      .afterDecode(range -> {
         float min = Math.min(range.getInclusiveMin(), range.getInclusiveMax());
         float max = Math.max(range.getInclusiveMin(), range.getInclusiveMax());
         range.setInclusiveMin(min);
         range.setInclusiveMax(max);
      })
      .build();
   Codec<String> BLOCK_TYPE = new ContainedAssetCodec<>(BlockType.class, BlockType.CODEC);
   Codec<String> FLUID_TYPE = new ContainedAssetCodec<>(Fluid.class, Fluid.CODEC);
   ArrayCodec<String> BLOCK_TYPE_ARRAY = new ArrayCodec<>(BLOCK_TYPE, String[]::new);
   ArrayCodec<String> FLUID_TYPE_ARRAY = new ArrayCodec<>(FLUID_TYPE, String[]::new);
}
