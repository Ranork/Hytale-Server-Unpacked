package com.hypixel.hytale.builtin.adventure.objectives.markers.reachlocation;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetKeyValidator;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.validation.ValidatorCache;
import com.hypixel.hytale.codec.validation.Validators;
import javax.annotation.Nonnull;

public class ReachLocationMarkerAsset implements JsonAssetWithMap<String, DefaultAssetMap<String, ReachLocationMarkerAsset>> {
   @Nonnull
   public static final AssetBuilderCodec<String, ReachLocationMarkerAsset> CODEC = AssetBuilderCodec.builder(
         ReachLocationMarkerAsset.class,
         ReachLocationMarkerAsset::new,
         Codec.STRING,
         (t, k) -> t.id = k,
         t -> t.id,
         (asset, data) -> asset.data = data,
         asset -> asset.data
      )
      .append(
         new KeyedCodec<>("Radius", Codec.FLOAT),
         (reachLocationMarkerAsset, aFloat) -> reachLocationMarkerAsset.radius = aFloat,
         reachLocationMarkerAsset -> reachLocationMarkerAsset.radius
      )
      .addValidator(Validators.greaterThan(0.0F))
      .add()
      .<String>append(
         new KeyedCodec<>("Name", Codec.STRING),
         (reachLocationMarkerAsset, s) -> reachLocationMarkerAsset.name = s,
         reachLocationMarkerAsset -> reachLocationMarkerAsset.name
      )
      .addValidator(Validators.nonNull())
      .addValidator(Validators.nonEmptyString())
      .add()
      .build();
   @Nonnull
   public static final ValidatorCache<String> VALIDATOR_CACHE = new ValidatorCache<>(new AssetKeyValidator<>(ReachLocationMarkerAsset::getAssetStore));
   private static AssetStore<String, ReachLocationMarkerAsset, DefaultAssetMap<String, ReachLocationMarkerAsset>> ASSET_STORE;
   protected AssetExtraInfo.Data data;
   protected String id;
   protected String name;
   protected float radius = 1.0F;

   public static AssetStore<String, ReachLocationMarkerAsset, DefaultAssetMap<String, ReachLocationMarkerAsset>> getAssetStore() {
      if (ASSET_STORE == null) {
         ASSET_STORE = AssetRegistry.getAssetStore(ReachLocationMarkerAsset.class);
      }

      return ASSET_STORE;
   }

   public static DefaultAssetMap<String, ReachLocationMarkerAsset> getAssetMap() {
      return (DefaultAssetMap<String, ReachLocationMarkerAsset>)getAssetStore().getAssetMap();
   }

   public String getId() {
      return this.id;
   }

   public float getRadius() {
      return this.radius;
   }

   public String getName() {
      return this.name;
   }

   @Nonnull
   @Override
   public String toString() {
      return "ReachLocationMarkerAsset{id='" + this.id + "', name='" + this.name + "', radius=" + this.radius + "}";
   }
}
