package com.hypixel.hytale.builtin.hytalegenerator.assets.materialproviders.spaceanddepth.conditionassets;

import com.hypixel.hytale.builtin.hytalegenerator.materialproviders.spaceanddepth.SpaceAndDepthMaterialProvider;
import com.hypixel.hytale.builtin.hytalegenerator.materialproviders.spaceanddepth.conditions.NotCondition;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import javax.annotation.Nonnull;

public class NotConditionAsset extends ConditionAsset {
   @Nonnull
   public static final BuilderCodec<NotConditionAsset> CODEC = BuilderCodec.builder(
         NotConditionAsset.class, NotConditionAsset::new, ConditionAsset.ABSTRACT_CODEC
      )
      .append(new KeyedCodec<>("Condition", ConditionAsset.CODEC, true), (t, k) -> t.conditionAsset = k, k -> k.conditionAsset)
      .add()
      .build();
   private ConditionAsset conditionAsset = new AlwaysTrueConditionAsset();

   @Nonnull
   @Override
   public SpaceAndDepthMaterialProvider.Condition build() {
      return new NotCondition(this.conditionAsset.build());
   }
}
