package com.hypixel.hytale.server.core.entity.entities.player.windows;

import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import javax.annotation.Nonnull;

public interface ItemContainerWindow {
   @Nonnull
   ItemContainer getItemContainer();
}
