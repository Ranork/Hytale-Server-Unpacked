package com.hypixel.hytale.builtin.adventure.shop;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetKeyValidator;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.validation.ValidatorCache;
import com.hypixel.hytale.server.core.entity.entities.player.pages.choices.ChoiceElement;
import java.util.Arrays;
import javax.annotation.Nonnull;

public class ShopAsset implements JsonAssetWithMap<String, DefaultAssetMap<String, ShopAsset>> {
   @Nonnull
   public static final AssetBuilderCodec<String, ShopAsset> CODEC = AssetBuilderCodec.builder(
         ShopAsset.class,
         ShopAsset::new,
         Codec.STRING,
         (shopAsset, s) -> shopAsset.id = s,
         shopAsset -> shopAsset.id,
         (shopAsset, data) -> shopAsset.extraData = data,
         shopAsset -> shopAsset.extraData
      )
      .addField(
         new KeyedCodec<>("Content", new ArrayCodec<>(ChoiceElement.CODEC, ChoiceElement[]::new)),
         (shopAsset, choiceElements) -> shopAsset.elements = choiceElements,
         shopAsset -> shopAsset.elements
      )
      .build();
   @Nonnull
   public static final ValidatorCache<String> VALIDATOR_CACHE = new ValidatorCache<>(new AssetKeyValidator<>(ShopAsset::getAssetStore));
   private static AssetStore<String, ShopAsset, DefaultAssetMap<String, ShopAsset>> ASSET_STORE;
   protected AssetExtraInfo.Data extraData;
   protected String id;
   protected ChoiceElement[] elements;

   @Nonnull
   public static AssetStore<String, ShopAsset, DefaultAssetMap<String, ShopAsset>> getAssetStore() {
      if (ASSET_STORE == null) {
         ASSET_STORE = AssetRegistry.getAssetStore(ShopAsset.class);
      }

      return ASSET_STORE;
   }

   public static DefaultAssetMap<String, ShopAsset> getAssetMap() {
      return (DefaultAssetMap<String, ShopAsset>)getAssetStore().getAssetMap();
   }

   public ShopAsset(String id, ChoiceElement[] elements) {
      this.id = id;
      this.elements = elements;
   }

   protected ShopAsset() {
   }

   public String getId() {
      return this.id;
   }

   public ChoiceElement[] getElements() {
      return this.elements;
   }

   @Nonnull
   @Override
   public String toString() {
      return "ShopAsset{id='" + this.id + "', elements=" + Arrays.toString((Object[])this.elements) + "}";
   }
}
