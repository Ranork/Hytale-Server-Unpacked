package com.hypixel.hytale.server.core.asset;

import com.hypixel.hytale.assetstore.AssetPack;
import com.hypixel.hytale.event.IEvent;

public class AssetPackRegisterEvent implements IEvent<Void> {
   private final AssetPack assetPack;

   public AssetPackRegisterEvent(AssetPack assetPack) {
      this.assetPack = assetPack;
   }

   public AssetPack getAssetPack() {
      return this.assetPack;
   }
}
