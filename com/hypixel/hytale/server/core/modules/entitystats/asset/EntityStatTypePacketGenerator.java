package com.hypixel.hytale.server.core.modules.entitystats.asset;

import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.UpdateType;
import com.hypixel.hytale.protocol.packets.assets.UpdateEntityStatTypes;
import com.hypixel.hytale.server.core.asset.packet.SimpleAssetPacketGenerator;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import javax.annotation.Nonnull;

public class EntityStatTypePacketGenerator extends SimpleAssetPacketGenerator<String, EntityStatType, IndexedLookupTableAssetMap<String, EntityStatType>> {
   @Nonnull
   public ToClientPacket generateInitPacket(@Nonnull IndexedLookupTableAssetMap<String, EntityStatType> assetMap, @Nonnull Map<String, EntityStatType> assets) {
      UpdateEntityStatTypes packet = new UpdateEntityStatTypes();
      packet.type = UpdateType.Init;
      packet.types = new Int2ObjectOpenHashMap();

      for (Entry<String, EntityStatType> entry : assets.entrySet()) {
         String key = entry.getKey();
         int index = assetMap.getIndex(key);
         if (index == Integer.MIN_VALUE) {
            throw new IllegalArgumentException("Unknown key! " + key);
         }

         packet.types.put(index, entry.getValue().toPacket());
      }

      packet.maxId = assetMap.getNextIndex();
      return packet;
   }

   @Nonnull
   public ToClientPacket generateUpdatePacket(
      @Nonnull IndexedLookupTableAssetMap<String, EntityStatType> assetMap, @Nonnull Map<String, EntityStatType> loadedAssets
   ) {
      UpdateEntityStatTypes packet = new UpdateEntityStatTypes();
      packet.type = UpdateType.AddOrUpdate;
      packet.types = new Int2ObjectOpenHashMap();

      for (Entry<String, EntityStatType> entry : loadedAssets.entrySet()) {
         String key = entry.getKey();
         int index = assetMap.getIndex(key);
         if (index == Integer.MIN_VALUE) {
            throw new IllegalArgumentException("Unknown key! " + key);
         }

         packet.types.put(index, entry.getValue().toPacket());
      }

      packet.maxId = assetMap.getNextIndex();
      return packet;
   }

   @Nonnull
   public ToClientPacket generateRemovePacket(@Nonnull IndexedLookupTableAssetMap<String, EntityStatType> assetMap, @Nonnull Set<String> removed) {
      UpdateEntityStatTypes packet = new UpdateEntityStatTypes();
      packet.type = UpdateType.Remove;
      packet.types = new Int2ObjectOpenHashMap();

      for (String key : removed) {
         int index = assetMap.getIndex(key);
         if (index == Integer.MIN_VALUE) {
            throw new IllegalArgumentException("Unknown key! " + key);
         }

         packet.types.put(index, null);
      }

      packet.maxId = assetMap.getNextIndex();
      return packet;
   }
}
