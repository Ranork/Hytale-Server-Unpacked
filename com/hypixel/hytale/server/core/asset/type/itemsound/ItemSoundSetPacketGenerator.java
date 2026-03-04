package com.hypixel.hytale.server.core.asset.type.itemsound;

import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.UpdateType;
import com.hypixel.hytale.protocol.packets.assets.UpdateItemSoundSets;
import com.hypixel.hytale.server.core.asset.packet.SimpleAssetPacketGenerator;
import com.hypixel.hytale.server.core.asset.type.itemsound.config.ItemSoundSet;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import javax.annotation.Nonnull;

public class ItemSoundSetPacketGenerator extends SimpleAssetPacketGenerator<String, ItemSoundSet, IndexedLookupTableAssetMap<String, ItemSoundSet>> {
   @Nonnull
   public ToClientPacket generateInitPacket(@Nonnull IndexedLookupTableAssetMap<String, ItemSoundSet> assetMap, @Nonnull Map<String, ItemSoundSet> assets) {
      UpdateItemSoundSets packet = new UpdateItemSoundSets();
      packet.type = UpdateType.Init;
      packet.itemSoundSets = new Int2ObjectOpenHashMap();

      for (Entry<String, ItemSoundSet> entry : assets.entrySet()) {
         String key = entry.getKey();
         int index = assetMap.getIndex(key);
         if (index == Integer.MIN_VALUE) {
            throw new IllegalArgumentException("Unknown key! " + key);
         }

         packet.itemSoundSets.put(index, entry.getValue().toPacket());
      }

      packet.maxId = assetMap.getNextIndex();
      return packet;
   }

   @Nonnull
   public ToClientPacket generateUpdatePacket(
      @Nonnull IndexedLookupTableAssetMap<String, ItemSoundSet> assetMap, @Nonnull Map<String, ItemSoundSet> loadedAssets
   ) {
      UpdateItemSoundSets packet = new UpdateItemSoundSets();
      packet.type = UpdateType.AddOrUpdate;
      packet.itemSoundSets = new Int2ObjectOpenHashMap();

      for (Entry<String, ItemSoundSet> entry : loadedAssets.entrySet()) {
         String key = entry.getKey();
         int index = assetMap.getIndex(key);
         if (index == Integer.MIN_VALUE) {
            throw new IllegalArgumentException("Unknown key! " + key);
         }

         packet.itemSoundSets.put(index, entry.getValue().toPacket());
      }

      packet.maxId = assetMap.getNextIndex();
      return packet;
   }

   @Nonnull
   public ToClientPacket generateRemovePacket(@Nonnull IndexedLookupTableAssetMap<String, ItemSoundSet> assetMap, @Nonnull Set<String> removed) {
      UpdateItemSoundSets packet = new UpdateItemSoundSets();
      packet.type = UpdateType.Remove;
      packet.itemSoundSets = new Int2ObjectOpenHashMap();

      for (String key : removed) {
         int index = assetMap.getIndex(key);
         if (index == Integer.MIN_VALUE) {
            throw new IllegalArgumentException("Unknown key! " + key);
         }

         packet.itemSoundSets.put(index, null);
      }

      packet.maxId = assetMap.getNextIndex();
      return packet;
   }
}
