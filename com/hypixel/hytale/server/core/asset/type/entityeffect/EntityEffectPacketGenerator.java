package com.hypixel.hytale.server.core.asset.type.entityeffect;

import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.UpdateType;
import com.hypixel.hytale.protocol.packets.assets.UpdateEntityEffects;
import com.hypixel.hytale.server.core.asset.packet.SimpleAssetPacketGenerator;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.EntityEffect;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import javax.annotation.Nonnull;

public class EntityEffectPacketGenerator extends SimpleAssetPacketGenerator<String, EntityEffect, IndexedLookupTableAssetMap<String, EntityEffect>> {
   @Nonnull
   public ToClientPacket generateInitPacket(@Nonnull IndexedLookupTableAssetMap<String, EntityEffect> assetMap, @Nonnull Map<String, EntityEffect> assets) {
      UpdateEntityEffects packet = new UpdateEntityEffects();
      packet.type = UpdateType.Init;
      packet.entityEffects = new Int2ObjectOpenHashMap(assets.size());

      for (Entry<String, EntityEffect> entry : assets.entrySet()) {
         String key = entry.getKey();
         int index = assetMap.getIndex(key);
         if (index == Integer.MIN_VALUE) {
            throw new IllegalArgumentException("Unknown key! " + key);
         }

         packet.entityEffects.put(index, entry.getValue().toPacket());
      }

      packet.maxId = assetMap.getNextIndex();
      return packet;
   }

   @Nonnull
   protected ToClientPacket generateUpdatePacket(
      @Nonnull IndexedLookupTableAssetMap<String, EntityEffect> assetMap, @Nonnull Map<String, EntityEffect> loadedAssets
   ) {
      UpdateEntityEffects packet = new UpdateEntityEffects();
      packet.type = UpdateType.AddOrUpdate;
      packet.entityEffects = new Int2ObjectOpenHashMap(loadedAssets.size());

      for (Entry<String, EntityEffect> entry : loadedAssets.entrySet()) {
         String key = entry.getKey();
         int index = assetMap.getIndex(key);
         if (index == Integer.MIN_VALUE) {
            throw new IllegalArgumentException("Unknown key! " + key);
         }

         packet.entityEffects.put(index, entry.getValue().toPacket());
      }

      packet.maxId = assetMap.getNextIndex();
      return packet;
   }

   @Nonnull
   protected ToClientPacket generateRemovePacket(@Nonnull IndexedLookupTableAssetMap<String, EntityEffect> assetMap, @Nonnull Set<String> removed) {
      UpdateEntityEffects packet = new UpdateEntityEffects();
      packet.type = UpdateType.Remove;
      packet.entityEffects = new Int2ObjectOpenHashMap(removed.size());

      for (String key : removed) {
         int index = assetMap.getIndex(key);
         if (index == Integer.MIN_VALUE) {
            throw new IllegalArgumentException("Unknown key! " + key);
         }

         packet.entityEffects.put(index, null);
      }

      packet.maxId = assetMap.getNextIndex();
      return packet;
   }
}
