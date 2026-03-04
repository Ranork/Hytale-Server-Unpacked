package com.hypixel.hytale.server.core.modules.entity.repulsion;

import com.hypixel.hytale.assetstore.AssetUpdateQuery;
import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.UpdateType;
import com.hypixel.hytale.protocol.packets.assets.UpdateRepulsionConfig;
import com.hypixel.hytale.server.core.asset.packet.AssetPacketGenerator;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import javax.annotation.Nonnull;

public class RepulsionConfigPacketGenerator extends AssetPacketGenerator<String, RepulsionConfig, IndexedLookupTableAssetMap<String, RepulsionConfig>> {
   @Nonnull
   public ToClientPacket generateInitPacket(@Nonnull IndexedLookupTableAssetMap<String, RepulsionConfig> assetMap, @Nonnull Map<String, RepulsionConfig> assets) {
      Int2ObjectMap<com.hypixel.hytale.protocol.RepulsionConfig> repulsionConfigs = new Int2ObjectOpenHashMap();

      for (Entry<String, RepulsionConfig> entry : assets.entrySet()) {
         repulsionConfigs.put(assetMap.getIndex(entry.getKey()), entry.getValue().toPacket());
      }

      return new UpdateRepulsionConfig(UpdateType.Init, assetMap.getNextIndex(), repulsionConfigs);
   }

   @Nonnull
   public ToClientPacket generateUpdatePacket(
      @Nonnull IndexedLookupTableAssetMap<String, RepulsionConfig> assetMap,
      @Nonnull Map<String, RepulsionConfig> loadedAssets,
      @Nonnull AssetUpdateQuery query
   ) {
      Int2ObjectMap<com.hypixel.hytale.protocol.RepulsionConfig> repulsionConfigs = new Int2ObjectOpenHashMap();

      for (Entry<String, RepulsionConfig> entry : loadedAssets.entrySet()) {
         repulsionConfigs.put(assetMap.getIndex(entry.getKey()), entry.getValue().toPacket());
      }

      return new UpdateRepulsionConfig(UpdateType.AddOrUpdate, assetMap.getNextIndex(), repulsionConfigs);
   }

   @Nonnull
   public ToClientPacket generateRemovePacket(
      @Nonnull IndexedLookupTableAssetMap<String, RepulsionConfig> assetMap, @Nonnull Set<String> removed, @Nonnull AssetUpdateQuery query
   ) {
      Int2ObjectMap<com.hypixel.hytale.protocol.RepulsionConfig> repulsionConfigs = new Int2ObjectOpenHashMap();

      for (String entry : removed) {
         repulsionConfigs.put(assetMap.getIndex(entry), new com.hypixel.hytale.protocol.RepulsionConfig());
      }

      return new UpdateRepulsionConfig(UpdateType.Remove, assetMap.getNextIndex(), repulsionConfigs);
   }
}
