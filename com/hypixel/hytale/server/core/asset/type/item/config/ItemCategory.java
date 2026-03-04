package com.hypixel.hytale.server.core.asset.type.item.config;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetKeyValidator;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.validation.ValidatorCache;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.common.util.ArrayUtil;
import com.hypixel.hytale.protocol.ItemGridInfoDisplayMode;
import com.hypixel.hytale.server.core.asset.common.CommonAssetValidator;
import com.hypixel.hytale.server.core.io.NetworkSerializable;
import java.lang.ref.SoftReference;
import java.util.Arrays;
import java.util.Comparator;
import javax.annotation.Nonnull;

public class ItemCategory
   implements JsonAssetWithMap<String, DefaultAssetMap<String, ItemCategory>>,
   NetworkSerializable<com.hypixel.hytale.protocol.ItemCategory> {
   private static final AssetBuilderCodec.Builder<String, ItemCategory> CODEC_BUILDER = AssetBuilderCodec.builder(
         ItemCategory.class,
         ItemCategory::new,
         Codec.STRING,
         (itemCategory, k) -> itemCategory.id = k,
         itemCategory -> itemCategory.id,
         (asset, data) -> asset.data = data,
         asset -> asset.data
      )
      .addField(new KeyedCodec<>("Id", Codec.STRING), (itemCategory, s) -> itemCategory.id = s, itemCategory -> itemCategory.id)
      .addField(new KeyedCodec<>("Name", Codec.STRING), (itemCategory, s) -> itemCategory.name = s, itemCategory -> itemCategory.name)
      .<String>append(new KeyedCodec<>("Icon", Codec.STRING), (itemCategory, s) -> itemCategory.icon = s, itemCategory -> itemCategory.icon)
      .addValidator(CommonAssetValidator.ICON_ITEM_CATEGORIES)
      .add()
      .<ItemGridInfoDisplayMode>append(
         new KeyedCodec<>("InfoDisplayMode", new EnumCodec<>(ItemGridInfoDisplayMode.class), false, true),
         (itemCategory, s) -> itemCategory.infoDisplayMode = s,
         itemCategory -> itemCategory.infoDisplayMode
      )
      .addValidator(Validators.nonNull())
      .add()
      .addField(new KeyedCodec<>("Order", Codec.INTEGER), (itemCategory, s) -> itemCategory.order = s, itemCategory -> itemCategory.order)
      .afterDecode(itemCategory -> {
         if (itemCategory.children != null) {
            Arrays.sort(itemCategory.children, Comparator.comparingInt(value -> value.order));
         }
      });
   public static final AssetBuilderCodec<String, ItemCategory> CODEC = CODEC_BUILDER.build();
   private static AssetStore<String, ItemCategory, DefaultAssetMap<String, ItemCategory>> ASSET_STORE;
   public static final ValidatorCache<String> VALIDATOR_CACHE = new ValidatorCache<>(new AssetKeyValidator<>(ItemCategory::getAssetStore));
   protected AssetExtraInfo.Data data;
   protected String id;
   protected String name;
   protected String icon;
   protected int order;
   @Nonnull
   protected ItemGridInfoDisplayMode infoDisplayMode = ItemGridInfoDisplayMode.Tooltip;
   protected ItemCategory[] children;
   private SoftReference<com.hypixel.hytale.protocol.ItemCategory> cachedPacket;

   public static AssetStore<String, ItemCategory, DefaultAssetMap<String, ItemCategory>> getAssetStore() {
      if (ASSET_STORE == null) {
         ASSET_STORE = AssetRegistry.getAssetStore(ItemCategory.class);
      }

      return ASSET_STORE;
   }

   public static DefaultAssetMap<String, ItemCategory> getAssetMap() {
      return (DefaultAssetMap<String, ItemCategory>)getAssetStore().getAssetMap();
   }

   public ItemCategory(String id, String name, String icon, ItemGridInfoDisplayMode infoDisplayMode, ItemCategory[] children) {
      this.id = id;
      this.name = name;
      this.icon = icon;
      this.infoDisplayMode = infoDisplayMode;
      this.children = children;
   }

   protected ItemCategory() {
   }

   @Nonnull
   public com.hypixel.hytale.protocol.ItemCategory toPacket() {
      com.hypixel.hytale.protocol.ItemCategory cached = this.cachedPacket == null ? null : this.cachedPacket.get();
      if (cached != null) {
         return cached;
      } else {
         com.hypixel.hytale.protocol.ItemCategory packet = new com.hypixel.hytale.protocol.ItemCategory();
         packet.id = this.id;
         packet.name = this.name;
         packet.icon = this.icon;
         packet.order = this.order;
         packet.infoDisplayMode = this.infoDisplayMode;
         if (this.children != null && this.children.length > 0) {
            packet.children = ArrayUtil.copyAndMutate(this.children, ItemCategory::toPacket, com.hypixel.hytale.protocol.ItemCategory[]::new);
         }

         this.cachedPacket = new SoftReference<>(packet);
         return packet;
      }
   }

   public String getId() {
      return this.id;
   }

   public String getName() {
      return this.name;
   }

   public String getIcon() {
      return this.icon;
   }

   public int getOrder() {
      return this.order;
   }

   public ItemGridInfoDisplayMode getInfoDisplayMode() {
      return this.infoDisplayMode;
   }

   public ItemCategory[] getChildren() {
      return this.children;
   }

   @Nonnull
   @Override
   public String toString() {
      return "ItemCategory{id='"
         + this.id
         + "', name='"
         + this.name
         + "', icon='"
         + this.icon
         + "', order="
         + this.order
         + ", infoDisplayMode='"
         + this.infoDisplayMode
         + "', children="
         + Arrays.toString((Object[])this.children)
         + "}";
   }

   static {
      CODEC_BUILDER.addField(
         new KeyedCodec<>("Children", new ArrayCodec<>(CODEC, ItemCategory[]::new)),
         (itemCategory, l) -> itemCategory.children = l,
         itemCategory -> itemCategory.children
      );
   }
}
