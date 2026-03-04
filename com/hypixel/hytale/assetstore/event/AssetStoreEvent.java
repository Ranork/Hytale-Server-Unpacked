package com.hypixel.hytale.assetstore.event;

import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.event.IEvent;
import javax.annotation.Nonnull;

public abstract class AssetStoreEvent<KeyType> implements IEvent<KeyType> {
   @Nonnull
   private final AssetStore<?, ?, ?> assetStore;

   public AssetStoreEvent(@Nonnull AssetStore<?, ?, ?> assetStore) {
      this.assetStore = assetStore;
   }

   @Nonnull
   public AssetStore<?, ?, ?> getAssetStore() {
      return this.assetStore;
   }

   @Nonnull
   @Override
   public String toString() {
      return "AssetStoreEvent{assetStore=" + this.assetStore + "}";
   }
}
