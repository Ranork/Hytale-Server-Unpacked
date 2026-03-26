package com.hypixel.hytale.builtin.hytalegenerator.assets.props;

import com.hypixel.hytale.builtin.hytalegenerator.BlockMask;
import com.hypixel.hytale.builtin.hytalegenerator.assets.blockmask.BlockMaskAsset;
import com.hypixel.hytale.builtin.hytalegenerator.props.EmptyProp;
import com.hypixel.hytale.builtin.hytalegenerator.props.MaskProp;
import com.hypixel.hytale.builtin.hytalegenerator.props.Prop;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import javax.annotation.Nonnull;

public class MaskPropAsset extends PropAsset {
   @Nonnull
   public static final BuilderCodec<MaskPropAsset> CODEC = BuilderCodec.builder(MaskPropAsset.class, MaskPropAsset::new, PropAsset.ABSTRACT_CODEC)
      .append(new KeyedCodec<>("Prop", PropAsset.CODEC, true), (asset, value) -> asset.propAsset = value, asset -> asset.propAsset)
      .add()
      .append(new KeyedCodec<>("Mask", BlockMaskAsset.CODEC, true), (asset, value) -> asset.blockMaskAsset = value, asset -> asset.blockMaskAsset)
      .add()
      .build();
   @Nonnull
   private PropAsset propAsset = new EmptyPropAsset();
   @Nonnull
   private BlockMaskAsset blockMaskAsset = new BlockMaskAsset();

   @Nonnull
   @Override
   public Prop build(@Nonnull PropAsset.Argument argument) {
      if (super.skip()) {
         return EmptyProp.INSTANCE;
      } else {
         Prop prop = this.propAsset.build(argument);
         BlockMask mask = this.blockMaskAsset.build(argument.materialCache);
         return new MaskProp(prop, mask);
      }
   }

   @Override
   public void cleanUp() {
      this.propAsset.cleanUp();
      this.blockMaskAsset.cleanUp();
   }
}
