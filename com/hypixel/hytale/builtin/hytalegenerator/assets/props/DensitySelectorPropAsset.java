package com.hypixel.hytale.builtin.hytalegenerator.assets.props;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.builtin.hytalegenerator.assets.Cleanable;
import com.hypixel.hytale.builtin.hytalegenerator.assets.delimiters.RangeDoubleAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.ConstantDensityAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.DensityAsset;
import com.hypixel.hytale.builtin.hytalegenerator.delimiters.DelimiterDouble;
import com.hypixel.hytale.builtin.hytalegenerator.delimiters.RangeDouble;
import com.hypixel.hytale.builtin.hytalegenerator.density.Density;
import com.hypixel.hytale.builtin.hytalegenerator.props.DensitySelectorProp;
import com.hypixel.hytale.builtin.hytalegenerator.props.EmptyProp;
import com.hypixel.hytale.builtin.hytalegenerator.props.Prop;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

public class DensitySelectorPropAsset extends PropAsset {
   @Nonnull
   public static final BuilderCodec<DensitySelectorPropAsset> CODEC = BuilderCodec.builder(
         DensitySelectorPropAsset.class, DensitySelectorPropAsset::new, PropAsset.ABSTRACT_CODEC
      )
      .append(
         new KeyedCodec<>("Delimiters", new ArrayCodec<>(DensitySelectorPropAsset.DelimiterAsset.CODEC, DensitySelectorPropAsset.DelimiterAsset[]::new), true),
         (asset, value) -> asset.delimiterAssets = value,
         asset -> asset.delimiterAssets
      )
      .add()
      .append(new KeyedCodec<>("Density", DensityAsset.CODEC, true), (asset, value) -> asset.densityAsset = value, asset -> asset.densityAsset)
      .add()
      .build();
   @Nonnull
   private DensitySelectorPropAsset.DelimiterAsset[] delimiterAssets = new DensitySelectorPropAsset.DelimiterAsset[0];
   @Nonnull
   private DensityAsset densityAsset = new ConstantDensityAsset();

   @Nonnull
   @Override
   public Prop build(@Nonnull PropAsset.Argument argument) {
      if (super.skip()) {
         return EmptyProp.INSTANCE;
      } else {
         List<DelimiterDouble<Prop>> delimiters = new ArrayList<>(this.delimiterAssets.length);

         for (DensitySelectorPropAsset.DelimiterAsset delimiterAsset : this.delimiterAssets) {
            RangeDouble range = delimiterAsset.rangeAsset.build();
            Prop prop = delimiterAsset.propAsset.build(argument);
            DelimiterDouble<Prop> delimiter = new DelimiterDouble<>(range, prop);
            delimiters.add(delimiter);
         }

         Density density = this.densityAsset.build(DensityAsset.from(argument));
         return new DensitySelectorProp(delimiters, density);
      }
   }

   @Override
   public void cleanUp() {
      for (DensitySelectorPropAsset.DelimiterAsset delimiterAsset : this.delimiterAssets) {
         delimiterAsset.propAsset.cleanUp();
      }

      this.densityAsset.cleanUp();
   }

   public static class DelimiterAsset implements Cleanable, JsonAssetWithMap<String, DefaultAssetMap<String, DensitySelectorPropAsset.DelimiterAsset>> {
      @Nonnull
      public static final AssetBuilderCodec<String, DensitySelectorPropAsset.DelimiterAsset> CODEC = AssetBuilderCodec.builder(
            DensitySelectorPropAsset.DelimiterAsset.class,
            DensitySelectorPropAsset.DelimiterAsset::new,
            Codec.STRING,
            (asset, id) -> asset.id = id,
            config -> config.id,
            (config, data) -> config.data = data,
            config -> config.data
         )
         .append(new KeyedCodec<>("Range", RangeDoubleAsset.CODEC, true), (asset, value) -> asset.rangeAsset = value, asset -> asset.rangeAsset)
         .add()
         .append(new KeyedCodec<>("Prop", PropAsset.CODEC, true), (asset, value) -> asset.propAsset = value, asset -> asset.propAsset)
         .add()
         .build();
      private String id;
      private AssetExtraInfo.Data data;
      @Nonnull
      private RangeDoubleAsset rangeAsset = new RangeDoubleAsset();
      @Nonnull
      private PropAsset propAsset = new EmptyPropAsset();

      public String getId() {
         return this.id;
      }

      @Override
      public void cleanUp() {
         this.propAsset.cleanUp();
      }
   }
}
