package com.hypixel.hytale.server.core.modules.item;

import com.hypixel.hytale.assetstore.event.LoadedAssetsEvent;
import com.hypixel.hytale.codec.lookup.Priority;
import com.hypixel.hytale.common.plugin.PluginManifest;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.asset.type.item.config.ItemCategory;
import com.hypixel.hytale.server.core.asset.type.item.config.ItemDrop;
import com.hypixel.hytale.server.core.asset.type.item.config.ItemDropList;
import com.hypixel.hytale.server.core.asset.type.item.config.ItemWeapon;
import com.hypixel.hytale.server.core.asset.type.item.config.damageData.DamageBreakdown;
import com.hypixel.hytale.server.core.asset.type.item.config.damageData.WeaponDamageDataCollector;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.EmptyItemContainer;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.inventory.container.SimpleItemContainer;
import com.hypixel.hytale.server.core.modules.item.commands.SpawnItemCommand;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ItemModule extends JavaPlugin {
   public static final PluginManifest MANIFEST = PluginManifest.corePlugin(ItemModule.class).build();
   private static ItemModule instance;

   public static ItemModule get() {
      return instance;
   }

   public ItemModule(@Nonnull JavaPluginInit init) {
      super(init);
      instance = this;
   }

   @Override
   protected void setup() {
      this.getCommandRegistry().registerCommand(new SpawnItemCommand());
      ItemContainer.CODEC.register(Priority.DEFAULT, "Simple", SimpleItemContainer.class, SimpleItemContainer.CODEC);
      ItemContainer.CODEC.register("Empty", EmptyItemContainer.class, EmptyItemContainer.CODEC);
      this.getEventRegistry().register(LoadedAssetsEvent.class, Item.class, this::computeWeaponData);
   }

   private void computeWeaponData(@Nonnull LoadedAssetsEvent<String, Item, ?> event) {
      int weapons = 0;
      int withData = 0;

      for (Item item : event.getLoadedAssets().values()) {
         if (item.getWeapon() != null) {
            weapons++;
            ItemWeapon weapon = item.unshareWeapon();
            DamageBreakdown basicDamageBreakdown = WeaponDamageDataCollector.calculate(item, InteractionType.Primary);
            weapon.setBasicDamageBreakdown(basicDamageBreakdown);
            DamageBreakdown ultDamageBreakdown = WeaponDamageDataCollector.calculate(item, InteractionType.Ability1);
            weapon.setUltimateDamageBreakdown(ultDamageBreakdown);
            item.invalidatePacketCache();
            if (!basicDamageBreakdown.entries().isEmpty()) {
               withData++;
            }
         }
      }

      this.getLogger().at(Level.INFO).log("Computed damage data for %d of %d weapon items", withData, weapons);
   }

   @Nonnull
   public List<String> getFlatItemCategoryList() {
      List<String> ids = new ObjectArrayList();
      ItemCategory[] itemCategories = ItemCategory.getAssetMap().getAssetMap().values().toArray(ItemCategory[]::new);

      for (ItemCategory category : itemCategories) {
         ItemCategory[] children = category.getChildren();
         if (children != null) {
            this.flattenCategories(category.getId() + ".", children, ids);
         }
      }

      return ids;
   }

   private void flattenCategories(String parent, @Nonnull ItemCategory[] itemCategories, @Nonnull List<String> categoryIds) {
      for (ItemCategory category : itemCategories) {
         String id = parent + category.getId();
         categoryIds.add(id);
         ItemCategory[] children = category.getChildren();
         if (children != null) {
            this.flattenCategories(id + ".", children, categoryIds);
         }
      }
   }

   @Nonnull
   public List<ItemStack> getRandomItemDrops(@Nullable String dropListId) {
      if (this.isDisabled()) {
         return Collections.emptyList();
      } else if (dropListId == null) {
         return Collections.emptyList();
      } else {
         ItemDropList itemDropList = ItemDropList.getAssetMap().getAsset(dropListId);
         if (itemDropList != null && itemDropList.getContainer() != null) {
            List<ItemStack> generatedItemDrops = new ObjectArrayList();
            ThreadLocalRandom random = ThreadLocalRandom.current();
            List<ItemDrop> configuredItemDrops = new ObjectArrayList();
            itemDropList.getContainer().populateDrops(configuredItemDrops, random::nextDouble, dropListId);

            for (ItemDrop drop : configuredItemDrops) {
               if (drop != null && drop.getItemId() != null) {
                  int amount = drop.getRandomQuantity(random);
                  if (amount > 0) {
                     generatedItemDrops.add(new ItemStack(drop.getItemId(), amount, drop.getMetadata()));
                  }
               } else {
                  ((HytaleLogger.Api)this.getLogger().atWarning())
                     .log("ItemModule::getRandomItemDrops - Tried to create ItemDrop for non-existent item in drop list id '%s'", dropListId);
               }
            }

            return generatedItemDrops;
         } else {
            return Collections.emptyList();
         }
      }
   }

   public static boolean exists(String key) {
      if ("Empty".equals(key)) {
         return true;
      } else {
         return "Unknown".equals(key) ? true : Item.getAssetMap().getAsset(key) != null;
      }
   }
}
