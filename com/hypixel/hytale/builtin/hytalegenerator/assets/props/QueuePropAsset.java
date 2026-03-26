package com.hypixel.hytale.builtin.hytalegenerator.assets.props;

import com.hypixel.hytale.builtin.hytalegenerator.props.EmptyProp;
import com.hypixel.hytale.builtin.hytalegenerator.props.Prop;
import com.hypixel.hytale.builtin.hytalegenerator.props.UnionProp;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import java.util.ArrayList;
import javax.annotation.Nonnull;

public class QueuePropAsset extends PropAsset {
   @Nonnull
   public static final BuilderCodec<QueuePropAsset> CODEC = BuilderCodec.builder(QueuePropAsset.class, QueuePropAsset::new, PropAsset.ABSTRACT_CODEC)
      .append(
         new KeyedCodec<>("Props", new ArrayCodec<>(PropAsset.CODEC, PropAsset[]::new), true),
         (asset, value) -> asset.propAssets = value,
         asset -> asset.propAssets
      )
      .add()
      .build();
   private PropAsset[] propAssets = new PropAsset[0];

   @Nonnull
   @Override
   public Prop build(@Nonnull PropAsset.Argument argument) {
      if (super.skip()) {
         return EmptyProp.INSTANCE;
      } else {
         ArrayList<Prop> props = new ArrayList<>(this.propAssets.length);

         for (PropAsset asset : this.propAssets) {
            props.add(asset.build(argument));
         }

         return new UnionProp(props);
      }
   }

   @Override
   public void cleanUp() {
      for (PropAsset propAsset : this.propAssets) {
         propAsset.cleanUp();
      }
   }
}
