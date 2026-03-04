package com.hypixel.hytale.builtin.hytalegenerator.assets.environmentproviders;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.builtin.hytalegenerator.assets.Cleanable;
import com.hypixel.hytale.builtin.hytalegenerator.assets.delimiters.RangeDoubleAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.DensityAsset;
import com.hypixel.hytale.builtin.hytalegenerator.delimiters.DelimiterDouble;
import com.hypixel.hytale.builtin.hytalegenerator.delimiters.RangeDouble;
import com.hypixel.hytale.builtin.hytalegenerator.density.Density;
import com.hypixel.hytale.builtin.hytalegenerator.environmentproviders.DensityDelimitedEnvironmentProvider;
import com.hypixel.hytale.builtin.hytalegenerator.environmentproviders.EnvironmentProvider;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

public class DensityDelimitedEnvironmentProviderAsset extends EnvironmentProviderAsset {
   @Nonnull
   public static final BuilderCodec<DensityDelimitedEnvironmentProviderAsset> CODEC = BuilderCodec.builder(
         DensityDelimitedEnvironmentProviderAsset.class, DensityDelimitedEnvironmentProviderAsset::new, EnvironmentProviderAsset.ABSTRACT_CODEC
      )
      .append(
         new KeyedCodec<>(
            "Delimiters",
            new ArrayCodec<>(DensityDelimitedEnvironmentProviderAsset.DelimiterAsset.CODEC, DensityDelimitedEnvironmentProviderAsset.DelimiterAsset[]::new),
            true
         ),
         (t, k) -> t.delimiterAssets = k,
         k -> k.delimiterAssets
      )
      .add()
      .append(new KeyedCodec<>("Density", DensityAsset.CODEC, true), (t, value) -> t.densityAsset = value, t -> t.densityAsset)
      .add()
      .build();
   private DensityDelimitedEnvironmentProviderAsset.DelimiterAsset[] delimiterAssets = new DensityDelimitedEnvironmentProviderAsset.DelimiterAsset[0];
   private DensityAsset densityAsset = DensityAsset.getFallbackAsset();

   @Nonnull
   @Override
   public EnvironmentProvider build(@Nonnull EnvironmentProviderAsset.Argument argument) {
      if (super.isSkipped()) {
         return EnvironmentProvider.noEnvironmentProvider();
      } else {
         List<DelimiterDouble<EnvironmentProvider>> delimiters = new ArrayList<>(this.delimiterAssets.length);

         for (DensityDelimitedEnvironmentProviderAsset.DelimiterAsset delimiterAsset : this.delimiterAssets) {
            delimiters.add(delimiterAsset.build(argument));
         }

         Density density = this.densityAsset.build(DensityAsset.from(argument));
         return new DensityDelimitedEnvironmentProvider(delimiters, density);
      }
   }

   @Override
   public void cleanUp() {
      this.densityAsset.cleanUp();

      for (DensityDelimitedEnvironmentProviderAsset.DelimiterAsset delimiterAsset : this.delimiterAssets) {
         delimiterAsset.cleanUp();
      }
   }

   public static class DelimiterAsset
      implements Cleanable,
      JsonAssetWithMap<String, DefaultAssetMap<String, DensityDelimitedEnvironmentProviderAsset.DelimiterAsset>> {
      @Nonnull
      public static final AssetBuilderCodec<String, DensityDelimitedEnvironmentProviderAsset.DelimiterAsset> CODEC = AssetBuilderCodec.builder(
            DensityDelimitedEnvironmentProviderAsset.DelimiterAsset.class,
            DensityDelimitedEnvironmentProviderAsset.DelimiterAsset::new,
            Codec.STRING,
            (asset, id) -> asset.id = id,
            config -> config.id,
            (config, data) -> config.data = data,
            config -> config.data
         )
         .append(new KeyedCodec<>("Range", RangeDoubleAsset.CODEC, true), (t, value) -> t.rangeAsset = value, t -> t.rangeAsset)
         .add()
         .append(
            new KeyedCodec<>("Environment", EnvironmentProviderAsset.CODEC, true),
            (t, value) -> t.environmentProviderAsset = value,
            t -> t.environmentProviderAsset
         )
         .add()
         .build();
      private String id;
      private AssetExtraInfo.Data data;
      private RangeDoubleAsset rangeAsset = new RangeDoubleAsset();
      private EnvironmentProviderAsset environmentProviderAsset = EnvironmentProviderAsset.getFallbackAsset();

      @Nonnull
      public DelimiterDouble<EnvironmentProvider> build(@Nonnull EnvironmentProviderAsset.Argument argument) {
         RangeDouble range = this.rangeAsset.build();
         EnvironmentProvider environmentProvider = this.environmentProviderAsset.build(argument);
         return new DelimiterDouble<>(range, environmentProvider);
      }

      public String getId() {
         return this.id;
      }

      @Override
      public void cleanUp() {
         this.environmentProviderAsset.cleanUp();
      }
   }
}
