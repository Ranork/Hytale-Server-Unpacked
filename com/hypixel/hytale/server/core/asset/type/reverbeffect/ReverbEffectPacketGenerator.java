package com.hypixel.hytale.server.core.asset.type.reverbeffect;

import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.UpdateType;
import com.hypixel.hytale.protocol.packets.assets.UpdateReverbEffects;
import com.hypixel.hytale.server.core.asset.packet.SimpleAssetPacketGenerator;
import com.hypixel.hytale.server.core.asset.type.reverbeffect.config.ReverbEffect;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import javax.annotation.Nonnull;

public class ReverbEffectPacketGenerator extends SimpleAssetPacketGenerator<String, ReverbEffect, IndexedLookupTableAssetMap<String, ReverbEffect>> {
   @Nonnull
   public ToClientPacket generateInitPacket(@Nonnull IndexedLookupTableAssetMap<String, ReverbEffect> assetMap, @Nonnull Map<String, ReverbEffect> assets) {
      UpdateReverbEffects packet = new UpdateReverbEffects();
      packet.type = UpdateType.Init;
      packet.effects = new Int2ObjectOpenHashMap(assets.size());

      for (Entry<String, ReverbEffect> entry : assets.entrySet()) {
         String key = entry.getKey();
         int index = assetMap.getIndex(key);
         if (index == Integer.MIN_VALUE) {
            throw new IllegalArgumentException("Unknown key! " + key);
         }

         packet.effects.put(index, entry.getValue().toPacket());
      }

      packet.maxId = assetMap.getNextIndex();
      return packet;
   }

   @Nonnull
   public ToClientPacket generateUpdatePacket(
      @Nonnull IndexedLookupTableAssetMap<String, ReverbEffect> assetMap, @Nonnull Map<String, ReverbEffect> loadedAssets
   ) {
      UpdateReverbEffects packet = new UpdateReverbEffects();
      packet.type = UpdateType.AddOrUpdate;
      packet.effects = new Int2ObjectOpenHashMap(loadedAssets.size());

      for (Entry<String, ReverbEffect> entry : loadedAssets.entrySet()) {
         String key = entry.getKey();
         int index = assetMap.getIndex(key);
         if (index == Integer.MIN_VALUE) {
            throw new IllegalArgumentException("Unknown key! " + key);
         }

         packet.effects.put(index, entry.getValue().toPacket());
      }

      packet.maxId = assetMap.getNextIndex();
      return packet;
   }

   @Nonnull
   public ToClientPacket generateRemovePacket(@Nonnull IndexedLookupTableAssetMap<String, ReverbEffect> assetMap, @Nonnull Set<String> removed) {
      UpdateReverbEffects packet = new UpdateReverbEffects();
      packet.type = UpdateType.Remove;
      packet.effects = new Int2ObjectOpenHashMap(removed.size());

      for (String key : removed) {
         int index = assetMap.getIndex(key);
         if (index == Integer.MIN_VALUE) {
            throw new IllegalArgumentException("Unknown key! " + key);
         }

         packet.effects.put(index, null);
      }

      packet.maxId = assetMap.getNextIndex();
      return packet;
   }
}
