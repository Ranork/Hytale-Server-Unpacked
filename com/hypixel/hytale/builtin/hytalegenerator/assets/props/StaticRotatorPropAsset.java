package com.hypixel.hytale.builtin.hytalegenerator.assets.props;

import com.hypixel.hytale.builtin.hytalegenerator.assets.material.OrthogonalRotationAsset;
import com.hypixel.hytale.builtin.hytalegenerator.props.EmptyProp;
import com.hypixel.hytale.builtin.hytalegenerator.props.Prop;
import com.hypixel.hytale.builtin.hytalegenerator.props.StaticRotatorProp;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.RotationTuple;
import javax.annotation.Nonnull;

public class StaticRotatorPropAsset extends PropAsset {
   @Nonnull
   public static final BuilderCodec<StaticRotatorPropAsset> CODEC = BuilderCodec.builder(
         StaticRotatorPropAsset.class, StaticRotatorPropAsset::new, PropAsset.ABSTRACT_CODEC
      )
      .append(new KeyedCodec<>("Prop", PropAsset.CODEC, true), (asset, value) -> asset.propAsset = value, asset -> asset.propAsset)
      .add()
      .append(new KeyedCodec<>("Rotation", OrthogonalRotationAsset.CODEC, true), (asset, value) -> asset.rotationAsset = value, asset -> asset.rotationAsset)
      .add()
      .build();
   @Nonnull
   private PropAsset propAsset = new EmptyPropAsset();
   @Nonnull
   private OrthogonalRotationAsset rotationAsset = new OrthogonalRotationAsset();

   @Nonnull
   @Override
   public Prop build(@Nonnull PropAsset.Argument argument) {
      if (super.skip()) {
         return EmptyProp.INSTANCE;
      } else {
         Prop prop = this.propAsset.build(argument);
         RotationTuple rotation = this.rotationAsset.build();
         return new StaticRotatorProp(prop, rotation, argument.materialCache);
      }
   }

   @Override
   public void cleanUp() {
      this.propAsset.cleanUp();
   }
}
