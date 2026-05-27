package com.hypixel.hytale.server.core.asset.type.physicalmaterial;

import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.UpdateType;
import com.hypixel.hytale.protocol.packets.assets.UpdatePhysicalMaterials;
import com.hypixel.hytale.server.core.asset.packet.SimpleAssetPacketGenerator;
import com.hypixel.hytale.server.core.asset.type.physicalmaterial.config.PhysicalMaterial;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import javax.annotation.Nonnull;

public class PhysicalMaterialPacketGenerator extends SimpleAssetPacketGenerator<String, PhysicalMaterial, IndexedLookupTableAssetMap<String, PhysicalMaterial>> {
   @Nonnull
   public ToClientPacket generateInitPacket(
      @Nonnull IndexedLookupTableAssetMap<String, PhysicalMaterial> assetMap, @Nonnull Map<String, PhysicalMaterial> assets
   ) {
      UpdatePhysicalMaterials packet = new UpdatePhysicalMaterials();
      packet.type = UpdateType.Init;
      packet.physicalMaterials = new Int2ObjectOpenHashMap();

      for (Entry<String, PhysicalMaterial> entry : assets.entrySet()) {
         String key = entry.getKey();
         int index = assetMap.getIndex(key);
         if (index == Integer.MIN_VALUE) {
            throw new IllegalArgumentException("Unknown key! " + key);
         }

         packet.physicalMaterials.put(index, entry.getValue().toPacket());
      }

      packet.maxId = assetMap.getNextIndex();
      return packet;
   }

   @Nonnull
   public ToClientPacket generateUpdatePacket(
      @Nonnull IndexedLookupTableAssetMap<String, PhysicalMaterial> assetMap, @Nonnull Map<String, PhysicalMaterial> loadedAssets
   ) {
      UpdatePhysicalMaterials packet = new UpdatePhysicalMaterials();
      packet.type = UpdateType.AddOrUpdate;
      packet.physicalMaterials = new Int2ObjectOpenHashMap();

      for (Entry<String, PhysicalMaterial> entry : loadedAssets.entrySet()) {
         String key = entry.getKey();
         int index = assetMap.getIndex(key);
         if (index == Integer.MIN_VALUE) {
            throw new IllegalArgumentException("Unknown key! " + key);
         }

         packet.physicalMaterials.put(index, entry.getValue().toPacket());
      }

      packet.maxId = assetMap.getNextIndex();
      return packet;
   }

   @Nonnull
   public ToClientPacket generateRemovePacket(@Nonnull IndexedLookupTableAssetMap<String, PhysicalMaterial> assetMap, @Nonnull Set<String> removed) {
      UpdatePhysicalMaterials packet = new UpdatePhysicalMaterials();
      packet.type = UpdateType.Remove;
      packet.physicalMaterials = new Int2ObjectOpenHashMap();

      for (String key : removed) {
         int index = assetMap.getIndex(key);
         if (index == Integer.MIN_VALUE) {
            throw new IllegalArgumentException("Unknown key! " + key);
         }

         packet.physicalMaterials.put(index, null);
      }

      packet.maxId = assetMap.getNextIndex();
      return packet;
   }
}
