package com.hypixel.hytale.builtin.hytalegenerator.assets.props;

import com.hypixel.hytale.builtin.hytalegenerator.props.Prop;
import com.hypixel.hytale.builtin.hytalegenerator.props.QueueProp;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import java.util.ArrayList;
import javax.annotation.Nonnull;

public class QueuePropAsset extends PropAsset {
   @Nonnull
   public static final BuilderCodec<QueuePropAsset> CODEC = BuilderCodec.builder(QueuePropAsset.class, QueuePropAsset::new, PropAsset.ABSTRACT_CODEC)
      .append(
         new KeyedCodec<>("Queue", new ArrayCodec<>(PropAsset.CODEC, PropAsset[]::new), true), (asset, v) -> asset.propAssets = v, asset -> asset.propAssets
      )
      .add()
      .build();
   private PropAsset[] propAssets = new PropAsset[0];

   @Nonnull
   @Override
   public Prop build(@Nonnull PropAsset.Argument argument) {
      if (super.skip()) {
         return Prop.noProp();
      } else {
         ArrayList<Prop> propsQueue = new ArrayList<>(this.propAssets.length);

         for (PropAsset asset : this.propAssets) {
            propsQueue.add(asset.build(argument));
         }

         return new QueueProp(propsQueue);
      }
   }

   @Override
   public void cleanUp() {
      for (PropAsset propAsset : this.propAssets) {
         propAsset.cleanUp();
      }
   }
}
