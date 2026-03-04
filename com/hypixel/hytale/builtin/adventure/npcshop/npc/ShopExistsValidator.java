package com.hypixel.hytale.builtin.adventure.npcshop.npc;

import com.hypixel.hytale.builtin.adventure.shop.ShopAsset;
import com.hypixel.hytale.server.npc.asset.builder.validators.AssetValidator;
import java.util.EnumSet;
import javax.annotation.Nonnull;

public class ShopExistsValidator extends AssetValidator {
   @Nonnull
   private static final ShopExistsValidator DEFAULT_INSTANCE = new ShopExistsValidator();

   private ShopExistsValidator() {
   }

   private ShopExistsValidator(EnumSet<AssetValidator.Config> config) {
      super(config);
   }

   @Nonnull
   @Override
   public String getDomain() {
      return "Shop";
   }

   @Override
   public boolean test(String marker) {
      return ShopAsset.getAssetMap().getAsset(marker) != null;
   }

   @Nonnull
   @Override
   public String errorMessage(String marker, String attributeName) {
      return "The shop asset with the name \"" + marker + "\" does not exist for attribute \"" + attributeName + "\"";
   }

   @Nonnull
   @Override
   public String getAssetName() {
      return ShopAsset.class.getSimpleName();
   }

   public static ShopExistsValidator required() {
      return DEFAULT_INSTANCE;
   }

   @Nonnull
   public static ShopExistsValidator withConfig(EnumSet<AssetValidator.Config> config) {
      return new ShopExistsValidator(config);
   }
}
