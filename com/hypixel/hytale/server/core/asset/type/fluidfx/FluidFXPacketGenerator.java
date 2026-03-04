package com.hypixel.hytale.server.core.asset.type.fluidfx;

import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.UpdateType;
import com.hypixel.hytale.protocol.packets.assets.UpdateFluidFX;
import com.hypixel.hytale.server.core.asset.packet.SimpleAssetPacketGenerator;
import com.hypixel.hytale.server.core.asset.type.fluidfx.config.FluidFX;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import javax.annotation.Nonnull;

public class FluidFXPacketGenerator extends SimpleAssetPacketGenerator<String, FluidFX, IndexedLookupTableAssetMap<String, FluidFX>> {
   @Nonnull
   public ToClientPacket generateInitPacket(@Nonnull IndexedLookupTableAssetMap<String, FluidFX> assetMap, @Nonnull Map<String, FluidFX> assets) {
      UpdateFluidFX packet = new UpdateFluidFX();
      packet.type = UpdateType.Init;
      packet.fluidFX = new Int2ObjectOpenHashMap();

      for (Entry<String, FluidFX> entry : assets.entrySet()) {
         String key = entry.getKey();
         int index = assetMap.getIndex(key);
         if (index == Integer.MIN_VALUE) {
            throw new IllegalArgumentException("Unknown key! " + key);
         }

         packet.fluidFX.put(index, entry.getValue().toPacket());
      }

      packet.maxId = assetMap.getNextIndex();
      return packet;
   }

   @Nonnull
   public ToClientPacket generateUpdatePacket(@Nonnull IndexedLookupTableAssetMap<String, FluidFX> assetMap, @Nonnull Map<String, FluidFX> loadedAssets) {
      UpdateFluidFX packet = new UpdateFluidFX();
      packet.type = UpdateType.AddOrUpdate;
      packet.fluidFX = new Int2ObjectOpenHashMap();

      for (Entry<String, FluidFX> entry : loadedAssets.entrySet()) {
         String key = entry.getKey();
         int index = assetMap.getIndex(key);
         if (index == Integer.MIN_VALUE) {
            throw new IllegalArgumentException("Unknown key! " + key);
         }

         packet.fluidFX.put(index, entry.getValue().toPacket());
      }

      packet.maxId = assetMap.getNextIndex();
      return packet;
   }

   @Nonnull
   public ToClientPacket generateRemovePacket(@Nonnull IndexedLookupTableAssetMap<String, FluidFX> assetMap, @Nonnull Set<String> removed) {
      UpdateFluidFX packet = new UpdateFluidFX();
      packet.type = UpdateType.Remove;
      packet.fluidFX = new Int2ObjectOpenHashMap();

      for (String key : removed) {
         int index = assetMap.getIndex(key);
         if (index == Integer.MIN_VALUE) {
            throw new IllegalArgumentException("Unknown key! " + key);
         }

         packet.fluidFX.put(index, null);
      }

      packet.maxId = assetMap.getNextIndex();
      return packet;
   }
}
