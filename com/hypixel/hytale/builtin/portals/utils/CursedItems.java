package com.hypixel.hytale.builtin.portals.utils;

import com.hypixel.hytale.server.core.asset.type.item.config.metadata.AdventureMetadata;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.Nonnull;

public final class CursedItems {
   private CursedItems() {
   }

   public static boolean uncurseAll(@Nonnull ItemContainer itemContainer) {
      AtomicBoolean uncursedAny = new AtomicBoolean(false);
      itemContainer.replaceAll((slot, existing) -> {
         AdventureMetadata adventureMeta = existing.getFromMetadataOrNull("Adventure", AdventureMetadata.CODEC);
         if (adventureMeta == null) {
            return existing;
         } else if (!adventureMeta.isCursed()) {
            return existing;
         } else {
            adventureMeta.setCursed(false);
            uncursedAny.setPlain(true);
            return existing.withMetadata("Adventure", AdventureMetadata.CODEC, adventureMeta);
         }
      });
      return uncursedAny.get();
   }

   public static void deleteAll(@Nonnull Player player) {
      deleteAll(player.getInventory().getCombinedEverything());
   }

   public static void deleteAll(@Nonnull ItemContainer itemContainer) {
      itemContainer.replaceAll((slot, existing) -> {
         AdventureMetadata adventureMeta = existing.getFromMetadataOrNull(AdventureMetadata.KEYED_CODEC);
         boolean cursed = adventureMeta != null && adventureMeta.isCursed();
         return cursed ? ItemStack.EMPTY : existing;
      });
   }
}
