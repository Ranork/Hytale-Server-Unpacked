package com.hypixel.hytale.server.core.asset.type.item;

import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.UpdateType;
import com.hypixel.hytale.protocol.packets.assets.UpdateItemCategories;
import com.hypixel.hytale.server.core.asset.packet.DefaultAssetPacketGenerator;
import com.hypixel.hytale.server.core.asset.type.item.config.ItemCategory;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;

public class ItemCategoryPacketGenerator extends DefaultAssetPacketGenerator<String, ItemCategory> {
   @Nonnull
   @Override
   public ToClientPacket generateInitPacket(@Nonnull DefaultAssetMap<String, ItemCategory> assetMap, @Nonnull Map<String, ItemCategory> assets) {
      Map<String, ItemCategory> assetsFromMap = assetMap.getAssetMap();
      if (assets.size() != assetsFromMap.size()) {
         throw new UnsupportedOperationException("Item categories can not handle partial init packets!!!");
      } else {
         UpdateItemCategories packet = new UpdateItemCategories();
         packet.type = UpdateType.Init;
         com.hypixel.hytale.protocol.ItemCategory[] arr = new com.hypixel.hytale.protocol.ItemCategory[assets.size()];
         int i = 0;

         for (ItemCategory itemCategory : assets.values()) {
            arr[i++] = itemCategory.toPacket();
         }

         packet.itemCategories = arr;
         return packet;
      }
   }

   @Nonnull
   @Override
   public ToClientPacket generateUpdatePacket(@Nonnull Map<String, ItemCategory> assets) {
      UpdateItemCategories packet = new UpdateItemCategories();
      packet.type = UpdateType.AddOrUpdate;
      com.hypixel.hytale.protocol.ItemCategory[] arr = new com.hypixel.hytale.protocol.ItemCategory[assets.size()];
      int i = 0;

      for (ItemCategory itemCategory : assets.values()) {
         arr[i++] = itemCategory.toPacket();
      }

      packet.itemCategories = arr;
      return packet;
   }

   @Nonnull
   @Override
   public ToClientPacket generateRemovePacket(@Nonnull Set<String> removed) {
      UpdateItemCategories packet = new UpdateItemCategories();
      packet.type = UpdateType.Remove;
      com.hypixel.hytale.protocol.ItemCategory[] arr = new com.hypixel.hytale.protocol.ItemCategory[removed.size()];
      int i = 0;

      for (String id : removed) {
         com.hypixel.hytale.protocol.ItemCategory itemCategory = new com.hypixel.hytale.protocol.ItemCategory();
         itemCategory.id = id;
         arr[i++] = itemCategory;
      }

      packet.itemCategories = arr;
      return packet;
   }
}
