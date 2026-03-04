package com.hypixel.hytale.assetstore.event;

import com.hypixel.hytale.assetstore.AssetStore;
import javax.annotation.Nonnull;

public class RegisterAssetStoreEvent extends AssetStoreEvent<Void> {
   public RegisterAssetStoreEvent(@Nonnull AssetStore<?, ?, ?> assetStore) {
      super(assetStore);
   }
}
