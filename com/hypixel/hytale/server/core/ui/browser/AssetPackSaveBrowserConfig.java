package com.hypixel.hytale.server.core.ui.browser;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public record AssetPackSaveBrowserConfig(@Nonnull String listElementId, @Nullable String searchInputId) {
   public static AssetPackSaveBrowserConfig defaults() {
      return new AssetPackSaveBrowserConfig("#PackList", "#SearchInput");
   }
}
