package com.hypixel.hytale.builtin.hytalegenerator.assets.props;

import com.hypixel.hytale.builtin.hytalegenerator.props.EmptyProp;
import com.hypixel.hytale.builtin.hytalegenerator.props.Prop;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import javax.annotation.Nonnull;

public class EmptyPropAsset extends PropAsset {
   @Nonnull
   public static final BuilderCodec<EmptyPropAsset> CODEC = BuilderCodec.builder(EmptyPropAsset.class, EmptyPropAsset::new, PropAsset.ABSTRACT_CODEC).build();

   @Nonnull
   @Override
   public Prop build(@Nonnull PropAsset.Argument argument) {
      return EmptyProp.INSTANCE;
   }

   @Override
   public void cleanUp() {
   }
}
