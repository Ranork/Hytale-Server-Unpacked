package com.hypixel.hytale.server.core.asset.type.item;

import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.protocol.ItemCategory;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.UpdateType;
import com.hypixel.hytale.protocol.packets.assets.UpdateFieldcraftCategories;
import com.hypixel.hytale.server.core.asset.packet.DefaultAssetPacketGenerator;
import com.hypixel.hytale.server.core.asset.type.item.config.FieldcraftCategory;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;

public class FieldcraftCategoryPacketGenerator extends DefaultAssetPacketGenerator<String, FieldcraftCategory> {
   @Nonnull
   @Override
   public ToClientPacket generateInitPacket(@Nonnull DefaultAssetMap<String, FieldcraftCategory> assetMap, @Nonnull Map<String, FieldcraftCategory> assets) {
      Map<String, FieldcraftCategory> assetsFromMap = assetMap.getAssetMap();
      if (assets.size() != assetsFromMap.size()) {
         throw new UnsupportedOperationException("Item categories can not handle partial init packets!!!");
      } else {
         UpdateFieldcraftCategories packet = new UpdateFieldcraftCategories();
         packet.type = UpdateType.Init;
         ItemCategory[] arr = new ItemCategory[assets.size()];
         int i = 0;

         for (FieldcraftCategory itemCategory : assets.values()) {
            arr[i++] = itemCategory.toPacket();
         }

         packet.itemCategories = arr;
         return packet;
      }
   }

   @Nonnull
   @Override
   public ToClientPacket generateUpdatePacket(@Nonnull Map<String, FieldcraftCategory> assets) {
      UpdateFieldcraftCategories packet = new UpdateFieldcraftCategories();
      packet.type = UpdateType.AddOrUpdate;
      ItemCategory[] arr = new ItemCategory[assets.size()];
      int i = 0;

      for (FieldcraftCategory itemCategory : assets.values()) {
         arr[i++] = itemCategory.toPacket();
      }

      packet.itemCategories = arr;
      return packet;
   }

   @Override
   public ToClientPacket generateRemovePacket(Set<String> removed) {
      throw new IllegalArgumentException("We don't support removing item categories at this time!");
   }
}
