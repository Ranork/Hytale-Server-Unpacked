package com.hypixel.hytale.server.core.asset.type.itemanimation;

import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.UpdateType;
import com.hypixel.hytale.protocol.packets.assets.UpdateItemPlayerAnimations;
import com.hypixel.hytale.server.core.asset.packet.DefaultAssetPacketGenerator;
import com.hypixel.hytale.server.core.asset.type.itemanimation.config.ItemPlayerAnimations;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;

public class ItemPlayerAnimationsPacketGenerator extends DefaultAssetPacketGenerator<String, ItemPlayerAnimations> {
   @Nonnull
   @Override
   public ToClientPacket generateInitPacket(DefaultAssetMap<String, ItemPlayerAnimations> assetMap, @Nonnull Map<String, ItemPlayerAnimations> assets) {
      UpdateItemPlayerAnimations packet = new UpdateItemPlayerAnimations();
      packet.type = UpdateType.Init;
      packet.itemPlayerAnimations = new Object2ObjectOpenHashMap(assets.size());

      for (ItemPlayerAnimations itemPlayerAnimation : assets.values()) {
         packet.itemPlayerAnimations.put(itemPlayerAnimation.getId(), itemPlayerAnimation.toPacket());
      }

      return packet;
   }

   @Nonnull
   @Override
   public ToClientPacket generateUpdatePacket(@Nonnull Map<String, ItemPlayerAnimations> loadedAssets) {
      UpdateItemPlayerAnimations packet = new UpdateItemPlayerAnimations();
      packet.type = UpdateType.AddOrUpdate;
      packet.itemPlayerAnimations = new Object2ObjectOpenHashMap(loadedAssets.size());

      for (ItemPlayerAnimations itemPlayerAnimation : loadedAssets.values()) {
         packet.itemPlayerAnimations.put(itemPlayerAnimation.getId(), itemPlayerAnimation.toPacket());
      }

      return packet;
   }

   @Nonnull
   @Override
   public ToClientPacket generateRemovePacket(@Nonnull Set<String> removed) {
      UpdateItemPlayerAnimations packet = new UpdateItemPlayerAnimations();
      packet.type = UpdateType.Remove;
      packet.itemPlayerAnimations = new Object2ObjectOpenHashMap(removed.size());

      for (String key : removed) {
         com.hypixel.hytale.protocol.ItemPlayerAnimations itemPlayerAnimation = new com.hypixel.hytale.protocol.ItemPlayerAnimations();
         itemPlayerAnimation.id = key;
         packet.itemPlayerAnimations.put(key, itemPlayerAnimation);
      }

      return packet;
   }
}
