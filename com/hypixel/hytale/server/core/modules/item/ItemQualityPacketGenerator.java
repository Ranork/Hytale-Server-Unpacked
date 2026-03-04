package com.hypixel.hytale.server.core.modules.item;

import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.UpdateType;
import com.hypixel.hytale.protocol.packets.assets.UpdateItemQualities;
import com.hypixel.hytale.server.core.asset.packet.SimpleAssetPacketGenerator;
import com.hypixel.hytale.server.core.asset.type.item.config.ItemQuality;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import javax.annotation.Nonnull;

public class ItemQualityPacketGenerator extends SimpleAssetPacketGenerator<String, ItemQuality, IndexedLookupTableAssetMap<String, ItemQuality>> {
   @Nonnull
   public ToClientPacket generateInitPacket(@Nonnull IndexedLookupTableAssetMap<String, ItemQuality> assetMap, @Nonnull Map<String, ItemQuality> assets) {
      UpdateItemQualities packet = new UpdateItemQualities();
      packet.type = UpdateType.Init;
      packet.itemQualities = new Int2ObjectOpenHashMap();

      for (Entry<String, ItemQuality> entry : assets.entrySet()) {
         String key = entry.getKey();
         int index = assetMap.getIndex(key);
         if (index == Integer.MIN_VALUE) {
            throw new IllegalArgumentException("Unknown key! " + key);
         }

         packet.itemQualities.put(index, entry.getValue().toPacket());
      }

      packet.maxId = assetMap.getNextIndex();
      return packet;
   }

   @Nonnull
   protected ToClientPacket generateUpdatePacket(
      @Nonnull IndexedLookupTableAssetMap<String, ItemQuality> assetMap, @Nonnull Map<String, ItemQuality> loadedAssets
   ) {
      UpdateItemQualities packet = new UpdateItemQualities();
      packet.type = UpdateType.AddOrUpdate;
      packet.itemQualities = new Int2ObjectOpenHashMap();

      for (Entry<String, ItemQuality> entry : loadedAssets.entrySet()) {
         String key = entry.getKey();
         int index = assetMap.getIndex(key);
         if (index == Integer.MIN_VALUE) {
            throw new IllegalArgumentException("Unknown key! " + key);
         }

         packet.itemQualities.put(index, entry.getValue().toPacket());
      }

      packet.maxId = assetMap.getNextIndex();
      return packet;
   }

   @Nonnull
   protected ToClientPacket generateRemovePacket(@Nonnull IndexedLookupTableAssetMap<String, ItemQuality> assetMap, @Nonnull Set<String> removed) {
      UpdateItemQualities packet = new UpdateItemQualities();
      packet.type = UpdateType.Remove;
      packet.itemQualities = new Int2ObjectOpenHashMap();

      for (String key : removed) {
         int index = assetMap.getIndex(key);
         if (index == Integer.MIN_VALUE) {
            throw new IllegalArgumentException("Unknown key! " + key);
         }

         packet.itemQualities.put(index, null);
      }

      packet.maxId = assetMap.getNextIndex();
      return packet;
   }
}
