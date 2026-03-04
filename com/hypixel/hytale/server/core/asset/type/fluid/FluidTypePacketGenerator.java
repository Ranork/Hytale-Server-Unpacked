package com.hypixel.hytale.server.core.asset.type.fluid;

import com.hypixel.hytale.assetstore.AssetUpdateQuery;
import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.UpdateType;
import com.hypixel.hytale.protocol.packets.assets.UpdateFluids;
import com.hypixel.hytale.server.core.asset.packet.AssetPacketGenerator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import javax.annotation.Nonnull;

public class FluidTypePacketGenerator extends AssetPacketGenerator<String, Fluid, IndexedLookupTableAssetMap<String, Fluid>> {
   @Nonnull
   public ToClientPacket generateInitPacket(@Nonnull IndexedLookupTableAssetMap<String, Fluid> assetMap, @Nonnull Map<String, Fluid> assets) {
      UpdateFluids packet = new UpdateFluids();
      packet.type = UpdateType.Init;
      HashMap<Integer, com.hypixel.hytale.protocol.Fluid> fluidTypes = new HashMap<>();

      for (Entry<String, Fluid> entry : assets.entrySet()) {
         int index = assetMap.getIndex(entry.getKey());
         if (index == Integer.MIN_VALUE) {
            throw new IllegalArgumentException("Unknown key: " + entry.getKey());
         }

         fluidTypes.put(index, entry.getValue().toPacket());
      }

      packet.fluids = fluidTypes;
      packet.maxId = assetMap.getNextIndex();
      return packet;
   }

   @Nonnull
   public ToClientPacket generateUpdatePacket(
      @Nonnull IndexedLookupTableAssetMap<String, Fluid> assetMap, @Nonnull Map<String, Fluid> loadedAssets, @Nonnull AssetUpdateQuery query
   ) {
      UpdateFluids packet = new UpdateFluids();
      packet.type = UpdateType.AddOrUpdate;
      HashMap<Integer, com.hypixel.hytale.protocol.Fluid> fluidTypes = new HashMap<>();

      for (Entry<String, Fluid> entry : loadedAssets.entrySet()) {
         int index = assetMap.getIndex(entry.getKey());
         if (index == Integer.MIN_VALUE) {
            throw new IllegalArgumentException("Unknown key: " + entry.getKey());
         }

         fluidTypes.put(index, entry.getValue().toPacket());
      }

      packet.fluids = fluidTypes;
      packet.maxId = assetMap.getNextIndex();
      return packet;
   }

   @Nonnull
   public ToClientPacket generateRemovePacket(
      @Nonnull IndexedLookupTableAssetMap<String, Fluid> assetMap, @Nonnull Set<String> removed, @Nonnull AssetUpdateQuery query
   ) {
      UpdateFluids packet = new UpdateFluids();
      packet.type = UpdateType.Remove;
      HashMap<Integer, com.hypixel.hytale.protocol.Fluid> fluidTypes = new HashMap<>();

      for (String key : removed) {
         int index = assetMap.getIndex(key);
         if (index == Integer.MIN_VALUE) {
            throw new IllegalArgumentException("Unknown key: " + key);
         }

         Fluid fluid = new Fluid(key);
         fluidTypes.put(index, fluid.toPacket());
      }

      packet.fluids = fluidTypes;
      packet.maxId = assetMap.getNextIndex();
      return packet;
   }
}
