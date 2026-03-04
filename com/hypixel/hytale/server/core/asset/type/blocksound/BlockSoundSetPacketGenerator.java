package com.hypixel.hytale.server.core.asset.type.blocksound;

import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.UpdateType;
import com.hypixel.hytale.protocol.packets.assets.UpdateBlockSoundSets;
import com.hypixel.hytale.server.core.asset.packet.SimpleAssetPacketGenerator;
import com.hypixel.hytale.server.core.asset.type.blocksound.config.BlockSoundSet;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import javax.annotation.Nonnull;

public class BlockSoundSetPacketGenerator extends SimpleAssetPacketGenerator<String, BlockSoundSet, IndexedLookupTableAssetMap<String, BlockSoundSet>> {
   @Nonnull
   public ToClientPacket generateInitPacket(@Nonnull IndexedLookupTableAssetMap<String, BlockSoundSet> assetMap, @Nonnull Map<String, BlockSoundSet> assets) {
      UpdateBlockSoundSets packet = new UpdateBlockSoundSets();
      packet.type = UpdateType.Init;
      packet.blockSoundSets = new Int2ObjectOpenHashMap();

      for (Entry<String, BlockSoundSet> entry : assets.entrySet()) {
         String key = entry.getKey();
         int index = assetMap.getIndex(key);
         if (index == Integer.MIN_VALUE) {
            throw new IllegalArgumentException("Unknown key! " + key);
         }

         packet.blockSoundSets.put(index, entry.getValue().toPacket());
      }

      packet.maxId = assetMap.getNextIndex();
      return packet;
   }

   @Nonnull
   public ToClientPacket generateUpdatePacket(
      @Nonnull IndexedLookupTableAssetMap<String, BlockSoundSet> assetMap, @Nonnull Map<String, BlockSoundSet> loadedAssets
   ) {
      UpdateBlockSoundSets packet = new UpdateBlockSoundSets();
      packet.type = UpdateType.AddOrUpdate;
      packet.blockSoundSets = new Int2ObjectOpenHashMap();

      for (Entry<String, BlockSoundSet> entry : loadedAssets.entrySet()) {
         String key = entry.getKey();
         int index = assetMap.getIndex(key);
         if (index == Integer.MIN_VALUE) {
            throw new IllegalArgumentException("Unknown key! " + key);
         }

         packet.blockSoundSets.put(index, entry.getValue().toPacket());
      }

      packet.maxId = assetMap.getNextIndex();
      return packet;
   }

   @Nonnull
   public ToClientPacket generateRemovePacket(@Nonnull IndexedLookupTableAssetMap<String, BlockSoundSet> assetMap, @Nonnull Set<String> removed) {
      UpdateBlockSoundSets packet = new UpdateBlockSoundSets();
      packet.type = UpdateType.Remove;
      packet.blockSoundSets = new Int2ObjectOpenHashMap();

      for (String key : removed) {
         int index = assetMap.getIndex(key);
         if (index == Integer.MIN_VALUE) {
            throw new IllegalArgumentException("Unknown key! " + key);
         }

         packet.blockSoundSets.put(index, null);
      }

      packet.maxId = assetMap.getNextIndex();
      return packet;
   }
}
