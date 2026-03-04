package com.hypixel.hytale.builtin.hytalegenerator.assets.props;

import com.hypixel.hytale.builtin.hytalegenerator.props.OffsetProp;
import com.hypixel.hytale.builtin.hytalegenerator.props.Prop;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.math.vector.Vector3i;
import javax.annotation.Nonnull;

public class OffsetPropAsset extends PropAsset {
   @Nonnull
   public static final BuilderCodec<OffsetPropAsset> CODEC = BuilderCodec.builder(OffsetPropAsset.class, OffsetPropAsset::new, PropAsset.ABSTRACT_CODEC)
      .append(new KeyedCodec<>("Offset", Vector3i.CODEC, true), (asset, value) -> asset.offset_voxelGrid = value, asset -> asset.offset_voxelGrid)
      .add()
      .append(new KeyedCodec<>("Prop", PropAsset.CODEC, true), (asset, value) -> asset.propAsset = value, asset -> asset.propAsset)
      .add()
      .build();
   private Vector3i offset_voxelGrid = new Vector3i();
   private PropAsset propAsset = new NoPropAsset();

   @Nonnull
   @Override
   public Prop build(@Nonnull PropAsset.Argument argument) {
      return (Prop)(super.skip() ? Prop.noProp() : new OffsetProp(this.offset_voxelGrid, this.propAsset.build(argument)));
   }

   @Override
   public void cleanUp() {
      this.propAsset.cleanUp();
   }
}
