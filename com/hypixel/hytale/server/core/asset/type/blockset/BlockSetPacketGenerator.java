package com.hypixel.hytale.server.core.asset.type.blockset;

import com.hypixel.hytale.assetstore.AssetUpdateQuery;
import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.UpdateType;
import com.hypixel.hytale.protocol.packets.assets.UpdateBlockSets;
import com.hypixel.hytale.server.core.asset.packet.AssetPacketGenerator;
import com.hypixel.hytale.server.core.asset.type.blockset.config.BlockSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockSetPacketGenerator extends AssetPacketGenerator<String, BlockSet, IndexedLookupTableAssetMap<String, BlockSet>> {
   @Nonnull
   public ToClientPacket generateInitPacket(@Nonnull IndexedLookupTableAssetMap<String, BlockSet> assetMap, Map<String, BlockSet> assets) {
      UpdateBlockSets packet = new UpdateBlockSets();
      packet.type = UpdateType.Init;
      packet.blockSets = assetMap.getAssetMap().entrySet().stream().collect(Collectors.toMap(Entry::getKey, entry -> entry.getValue().toPacket()));
      return packet;
   }

   @Nonnull
   public ToClientPacket generateUpdatePacket(
      IndexedLookupTableAssetMap<String, BlockSet> assetMap, @Nonnull Map<String, BlockSet> loadedAssets, @Nonnull AssetUpdateQuery query
   ) {
      UpdateBlockSets packet = new UpdateBlockSets();
      packet.type = UpdateType.AddOrUpdate;
      packet.blockSets = loadedAssets.entrySet().stream().collect(Collectors.toMap(Entry::getKey, entry -> entry.getValue().toPacket()));
      return packet;
   }

   @Nullable
   public ToClientPacket generateRemovePacket(
      IndexedLookupTableAssetMap<String, BlockSet> assetMap, @Nonnull Set<String> removed, @Nonnull AssetUpdateQuery query
   ) {
      UpdateBlockSets packet = new UpdateBlockSets();
      packet.type = UpdateType.Remove;
      packet.blockSets = new Object2ObjectOpenHashMap();

      for (String string : removed) {
         packet.blockSets.put(string, null);
      }

      return null;
   }
}
