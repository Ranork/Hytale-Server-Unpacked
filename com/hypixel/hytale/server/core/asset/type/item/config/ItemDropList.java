package com.hypixel.hytale.server.core.asset.type.item.config;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetKeyValidator;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.codec.ContainedAssetCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.validation.ValidatorCache;
import com.hypixel.hytale.server.core.asset.type.item.config.container.ItemDropContainer;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import javax.annotation.Nonnull;

public class ItemDropList implements JsonAssetWithMap<String, DefaultAssetMap<String, ItemDropList>> {
   public static final AssetBuilderCodec<String, ItemDropList> CODEC = AssetBuilderCodec.builder(
         ItemDropList.class,
         ItemDropList::new,
         Codec.STRING,
         (itemDropList, s) -> itemDropList.id = s,
         itemDropList -> itemDropList.id,
         (asset, data) -> asset.data = data,
         asset -> asset.data
      )
      .appendInherited(
         new KeyedCodec<>("Container", ItemDropContainer.CODEC),
         (itemDropList, o) -> itemDropList.container = o,
         itemDropList -> itemDropList.container,
         (itemDropList, parent) -> itemDropList.container = parent.container
      )
      .add()
      .validator((asset, results) -> {
         ItemDropContainer container = asset.getContainer();
         if (container != null) {
            List<ItemDrop> allDrops = container.getAllDrops(new ObjectArrayList());
            if (allDrops.isEmpty()) {
               results.fail("Container must have something to drop!");
            }
         }
      })
      .build();
   public static final Codec<String> CHILD_ASSET_CODEC = new ContainedAssetCodec<>(ItemDropList.class, CODEC);
   public static final ValidatorCache<String> VALIDATOR_CACHE = new ValidatorCache<>(new AssetKeyValidator<>(ItemDropList::getAssetStore));
   private static AssetStore<String, ItemDropList, DefaultAssetMap<String, ItemDropList>> ASSET_STORE;
   protected AssetExtraInfo.Data data;
   protected String id;
   protected ItemDropContainer container;

   public static AssetStore<String, ItemDropList, DefaultAssetMap<String, ItemDropList>> getAssetStore() {
      if (ASSET_STORE == null) {
         ASSET_STORE = AssetRegistry.getAssetStore(ItemDropList.class);
      }

      return ASSET_STORE;
   }

   public static DefaultAssetMap<String, ItemDropList> getAssetMap() {
      return (DefaultAssetMap<String, ItemDropList>)getAssetStore().getAssetMap();
   }

   public ItemDropList(String id, ItemDropContainer container) {
      this.id = id;
      this.container = container;
   }

   protected ItemDropList() {
   }

   public String getId() {
      return this.id;
   }

   public ItemDropContainer getContainer() {
      return this.container;
   }

   @Nonnull
   @Override
   public String toString() {
      return "ItemDropList{id='" + this.id + "', container=" + this.container + "}";
   }
}
