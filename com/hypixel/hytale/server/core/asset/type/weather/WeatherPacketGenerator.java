package com.hypixel.hytale.server.core.asset.type.weather;

import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.UpdateType;
import com.hypixel.hytale.protocol.packets.assets.UpdateWeathers;
import com.hypixel.hytale.server.core.asset.packet.SimpleAssetPacketGenerator;
import com.hypixel.hytale.server.core.asset.type.weather.config.Weather;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import javax.annotation.Nonnull;

public class WeatherPacketGenerator extends SimpleAssetPacketGenerator<String, Weather, IndexedLookupTableAssetMap<String, Weather>> {
   @Nonnull
   public ToClientPacket generateInitPacket(@Nonnull IndexedLookupTableAssetMap<String, Weather> assetMap, @Nonnull Map<String, Weather> assets) {
      UpdateWeathers packet = new UpdateWeathers();
      packet.type = UpdateType.Init;
      packet.weathers = new Int2ObjectOpenHashMap();

      for (Entry<String, Weather> entry : assets.entrySet()) {
         String key = entry.getKey();
         int index = assetMap.getIndex(key);
         if (index == Integer.MIN_VALUE) {
            throw new IllegalArgumentException("Unknown key! " + key);
         }

         packet.weathers.put(index, entry.getValue().toPacket());
      }

      packet.maxId = assetMap.getNextIndex();
      return packet;
   }

   @Nonnull
   public ToClientPacket generateUpdatePacket(@Nonnull IndexedLookupTableAssetMap<String, Weather> assetMap, @Nonnull Map<String, Weather> loadedAssets) {
      UpdateWeathers packet = new UpdateWeathers();
      packet.type = UpdateType.AddOrUpdate;
      packet.weathers = new Int2ObjectOpenHashMap();

      for (Entry<String, Weather> entry : loadedAssets.entrySet()) {
         String key = entry.getKey();
         int index = assetMap.getIndex(key);
         if (index == Integer.MIN_VALUE) {
            throw new IllegalArgumentException("Unknown key! " + key);
         }

         packet.weathers.put(index, entry.getValue().toPacket());
      }

      packet.maxId = assetMap.getNextIndex();
      return packet;
   }

   @Nonnull
   public ToClientPacket generateRemovePacket(@Nonnull IndexedLookupTableAssetMap<String, Weather> assetMap, @Nonnull Set<String> removed) {
      UpdateWeathers packet = new UpdateWeathers();
      packet.type = UpdateType.Remove;
      packet.weathers = new Int2ObjectOpenHashMap();

      for (String key : removed) {
         int index = assetMap.getIndex(key);
         if (index == Integer.MIN_VALUE) {
            throw new IllegalArgumentException("Unknown key! " + key);
         }

         packet.weathers.put(index, null);
      }

      packet.maxId = assetMap.getNextIndex();
      return packet;
   }
}
