package com.hypixel.hytale.builtin.asseteditor.util;

import com.hypixel.hytale.assetstore.AssetMap;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.map.BlockTypeAssetMap;
import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.assetstore.map.LookupTableAssetMap;
import javax.annotation.Nonnull;

public class AssetStoreUtil {
   @Deprecated
   public static <K, T extends JsonAssetWithMap<K, M>, M extends AssetMap<K, T>> String getIdFromIndex(@Nonnull AssetStore<K, T, M> assetStore, int assetIndex) {
      M assetMap = assetStore.getAssetMap();
      if (assetMap instanceof BlockTypeAssetMap) {
         return ((BlockTypeAssetMap)assetMap).getAsset(assetIndex).getId().toString();
      } else if (assetMap instanceof IndexedLookupTableAssetMap) {
         return ((IndexedLookupTableAssetMap)assetMap).getAsset(assetIndex).getId().toString();
      } else if (assetMap instanceof LookupTableAssetMap) {
         return ((LookupTableAssetMap)assetMap).getAsset(assetIndex).getId().toString();
      } else {
         throw new IllegalArgumentException("Asset can't be looked up by index! " + assetIndex);
      }
   }
}
