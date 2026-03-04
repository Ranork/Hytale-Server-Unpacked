package com.hypixel.hytale.builtin.hytalegenerator.assets.props;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.builtin.hytalegenerator.assets.Cleanable;
import com.hypixel.hytale.builtin.hytalegenerator.datastructures.WeightedMap;
import com.hypixel.hytale.builtin.hytalegenerator.props.Prop;
import com.hypixel.hytale.builtin.hytalegenerator.props.WeightedProp;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.validation.Validators;
import javax.annotation.Nonnull;

public class WeightedPropAsset extends PropAsset {
   @Nonnull
   public static final BuilderCodec<WeightedPropAsset> CODEC = BuilderCodec.builder(WeightedPropAsset.class, WeightedPropAsset::new, PropAsset.ABSTRACT_CODEC)
      .append(
         new KeyedCodec<>("Entries", new ArrayCodec<>(WeightedPropAsset.EntryAsset.CODEC, WeightedPropAsset.EntryAsset[]::new), true),
         (asset, value) -> asset.entryAssets = value,
         asset -> asset.entryAssets
      )
      .add()
      .append(new KeyedCodec<>("Seed", Codec.STRING, true), (asset, value) -> asset.seed = value, asset -> asset.seed)
      .add()
      .build();
   private WeightedPropAsset.EntryAsset[] entryAssets = new WeightedPropAsset.EntryAsset[0];
   private String seed = "";

   @Nonnull
   @Override
   public Prop build(@Nonnull PropAsset.Argument argument) {
      if (!super.skip() && this.entryAssets.length != 0) {
         WeightedMap<Prop> weightedProps = new WeightedMap<>(this.entryAssets.length);
         PropAsset.Argument childArgument = new PropAsset.Argument(argument);
         childArgument.parentSeed = argument.parentSeed.child(this.seed);

         for (WeightedPropAsset.EntryAsset entryAsset : this.entryAssets) {
            weightedProps.add(entryAsset.propAsset.build(childArgument), entryAsset.weight);
         }

         return new WeightedProp(weightedProps, childArgument.parentSeed.createSupplier().get());
      } else {
         return Prop.noProp();
      }
   }

   @Override
   public void cleanUp() {
      for (WeightedPropAsset.EntryAsset asset : this.entryAssets) {
         asset.cleanUp();
      }
   }

   public static class EntryAsset implements Cleanable, JsonAssetWithMap<String, DefaultAssetMap<String, WeightedPropAsset.EntryAsset>> {
      @Nonnull
      public static final AssetBuilderCodec<String, WeightedPropAsset.EntryAsset> CODEC = AssetBuilderCodec.builder(
            WeightedPropAsset.EntryAsset.class,
            WeightedPropAsset.EntryAsset::new,
            Codec.STRING,
            (asset, id) -> asset.id = id,
            config -> config.id,
            (config, data) -> config.data = data,
            config -> config.data
         )
         .append(new KeyedCodec<>("Weight", Codec.DOUBLE, true), (asset, value) -> asset.weight = value, asset -> asset.weight)
         .addValidator(Validators.greaterThan(0.0))
         .add()
         .append(new KeyedCodec<>("Prop", PropAsset.CODEC, true), (asset, value) -> asset.propAsset = value, asset -> asset.propAsset)
         .add()
         .build();
      private String id;
      private AssetExtraInfo.Data data;
      private double weight = 1.0;
      private PropAsset propAsset = new NoPropAsset();

      public String getId() {
         return this.id;
      }

      @Override
      public void cleanUp() {
         this.propAsset.cleanUp();
      }
   }
}
