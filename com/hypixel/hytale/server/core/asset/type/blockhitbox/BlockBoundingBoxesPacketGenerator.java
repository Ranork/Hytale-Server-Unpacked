package com.hypixel.hytale.server.core.asset.type.blockhitbox;

import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.protocol.Hitbox;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.UpdateType;
import com.hypixel.hytale.protocol.packets.assets.UpdateBlockHitboxes;
import com.hypixel.hytale.server.core.asset.packet.SimpleAssetPacketGenerator;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import javax.annotation.Nonnull;

public class BlockBoundingBoxesPacketGenerator
   extends SimpleAssetPacketGenerator<String, BlockBoundingBoxes, IndexedLookupTableAssetMap<String, BlockBoundingBoxes>> {
   @Nonnull
   public ToClientPacket generateInitPacket(
      @Nonnull IndexedLookupTableAssetMap<String, BlockBoundingBoxes> assetMap, @Nonnull Map<String, BlockBoundingBoxes> assets
   ) {
      UpdateBlockHitboxes packet = new UpdateBlockHitboxes();
      packet.type = UpdateType.Init;
      Map<Integer, Hitbox[]> hitboxes = new Int2ObjectOpenHashMap();

      for (Entry<String, BlockBoundingBoxes> entry : assets.entrySet()) {
         String key = entry.getKey();
         int index = assetMap.getIndex(key);
         if (index == Integer.MIN_VALUE) {
            throw new IllegalArgumentException("Unknown key! " + key);
         }

         hitboxes.put(index, entry.getValue().toPacket());
      }

      packet.blockBaseHitboxes = hitboxes;
      packet.maxId = assetMap.getNextIndex();
      return packet;
   }

   @Nonnull
   public ToClientPacket generateUpdatePacket(
      @Nonnull IndexedLookupTableAssetMap<String, BlockBoundingBoxes> assetMap, @Nonnull Map<String, BlockBoundingBoxes> loadedAssets
   ) {
      UpdateBlockHitboxes packet = new UpdateBlockHitboxes();
      packet.type = UpdateType.AddOrUpdate;
      Map<Integer, Hitbox[]> hitboxes = new Int2ObjectOpenHashMap();

      for (Entry<String, BlockBoundingBoxes> entry : loadedAssets.entrySet()) {
         String key = entry.getKey();
         int index = assetMap.getIndex(key);
         if (index == Integer.MIN_VALUE) {
            throw new IllegalArgumentException("Unknown key! " + key);
         }

         hitboxes.put(index, entry.getValue().toPacket());
      }

      packet.blockBaseHitboxes = hitboxes;
      packet.maxId = assetMap.getNextIndex();
      return packet;
   }

   @Nonnull
   public ToClientPacket generateRemovePacket(@Nonnull IndexedLookupTableAssetMap<String, BlockBoundingBoxes> assetMap, @Nonnull Set<String> removed) {
      UpdateBlockHitboxes packet = new UpdateBlockHitboxes();
      packet.type = UpdateType.Remove;
      Map<Integer, Hitbox[]> hitboxes = new Int2ObjectOpenHashMap();

      for (String key : removed) {
         int index = assetMap.getIndex(key);
         if (index == Integer.MIN_VALUE) {
            throw new IllegalArgumentException("Unknown key! " + key);
         }

         hitboxes.put(index, null);
      }

      packet.blockBaseHitboxes = hitboxes;
      return packet;
   }
}
