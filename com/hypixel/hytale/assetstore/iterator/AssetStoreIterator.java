package com.hypixel.hytale.assetstore.iterator;

import com.hypixel.hytale.assetstore.AssetMap;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.JsonAsset;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AssetStoreIterator implements Iterator<AssetStore<?, ?, ?>>, Closeable {
   @Nonnull
   private final List<AssetStore<?, ?, ?>> list;

   public AssetStoreIterator(@Nonnull Collection<AssetStore<?, ?, ?>> values) {
      this.list = new ArrayList<>(values);
   }

   @Override
   public boolean hasNext() {
      return !this.list.isEmpty();
   }

   @Nullable
   public AssetStore<?, ?, ?> next() {
      Iterator<AssetStore<?, ?, ?>> iterator = this.list.iterator();

      while (iterator.hasNext()) {
         AssetStore<?, ? extends JsonAssetWithMap<?, ? extends AssetMap<?, ?>>, ? extends AssetMap<?, ? extends JsonAssetWithMap<?, ?>>> assetStore = (AssetStore<?, ? extends JsonAssetWithMap<?, ? extends AssetMap<?, ?>>, ? extends AssetMap<?, ? extends JsonAssetWithMap<?, ?>>>)iterator.next();
         if (!this.isWaitingForDependencies(assetStore)) {
            iterator.remove();
            return assetStore;
         }
      }

      return null;
   }

   public int size() {
      return this.list.size();
   }

   public boolean isWaitingForDependencies(@Nonnull AssetStore<?, ?, ?> assetStore) {
      for (Class<? extends JsonAsset<?>> aClass : assetStore.getLoadsAfter()) {
         AssetStore otherStore = AssetRegistry.getAssetStore(aClass);
         if (otherStore == null) {
            throw new IllegalArgumentException("Unable to find asset store: " + aClass);
         }

         if (this.list.contains(otherStore)) {
            return true;
         }
      }

      return false;
   }

   public boolean isBeingWaitedFor(@Nonnull AssetStore<?, ?, ?> assetStore) {
      Class<? extends JsonAssetWithMap<?, ? extends AssetMap<?, ?>>> assetClass = (Class<? extends JsonAssetWithMap<?, ? extends AssetMap<?, ?>>>)assetStore.getAssetClass();

      for (AssetStore<?, ?, ?> store : this.list) {
         if (store.getLoadsAfter().contains(assetClass)) {
            return true;
         }
      }

      return false;
   }

   @Override
   public void close() {
   }
}
