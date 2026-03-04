package com.hypixel.hytale.builtin.hytalegenerator.assets.material;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.builtin.hytalegenerator.assets.Cleanable;
import com.hypixel.hytale.builtin.hytalegenerator.material.FluidMaterial;
import com.hypixel.hytale.builtin.hytalegenerator.material.Material;
import com.hypixel.hytale.builtin.hytalegenerator.material.MaterialCache;
import com.hypixel.hytale.builtin.hytalegenerator.material.SolidMaterial;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.Rotation;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.RotationTuple;
import javax.annotation.Nonnull;

public class MaterialAsset implements JsonAssetWithMap<String, DefaultAssetMap<String, MaterialAsset>>, Cleanable {
   @Nonnull
   public static final AssetBuilderCodec<String, MaterialAsset> CODEC = AssetBuilderCodec.builder(
         MaterialAsset.class,
         MaterialAsset::new,
         Codec.STRING,
         (asset, id) -> asset.id = id,
         config -> config.id,
         (config, data) -> config.data = data,
         config -> config.data
      )
      .append(new KeyedCodec<>("Solid", Codec.STRING, true), (t, value) -> t.solidName = value, t -> t.solidName)
      .add()
      .append(new KeyedCodec<>("Fluid", Codec.STRING, true), (t, value) -> t.fluidName = value, t -> t.fluidName)
      .add()
      .append(new KeyedCodec<>("SolidBottomUp", Codec.BOOLEAN, false), (t, value) -> t.isSolidBottomUp = value, t -> t.isSolidBottomUp)
      .add()
      .append(new KeyedCodec<>("SolidRotation", OrthogonalRotationAsset.CODEC, false), (t, value) -> t.solidRotationAsset = value, t -> t.solidRotationAsset)
      .add()
      .build();
   private String id;
   private AssetExtraInfo.Data data;
   @Nonnull
   private String solidName = "";
   @Nonnull
   private String fluidName = "";
   private boolean isSolidBottomUp = false;
   private OrthogonalRotationAsset solidRotationAsset = new OrthogonalRotationAsset();

   public MaterialAsset() {
   }

   public MaterialAsset(@Nonnull String solidName, @Nonnull String fluidName, boolean isSolidBottomUp) {
      this.solidName = solidName;
      this.fluidName = fluidName;
      this.isSolidBottomUp = isSolidBottomUp;
   }

   public MaterialAsset(@Nonnull String solidName, @Nonnull String fluidName, @Nonnull OrthogonalRotationAsset solidRotationAsset) {
      this.solidName = solidName;
      this.fluidName = fluidName;
      this.isSolidBottomUp = false;
      this.solidRotationAsset = solidRotationAsset;
   }

   @Nonnull
   public Material build(@Nonnull MaterialCache materialCache) {
      RotationTuple rotation;
      if (this.solidRotationAsset.isNone() && this.isSolidBottomUp) {
         rotation = RotationTuple.of(Rotation.None, Rotation.OneEighty, Rotation.None);
      } else {
         rotation = this.solidRotationAsset.build();
      }

      SolidMaterial solid = materialCache.EMPTY_AIR;
      if (!this.solidName.isEmpty()) {
         solid = materialCache.getSolidMaterial(this.solidName, rotation);
      }

      FluidMaterial fluid = materialCache.EMPTY_FLUID;
      if (!this.fluidName.isEmpty()) {
         fluid = materialCache.getFluidMaterial(this.fluidName);
      }

      return new Material(solid, fluid);
   }

   public String getId() {
      return this.id;
   }

   @Override
   public void cleanUp() {
   }
}
