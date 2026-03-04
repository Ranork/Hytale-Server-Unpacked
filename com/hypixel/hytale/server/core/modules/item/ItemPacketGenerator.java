package com.hypixel.hytale.server.core.modules.item;

import com.hypixel.hytale.assetstore.AssetUpdateQuery;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.UpdateType;
import com.hypixel.hytale.protocol.packets.assets.UpdateItems;
import com.hypixel.hytale.server.core.asset.packet.AssetPacketGenerator;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import javax.annotation.Nonnull;

public class ItemPacketGenerator extends AssetPacketGenerator<String, Item, DefaultAssetMap<String, Item>> {
   @Nonnull
   public ToClientPacket generateInitPacket(DefaultAssetMap<String, Item> assetMap, @Nonnull Map<String, Item> assets) {
      UpdateItems packet = new UpdateItems();
      packet.type = UpdateType.Init;
      packet.items = new Object2ObjectOpenHashMap();

      for (Entry<String, Item> entry : assets.entrySet()) {
         packet.items.put(entry.getKey(), entry.getValue().toPacket());
      }

      packet.updateModels = true;
      packet.updateIcons = true;
      return packet;
   }

   @Nonnull
   public ToClientPacket generateUpdatePacket(DefaultAssetMap<String, Item> assetMap, @Nonnull Map<String, Item> loadedAssets, @Nonnull AssetUpdateQuery query) {
      UpdateItems packet = new UpdateItems();
      packet.type = UpdateType.AddOrUpdate;
      packet.items = new Object2ObjectOpenHashMap();

      for (Entry<String, Item> entry : loadedAssets.entrySet()) {
         packet.items.put(entry.getKey(), entry.getValue().toPacket());
      }

      AssetUpdateQuery.RebuildCache rebuildCache = query.getRebuildCache();
      packet.updateModels = rebuildCache.isBlockTextures() || rebuildCache.isModels();
      packet.updateIcons = rebuildCache.isItemIcons();
      return packet;
   }

   @Nonnull
   public ToClientPacket generateRemovePacket(DefaultAssetMap<String, Item> assetMap, @Nonnull Set<String> removed, @Nonnull AssetUpdateQuery query) {
      UpdateItems packet = new UpdateItems();
      packet.type = UpdateType.Remove;
      packet.removedItems = removed.toArray(String[]::new);
      AssetUpdateQuery.RebuildCache rebuildCache = query.getRebuildCache();
      packet.updateModels = rebuildCache.isBlockTextures() || rebuildCache.isModels();
      packet.updateIcons = rebuildCache.isItemIcons();
      return packet;
   }
}
