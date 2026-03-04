package com.hypixel.hytale.builtin.adventure.camera.asset.viewbobbing;

import com.hypixel.hytale.assetstore.AssetMap;
import com.hypixel.hytale.protocol.CachedPacket;
import com.hypixel.hytale.protocol.MovementType;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.UpdateType;
import com.hypixel.hytale.protocol.packets.assets.UpdateViewBobbing;
import com.hypixel.hytale.server.core.asset.packet.SimpleAssetPacketGenerator;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import javax.annotation.Nonnull;

public class ViewBobbingPacketGenerator extends SimpleAssetPacketGenerator<MovementType, ViewBobbing, AssetMap<MovementType, ViewBobbing>> {
   @Nonnull
   @Override
   public ToClientPacket generateInitPacket(AssetMap<MovementType, ViewBobbing> assetMap, @Nonnull Map<MovementType, ViewBobbing> assets) {
      return toCachedPacket(UpdateType.Init, assets);
   }

   @Nonnull
   @Override
   protected ToClientPacket generateUpdatePacket(AssetMap<MovementType, ViewBobbing> assetMap, @Nonnull Map<MovementType, ViewBobbing> loadedAssets) {
      return toCachedPacket(UpdateType.AddOrUpdate, loadedAssets);
   }

   @Nonnull
   @Override
   protected ToClientPacket generateRemovePacket(AssetMap<MovementType, ViewBobbing> assetMap, @Nonnull Set<MovementType> removed) {
      UpdateViewBobbing packet = new UpdateViewBobbing();
      packet.type = UpdateType.Remove;
      packet.profiles = new EnumMap<>(MovementType.class);

      for (MovementType type : removed) {
         packet.profiles.put(type, null);
      }

      return CachedPacket.cache(packet);
   }

   @Nonnull
   protected static ToClientPacket toCachedPacket(@Nonnull UpdateType type, @Nonnull Map<MovementType, ViewBobbing> assets) {
      UpdateViewBobbing packet = new UpdateViewBobbing();
      packet.type = type;
      packet.profiles = new EnumMap<>(MovementType.class);

      for (Entry<MovementType, ViewBobbing> entry : assets.entrySet()) {
         packet.profiles.put(entry.getKey(), entry.getValue().toPacket());
      }

      return CachedPacket.cache(packet);
   }
}
