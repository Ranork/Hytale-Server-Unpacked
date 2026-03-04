package com.hypixel.hytale.server.core.asset.type.blocktype;

import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.UpdateType;
import com.hypixel.hytale.protocol.packets.assets.UpdateBlockGroups;
import com.hypixel.hytale.server.core.asset.packet.DefaultAssetPacketGenerator;
import com.hypixel.hytale.server.core.asset.type.item.config.BlockGroup;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockGroupPacketGenerator extends DefaultAssetPacketGenerator<String, BlockGroup> {
   @Nonnull
   @Override
   public ToClientPacket generateInitPacket(@Nonnull DefaultAssetMap<String, BlockGroup> assetMap, Map<String, BlockGroup> assets) {
      UpdateBlockGroups packet = new UpdateBlockGroups();
      packet.type = UpdateType.Init;
      packet.groups = assetMap.getAssetMap().entrySet().stream().collect(Collectors.toMap(Entry::getKey, entry -> entry.getValue().toPacket()));
      return packet;
   }

   @Nonnull
   @Override
   public ToClientPacket generateUpdatePacket(@Nonnull Map<String, BlockGroup> loadedAssets) {
      UpdateBlockGroups packet = new UpdateBlockGroups();
      packet.type = UpdateType.AddOrUpdate;
      packet.groups = loadedAssets.entrySet().stream().collect(Collectors.toMap(Entry::getKey, entry -> entry.getValue().toPacket()));
      return packet;
   }

   @Nullable
   @Override
   public ToClientPacket generateRemovePacket(@Nonnull Set<String> removed) {
      UpdateBlockGroups packet = new UpdateBlockGroups();
      packet.type = UpdateType.Remove;
      packet.groups = new Object2ObjectOpenHashMap();

      for (String string : removed) {
         packet.groups.put(string, null);
      }

      return null;
   }
}
