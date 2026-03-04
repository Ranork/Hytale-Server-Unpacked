package com.hypixel.hytale.builtin.hytalegenerator.assets.props.prefabprop.directionality;

import com.hypixel.hytale.builtin.hytalegenerator.assets.patterns.ConstantPatternAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.patterns.PatternAsset;
import com.hypixel.hytale.builtin.hytalegenerator.props.directionality.Directionality;
import com.hypixel.hytale.builtin.hytalegenerator.props.directionality.StaticDirectionality;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.LegacyValidator;
import com.hypixel.hytale.server.core.prefab.PrefabRotation;
import javax.annotation.Nonnull;

public class StaticDirectionalityAsset extends DirectionalityAsset {
   @Nonnull
   public static final BuilderCodec<StaticDirectionalityAsset> CODEC = BuilderCodec.builder(
         StaticDirectionalityAsset.class, StaticDirectionalityAsset::new, DirectionalityAsset.ABSTRACT_CODEC
      )
      .append(new KeyedCodec<>("Rotation", Codec.INTEGER, false), (asset, v) -> asset.rotation = v, asset -> asset.rotation)
      .addValidator((LegacyValidator<? super Integer>)((v, r) -> {
         if (v != 0 && v != 90 && v != 180 && v != 270) {
            r.fail("Rotation can only have the values: 0, 90, 180, 270");
         }
      }))
      .add()
      .append(new KeyedCodec<>("Pattern", PatternAsset.CODEC, true), (asset, v) -> asset.patternAsset = v, asset -> asset.patternAsset)
      .add()
      .build();
   private int rotation = 0;
   private PatternAsset patternAsset = new ConstantPatternAsset();

   @Nonnull
   @Override
   public Directionality build(@Nonnull DirectionalityAsset.Argument argument) {
      PrefabRotation prefabRotation = switch (this.rotation) {
         case 90 -> PrefabRotation.ROTATION_90;
         case 180 -> PrefabRotation.ROTATION_180;
         case 270 -> PrefabRotation.ROTATION_270;
         default -> PrefabRotation.ROTATION_0;
      };
      return new StaticDirectionality(prefabRotation, this.patternAsset.build(PatternAsset.argumentFrom(argument)));
   }
}
