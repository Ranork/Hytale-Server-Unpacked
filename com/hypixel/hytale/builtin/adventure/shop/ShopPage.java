package com.hypixel.hytale.builtin.adventure.shop;

import com.hypixel.hytale.server.core.entity.entities.player.pages.choices.ChoiceBasePage;
import com.hypixel.hytale.server.core.entity.entities.player.pages.choices.ChoiceElement;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ShopPage extends ChoiceBasePage {
   public ShopPage(@Nonnull PlayerRef playerRef, String shopId) {
      super(playerRef, getShopElements(shopId), "Pages/ShopPage.ui");
   }

   @Nullable
   protected static ChoiceElement[] getShopElements(String shopId) {
      ShopAsset shopAsset = ShopAsset.getAssetMap().getAsset(shopId);
      return shopAsset == null ? null : shopAsset.getElements();
   }
}
