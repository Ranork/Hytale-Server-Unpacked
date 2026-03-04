package com.hypixel.hytale.server.core.modules.interaction.interaction;

import com.hypixel.hytale.assetstore.AssetUpdateQuery;
import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.UpdateType;
import com.hypixel.hytale.protocol.packets.assets.UpdateRootInteractions;
import com.hypixel.hytale.server.core.asset.packet.AssetPacketGenerator;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.RootInteraction;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import javax.annotation.Nonnull;

public class RootInteractionPacketGenerator extends AssetPacketGenerator<String, RootInteraction, IndexedLookupTableAssetMap<String, RootInteraction>> {
   @Nonnull
   public ToClientPacket generateInitPacket(@Nonnull IndexedLookupTableAssetMap<String, RootInteraction> assetMap, @Nonnull Map<String, RootInteraction> assets) {
      Int2ObjectMap<com.hypixel.hytale.protocol.RootInteraction> interactions = new Int2ObjectOpenHashMap();

      for (Entry<String, RootInteraction> entry : assets.entrySet()) {
         interactions.put(assetMap.getIndex(entry.getKey()), entry.getValue().toPacket());
      }

      return new UpdateRootInteractions(UpdateType.Init, assetMap.getNextIndex(), interactions);
   }

   @Nonnull
   public ToClientPacket generateUpdatePacket(
      @Nonnull IndexedLookupTableAssetMap<String, RootInteraction> assetMap,
      @Nonnull Map<String, RootInteraction> loadedAssets,
      @Nonnull AssetUpdateQuery query
   ) {
      Int2ObjectMap<com.hypixel.hytale.protocol.RootInteraction> interactions = new Int2ObjectOpenHashMap();

      for (Entry<String, RootInteraction> entry : loadedAssets.entrySet()) {
         interactions.put(assetMap.getIndex(entry.getKey()), entry.getValue().toPacket());
      }

      return new UpdateRootInteractions(UpdateType.AddOrUpdate, assetMap.getNextIndex(), interactions);
   }

   @Nonnull
   public ToClientPacket generateRemovePacket(
      @Nonnull IndexedLookupTableAssetMap<String, RootInteraction> assetMap, @Nonnull Set<String> removed, @Nonnull AssetUpdateQuery query
   ) {
      Int2ObjectMap<com.hypixel.hytale.protocol.RootInteraction> interactions = new Int2ObjectOpenHashMap();

      for (String entry : removed) {
         interactions.put(assetMap.getIndex(entry), new com.hypixel.hytale.protocol.RootInteraction());
      }

      return new UpdateRootInteractions(UpdateType.Remove, assetMap.getNextIndex(), interactions);
   }
}
