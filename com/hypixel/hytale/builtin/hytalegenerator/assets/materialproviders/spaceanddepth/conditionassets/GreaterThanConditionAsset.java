package com.hypixel.hytale.builtin.hytalegenerator.assets.materialproviders.spaceanddepth.conditionassets;

import com.hypixel.hytale.builtin.hytalegenerator.materialproviders.spaceanddepth.SpaceAndDepthMaterialProvider;
import com.hypixel.hytale.builtin.hytalegenerator.materialproviders.spaceanddepth.conditions.ConditionParameter;
import com.hypixel.hytale.builtin.hytalegenerator.materialproviders.spaceanddepth.conditions.GreaterThanCondition;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import javax.annotation.Nonnull;

public class GreaterThanConditionAsset extends ConditionAsset {
   @Nonnull
   public static final BuilderCodec<GreaterThanConditionAsset> CODEC = BuilderCodec.builder(
         GreaterThanConditionAsset.class, GreaterThanConditionAsset::new, ConditionAsset.ABSTRACT_CODEC
      )
      .append(new KeyedCodec<>("ContextToCheck", ConditionParameter.CODEC, true), (t, k) -> t.parameter = k, k -> k.parameter)
      .add()
      .append(new KeyedCodec<>("Threshold", Codec.INTEGER, true), (t, k) -> t.threshold = k, k -> k.threshold)
      .add()
      .build();
   private ConditionParameter parameter = ConditionParameter.SPACE_ABOVE_FLOOR;
   private int threshold;

   @Nonnull
   @Override
   public SpaceAndDepthMaterialProvider.Condition build() {
      return new GreaterThanCondition(this.threshold, this.parameter);
   }
}
