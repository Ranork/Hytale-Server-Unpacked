package com.hypixel.hytale.assetstore;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DecodedAsset<K, T extends JsonAsset<K>> implements AssetHolder<K> {
   private final K key;
   private final T asset;

   public DecodedAsset(K key, T asset) {
      this.key = key;
      this.asset = asset;
   }

   public K getKey() {
      return this.key;
   }

   public T getAsset() {
      return this.asset;
   }

   @Override
   public boolean equals(@Nullable Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         DecodedAsset<?, ?> that = (DecodedAsset<?, ?>)o;
         if (this.key != null ? this.key.equals(that.key) : that.key == null) {
            return this.asset != null ? this.asset.equals(that.asset) : that.asset == null;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      int result = this.key != null ? this.key.hashCode() : 0;
      return 31 * result + (this.asset != null ? this.asset.hashCode() : 0);
   }

   @Nonnull
   @Override
   public String toString() {
      return "DecodedAsset{key=" + this.key + ", asset=" + this.asset + "}";
   }
}
