package com.hypixel.hytale.server.core.modules.entity.hitboxcollision;

import com.hypixel.hytale.assetstore.AssetUpdateQuery;
import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.UpdateType;
import com.hypixel.hytale.protocol.packets.assets.UpdateHitboxCollisionConfig;
import com.hypixel.hytale.server.core.asset.packet.AssetPacketGenerator;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import javax.annotation.Nonnull;

public class HitboxCollisionConfigPacketGenerator
   extends AssetPacketGenerator<String, HitboxCollisionConfig, IndexedLookupTableAssetMap<String, HitboxCollisionConfig>> {
   @Nonnull
   public ToClientPacket generateInitPacket(
      @Nonnull IndexedLookupTableAssetMap<String, HitboxCollisionConfig> assetMap, @Nonnull Map<String, HitboxCollisionConfig> assets
   ) {
      Int2ObjectMap<com.hypixel.hytale.protocol.HitboxCollisionConfig> hitboxCollisionConfigs = new Int2ObjectOpenHashMap();

      for (Entry<String, HitboxCollisionConfig> entry : assets.entrySet()) {
         hitboxCollisionConfigs.put(assetMap.getIndex(entry.getKey()), entry.getValue().toPacket());
      }

      return new UpdateHitboxCollisionConfig(UpdateType.Init, assetMap.getNextIndex(), hitboxCollisionConfigs);
   }

   @Nonnull
   public ToClientPacket generateUpdatePacket(
      @Nonnull IndexedLookupTableAssetMap<String, HitboxCollisionConfig> assetMap,
      @Nonnull Map<String, HitboxCollisionConfig> loadedAssets,
      @Nonnull AssetUpdateQuery query
   ) {
      Int2ObjectMap<com.hypixel.hytale.protocol.HitboxCollisionConfig> hitboxCollisionConfigs = new Int2ObjectOpenHashMap();

      for (Entry<String, HitboxCollisionConfig> entry : loadedAssets.entrySet()) {
         hitboxCollisionConfigs.put(assetMap.getIndex(entry.getKey()), entry.getValue().toPacket());
      }

      return new UpdateHitboxCollisionConfig(UpdateType.AddOrUpdate, assetMap.getNextIndex(), hitboxCollisionConfigs);
   }

   @Nonnull
   public ToClientPacket generateRemovePacket(
      @Nonnull IndexedLookupTableAssetMap<String, HitboxCollisionConfig> assetMap, @Nonnull Set<String> removed, @Nonnull AssetUpdateQuery query
   ) {
      Int2ObjectMap<com.hypixel.hytale.protocol.HitboxCollisionConfig> hitboxCollisionConfigs = new Int2ObjectOpenHashMap();

      for (String entry : removed) {
         hitboxCollisionConfigs.put(assetMap.getIndex(entry), new com.hypixel.hytale.protocol.HitboxCollisionConfig());
      }

      return new UpdateHitboxCollisionConfig(UpdateType.Remove, assetMap.getNextIndex(), hitboxCollisionConfigs);
   }
}
