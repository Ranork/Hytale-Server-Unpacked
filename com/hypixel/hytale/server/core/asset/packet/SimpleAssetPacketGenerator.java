package com.hypixel.hytale.server.core.asset.packet;

import com.hypixel.hytale.assetstore.AssetMap;
import com.hypixel.hytale.assetstore.AssetUpdateQuery;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.protocol.ToClientPacket;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class SimpleAssetPacketGenerator<K, T extends JsonAssetWithMap<K, M>, M extends AssetMap<K, T>> extends AssetPacketGenerator<K, T, M> {
   @Override
   public abstract ToClientPacket generateInitPacket(M var1, Map<K, T> var2);

   @Override
   public ToClientPacket generateUpdatePacket(M assetMap, Map<K, T> loadedAssets, @Nonnull AssetUpdateQuery query) {
      return this.generateUpdatePacket(assetMap, loadedAssets);
   }

   @Override
   public ToClientPacket generateRemovePacket(M assetMap, Set<K> removed, @Nonnull AssetUpdateQuery query) {
      return this.generateRemovePacket(assetMap, removed);
   }

   protected abstract ToClientPacket generateUpdatePacket(M var1, Map<K, T> var2);

   @Nullable
   protected abstract ToClientPacket generateRemovePacket(M var1, Set<K> var2);
}
