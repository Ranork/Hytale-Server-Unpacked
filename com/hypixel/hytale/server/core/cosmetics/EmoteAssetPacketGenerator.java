package com.hypixel.hytale.server.core.cosmetics;

import com.hypixel.hytale.assetstore.AssetUpdateQuery;
import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.protocol.ProtocolEmote;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.UpdateType;
import com.hypixel.hytale.protocol.packets.assets.UpdateEmotes;
import com.hypixel.hytale.server.core.asset.packet.AssetPacketGenerator;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

public class EmoteAssetPacketGenerator extends AssetPacketGenerator<String, EmoteAsset, IndexedLookupTableAssetMap<String, EmoteAsset>> {
   public ToClientPacket generateInitPacket(IndexedLookupTableAssetMap<String, EmoteAsset> assetMap, Map<String, EmoteAsset> assets) {
      Int2ObjectMap<ProtocolEmote> emoteAssets = new Int2ObjectOpenHashMap();

      for (Entry<String, EmoteAsset> entry : assets.entrySet()) {
         emoteAssets.put(assetMap.getIndex(entry.getKey()), entry.getValue().toPacket());
      }

      return new UpdateEmotes(UpdateType.Init, assetMap.getNextIndex(), emoteAssets);
   }

   public ToClientPacket generateUpdatePacket(
      IndexedLookupTableAssetMap<String, EmoteAsset> assetMap, Map<String, EmoteAsset> loadedAssets, @NonNullDecl AssetUpdateQuery query
   ) {
      Int2ObjectMap<ProtocolEmote> emoteAssets = new Int2ObjectOpenHashMap();

      for (Entry<String, EmoteAsset> entry : loadedAssets.entrySet()) {
         emoteAssets.put(assetMap.getIndex(entry.getKey()), entry.getValue().toPacket());
      }

      return new UpdateEmotes(UpdateType.AddOrUpdate, assetMap.getNextIndex(), emoteAssets);
   }

   @NullableDecl
   public ToClientPacket generateRemovePacket(
      IndexedLookupTableAssetMap<String, EmoteAsset> assetMap, Set<String> removedAssets, @NonNullDecl AssetUpdateQuery query
   ) {
      Int2ObjectMap<ProtocolEmote> emoteAssets = new Int2ObjectOpenHashMap();

      for (String entry : removedAssets) {
         emoteAssets.put(assetMap.getIndex(entry), null);
      }

      return new UpdateEmotes(UpdateType.Remove, assetMap.getNextIndex(), emoteAssets);
   }
}
