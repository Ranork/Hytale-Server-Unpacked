package com.hypixel.hytale.assetstore.event;

import com.hypixel.hytale.assetstore.AssetMap;
import com.hypixel.hytale.assetstore.JsonAsset;
import java.util.Collections;
import java.util.Set;
import javax.annotation.Nonnull;

public class RemovedAssetsEvent<K, T extends JsonAsset<K>, M extends AssetMap<K, T>> extends AssetsEvent<K, T> {
   private final Class<T> tClass;
   private final M assetMap;
   @Nonnull
   private final Set<K> removedAssets;
   private final boolean replaced;

   public RemovedAssetsEvent(Class<T> tClass, M assetMap, @Nonnull Set<K> removedAssets, boolean replaced) {
      this.tClass = tClass;
      this.assetMap = assetMap;
      this.removedAssets = Collections.unmodifiableSet(removedAssets);
      this.replaced = replaced;
   }

   public Class<T> getAssetClass() {
      return this.tClass;
   }

   public M getAssetMap() {
      return this.assetMap;
   }

   @Nonnull
   public Set<K> getRemovedAssets() {
      return this.removedAssets;
   }

   public boolean isReplaced() {
      return this.replaced;
   }

   @Nonnull
   @Override
   public String toString() {
      return "RemovedAssetsEvent{removedAssets=" + this.removedAssets + ", replaced=" + this.replaced + "} " + super.toString();
   }
}
