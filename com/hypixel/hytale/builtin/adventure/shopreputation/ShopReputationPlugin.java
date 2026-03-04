package com.hypixel.hytale.builtin.adventure.shopreputation;

import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.builtin.adventure.reputation.assets.ReputationGroup;
import com.hypixel.hytale.builtin.adventure.reputation.assets.ReputationRank;
import com.hypixel.hytale.builtin.adventure.shop.ShopAsset;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import javax.annotation.Nonnull;

public class ShopReputationPlugin extends JavaPlugin {
   public ShopReputationPlugin(@Nonnull JavaPluginInit init) {
      super(init);
   }

   @Override
   protected void setup() {
      AssetRegistry.getAssetStore(ShopAsset.class).injectLoadsAfter(ReputationGroup.class);
      AssetRegistry.getAssetStore(ShopAsset.class).injectLoadsAfter(ReputationRank.class);
   }
}
