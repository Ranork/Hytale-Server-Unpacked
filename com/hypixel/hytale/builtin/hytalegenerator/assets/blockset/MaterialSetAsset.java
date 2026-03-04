package com.hypixel.hytale.builtin.hytalegenerator.assets.blockset;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.builtin.hytalegenerator.MaterialSet;
import com.hypixel.hytale.builtin.hytalegenerator.assets.Cleanable;
import com.hypixel.hytale.builtin.hytalegenerator.assets.material.MaterialAsset;
import com.hypixel.hytale.builtin.hytalegenerator.material.Material;
import com.hypixel.hytale.builtin.hytalegenerator.material.MaterialCache;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import javax.annotation.Nonnull;

public class MaterialSetAsset implements JsonAssetWithMap<String, DefaultAssetMap<String, MaterialSetAsset>>, Cleanable {
   @Nonnull
   public static final AssetBuilderCodec<String, MaterialSetAsset> CODEC = AssetBuilderCodec.builder(
         MaterialSetAsset.class,
         MaterialSetAsset::new,
         Codec.STRING,
         (asset, id) -> asset.id = id,
         config -> config.id,
         (config, data) -> config.data = data,
         config -> config.data
      )
      .append(new KeyedCodec<>("Inclusive", Codec.BOOLEAN, false), (t, k) -> t.inclusive = k, t -> t.inclusive)
      .add()
      .append(
         new KeyedCodec<>("Materials", new ArrayCodec<>(MaterialAsset.CODEC, MaterialAsset[]::new), true),
         (asset, value) -> asset.materialAssets = value,
         asset -> asset.materialAssets
      )
      .add()
      .build();
   private String id;
   private AssetExtraInfo.Data data;
   private boolean inclusive = true;
   private MaterialAsset[] materialAssets = new MaterialAsset[0];

   @Nonnull
   public MaterialSet build(@Nonnull MaterialCache materialCache) {
      List<Material> materials = new ObjectArrayList(this.materialAssets.length);

      for (MaterialAsset materialAsset : this.materialAssets) {
         if (materialAsset != null) {
            materials.add(materialAsset.build(materialCache));
         }
      }

      return new MaterialSet(this.inclusive, materials);
   }

   public String getId() {
      return this.id;
   }

   @Override
   public void cleanUp() {
      for (MaterialAsset materialAsset : this.materialAssets) {
         materialAsset.cleanUp();
      }
   }
}
