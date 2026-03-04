package com.hypixel.hytale.assetstore;

import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import java.util.Collections;
import java.util.Set;
import javax.annotation.Nonnull;

public class AssetReferences<CK, C extends JsonAssetWithMap<CK, ?>> {
   private final Class<C> parentAssetClass;
   private final Set<CK> parentKeys;

   public AssetReferences(Class<C> parentAssetClass, Set<CK> parentKeys) {
      this.parentAssetClass = parentAssetClass;
      this.parentKeys = parentKeys;
   }

   public Class<C> getParentAssetClass() {
      return this.parentAssetClass;
   }

   public Set<CK> getParentKeys() {
      return this.parentKeys;
   }

   public <T extends JsonAssetWithMap<K, ?>, K> void addChildAssetReferences(Class<T> tClass, K childKey) {
      Class parentAssetClass = this.parentAssetClass;
      AssetStore<CK, C, ?> assetStore = AssetRegistry.getAssetStore(parentAssetClass);

      for (CK parentKey : this.parentKeys) {
         assetStore.addChildAssetReferences(parentKey, tClass, Collections.singleton(childKey));
      }
   }

   @Nonnull
   @Override
   public String toString() {
      return "AssetReferences{parentAssetClass=" + this.parentAssetClass + ", parentKeys=" + this.parentKeys + "}";
   }
}
