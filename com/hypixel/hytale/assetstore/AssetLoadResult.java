package com.hypixel.hytale.assetstore;

import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

public class AssetLoadResult<K, T> {
   private final Map<K, T> loadedAssets;
   private final Map<K, Path> loadedKeyToPathMap;
   private final Set<K> failedToLoadKeys;
   private final Set<Path> failedToLoadPaths;
   private final Map<Class<? extends JsonAssetWithMap>, AssetLoadResult> childAssetResults;

   public AssetLoadResult(
      Map<K, T> loadedAssets,
      Map<K, Path> loadedKeyToPathMap,
      Set<K> failedToLoadKeys,
      Set<Path> failedToLoadPaths,
      Map<Class<? extends JsonAssetWithMap>, AssetLoadResult> childAssetResults
   ) {
      this.loadedAssets = loadedAssets;
      this.loadedKeyToPathMap = loadedKeyToPathMap;
      this.failedToLoadKeys = failedToLoadKeys;
      this.failedToLoadPaths = failedToLoadPaths;
      this.childAssetResults = childAssetResults;
   }

   public Map<K, T> getLoadedAssets() {
      return this.loadedAssets;
   }

   public Map<K, Path> getLoadedKeyToPathMap() {
      return this.loadedKeyToPathMap;
   }

   public Set<K> getFailedToLoadKeys() {
      return this.failedToLoadKeys;
   }

   public Set<Path> getFailedToLoadPaths() {
      return this.failedToLoadPaths;
   }

   public boolean hasFailed() {
      if (this.failedToLoadKeys.isEmpty() && this.failedToLoadPaths.isEmpty()) {
         for (AssetLoadResult result : this.childAssetResults.values()) {
            if (result.hasFailed()) {
               return true;
            }
         }

         return false;
      } else {
         return true;
      }
   }
}
