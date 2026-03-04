package com.hypixel.hytale.builtin.hytalegenerator.assets.materialproviders;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.builtin.hytalegenerator.LoggerUtil;
import com.hypixel.hytale.builtin.hytalegenerator.material.Material;
import com.hypixel.hytale.builtin.hytalegenerator.materialproviders.MaterialProvider;
import com.hypixel.hytale.builtin.hytalegenerator.materialproviders.StripedMaterialProvider;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import java.util.ArrayList;
import javax.annotation.Nonnull;

public class StripedMaterialProviderAsset extends MaterialProviderAsset {
   @Nonnull
   public static final BuilderCodec<StripedMaterialProviderAsset> CODEC = BuilderCodec.builder(
         StripedMaterialProviderAsset.class, StripedMaterialProviderAsset::new, MaterialProviderAsset.ABSTRACT_CODEC
      )
      .append(
         new KeyedCodec<>("Stripes", new ArrayCodec<>(StripedMaterialProviderAsset.StripeAsset.CODEC, StripedMaterialProviderAsset.StripeAsset[]::new), true),
         (t, k) -> t.stripeAssets = k,
         k -> k.stripeAssets
      )
      .add()
      .append(new KeyedCodec<>("Material", MaterialProviderAsset.CODEC, true), (t, k) -> t.materialProviderAsset = k, k -> k.materialProviderAsset)
      .add()
      .build();
   private StripedMaterialProviderAsset.StripeAsset[] stripeAssets = new StripedMaterialProviderAsset.StripeAsset[0];
   private MaterialProviderAsset materialProviderAsset = new ConstantMaterialProviderAsset();

   @Nonnull
   @Override
   public MaterialProvider<Material> build(@Nonnull MaterialProviderAsset.Argument argument) {
      if (super.skip()) {
         return MaterialProvider.noMaterialProvider();
      } else {
         ArrayList<StripedMaterialProvider.Stripe> stripes = new ArrayList<>();

         for (StripedMaterialProviderAsset.StripeAsset asset : this.stripeAssets) {
            if (asset == null) {
               LoggerUtil.getLogger().warning("Couldn't load a strip asset, will skip it.");
            } else {
               StripedMaterialProvider.Stripe stripe = new StripedMaterialProvider.Stripe(asset.topY, asset.bottomY);
               stripes.add(stripe);
            }
         }

         MaterialProvider<Material> materialProvider = this.materialProviderAsset.build(argument);
         return new StripedMaterialProvider<>(materialProvider, stripes);
      }
   }

   @Override
   public void cleanUp() {
      this.materialProviderAsset.cleanUp();
   }

   public static class StripeAsset implements JsonAssetWithMap<String, DefaultAssetMap<String, StripedMaterialProviderAsset.StripeAsset>> {
      @Nonnull
      public static final AssetBuilderCodec<String, StripedMaterialProviderAsset.StripeAsset> CODEC = AssetBuilderCodec.builder(
            StripedMaterialProviderAsset.StripeAsset.class,
            StripedMaterialProviderAsset.StripeAsset::new,
            Codec.STRING,
            (asset, id) -> asset.id = id,
            config -> config.id,
            (config, data) -> config.data = data,
            config -> config.data
         )
         .append(new KeyedCodec<>("TopY", Codec.INTEGER, true), (t, y) -> t.topY = y, t -> t.bottomY)
         .add()
         .append(new KeyedCodec<>("BottomY", Codec.INTEGER, true), (t, y) -> t.bottomY = y, t -> t.bottomY)
         .add()
         .build();
      private String id;
      private AssetExtraInfo.Data data;
      private int topY;
      private int bottomY;

      public String getId() {
         return this.id;
      }
   }
}
