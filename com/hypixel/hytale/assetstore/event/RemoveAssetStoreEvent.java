package com.hypixel.hytale.assetstore.event;

import com.hypixel.hytale.assetstore.AssetStore;
import javax.annotation.Nonnull;

public class RemoveAssetStoreEvent extends AssetStoreEvent<Void> {
   public RemoveAssetStoreEvent(@Nonnull AssetStore<?, ?, ?> assetStore) {
      super(assetStore);
   }
}
