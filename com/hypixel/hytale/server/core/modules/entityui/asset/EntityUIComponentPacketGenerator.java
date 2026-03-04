package com.hypixel.hytale.server.core.modules.entityui.asset;

import com.hypixel.hytale.assetstore.AssetUpdateQuery;
import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.UpdateType;
import com.hypixel.hytale.protocol.packets.assets.UpdateEntityUIComponents;
import com.hypixel.hytale.server.core.asset.packet.AssetPacketGenerator;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import javax.annotation.Nonnull;

public class EntityUIComponentPacketGenerator extends AssetPacketGenerator<String, EntityUIComponent, IndexedLookupTableAssetMap<String, EntityUIComponent>> {
   @Nonnull
   public ToClientPacket generateInitPacket(
      @Nonnull IndexedLookupTableAssetMap<String, EntityUIComponent> assetMap, @Nonnull Map<String, EntityUIComponent> assets
   ) {
      Int2ObjectMap<com.hypixel.hytale.protocol.EntityUIComponent> configs = new Int2ObjectOpenHashMap();

      for (Entry<String, EntityUIComponent> entry : assets.entrySet()) {
         configs.put(assetMap.getIndex(entry.getKey()), entry.getValue().toPacket());
      }

      return new UpdateEntityUIComponents(UpdateType.Init, assetMap.getNextIndex(), configs);
   }

   @Nonnull
   public ToClientPacket generateUpdatePacket(
      @Nonnull IndexedLookupTableAssetMap<String, EntityUIComponent> assetMap,
      @Nonnull Map<String, EntityUIComponent> loadedAssets,
      @Nonnull AssetUpdateQuery query
   ) {
      Int2ObjectMap<com.hypixel.hytale.protocol.EntityUIComponent> components = new Int2ObjectOpenHashMap();

      for (Entry<String, EntityUIComponent> entry : loadedAssets.entrySet()) {
         components.put(assetMap.getIndex(entry.getKey()), entry.getValue().toPacket());
      }

      return new UpdateEntityUIComponents(UpdateType.AddOrUpdate, assetMap.getNextIndex(), components);
   }

   @Nonnull
   public ToClientPacket generateRemovePacket(
      @Nonnull IndexedLookupTableAssetMap<String, EntityUIComponent> assetMap, @Nonnull Set<String> removed, @Nonnull AssetUpdateQuery query
   ) {
      Int2ObjectMap<com.hypixel.hytale.protocol.EntityUIComponent> configs = new Int2ObjectOpenHashMap();

      for (String entry : removed) {
         configs.put(assetMap.getIndex(entry), new com.hypixel.hytale.protocol.EntityUIComponent());
      }

      return new UpdateEntityUIComponents(UpdateType.Remove, assetMap.getNextIndex(), configs);
   }
}
