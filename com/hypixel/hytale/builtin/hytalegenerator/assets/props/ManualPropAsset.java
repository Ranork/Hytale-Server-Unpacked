package com.hypixel.hytale.builtin.hytalegenerator.assets.props;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.builtin.hytalegenerator.assets.material.MaterialAsset;
import com.hypixel.hytale.builtin.hytalegenerator.props.EmptyProp;
import com.hypixel.hytale.builtin.hytalegenerator.props.ManualProp;
import com.hypixel.hytale.builtin.hytalegenerator.props.Prop;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.math.vector.Vector3i;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

public class ManualPropAsset extends PropAsset {
   @Nonnull
   public static final BuilderCodec<ManualPropAsset> CODEC = BuilderCodec.builder(ManualPropAsset.class, ManualPropAsset::new, PropAsset.ABSTRACT_CODEC)
      .append(
         new KeyedCodec<>("Blocks", new ArrayCodec<>(ManualPropAsset.BlockAsset.CODEC, ManualPropAsset.BlockAsset[]::new), true),
         (asset, value) -> asset.blockAssets = value,
         asset -> asset.blockAssets
      )
      .add()
      .build();
   @Nonnull
   private ManualPropAsset.BlockAsset[] blockAssets = new ManualPropAsset.BlockAsset[0];

   @Nonnull
   @Override
   public Prop build(@Nonnull PropAsset.Argument argument) {
      if (super.skip()) {
         return EmptyProp.INSTANCE;
      } else {
         List<ManualProp.Block> blocks = new ArrayList<>(this.blockAssets.length);

         for (ManualPropAsset.BlockAsset blockAsset : this.blockAssets) {
            blocks.add(new ManualProp.Block(blockAsset.materialAsset.build(argument.materialCache), blockAsset.position));
         }

         return new ManualProp(blocks);
      }
   }

   public static class BlockAsset implements JsonAssetWithMap<String, DefaultAssetMap<String, WeightedPropAsset.EntryAsset>> {
      @Nonnull
      public static final AssetBuilderCodec<String, ManualPropAsset.BlockAsset> CODEC = AssetBuilderCodec.builder(
            ManualPropAsset.BlockAsset.class,
            ManualPropAsset.BlockAsset::new,
            Codec.STRING,
            (asset, id) -> asset.id = id,
            config -> config.id,
            (config, data) -> config.data = data,
            config -> config.data
         )
         .append(new KeyedCodec<>("Position", Vector3i.CODEC, true), (asset, value) -> asset.position = value, asset -> asset.position)
         .add()
         .append(new KeyedCodec<>("Material", MaterialAsset.CODEC, true), (asset, value) -> asset.materialAsset = value, asset -> asset.materialAsset)
         .add()
         .build();
      private String id;
      private AssetExtraInfo.Data data;
      private Vector3i position = new Vector3i();
      private MaterialAsset materialAsset = new MaterialAsset();

      public String getId() {
         return this.id;
      }
   }
}
