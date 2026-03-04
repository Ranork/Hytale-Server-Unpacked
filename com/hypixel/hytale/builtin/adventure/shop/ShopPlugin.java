package com.hypixel.hytale.builtin.adventure.shop;

import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.builtin.adventure.shop.barter.BarterShopAsset;
import com.hypixel.hytale.builtin.adventure.shop.barter.BarterShopState;
import com.hypixel.hytale.server.core.asset.HytaleAssetStore;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.entity.entities.player.pages.choices.ChoiceElement;
import com.hypixel.hytale.server.core.entity.entities.player.pages.choices.ChoiceInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.OpenCustomUIInteraction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.plugin.registry.AssetRegistry;
import java.util.logging.Level;
import javax.annotation.Nonnull;

public class ShopPlugin extends JavaPlugin {
   protected static ShopPlugin instance;

   public static ShopPlugin get() {
      return instance;
   }

   public ShopPlugin(@Nonnull JavaPluginInit init) {
      super(init);
   }

   @Override
   protected void setup() {
      instance = this;
      AssetRegistry assetRegistry = this.getAssetRegistry();
      assetRegistry.register(
         ((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)HytaleAssetStore.builder(
                           ShopAsset.class, new DefaultAssetMap()
                        )
                        .setPath("Shops"))
                     .setCodec(ShopAsset.CODEC))
                  .setKeyFunction(ShopAsset::getId))
               .loadsAfter(Item.class))
            .build()
      );
      assetRegistry.register(
         ((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)HytaleAssetStore.builder(
                           BarterShopAsset.class, new DefaultAssetMap()
                        )
                        .setPath("BarterShops"))
                     .setCodec(BarterShopAsset.CODEC))
                  .setKeyFunction(BarterShopAsset::getId))
               .loadsAfter(Item.class))
            .build()
      );
      this.getCodecRegistry(ChoiceElement.CODEC).register("ShopElement", ShopElement.class, ShopElement.CODEC);
      this.getCodecRegistry(ChoiceInteraction.CODEC).register("GiveItem", GiveItemInteraction.class, GiveItemInteraction.CODEC);
      this.getCodecRegistry(OpenCustomUIInteraction.PAGE_CODEC).register("Shop", ShopPageSupplier.class, ShopPageSupplier.CODEC);
   }

   @Override
   protected void start() {
      BarterShopState.initialize(this.getDataDirectory());
      this.getLogger().at(Level.INFO).log("Barter shop state initialized");
   }

   @Override
   protected void shutdown() {
      BarterShopState.shutdown();
      this.getLogger().at(Level.INFO).log("Barter shop state saved");
   }
}
