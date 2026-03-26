package com.hypixel.hytale.builtin.hytalegenerator.assets.props;

import com.hypixel.hytale.builtin.hytalegenerator.WeightedMap;
import com.hypixel.hytale.builtin.hytalegenerator.assets.material.OrthogonalRotationAsset;
import com.hypixel.hytale.builtin.hytalegenerator.props.EmptyProp;
import com.hypixel.hytale.builtin.hytalegenerator.props.Prop;
import com.hypixel.hytale.builtin.hytalegenerator.props.StaticRotatorProp;
import com.hypixel.hytale.builtin.hytalegenerator.props.WeightedProp;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.Rotation;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.RotationTuple;
import javax.annotation.Nonnull;

public class RandomRotatorPropAsset extends PropAsset {
   @Nonnull
   public static final BuilderCodec<RandomRotatorPropAsset> CODEC = BuilderCodec.builder(
         RandomRotatorPropAsset.class, RandomRotatorPropAsset::new, PropAsset.ABSTRACT_CODEC
      )
      .append(new KeyedCodec<>("Prop", PropAsset.CODEC, true), (asset, value) -> asset.propAsset = value, asset -> asset.propAsset)
      .add()
      .append(
         new KeyedCodec<>("Rotations", new ArrayCodec<>(OrthogonalRotationAsset.CODEC, OrthogonalRotationAsset[]::new), true),
         (asset, value) -> asset.rotationAssets = value,
         asset -> asset.rotationAssets
      )
      .add()
      .append(new KeyedCodec<>("HorizontalRotations", BuilderCodec.BOOLEAN, false), (asset, value) -> asset.allHorizontal = value, asset -> asset.allHorizontal)
      .add()
      .append(new KeyedCodec<>("Seed", BuilderCodec.STRING, true), (asset, value) -> asset.seed = value, asset -> asset.seed)
      .add()
      .build();
   @Nonnull
   private PropAsset propAsset = new EmptyPropAsset();
   @Nonnull
   private OrthogonalRotationAsset[] rotationAssets = new OrthogonalRotationAsset[0];
   private boolean allHorizontal = false;
   @Nonnull
   private String seed = "";

   @Nonnull
   @Override
   public Prop build(@Nonnull PropAsset.Argument argument) {
      if (super.skip()) {
         return EmptyProp.INSTANCE;
      } else if (!this.allHorizontal && this.rotationAssets.length == 0) {
         return EmptyProp.INSTANCE;
      } else {
         Prop childProp = this.propAsset.build(argument);
         WeightedMap<Prop> rotatedProps = new WeightedMap<>();
         if (this.allHorizontal) {
            Prop ninety = new StaticRotatorProp(childProp, RotationTuple.of(Rotation.Ninety, Rotation.None, Rotation.None), argument.materialCache);
            Prop oneEighty = new StaticRotatorProp(childProp, RotationTuple.of(Rotation.OneEighty, Rotation.None, Rotation.None), argument.materialCache);
            Prop twoSeventy = new StaticRotatorProp(childProp, RotationTuple.of(Rotation.TwoSeventy, Rotation.None, Rotation.None), argument.materialCache);
            rotatedProps.add(childProp, 1.0);
            rotatedProps.add(ninety, 1.0);
            rotatedProps.add(oneEighty, 1.0);
            rotatedProps.add(twoSeventy, 1.0);
         } else {
            for (OrthogonalRotationAsset rotationAsset : this.rotationAssets) {
               Prop rotatedProp = new StaticRotatorProp(childProp, rotationAsset.build(), argument.materialCache);
               rotatedProps.add(rotatedProp, 1.0);
            }
         }

         return new WeightedProp(rotatedProps, argument.parentSeed.child(this.seed).createSupplier().get());
      }
   }

   @Override
   public void cleanUp() {
      this.propAsset.cleanUp();
   }
}
