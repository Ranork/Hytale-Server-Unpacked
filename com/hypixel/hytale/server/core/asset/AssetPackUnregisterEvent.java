package com.hypixel.hytale.server.core.asset;

import com.hypixel.hytale.assetstore.AssetPack;
import com.hypixel.hytale.event.IEvent;

public class AssetPackUnregisterEvent implements IEvent<Void> {
   private final AssetPack assetPack;

   public AssetPackUnregisterEvent(AssetPack assetPack) {
      this.assetPack = assetPack;
   }

   public AssetPack getAssetPack() {
      return this.assetPack;
   }
}
