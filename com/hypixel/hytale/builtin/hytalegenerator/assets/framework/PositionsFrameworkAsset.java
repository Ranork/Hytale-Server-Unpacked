package com.hypixel.hytale.builtin.hytalegenerator.assets.framework;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.builtin.hytalegenerator.assets.positionproviders.ListPositionProviderAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.positionproviders.PositionProviderAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.worldstructures.WorldStructureAsset;
import com.hypixel.hytale.builtin.hytalegenerator.referencebundle.ReferenceBundle;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import java.util.HashMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class PositionsFrameworkAsset extends FrameworkAsset {
   @Nonnull
   public static final String NAME = "Positions";
   @Nonnull
   public static final Class<PositionsFrameworkAsset.Entries> CLASS = PositionsFrameworkAsset.Entries.class;
   @Nonnull
   public static final BuilderCodec<PositionsFrameworkAsset> CODEC = BuilderCodec.builder(
         PositionsFrameworkAsset.class, PositionsFrameworkAsset::new, FrameworkAsset.ABSTRACT_CODEC
      )
      .append(
         new KeyedCodec<>("Entries", new ArrayCodec<>(PositionsFrameworkAsset.EntryAsset.CODEC, PositionsFrameworkAsset.EntryAsset[]::new), true),
         (asset, value) -> asset.entryAssets = value,
         asset -> asset.entryAssets
      )
      .add()
      .build();
   private String id;
   private AssetExtraInfo.Data data;
   private PositionsFrameworkAsset.EntryAsset[] entryAssets = new PositionsFrameworkAsset.EntryAsset[0];

   private PositionsFrameworkAsset() {
   }

   @Override
   public String getId() {
      return this.id;
   }

   @Override
   public void build(@NonNullDecl WorldStructureAsset.Argument argument, @NonNullDecl ReferenceBundle referenceBundle) {
      PositionsFrameworkAsset.Entries entries = new PositionsFrameworkAsset.Entries();

      for (PositionsFrameworkAsset.EntryAsset entryAsset : this.entryAssets) {
         entries.put(entryAsset.name, entryAsset.positionProviderAsset);
      }

      referenceBundle.put("Positions", entries, CLASS);
   }

   public static class Entries extends HashMap<String, PositionProviderAsset> {
      @Nullable
      public static PositionsFrameworkAsset.Entries get(@Nonnull ReferenceBundle referenceBundle) {
         return referenceBundle.get("Positions", PositionsFrameworkAsset.CLASS);
      }

      @Nullable
      public static PositionProviderAsset get(@Nonnull String name, @Nonnull ReferenceBundle referenceBundle) {
         PositionsFrameworkAsset.Entries entries = get(referenceBundle);
         return entries == null ? null : entries.get(name);
      }
   }

   public static class EntryAsset implements JsonAssetWithMap<String, DefaultAssetMap<String, PositionsFrameworkAsset.EntryAsset>> {
      @Nonnull
      public static final AssetBuilderCodec<String, PositionsFrameworkAsset.EntryAsset> CODEC = AssetBuilderCodec.builder(
            PositionsFrameworkAsset.EntryAsset.class,
            PositionsFrameworkAsset.EntryAsset::new,
            Codec.STRING,
            (asset, id) -> asset.id = id,
            config -> config.id,
            (config, data) -> config.data = data,
            config -> config.data
         )
         .append(new KeyedCodec<>("Name", Codec.STRING, true), (asset, value) -> asset.name = value, asset -> asset.name)
         .add()
         .append(
            new KeyedCodec<>("Positions", PositionProviderAsset.CODEC, true),
            (asset, value) -> asset.positionProviderAsset = value,
            asset -> asset.positionProviderAsset
         )
         .add()
         .build();
      private String id;
      private AssetExtraInfo.Data data;
      private String name = "";
      private PositionProviderAsset positionProviderAsset = new ListPositionProviderAsset();

      public String getId() {
         return this.id;
      }
   }
}
