package com.hypixel.hytale.server.core.asset.type.blocktype;

import com.hypixel.hytale.assetstore.AssetUpdateQuery;
import com.hypixel.hytale.assetstore.map.BlockTypeAssetMap;
import com.hypixel.hytale.protocol.CachedPacket;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.UpdateType;
import com.hypixel.hytale.protocol.packets.assets.UpdateBlockTypes;
import com.hypixel.hytale.server.core.asset.packet.AssetPacketGenerator;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import javax.annotation.Nonnull;

public class BlockTypePacketGenerator extends AssetPacketGenerator<String, BlockType, BlockTypeAssetMap<String, BlockType>> {
   @Nonnull
   public ToClientPacket generateInitPacket(@Nonnull BlockTypeAssetMap<String, BlockType> assetMap, @Nonnull Map<String, BlockType> assets) {
      UpdateBlockTypes packet = new UpdateBlockTypes();
      packet.type = UpdateType.Init;
      Map<Integer, com.hypixel.hytale.protocol.BlockType> blockTypes = new Int2ObjectOpenHashMap();

      for (Entry<String, BlockType> entry : assets.entrySet()) {
         String key = entry.getKey();
         int index = assetMap.getIndex(key);
         if (index == Integer.MIN_VALUE) {
            throw new IllegalArgumentException("Unknown key! " + key);
         }

         blockTypes.put(index, entry.getValue().toPacket());
      }

      packet.blockTypes = blockTypes;
      packet.maxId = assetMap.getNextIndex();
      packet.updateBlockTextures = true;
      packet.updateModels = true;
      packet.updateModelTextures = true;
      packet.updateMapGeometry = true;
      return packet;
   }

   @Nonnull
   public ToClientPacket generateUpdatePacket(
      @Nonnull BlockTypeAssetMap<String, BlockType> assetMap, @Nonnull Map<String, BlockType> loadedAssets, @Nonnull AssetUpdateQuery query
   ) {
      UpdateBlockTypes packet = new UpdateBlockTypes();
      packet.type = UpdateType.AddOrUpdate;
      Map<Integer, com.hypixel.hytale.protocol.BlockType> blockTypes = new Int2ObjectOpenHashMap();

      for (Entry<String, BlockType> entry : loadedAssets.entrySet()) {
         String key = entry.getKey();
         int index = assetMap.getIndex(key);
         if (index == Integer.MIN_VALUE) {
            throw new IllegalArgumentException("Unknown key! " + key);
         }

         blockTypes.put(index, entry.getValue().toPacket());
      }

      packet.blockTypes = blockTypes;
      packet.maxId = assetMap.getNextIndex();
      AssetUpdateQuery.RebuildCache rebuildCache = query.getRebuildCache();
      packet.updateBlockTextures = rebuildCache.isBlockTextures();
      packet.updateModelTextures = rebuildCache.isModelTextures();
      packet.updateModels = rebuildCache.isModels();
      packet.updateMapGeometry = rebuildCache.isMapGeometry();
      return CachedPacket.cache(packet);
   }

   @Nonnull
   public ToClientPacket generateRemovePacket(
      @Nonnull BlockTypeAssetMap<String, BlockType> assetMap, @Nonnull Set<String> removed, @Nonnull AssetUpdateQuery query
   ) {
      UpdateBlockTypes packet = new UpdateBlockTypes();
      packet.type = UpdateType.Remove;
      Map<Integer, com.hypixel.hytale.protocol.BlockType> blockTypes = new Int2ObjectOpenHashMap();

      for (String key : removed) {
         int index = assetMap.getIndex(key);
         if (index == Integer.MIN_VALUE) {
            throw new IllegalArgumentException("Unknown key! " + key);
         }

         com.hypixel.hytale.protocol.BlockType blockType = new com.hypixel.hytale.protocol.BlockType();
         blockType.name = key;
         blockTypes.put(index, blockType);
      }

      packet.blockTypes = blockTypes;
      AssetUpdateQuery.RebuildCache rebuildCache = query.getRebuildCache();
      packet.updateBlockTextures = rebuildCache.isBlockTextures();
      packet.updateModels = rebuildCache.isModels();
      packet.updateModelTextures = rebuildCache.isModelTextures();
      packet.updateMapGeometry = rebuildCache.isMapGeometry();
      return CachedPacket.cache(packet);
   }
}
