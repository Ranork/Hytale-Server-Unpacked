package com.hypixel.hytale.server.core.asset.common.events;

import com.hypixel.hytale.event.IAsyncEvent;
import com.hypixel.hytale.protocol.Asset;
import com.hypixel.hytale.server.core.io.PacketHandler;
import java.util.Arrays;
import javax.annotation.Nonnull;

public class SendCommonAssetsEvent implements IAsyncEvent<Void> {
   private final PacketHandler packetHandler;
   private final Asset[] assets;

   public SendCommonAssetsEvent(PacketHandler packetHandler, Asset[] assets) {
      this.packetHandler = packetHandler;
      this.assets = assets;
   }

   public PacketHandler getPacketHandler() {
      return this.packetHandler;
   }

   public Asset[] getRequestedAssets() {
      return this.assets;
   }

   @Nonnull
   @Override
   public String toString() {
      return "SendCommonAssetsEvent{packetHandler=" + this.packetHandler + ", assets=" + Arrays.toString((Object[])this.assets) + "}";
   }
}
