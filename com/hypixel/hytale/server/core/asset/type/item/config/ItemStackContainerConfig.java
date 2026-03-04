package com.hypixel.hytale.server.core.asset.type.item.config;

import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.inventory.container.filter.FilterType;
import javax.annotation.Nonnull;

public class ItemStackContainerConfig {
   public static final ItemStackContainerConfig DEFAULT = new ItemStackContainerConfig();
   public static final BuilderCodec<ItemStackContainerConfig> CODEC = BuilderCodec.builder(ItemStackContainerConfig.class, ItemStackContainerConfig::new)
      .append(new KeyedCodec<>("Capacity", Codec.SHORT), (itemTool, s) -> itemTool.capacity = s, itemTool -> itemTool.capacity)
      .add()
      .append(new KeyedCodec<>("GlobalFilter", FilterType.CODEC), (itemTool, s) -> itemTool.globalFilter = s, itemTool -> itemTool.globalFilter)
      .add()
      .append(new KeyedCodec<>("ItemTag", Codec.STRING), (materialQuantity, s) -> materialQuantity.tag = s, materialQuantity -> materialQuantity.tag)
      .add()
      .afterDecode((config, extraInfo) -> {
         if (config.tag != null) {
            config.tagIndex = AssetRegistry.getOrCreateTagIndex(config.tag);
         }
      })
      .build();
   protected short capacity = 0;
   protected FilterType globalFilter = FilterType.ALLOW_ALL;
   protected String tag;
   protected volatile int tagIndex = Integer.MIN_VALUE;

   public short getCapacity() {
      return this.capacity;
   }

   public FilterType getGlobalFilter() {
      return this.globalFilter;
   }

   public int getTagIndex() {
      return this.tagIndex;
   }

   @Nonnull
   @Override
   public String toString() {
      return "ItemStackContainerConfig{capacity=" + this.capacity + ", globalFilter=" + this.globalFilter + ", tag='" + this.tag + "'}";
   }
}
