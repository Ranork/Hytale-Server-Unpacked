package com.hypixel.hytale.builtin.adventure.npcshop.npc;

import com.hypixel.hytale.builtin.adventure.shop.barter.BarterShopAsset;
import com.hypixel.hytale.server.npc.asset.builder.validators.AssetValidator;
import java.util.EnumSet;
import javax.annotation.Nonnull;

public class BarterShopExistsValidator extends AssetValidator {
   @Nonnull
   private static final BarterShopExistsValidator DEFAULT_INSTANCE = new BarterShopExistsValidator();

   private BarterShopExistsValidator() {
   }

   private BarterShopExistsValidator(EnumSet<AssetValidator.Config> config) {
      super(config);
   }

   @Nonnull
   @Override
   public String getDomain() {
      return "BarterShop";
   }

   @Override
   public boolean test(String marker) {
      return BarterShopAsset.getAssetMap().getAsset(marker) != null;
   }

   @Nonnull
   @Override
   public String errorMessage(String marker, String attributeName) {
      return "The barter shop asset with the name \"" + marker + "\" does not exist for attribute \"" + attributeName + "\"";
   }

   @Nonnull
   @Override
   public String getAssetName() {
      return BarterShopAsset.class.getSimpleName();
   }

   public static BarterShopExistsValidator required() {
      return DEFAULT_INSTANCE;
   }

   @Nonnull
   public static BarterShopExistsValidator withConfig(EnumSet<AssetValidator.Config> config) {
      return new BarterShopExistsValidator(config);
   }
}
