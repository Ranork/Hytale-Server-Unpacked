package com.hypixel.hytale.assetstore.event;

import com.hypixel.hytale.assetstore.AssetMap;
import com.hypixel.hytale.assetstore.AssetUpdateQuery;
import com.hypixel.hytale.assetstore.JsonAsset;
import java.util.Collections;
import java.util.Map;
import javax.annotation.Nonnull;

public class LoadedAssetsEvent<K, T extends JsonAsset<K>, M extends AssetMap<K, T>> extends AssetsEvent<K, T> {
   @Nonnull
   private final Class<T> tClass;
   @Nonnull
   private final M assetMap;
   @Nonnull
   private final Map<K, T> loadedAssets;
   private final boolean initial;
   @Nonnull
   private final AssetUpdateQuery query;

   public LoadedAssetsEvent(@Nonnull Class<T> tClass, @Nonnull M assetMap, @Nonnull Map<K, T> loadedAssets, boolean initial, @Nonnull AssetUpdateQuery query) {
      this.tClass = tClass;
      this.assetMap = assetMap;
      this.loadedAssets = Collections.unmodifiableMap(loadedAssets);
      this.initial = initial;
      this.query = query;
   }

   public Class<T> getAssetClass() {
      return this.tClass;
   }

   public M getAssetMap() {
      return this.assetMap;
   }

   @Nonnull
   public Map<K, T> getLoadedAssets() {
      return this.loadedAssets;
   }

   public boolean isInitial() {
      return this.initial;
   }

   @Nonnull
   public AssetUpdateQuery getQuery() {
      return this.query;
   }

   @Nonnull
   @Override
   public String toString() {
      return "LoadedAssetsEvent{loadedAssets=" + this.loadedAssets + ", initial=" + this.initial + ", query=" + this.query + "} " + super.toString();
   }
}
