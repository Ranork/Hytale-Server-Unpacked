package com.hypixel.hytale.server.core.asset.type.audiostate;

import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.UpdateType;
import com.hypixel.hytale.protocol.packets.assets.UpdateAudioStates;
import com.hypixel.hytale.server.core.asset.packet.SimpleAssetPacketGenerator;
import com.hypixel.hytale.server.core.asset.type.audiostate.config.AudioState;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import javax.annotation.Nonnull;

public class AudioStatePacketGenerator extends SimpleAssetPacketGenerator<String, AudioState, IndexedLookupTableAssetMap<String, AudioState>> {
   @Nonnull
   public ToClientPacket generateInitPacket(@Nonnull IndexedLookupTableAssetMap<String, AudioState> assetMap, @Nonnull Map<String, AudioState> assets) {
      UpdateAudioStates packet = new UpdateAudioStates();
      packet.type = UpdateType.Init;
      packet.audioStates = new Int2ObjectOpenHashMap(assets.size());

      for (Entry<String, AudioState> entry : assets.entrySet()) {
         String key = entry.getKey();
         int index = assetMap.getIndex(key);
         if (index == Integer.MIN_VALUE) {
            throw new IllegalArgumentException("Unknown key! " + key);
         }

         packet.audioStates.put(index, entry.getValue().toPacket());
      }

      packet.maxId = assetMap.getNextIndex();
      return packet;
   }

   @Nonnull
   public ToClientPacket generateUpdatePacket(@Nonnull IndexedLookupTableAssetMap<String, AudioState> assetMap, @Nonnull Map<String, AudioState> loadedAssets) {
      UpdateAudioStates packet = new UpdateAudioStates();
      packet.type = UpdateType.AddOrUpdate;
      packet.audioStates = new Int2ObjectOpenHashMap(loadedAssets.size());

      for (Entry<String, AudioState> entry : loadedAssets.entrySet()) {
         String key = entry.getKey();
         int index = assetMap.getIndex(key);
         if (index == Integer.MIN_VALUE) {
            throw new IllegalArgumentException("Unknown key! " + key);
         }

         packet.audioStates.put(index, entry.getValue().toPacket());
      }

      packet.maxId = assetMap.getNextIndex();
      return packet;
   }

   @Nonnull
   public ToClientPacket generateRemovePacket(@Nonnull IndexedLookupTableAssetMap<String, AudioState> assetMap, @Nonnull Set<String> removed) {
      UpdateAudioStates packet = new UpdateAudioStates();
      packet.type = UpdateType.Remove;
      packet.audioStates = new Int2ObjectOpenHashMap(removed.size());

      for (String key : removed) {
         int index = assetMap.getIndex(key);
         if (index == Integer.MIN_VALUE) {
            throw new IllegalArgumentException("Unknown key! " + key);
         }

         packet.audioStates.put(index, null);
      }

      packet.maxId = assetMap.getNextIndex();
      return packet;
   }
}
