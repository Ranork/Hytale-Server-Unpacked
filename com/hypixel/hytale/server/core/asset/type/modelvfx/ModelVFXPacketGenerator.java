package com.hypixel.hytale.server.core.asset.type.modelvfx;

import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.UpdateType;
import com.hypixel.hytale.protocol.packets.assets.UpdateModelvfxs;
import com.hypixel.hytale.server.core.asset.packet.SimpleAssetPacketGenerator;
import com.hypixel.hytale.server.core.asset.type.modelvfx.config.ModelVFX;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import javax.annotation.Nonnull;

public class ModelVFXPacketGenerator extends SimpleAssetPacketGenerator<String, ModelVFX, IndexedLookupTableAssetMap<String, ModelVFX>> {
   @Nonnull
   public ToClientPacket generateInitPacket(@Nonnull IndexedLookupTableAssetMap<String, ModelVFX> assetMap, @Nonnull Map<String, ModelVFX> assets) {
      UpdateModelvfxs packet = new UpdateModelvfxs();
      packet.type = UpdateType.Init;
      packet.modelVFXs = new Int2ObjectOpenHashMap(assets.size());

      for (Entry<String, ModelVFX> entry : assets.entrySet()) {
         String key = entry.getKey();
         int index = assetMap.getIndex(key);
         if (index == Integer.MIN_VALUE) {
            throw new IllegalArgumentException("Unknown key! " + key);
         }

         packet.modelVFXs.put(index, entry.getValue().toPacket());
      }

      packet.maxId = assetMap.getNextIndex();
      return packet;
   }

   @Nonnull
   protected ToClientPacket generateUpdatePacket(@Nonnull IndexedLookupTableAssetMap<String, ModelVFX> assetMap, @Nonnull Map<String, ModelVFX> loadedAssets) {
      UpdateModelvfxs packet = new UpdateModelvfxs();
      packet.type = UpdateType.AddOrUpdate;
      packet.modelVFXs = new Int2ObjectOpenHashMap(loadedAssets.size());

      for (Entry<String, ModelVFX> entry : loadedAssets.entrySet()) {
         String key = entry.getKey();
         int index = assetMap.getIndex(key);
         if (index == Integer.MIN_VALUE) {
            throw new IllegalArgumentException("Unknown key! " + key);
         }

         packet.modelVFXs.put(index, entry.getValue().toPacket());
      }

      packet.maxId = assetMap.getNextIndex();
      return packet;
   }

   @Nonnull
   protected ToClientPacket generateRemovePacket(@Nonnull IndexedLookupTableAssetMap<String, ModelVFX> assetMap, @Nonnull Set<String> removed) {
      UpdateModelvfxs packet = new UpdateModelvfxs();
      packet.type = UpdateType.Remove;
      packet.modelVFXs = new Int2ObjectOpenHashMap(removed.size());

      for (String key : removed) {
         int index = assetMap.getIndex(key);
         if (index == Integer.MIN_VALUE) {
            throw new IllegalArgumentException("Unknown key! " + key);
         }

         packet.modelVFXs.put(index, null);
      }

      packet.maxId = assetMap.getNextIndex();
      return packet;
   }
}
