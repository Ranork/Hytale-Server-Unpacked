package com.hypixel.hytale.builtin.hytalegenerator.assets.patterns;

import com.hypixel.hytale.builtin.hytalegenerator.assets.material.MaterialAsset;
import com.hypixel.hytale.builtin.hytalegenerator.material.Material;
import com.hypixel.hytale.builtin.hytalegenerator.patterns.MaterialPattern;
import com.hypixel.hytale.builtin.hytalegenerator.patterns.Pattern;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import javax.annotation.Nonnull;

public class MaterialPatternAsset extends PatternAsset {
   @Nonnull
   public static final BuilderCodec<MaterialPatternAsset> CODEC = BuilderCodec.builder(
         MaterialPatternAsset.class, MaterialPatternAsset::new, PatternAsset.ABSTRACT_CODEC
      )
      .append(new KeyedCodec<>("Material", MaterialAsset.CODEC, true), (asset, value) -> asset.materialAsset = value, value -> value.materialAsset)
      .add()
      .build();
   private MaterialAsset materialAsset = new MaterialAsset();

   @Nonnull
   @Override
   public Pattern build(@Nonnull PatternAsset.Argument argument) {
      if (super.isSkipped()) {
         return Pattern.noPattern();
      } else {
         Material material = this.materialAsset.build(argument.materialCache);
         return new MaterialPattern(material);
      }
   }
}
