package com.hypixel.hytale.server.core.asset.type.musiccontainer;

import com.hypixel.hytale.assetstore.map.IndexedAssetMap;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.UpdateType;
import com.hypixel.hytale.protocol.packets.assets.UpdateMusicContainers;
import com.hypixel.hytale.server.core.asset.packet.SimpleAssetPacketGenerator;
import com.hypixel.hytale.server.core.asset.type.musiccontainer.config.MusicContainer;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import javax.annotation.Nonnull;

public class MusicContainerPacketGenerator extends SimpleAssetPacketGenerator<String, MusicContainer, IndexedAssetMap<String, MusicContainer>> {
   @Nonnull
   public ToClientPacket generateInitPacket(@Nonnull IndexedAssetMap<String, MusicContainer> assetMap, @Nonnull Map<String, MusicContainer> assets) {
      UpdateMusicContainers packet = new UpdateMusicContainers();
      packet.type = UpdateType.Init;
      packet.musicContainers = new Object2ObjectOpenHashMap();

      for (Entry<String, MusicContainer> entry : assets.entrySet()) {
         String key = entry.getKey();
         int index = assetMap.getIndex(key);
         if (index == Integer.MIN_VALUE) {
            throw new IllegalArgumentException("Unknown key! " + key);
         }

         packet.musicContainers.put(index, entry.getValue().toPacket());
      }

      packet.maxId = assetMap.getNextIndex();
      return packet;
   }

   @Nonnull
   public ToClientPacket generateUpdatePacket(@Nonnull IndexedAssetMap<String, MusicContainer> assetMap, @Nonnull Map<String, MusicContainer> loadedAssets) {
      UpdateMusicContainers packet = new UpdateMusicContainers();
      packet.type = UpdateType.AddOrUpdate;
      packet.musicContainers = new Object2ObjectOpenHashMap();

      for (Entry<String, MusicContainer> entry : loadedAssets.entrySet()) {
         String key = entry.getKey();
         int index = assetMap.getIndex(key);
         if (index == Integer.MIN_VALUE) {
            throw new IllegalArgumentException("Unknown key! " + key);
         }

         packet.musicContainers.put(index, entry.getValue().toPacket());
      }

      packet.maxId = assetMap.getNextIndex();
      return packet;
   }

   @Nonnull
   public ToClientPacket generateRemovePacket(@Nonnull IndexedAssetMap<String, MusicContainer> assetMap, @Nonnull Set<String> removed) {
      UpdateMusicContainers packet = new UpdateMusicContainers();
      packet.type = UpdateType.Remove;
      packet.musicContainers = new Object2ObjectOpenHashMap();

      for (String key : removed) {
         int index = assetMap.getIndex(key);
         if (index == Integer.MIN_VALUE) {
            throw new IllegalArgumentException("Unknown key! " + key);
         }

         packet.musicContainers.put(index, null);
      }

      packet.maxId = assetMap.getNextIndex();
      return packet;
   }
}
