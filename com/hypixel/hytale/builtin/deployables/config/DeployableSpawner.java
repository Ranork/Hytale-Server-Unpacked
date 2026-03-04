package com.hypixel.hytale.builtin.deployables.config;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.math.vector.Vector3d;
import javax.annotation.Nonnull;

public class DeployableSpawner implements JsonAssetWithMap<String, DefaultAssetMap<String, DeployableSpawner>> {
   @Nonnull
   public static final AssetBuilderCodec<String, DeployableSpawner> CODEC = AssetBuilderCodec.builder(
         DeployableSpawner.class, DeployableSpawner::new, Codec.STRING, (t, k) -> t.id = k, t -> t.id, (asset, data) -> asset.data = data, asset -> asset.data
      )
      .append(new KeyedCodec<>("Config", DeployableConfig.CODEC), (i, s) -> i.config = s, i -> i.config)
      .addValidator(Validators.nonNull())
      .add()
      .append(new KeyedCodec<>("PositionOffsets", new ArrayCodec<>(Vector3d.CODEC, Vector3d[]::new)), (i, s) -> i.positionOffsets = s, i -> i.positionOffsets)
      .add()
      .build();
   private static DefaultAssetMap<String, DeployableSpawner> ASSET_MAP;
   protected String id;
   protected AssetExtraInfo.Data data;
   private DeployableConfig config;
   private Vector3d[] positionOffsets;

   public DeployableSpawner(String id, DeployableConfig config, Vector3d[] positionOffsets) {
      this.id = id;
      this.config = config;
      this.positionOffsets = positionOffsets;
   }

   public DeployableSpawner() {
   }

   public static DefaultAssetMap<String, DeployableSpawner> getAssetMap() {
      if (ASSET_MAP == null) {
         ASSET_MAP = (DefaultAssetMap<String, DeployableSpawner>)AssetRegistry.getAssetStore(DeployableSpawner.class).getAssetMap();
      }

      return ASSET_MAP;
   }

   public Vector3d[] getPositionOffsets() {
      return this.positionOffsets;
   }

   public DeployableConfig getConfig() {
      return this.config;
   }

   public String getId() {
      return this.id;
   }
}
