package com.hypixel.hytale.server.core.modules.item;

import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.UpdateType;
import com.hypixel.hytale.protocol.packets.assets.UpdateItemReticles;
import com.hypixel.hytale.server.core.asset.packet.SimpleAssetPacketGenerator;
import com.hypixel.hytale.server.core.asset.type.item.config.ItemReticleConfig;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import javax.annotation.Nonnull;

public class ItemReticleConfigPacketGenerator
   extends SimpleAssetPacketGenerator<String, ItemReticleConfig, IndexedLookupTableAssetMap<String, ItemReticleConfig>> {
   @Nonnull
   public ToClientPacket generateInitPacket(
      @Nonnull IndexedLookupTableAssetMap<String, ItemReticleConfig> assetMap, @Nonnull Map<String, ItemReticleConfig> assets
   ) {
      UpdateItemReticles packet = new UpdateItemReticles();
      packet.type = UpdateType.Init;
      packet.itemReticleConfigs = new Int2ObjectOpenHashMap();

      for (Entry<String, ItemReticleConfig> entry : assets.entrySet()) {
         String key = entry.getKey();
         int index = assetMap.getIndex(key);
         if (index == Integer.MIN_VALUE) {
            throw new IllegalArgumentException("Unknown key! " + key);
         }

         packet.itemReticleConfigs.put(index, entry.getValue().toPacket());
      }

      packet.maxId = assetMap.getNextIndex();
      return packet;
   }

   @Nonnull
   public ToClientPacket generateUpdatePacket(
      @Nonnull IndexedLookupTableAssetMap<String, ItemReticleConfig> assetMap, @Nonnull Map<String, ItemReticleConfig> loadedAssets
   ) {
      UpdateItemReticles packet = new UpdateItemReticles();
      packet.type = UpdateType.AddOrUpdate;
      packet.itemReticleConfigs = new Int2ObjectOpenHashMap();

      for (Entry<String, ItemReticleConfig> entry : loadedAssets.entrySet()) {
         String key = entry.getKey();
         int index = assetMap.getIndex(key);
         if (index == Integer.MIN_VALUE) {
            throw new IllegalArgumentException("Unknown key! " + key);
         }

         packet.itemReticleConfigs.put(index, entry.getValue().toPacket());
      }

      packet.maxId = assetMap.getNextIndex();
      return packet;
   }

   @Nonnull
   public ToClientPacket generateRemovePacket(@Nonnull IndexedLookupTableAssetMap<String, ItemReticleConfig> assetMap, @Nonnull Set<String> removed) {
      UpdateItemReticles packet = new UpdateItemReticles();
      packet.type = UpdateType.Remove;
      packet.itemReticleConfigs = new Int2ObjectOpenHashMap();

      for (String key : removed) {
         int index = assetMap.getIndex(key);
         if (index == Integer.MIN_VALUE) {
            throw new IllegalArgumentException("Unknown key! " + key);
         }

         packet.itemReticleConfigs.put(index, null);
      }

      packet.maxId = assetMap.getNextIndex();
      return packet;
   }
}
