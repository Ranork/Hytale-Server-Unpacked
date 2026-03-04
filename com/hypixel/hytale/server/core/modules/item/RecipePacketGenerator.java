package com.hypixel.hytale.server.core.modules.item;

import com.hypixel.hytale.assetstore.AssetUpdateQuery;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.UpdateType;
import com.hypixel.hytale.protocol.packets.assets.UpdateRecipes;
import com.hypixel.hytale.server.core.asset.packet.AssetPacketGenerator;
import com.hypixel.hytale.server.core.asset.type.item.config.CraftingRecipe;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import javax.annotation.Nonnull;

public class RecipePacketGenerator extends AssetPacketGenerator<String, CraftingRecipe, DefaultAssetMap<String, CraftingRecipe>> {
   @Nonnull
   public ToClientPacket generateInitPacket(DefaultAssetMap<String, CraftingRecipe> assetMap, @Nonnull Map<String, CraftingRecipe> assets) {
      UpdateRecipes packet = new UpdateRecipes();
      packet.type = UpdateType.Init;
      packet.recipes = new Object2ObjectOpenHashMap();

      for (Entry<String, CraftingRecipe> entry : assets.entrySet()) {
         packet.recipes.put(entry.getKey(), entry.getValue().toPacket(entry.getKey()));
      }

      return packet;
   }

   @Nonnull
   public ToClientPacket generateUpdatePacket(
      DefaultAssetMap<String, CraftingRecipe> assetMap, @Nonnull Map<String, CraftingRecipe> loadedAssets, @Nonnull AssetUpdateQuery query
   ) {
      UpdateRecipes packet = new UpdateRecipes();
      packet.type = UpdateType.AddOrUpdate;
      packet.recipes = new Object2ObjectOpenHashMap();

      for (Entry<String, CraftingRecipe> entry : loadedAssets.entrySet()) {
         packet.recipes.put(entry.getKey(), entry.getValue().toPacket(entry.getKey()));
      }

      return packet;
   }

   @Nonnull
   public ToClientPacket generateRemovePacket(DefaultAssetMap<String, CraftingRecipe> assetMap, @Nonnull Set<String> removed, @Nonnull AssetUpdateQuery query) {
      UpdateRecipes packet = new UpdateRecipes();
      packet.type = UpdateType.Remove;
      packet.recipes = new Object2ObjectOpenHashMap();

      for (String key : removed) {
         packet.recipes.put(key, null);
      }

      return packet;
   }
}
