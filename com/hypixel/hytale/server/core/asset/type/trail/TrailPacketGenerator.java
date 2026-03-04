package com.hypixel.hytale.server.core.asset.type.trail;

import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.UpdateType;
import com.hypixel.hytale.protocol.packets.assets.UpdateTrails;
import com.hypixel.hytale.server.core.asset.packet.DefaultAssetPacketGenerator;
import com.hypixel.hytale.server.core.asset.type.trail.config.Trail;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import javax.annotation.Nonnull;

public class TrailPacketGenerator extends DefaultAssetPacketGenerator<String, Trail> {
   @Nonnull
   @Override
   public ToClientPacket generateInitPacket(DefaultAssetMap<String, Trail> assetMap, @Nonnull Map<String, Trail> assets) {
      UpdateTrails packet = new UpdateTrails();
      packet.type = UpdateType.Init;
      packet.trails = new Object2ObjectOpenHashMap();

      for (Entry<String, Trail> entry : assets.entrySet()) {
         packet.trails.put(entry.getKey(), entry.getValue().toPacket());
      }

      return packet;
   }

   @Nonnull
   @Override
   public ToClientPacket generateUpdatePacket(@Nonnull Map<String, Trail> loadedAssets) {
      UpdateTrails packet = new UpdateTrails();
      packet.type = UpdateType.AddOrUpdate;
      packet.trails = new Object2ObjectOpenHashMap();

      for (Entry<String, Trail> entry : loadedAssets.entrySet()) {
         packet.trails.put(entry.getKey(), entry.getValue().toPacket());
      }

      return packet;
   }

   @Nonnull
   @Override
   public ToClientPacket generateRemovePacket(@Nonnull Set<String> removed) {
      UpdateTrails packet = new UpdateTrails();
      packet.type = UpdateType.Remove;
      packet.trails = new Object2ObjectOpenHashMap();

      for (String key : removed) {
         packet.trails.put(key, null);
      }

      return packet;
   }
}
