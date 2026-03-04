package com.hypixel.hytale.server.core.inventory.transaction;

import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface Transaction {
   boolean succeeded();

   boolean wasSlotModified(short var1);

   @Nonnull
   Transaction toParent(ItemContainer var1, short var2, ItemContainer var3);

   @Nullable
   Transaction fromParent(ItemContainer var1, short var2, ItemContainer var3);
}
