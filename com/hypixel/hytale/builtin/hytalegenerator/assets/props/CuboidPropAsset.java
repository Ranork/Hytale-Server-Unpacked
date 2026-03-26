package com.hypixel.hytale.builtin.hytalegenerator.assets.props;

import com.hypixel.hytale.builtin.hytalegenerator.assets.bounds.IntegerBounds3dAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.materialproviders.ConstantMaterialProviderAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.materialproviders.MaterialProviderAsset;
import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import com.hypixel.hytale.builtin.hytalegenerator.material.Material;
import com.hypixel.hytale.builtin.hytalegenerator.materialproviders.MaterialProvider;
import com.hypixel.hytale.builtin.hytalegenerator.props.CuboidProp;
import com.hypixel.hytale.builtin.hytalegenerator.props.EmptyProp;
import com.hypixel.hytale.builtin.hytalegenerator.props.Prop;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import javax.annotation.Nonnull;

public class CuboidPropAsset extends PropAsset {
   @Nonnull
   public static final BuilderCodec<CuboidPropAsset> CODEC = BuilderCodec.builder(CuboidPropAsset.class, CuboidPropAsset::new, PropAsset.ABSTRACT_CODEC)
      .append(new KeyedCodec<>("Bounds", IntegerBounds3dAsset.CODEC, true), (asset, value) -> asset.boundsAsset = value, asset -> asset.boundsAsset)
      .add()
      .append(
         new KeyedCodec<>("Material", MaterialProviderAsset.CODEC, true),
         (asset, value) -> asset.materialProviderAsset = value,
         asset -> asset.materialProviderAsset
      )
      .add()
      .build();
   private IntegerBounds3dAsset boundsAsset = new IntegerBounds3dAsset();
   private MaterialProviderAsset materialProviderAsset = new ConstantMaterialProviderAsset();

   @Nonnull
   @Override
   public Prop build(@Nonnull PropAsset.Argument argument) {
      if (super.skip()) {
         return EmptyProp.INSTANCE;
      } else {
         Bounds3i bounds = this.boundsAsset.build();
         bounds.correct();
         MaterialProvider<Material> materialProvider = this.materialProviderAsset.build(MaterialProviderAsset.argumentFrom(argument));
         return new CuboidProp(bounds, materialProvider);
      }
   }

   @Override
   public void cleanUp() {
      this.materialProviderAsset.cleanUp();
   }
}
