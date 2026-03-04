package com.hypixel.hytale.server.npc.asset.builder.validators.asset;

import com.hypixel.hytale.server.core.asset.type.item.config.ItemDropList;
import com.hypixel.hytale.server.npc.asset.builder.validators.AssetValidator;
import com.hypixel.hytale.server.npc.util.InventoryHelper;
import java.util.EnumSet;
import javax.annotation.Nonnull;

public class ItemDropListExistsValidator extends AssetValidator {
   private static final ItemDropListExistsValidator DEFAULT_INSTANCE = new ItemDropListExistsValidator();

   private ItemDropListExistsValidator() {
   }

   private ItemDropListExistsValidator(EnumSet<AssetValidator.Config> config) {
      super(config);
   }

   @Nonnull
   @Override
   public String getDomain() {
      return "ItemDropList";
   }

   @Override
   public boolean test(String value) {
      return InventoryHelper.itemDropListKeyExists(value);
   }

   @Nonnull
   @Override
   public String errorMessage(String value, String attribute) {
      return "The item drop list with the name \"" + value + "\" does not exist for attribute \"" + attribute + "\"";
   }

   @Nonnull
   @Override
   public String getAssetName() {
      return ItemDropList.class.getSimpleName();
   }

   public static ItemDropListExistsValidator required() {
      return DEFAULT_INSTANCE;
   }

   @Nonnull
   public static ItemDropListExistsValidator withConfig(EnumSet<AssetValidator.Config> config) {
      return new ItemDropListExistsValidator(config);
   }
}
