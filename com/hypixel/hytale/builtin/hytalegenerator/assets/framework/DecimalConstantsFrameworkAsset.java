package com.hypixel.hytale.builtin.hytalegenerator.assets.framework;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.builtin.hytalegenerator.assets.worldstructures.WorldStructureAsset;
import com.hypixel.hytale.builtin.hytalegenerator.referencebundle.ReferenceBundle;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import java.util.HashMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DecimalConstantsFrameworkAsset extends FrameworkAsset {
   @Nonnull
   public static final String NAME = "DecimalConstants";
   @Nonnull
   public static final Class<DecimalConstantsFrameworkAsset.Entries> CLASS = DecimalConstantsFrameworkAsset.Entries.class;
   @Nonnull
   public static final BuilderCodec<DecimalConstantsFrameworkAsset> CODEC = BuilderCodec.builder(
         DecimalConstantsFrameworkAsset.class, DecimalConstantsFrameworkAsset::new, FrameworkAsset.ABSTRACT_CODEC
      )
      .append(
         new KeyedCodec<>("Entries", new ArrayCodec<>(DecimalConstantsFrameworkAsset.EntryAsset.CODEC, DecimalConstantsFrameworkAsset.EntryAsset[]::new), true),
         (asset, value) -> asset.entryAssets = value,
         asset -> asset.entryAssets
      )
      .add()
      .build();
   private String id;
   private AssetExtraInfo.Data data;
   private DecimalConstantsFrameworkAsset.EntryAsset[] entryAssets = new DecimalConstantsFrameworkAsset.EntryAsset[0];

   private DecimalConstantsFrameworkAsset() {
   }

   @Override
   public String getId() {
      return this.id;
   }

   @Override
   public void build(@Nonnull WorldStructureAsset.Argument argument, @Nonnull ReferenceBundle referenceBundle) {
      DecimalConstantsFrameworkAsset.Entries entries = new DecimalConstantsFrameworkAsset.Entries();

      for (DecimalConstantsFrameworkAsset.EntryAsset entryAsset : this.entryAssets) {
         entries.put(entryAsset.name, entryAsset.value);
      }

      referenceBundle.put("DecimalConstants", entries, CLASS);
   }

   public static class Entries extends HashMap<String, Double> {
      @Nullable
      public static DecimalConstantsFrameworkAsset.Entries get(@Nonnull ReferenceBundle referenceBundle) {
         return referenceBundle.get("DecimalConstants", DecimalConstantsFrameworkAsset.CLASS);
      }

      @Nullable
      public static Double get(@Nonnull String name, @Nonnull ReferenceBundle referenceBundle) {
         DecimalConstantsFrameworkAsset.Entries entries = get(referenceBundle);
         return entries == null ? null : entries.get(name);
      }
   }

   public static class EntryAsset implements JsonAssetWithMap<String, DefaultAssetMap<String, DecimalConstantsFrameworkAsset.EntryAsset>> {
      @Nonnull
      public static final AssetBuilderCodec<String, DecimalConstantsFrameworkAsset.EntryAsset> CODEC = AssetBuilderCodec.builder(
            DecimalConstantsFrameworkAsset.EntryAsset.class,
            DecimalConstantsFrameworkAsset.EntryAsset::new,
            Codec.STRING,
            (asset, id) -> asset.id = id,
            config -> config.id,
            (config, data) -> config.data = data,
            config -> config.data
         )
         .append(new KeyedCodec<>("Name", Codec.STRING, true), (asset, value) -> asset.name = value, asset -> asset.name)
         .add()
         .append(new KeyedCodec<>("Value", Codec.DOUBLE, true), (asset, value) -> asset.value = value, asset -> asset.value)
         .add()
         .build();
      private String id;
      private AssetExtraInfo.Data data;
      private String name = "";
      private double value = 0.0;

      public String getId() {
         return this.id;
      }
   }
}
