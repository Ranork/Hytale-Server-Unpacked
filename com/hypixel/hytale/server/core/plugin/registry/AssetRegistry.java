package com.hypixel.hytale.server.core.plugin.registry;

import com.hypixel.hytale.assetstore.AssetMap;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.function.consumer.BooleanConsumer;
import java.util.List;
import javax.annotation.Nonnull;

public class AssetRegistry {
   protected final List<BooleanConsumer> unregister;

   public AssetRegistry(List<BooleanConsumer> unregister) {
      this.unregister = unregister;
   }

   @Nonnull
   public <K, T extends JsonAssetWithMap<K, M>, M extends AssetMap<K, T>, S extends AssetStore<K, T, M>> AssetRegistry register(@Nonnull S assetStore) {
      com.hypixel.hytale.assetstore.AssetRegistry.register(assetStore);
      this.unregister.add(shutdown -> com.hypixel.hytale.assetstore.AssetRegistry.unregister(assetStore));
      return this;
   }

   public void shutdown() {
   }
}
